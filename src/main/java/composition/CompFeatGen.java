package composition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.illinois.cs.cogcomp.quant.standardize.Ratio;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class CompFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;

	public CompFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		CompX x = (CompX) arg0;
		CompY y = (CompY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(CompX x, CompY y) {
		List<String> features = new ArrayList<>();
		features.addAll(templateFeatures(x, y));
		for(IntPair slot : y.emptySlots) {
			features.addAll(alignmentFeatures(x, y, slot));
		}
		for(int i=0; i<y.emptySlots.size(); ++i) {
			for(int j=i+1; j<y.emptySlots.size(); ++j) {
				features.addAll(pairwiseFeatures(
						x, y, y.emptySlots.get(i), y.emptySlots.get(j)));
			}
		}
		return features;
	}

	public static List<String> alignmentFeatures(CompX x, CompY y, IntPair slot) {
		List<String> features = new ArrayList<>();
		String prefix = "";
		if(slot.getFirst() == 4) prefix = "C";
		if(slot.getFirst() == 1 || slot.getFirst() == 3){
			prefix = ""+y.operations.get(slot.getFirst())+"_AB2";
		} 
		if(slot.getFirst() == 0 || slot.getFirst() == 2){
			prefix = ""+y.operations.get(slot.getFirst())+"_AB1";
		}
		Double d = y.terms.get(slot.getFirst()).get(slot.getSecond()).getSecond();
		List<QuantSpan> quantSpans = Tools.getRelevantQuantSpans(d, x.relationQuantities);
		for(int j=0; j<quantSpans.size(); ++j) {
			QuantSpan qs = quantSpans.get(j);
			int tokenId = x.ta.getTokenIdFromCharacterOffset(qs.start);
			Sentence sent = x.ta.getSentenceFromToken(tokenId);
			List<Constituent> sentLemmas = FeatGen.partialLemmas(
					x.lemmas, sent.getStartSpan(), sent.getEndSpan());
			List<Pair<String, IntPair>> sentSkeleton = FeatGen.getPartialSkeleton(
					x.skeleton, sent.getStartSpan(), sent.getEndSpan());
			features.add(prefix+"_Unit_"+Tools.getUnit(qs));
			for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton, tokenId, 3)) {
				features.add(prefix+"_"+feature);
			}
			if(Tools.getUnit(qs).contains("percent")) {
				features.add(prefix+"_percent");
			}
		}
		return features;
	}
	
	public static List<String> pairwiseFeatures(CompX x, CompY y, IntPair slot1, IntPair slot2) {
		List<String> features = new ArrayList<>();
		Double d1 = y.terms.get(slot1.getFirst()).get(slot1.getSecond()).getSecond();
		Double d2 = y.terms.get(slot2.getFirst()).get(slot2.getSecond()).getSecond();
		List<QuantSpan> quantSpans1 = Tools.getRelevantQuantSpans(d1, x.relationQuantities);
		List<QuantSpan> quantSpans2 = Tools.getRelevantQuantSpans(d2, x.relationQuantities);
		String prefix = "";
		if((slot1.getFirst() == 0 && slot2.getFirst() == 3) || 
				(slot1.getFirst() == 1 && slot2.getFirst() == 2)) {
			prefix = "A1B2";
		} else {
			if(slot1.getFirst() == 0 || slot1.getFirst() == 2) prefix += "A1";
			if(slot1.getFirst() == 1 || slot1.getFirst() == 3) prefix += "A2";
			if(slot1.getFirst() == 4) prefix += "C";
			if(slot2.getFirst() == 0 || slot2.getFirst() == 2) prefix += "A1";
			if(slot2.getFirst() == 1 || slot2.getFirst() == 3) prefix += "A2";
			if(slot2.getFirst() == 4) prefix += "C";
		}
		for(int k=0; k<quantSpans1.size(); ++k) {
			for(int l=0; l<quantSpans2.size(); ++l) {
				int tokenId1 = x.ta.getTokenIdFromCharacterOffset(quantSpans1.get(k).start);
				Sentence sent1 = x.ta.getSentenceFromToken(tokenId1);
				QuantSpan qs1 = quantSpans1.get(k);
				QuantSpan qs2 = quantSpans2.get(l);
				int tokenId2 = x.ta.getTokenIdFromCharacterOffset(quantSpans2.get(l).start);
				Sentence sent2 = x.ta.getSentenceFromToken(tokenId2);
				if(sent1.getSentenceId() == sent2.getSentenceId()) {
					List<Pair<String, IntPair>> skeleton = FeatGen.getPartialSkeleton(
							x.skeleton, Math.min(tokenId1, tokenId2), Math.max(tokenId1, tokenId2)+1);
					boolean numberInBetween = false;
					for(int i=2; i<skeleton.size()-2; ++i) {
						if(skeleton.get(i).getFirst().equals("NUMBER")) {
							numberInBetween = true;
							features.add(prefix+"_NUMBER_IN_BETWEEN");
							break;
						}
					}
					if(!numberInBetween) {
						for(int i=0; i<skeleton.size(); ++i) {
							features.add(prefix+"_MidUnigram_"+skeleton.get(i).getFirst());
						}
						for(int i=0; i<skeleton.size()-1; ++i) {
							features.add(prefix+"_MidBigram_"+skeleton.get(i).getFirst()
									+"_"+skeleton.get(i+1).getFirst());
						}
					}
				}
				
			}
		}
		return features;
	}
	
	public static List<String> templateFeatures(CompX x, CompY y) {
		List<String> features = new ArrayList<>();
		Set<Integer> relevantSentenceIds = new HashSet<>();
		for(IntPair slot : y.emptySlots) {
			Double d = y.terms.get(slot.getFirst()).get(slot.getSecond()).getSecond();
			List<QuantSpan> quantSpans = Tools.getRelevantQuantSpans(d, x.relationQuantities);
			int tokenId = x.ta.getTokenIdFromCharacterOffset(quantSpans.get(0).start);
			Sentence sent = x.ta.getSentenceFromToken(tokenId);
			relevantSentenceIds.add(sent.getSentenceId());
		}
		features.add("Operation_AB1_"+y.operations.get(0));
		features.add("Operation_AB2_"+y.operations.get(1));
		features.add("Operation_AB_"+y.operations.get(0)+"_"+y.operations.get(1));
		features.add("Operation_AB1_"+y.operations.get(2));
		features.add("Operation_AB2_"+y.operations.get(3));
		features.add("Operation_AB_"+y.operations.get(2)+"_"+y.operations.get(3));
		features.add("IsOneVar_"+y.isOneVar);
		for(int i=0; i<4; ++i) {
			String prefix = "";
			if(i==0 || i==2) prefix = "AB1_"+y.operations.get(i);
			if(i==1 || i==3) prefix = "AB2_"+y.operations.get(i);
			if(i==4) prefix = "C";
			for(Integer j : relevantSentenceIds) {
				Sentence sent = x.ta.getSentence(j);
				if(sent.getText().contains("difference")) {
					features.add(prefix+"_DIFFERENCE MENTIONED");
				}
				if(sent.getText().contains("sum") || sent.getText().contains("total")) {
					features.add(prefix+"_SUM_MENTIONED");
				}
				if(sent.getText().contains("more than")) {
					features.add(prefix+"_MORE_THAN_MENTIONED");
				}
				if(sent.getText().contains("less than")) {
					features.add(prefix+"_LESS_THAN_MENTIONED");
				}
			}
		}
		return features;
	}
}