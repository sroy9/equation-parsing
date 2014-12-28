package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class RelationFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;

	public RelationFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		RelationX blob = (RelationX) arg0;
		RelationY relationY= (RelationY) arg1;
		List<String> features = getFeatures(blob, relationY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	// Cluster Features
	public IFeatureVector getFeatureVector(RelationX blob, RelationY labelSet) {
		List<String> feats = getFeatures(blob, labelSet);
		return FeatGen.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
		features.addAll(singleFeatures(x, y));
		for(int i=Math.max(0, x.index-3); i<x.index; i++) {
			features.addAll(pairwiseFeatures(x, y, i));
		}
		return features;
	}
	
	public List<String> singleFeatures(RelationX x, RelationY labelSet) {
		List<String> features = new ArrayList<>();
		String prefix = labelSet.relation.substring(0,1);
		QuantSpan qs = x.quantities.get(x.index);
		int tokenId = x.ta.getTokenIdFromCharacterOffset(qs.start);
		Sentence sent = x.ta.getSentenceFromToken(tokenId);
		List<Pair<String, IntPair>> sentSkeleton = FeatGen.getPartialSkeleton(
				x.skeleton, sent.getStartSpan(), sent.getEndSpan());
		
		for(int i=0; i<x.skeleton.size(); ++i) {
			features.add(prefix+"_ProbUnigram_"+x.skeleton.get(i).getFirst());
		}
		for(int i=0; i<x.skeleton.size()-1; ++i) {
			features.add(prefix+"_ProbBigram_"+x.skeleton.get(i).getFirst()
					+"_"+x.skeleton.get(i+1).getFirst());
		}
		
		if(Tools.safeEquals(Tools.getValue(qs), 1.0) || Tools.safeEquals(Tools.getValue(qs), 2.0)) {
			features.add(prefix+"_1_OR_2");
		}
		for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton, tokenId, 3)) {
			features.add(prefix+"_Neighbor_"+feature);
		}
		for(int i=0; i<sentSkeleton.size(); ++i) {
			features.add(prefix+"_SentUnigram_"+sentSkeleton.get(i).getFirst());
		}
		for(int i=0; i<sentSkeleton.size()-1; ++i) {
			features.add(prefix+"_SentBigram_"+sentSkeleton.get(i).getFirst()
					+"_"+sentSkeleton.get(i+1).getFirst());
		}
		boolean Rbefore = false;
		for(int i=0; i<x.index; i++) {
			if(x.relations.get(i).startsWith("R")) {
				Rbefore = true;
			}
		}
		features.add(labelSet.relation+"_Rbefore_"+Rbefore);
		return features;
	}
	
	public List<String> pairwiseFeatures(RelationX x, RelationY y, int index) {
		List<String> features = new ArrayList<>();
		String prefix = "";
		if(y.relation.equals("R1") && x.relations.get(index).equals("R1")) {
			prefix = "SAME";
		} else if(y.relation.equals("R2") && x.relations.get(index).equals("R2")) {
			prefix = "SAME";
		} else if(y.relation.equals("R1") && x.relations.get(index).equals("R2")) {
			prefix = "DIFF";
		} else if(y.relation.equals("R2") && x.relations.get(index).equals("R1")) {
			prefix = "DIFF";
		} else {
			return features;
		}
		QuantSpan qs1 = x.quantities.get(index);
		int tokenId1 = x.ta.getTokenIdFromCharacterOffset(qs1.start);
		Sentence sent1 = x.ta.getSentenceFromToken(tokenId1);
		List<Pair<String, IntPair>> sentSkeleton1 = FeatGen.getPartialSkeleton(
				x.skeleton, sent1.getStartSpan(), sent1.getEndSpan());
		QuantSpan qs2 = x.quantities.get(x.index);
		int tokenId2 = x.ta.getTokenIdFromCharacterOffset(qs2.start);
		Sentence sent2 = x.ta.getSentenceFromToken(tokenId2);
		List<Pair<String, IntPair>> sentSkeleton2 = FeatGen.getPartialSkeleton(
				x.skeleton, sent2.getStartSpan(), sent2.getEndSpan());
		if(Tools.safeEquals(Tools.getValue(qs1), Tools.getValue(qs2))) {
			features.add(prefix+"_SAME_NUMBER");
		}
		if(Tools.getUnit(qs1).contains(Tools.getUnit(qs2)) || 
				Tools.getUnit(qs2).contains(Tools.getUnit(qs1))) {
			features.add(prefix+"_SAME_UNIT");
		}
		if(sent1.getSentenceId() == sent2.getSentenceId()) {
			features.add(prefix+"_SAME_SENTENCE");
		} else {
			features.add(prefix+"_DIFF_SENTENCE");
		}
		for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton1, tokenId1, 3)) {
			features.add(prefix+"_Other_Neighbor_"+feature);
		}
		for(int i=0; i<sentSkeleton1.size(); ++i) {
			features.add(prefix+"_Other_SentUnigram_"+sentSkeleton1.get(i).getFirst());
		}
		for(int i=0; i<sentSkeleton1.size()-1; ++i) {
			features.add(prefix+"_Other_SentBigram_"+sentSkeleton1.get(i).getFirst()
					+"_"+sentSkeleton1.get(i+1).getFirst());
		}for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton2, tokenId2, 3)) {
			features.add(prefix+"_Mine_Neighbor_"+feature);
		}
		for(int i=0; i<sentSkeleton2.size(); ++i) {
			features.add(prefix+"_Mine_SentUnigram_"+sentSkeleton2.get(i).getFirst());
		}
		for(int i=0; i<sentSkeleton2.size()-1; ++i) {
			features.add(prefix+"_Mine_SentBigram_"+sentSkeleton2.get(i).getFirst()
					+"_"+sentSkeleton2.get(i+1).getFirst());
		}
		
		
		return features;
	}
	
	
	
}