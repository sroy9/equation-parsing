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
		for(int i=0; i<x.index; i++) {
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
		for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton, tokenId, 3)) {
			features.add(prefix+"_"+feature);
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
		features.add(labelSet.relation+"_"+"Rbefore_"+Rbefore);
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
		return features;
	}
	
	
	
}