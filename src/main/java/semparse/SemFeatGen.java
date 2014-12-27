package semparse;

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

public class SemFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;

	public SemFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		SemX x = (SemX) arg0;
		SemY y = (SemY) arg1;
		List<String> features = new ArrayList<>();
		features.addAll(getFeatures(x, y));
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public IFeatureVector getFeatureVector(SemX x, SemY y) {
		List<String> feats = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getFeatures(SemX x, SemY y) {
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

	public List<String> alignmentFeatures(SemX x, SemY y, IntPair slot) {
		List<String> features = new ArrayList<>();
		Double d = y.terms.get(slot.getFirst()).get(slot.getSecond()).getSecond();
		List<QuantSpan> quantSpans = Tools.getRelevantQuantSpans(d, x.relationQuantities);
		int tokenId = x.ta.getTokenIdFromCharacterOffset(quantSpans.get(0).start);
		Sentence sent = x.ta.getSentenceFromToken(tokenId);
		List<Constituent> sentLemmas = FeatGen.partialLemmas(
				x.lemmas, sent.getStartSpan(), sent.getEndSpan());
		List<Pair<String, IntPair>> sentSkeleton = FeatGen.getPartialSkeleton(
				x.skeleton, sent.getStartSpan(), sent.getEndSpan());
		String prefix;
		if(slot.getFirst() == 4) prefix = ""+slot.getFirst();
		else if(slot.getFirst() == 1 || slot.getFirst() == 3){
			prefix = ""+y.operations.get(slot.getFirst())+"_AB2";
		} else {
			prefix = ""+y.operations.get(slot.getFirst())+"_AB1";
		}
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
		return features;
	}
	
	public List<String> pairwiseFeatures(SemX x, SemY y, IntPair slot1, IntPair slot2) {
		List<String> features = new ArrayList<>();
		Double d1 = y.terms.get(slot1.getFirst()).get(slot1.getSecond()).getSecond();
		List<QuantSpan> quantSpans1 = Tools.getRelevantQuantSpans(d1, x.relationQuantities);
		int tokenId1 = x.ta.getTokenIdFromCharacterOffset(quantSpans1.get(0).start);
		Sentence sent1 = x.ta.getSentenceFromToken(tokenId1);
		Double d2 = y.terms.get(slot2.getFirst()).get(slot2.getSecond()).getSecond();
		List<QuantSpan> quantSpans2 = Tools.getRelevantQuantSpans(d2, x.relationQuantities);
		int tokenId2 = x.ta.getTokenIdFromCharacterOffset(quantSpans2.get(0).start);
		Sentence sent2 = x.ta.getSentenceFromToken(tokenId2);
		String prefix = "";
		if((slot1.getFirst() == 0 && slot2.getSecond() == 1) || 
				(slot1.getFirst() == 2 && slot2.getSecond() == 3)) {
			prefix = "A1A2";
		} else if((slot1.getFirst() == 0 && slot2.getSecond() == 3) || 
				(slot1.getFirst() == 1 && slot2.getSecond() == 2)) {
			prefix = "A1B2";
		} else if((slot1.getFirst() == 1 && slot2.getSecond() == 1) || 
				(slot1.getFirst() == 3 && slot2.getSecond() == 3)) {
			prefix = "A2A2";
		} else {
			prefix = slot1.getFirst()+"_"+slot2.getFirst();
		}
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
		return features;
	}
	
	public List<String> templateFeatures(SemX x, SemY y) {
		List<String> features = new ArrayList<>();
		Set<Integer> relevantSentenceIds = new HashSet<>();
		for(IntPair slot : y.emptySlots) {
			Double d = y.terms.get(slot.getFirst()).get(slot.getSecond()).getSecond();
			List<QuantSpan> quantSpans = Tools.getRelevantQuantSpans(d, x.relationQuantities);
			int tokenId = x.ta.getTokenIdFromCharacterOffset(quantSpans.get(0).start);
			Sentence sent = x.ta.getSentenceFromToken(tokenId);
			relevantSentenceIds.add(sent.getSentenceId());
		}
		for(int i=0; i<4; ++i) {
			for(Integer j : relevantSentenceIds) {
				Sentence sent = x.ta.getSentence(j);
				String prefix = i+"_"+y.operations.get(i);
				if(sent.getText().contains("difference")) {
					features.add(prefix+"_DIFFERENCE MENTIONED");
				}
				if(sent.getText().contains("sum") || sent.getText().contains("total")) {
					features.add(prefix+"_SUM_MENTIONED");
				}
				if(sent.getText().contains("more than")) {
					features.add(prefix+"_MORE_MENTIONED");
				}
				if(sent.getText().contains("less than")) {
					features.add(prefix+"_LESS_MENTIONED");
				}
			}
		}
		return features;
	}
}