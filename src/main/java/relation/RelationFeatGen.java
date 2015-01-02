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
	
	public IFeatureVector getRelationFeatureVector(RelationX x, RelationY y) {
		List<String> feats = getRelationFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(feats, lm);
	}
	
	public IFeatureVector getEquationFeatureVector(RelationX x, RelationY y) {
		List<String> feats = getEquationFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
//		System.out.println("GetFeatures : "+y.equations.size());
		features.addAll(getRelationFeatures(x, y));
		features.addAll(getEquationFeatures(x, y));
		return features;
	}
	
	public List<String> getRelationFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<y.relations.size(); ++i) {
			features.addAll(relationFeatures(x, y, i));
		}
		return features;
	}
	
	public List<String> getEquationFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
		features.addAll(solutionFeatures(x, y));
		return features;
	}
	
	public List<String> relationFeatures(RelationX x, RelationY y, int index) {
		List<String> features = new ArrayList<>();
		String prefix = "";
		if(y.relations.get(index).startsWith("R")) {
			for(int i=0; i<index; ++i) {
				if(y.relations.get(i).startsWith("R")) {
					prefix = (y.relations.get(index).equals(
							y.relations.get(i)) ? "SAME" : "DIFF");
					QuantSpan qs1 = x.quantities.get(i);
					QuantSpan qs2 = x.quantities.get(index);
					int tokenId1 = x.ta.getTokenIdFromCharacterOffset(qs1.start);
					Sentence sent1 = x.ta.getSentenceFromToken(tokenId1);
					int tokenId2 = x.ta.getTokenIdFromCharacterOffset(qs2.start);
					Sentence sent2 = x.ta.getSentenceFromToken(tokenId2);
					if(sent1.getSentenceId() == sent2.getSentenceId()) {
						List<Pair<String, IntPair>> skeleton = FeatGen.getPartialSkeleton(
								x.skeleton, Math.min(tokenId1, tokenId2), Math.max(tokenId1, tokenId2)+1);
						features.add(prefix+"_SameSentence");
						boolean numberInBetween = false;
						for(int j=2; j<skeleton.size()-2; ++j) {
							if(skeleton.get(j).getFirst().equals("NUMBER")) {
								numberInBetween = true;
								features.add(prefix+"_NUMBER_IN_BETWEEN");
								break;
							}
						}
						if(!numberInBetween) {
							for(int j=0; j<skeleton.size(); ++j) {
								features.add(prefix+"_MidUnigram_"+skeleton.get(j).getFirst());
							}
							for(int j=0; j<skeleton.size()-1; ++j) {
								features.add(prefix+"_MidBigram_"+skeleton.get(j).getFirst()
										+"_"+skeleton.get(j+1).getFirst());
							}
							for(String feature : FeatGen.getLemmatizedUnigrams(x.lemmas, 
									Math.min(tokenId1, tokenId2), 
									Math.max(tokenId1, tokenId2))) {
								features.add(prefix+"_MidUnigram_"+feature);
							}
							for(String feature : FeatGen.getLemmatizedBigrams(x.lemmas, 
									Math.min(tokenId1, tokenId2), 
									Math.max(tokenId1, tokenId2))) {
								features.add(prefix+"_MidBigram_"+feature);
							}
						}
					}
				}
			}
		} else {
			prefix = y.relations.get(index);
			int tokenId = x.ta.getTokenIdFromCharacterOffset(x.quantities.get(index).start);
			Sentence sent = x.ta.getSentenceFromToken(tokenId);
			List<Constituent> sentLemmas = FeatGen.partialLemmas(
					x.lemmas, sent.getStartSpan(), sent.getEndSpan());
			List<Pair<String, IntPair>> sentSkeleton = FeatGen.getPartialSkeleton(
					x.skeleton, sent.getStartSpan(), sent.getEndSpan());
//			for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton, tokenId, 3)) {
//				features.add(prefix+"_"+feature);
//			}
//			for(int i=0; i<sentSkeleton.size(); ++i) {
//				features.add(prefix+"_SentUnigram_"+sentSkeleton.get(i).getFirst());
//			}
//			for(int i=0; i<sentSkeleton.size()-1; ++i) {
//				features.add(prefix+"_SentBigram_"+sentSkeleton.get(i).getFirst()
//						+"_"+sentSkeleton.get(i+1).getFirst());
//			}
		}
		return features;
	}
	
	public List<String> solutionFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
		List<Double> solns = EquationSolver.solveSemYs(y.equations); 
		if(solns == null) {
			features.add("Not_Solvable");
			return features;
		}
		for(Double d : solns) {
			if(d-d.intValue() < 0.0001) {
				features.add("Integer_Solution");
			}
			if(d>0) features.add("Positive solution");
		}
		return features;
	}
	
	public List<String> pairwiseFeatures(RelationX x, RelationY y, int index) {
		List<String> features = new ArrayList<>();
//		String prefix = "";
//		if(y.relation.equals("R1") && x.relations.get(index).equals("R1")) {
//			prefix = "SAME";
//		} else if(y.relation.equals("R2") && x.relations.get(index).equals("R2")) {
//			prefix = "SAME";
//		} else if(y.relation.equals("R1") && x.relations.get(index).equals("R2")) {
//			prefix = "DIFF";
//		} else if(y.relation.equals("R2") && x.relations.get(index).equals("R1")) {
//			prefix = "DIFF";
//		} else {
//			return features;
//		}
//		QuantSpan qs1 = x.quantities.get(index);
//		int tokenId1 = x.ta.getTokenIdFromCharacterOffset(qs1.start);
//		Sentence sent1 = x.ta.getSentenceFromToken(tokenId1);
//		List<Pair<String, IntPair>> sentSkeleton1 = FeatGen.getPartialSkeleton(
//				x.skeleton, sent1.getStartSpan(), sent1.getEndSpan());
//		QuantSpan qs2 = x.quantities.get(x.index);
//		int tokenId2 = x.ta.getTokenIdFromCharacterOffset(qs2.start);
//		Sentence sent2 = x.ta.getSentenceFromToken(tokenId2);
//		List<Pair<String, IntPair>> sentSkeleton2 = FeatGen.getPartialSkeleton(
//				x.skeleton, sent2.getStartSpan(), sent2.getEndSpan());
//		if(Tools.safeEquals(Tools.getValue(qs1), Tools.getValue(qs2))) {
//			features.add(prefix+"_SAME_NUMBER");
//		}
//		if(Tools.getUnit(qs1).contains(Tools.getUnit(qs2)) || 
//				Tools.getUnit(qs2).contains(Tools.getUnit(qs1))) {
//			features.add(prefix+"_SAME_UNIT");
//		}
//		if(Tools.safeEquals(Tools.getValue(qs1), Tools.getValue(qs2)) &&
//				(Tools.getUnit(qs1).contains(Tools.getUnit(qs2)) || 
//				Tools.getUnit(qs2).contains(Tools.getUnit(qs1)))) {
//			features.add(prefix+"_SAME_NUMBER_UNIT");
//		}
//		if(sent2.getText().contains("?")) features.add(prefix+"_QuestionSentence");
//		features.addAll(FeatGen.getConjunctions(features));
//		
//		if(sent1.getSentenceId() == sent2.getSentenceId()) {
//			features.add(prefix+"_SAME_SENTENCE");
//		} else {
//			features.add(prefix+"_DIFF_SENTENCE");
//		}
//		for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton1, tokenId1, 3)) {
//			features.add(prefix+"_Other_Neighbor_"+feature);
//		}
//		for(int i=0; i<sentSkeleton1.size(); ++i) {
//			features.add(prefix+"_Other_SentUnigram_"+sentSkeleton1.get(i).getFirst());
//		}
//		for(int i=0; i<sentSkeleton1.size()-1; ++i) {
//			features.add(prefix+"_Other_SentBigram_"+sentSkeleton1.get(i).getFirst()
//					+"_"+sentSkeleton1.get(i+1).getFirst());
//		}
//		for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton2, tokenId2, 3)) {
//			features.add(prefix+"_Mine_Neighbor_"+feature);
//		}
//		for(int i=0; i<sentSkeleton2.size(); ++i) {
//			features.add(prefix+"_Mine_SentUnigram_"+sentSkeleton2.get(i).getFirst());
//		}
//		for(int i=0; i<sentSkeleton2.size()-1; ++i) {
//			features.add(prefix+"_Mine_SentBigram_"+sentSkeleton2.get(i).getFirst()
//					+"_"+sentSkeleton2.get(i+1).getFirst());
//		}
		return features;
	}
	
	
	
}