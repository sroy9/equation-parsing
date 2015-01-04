package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import semparse.SemFeatGen;
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
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class RelationFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public RelationFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		RelationX x = (RelationX) arg0;
		RelationY y = (RelationY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	public static List<String> getFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
		features.addAll(globalFeatures(x, y));
		for(int i=0; i<y.relations.size(); ++i) {
			features.addAll(relationFeatures(x, y, i));
		}
		return features;
	}
	
	public static List<String> globalFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
		if(Tools.getNONEcount(y.relations) > 1) features.add("NONE>1");
		else if(Tools.getNONEcount(y.relations) > 2) features.add("NONE>2");
		else if(Tools.getNONEcount(y.relations) > 3) features.add("NONE>3");
		features.add("BOTH_"+Tools.getBOTHcount(y.relations));
		if(x.quantities.size()>2) features.add("NumQuant>2");
		else if(x.quantities.size()>4) features.add("NumQuant>4");
		else if(x.quantities.size()>6) features.add("NumQuant>6");
		else if(x.quantities.size()>8) features.add("NumQuant>8");
		features.add("IsOneVar_"+Tools.isOneVar(y.relations));
		features.add("NumQuestionSentences_"+FeatGen.getQuestionSentences(x.ta).size());
		return features;
	}
	
	public static List<String> relationFeatures(RelationX x, RelationY y, int index) {
		List<String> features = new ArrayList<>();
		String prefix = "";
		if(y.relations.get(index).startsWith("R")) {
			for(int i=0; i<index; ++i) {
				if(y.relations.get(i).startsWith("R")) {
					prefix = (y.relations.get(index).equals(
							y.relations.get(i)) ? "SAME" : "DIFF");
					for(String feature : pairWise(x, y, i, index)) {
						features.add(prefix+"_"+feature);
					}
				}
			}
		} else {
			prefix = y.relations.get(index);
			for(String feature : single(x, y, index)) {
				features.add(prefix+"_"+feature);
			}
		}
		return features;
	}
	
	public static List<String> pairWise(RelationX x, RelationY y, int index1, int index2) {
		List<String> features = new ArrayList<>();
		QuantSpan qs1 = x.quantities.get(index1);
		QuantSpan qs2 = x.quantities.get(index2);
		int tokenId1 = x.ta.getTokenIdFromCharacterOffset(qs1.start);
		Sentence sent1 = x.ta.getSentenceFromToken(tokenId1);
		int tokenId2 = x.ta.getTokenIdFromCharacterOffset(qs2.start);
		Sentence sent2 = x.ta.getSentenceFromToken(tokenId2);
		if(sent1.getSentenceId() == sent2.getSentenceId()) {
			features.add("SameSentence");
			List<Pair<String, IntPair>> skeleton = FeatGen.getPartialSkeleton(
					x.skeleton, tokenId1+1, tokenId2);
			for(int i=0; i<skeleton.size(); ++i) {
				features.add("MidUnigram_"+skeleton.get(i).getFirst());
			}
			for(int i=0; i<skeleton.size()-1; ++i) {
				features.add("MidBigram_"+skeleton.get(i).getFirst()
						+"_"+skeleton.get(i+1).getFirst());
			}
		}
		if(Tools.safeEquals(Tools.getValue(qs1), Tools.getValue(qs2))) {
			features.add("SameNumber");
		}
		if(Tools.getUnit(qs1).contains(Tools.getUnit(qs2)) ||
				Tools.getUnit(qs2).contains(Tools.getUnit(qs1))) {
			features.add("SameUnit");
		}
		return features;
	}
	
	public static List<String> single(RelationX x, RelationY y, int index) {
		List<String> features = new ArrayList<>();
		QuantSpan qs = x.quantities.get(index);
		int tokenId = x.ta.getTokenIdFromCharacterOffset(x.quantities.get(index).start);
		Sentence sent = x.ta.getSentenceFromToken(tokenId);
		List<Constituent> sentLemmas = FeatGen.partialLemmas(
				x.lemmas, sent.getStartSpan(), sent.getEndSpan());
		List<Pair<String, IntPair>> sentSkeleton = FeatGen.getPartialSkeleton(
				x.skeleton, sent.getStartSpan(), sent.getEndSpan());
		features.addAll(FeatGen.neighboringSkeletonTokens(sentSkeleton, tokenId, 3));
		features.add("UNIT_"+Tools.getUnit(qs));
		if(Tools.safeEquals(1.0, Tools.getValue(qs)) || Tools.safeEquals(2.0, Tools.getValue(qs))) {
			features.add("ONE_OR_TWO");
		}
		return features;
	}
	
}