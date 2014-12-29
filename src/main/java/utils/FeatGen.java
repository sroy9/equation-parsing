package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class FeatGen {
	
	public static IFeatureVector getFeatureVectorFromList(
			List<String> features, Lexiconer lm) {
		FeatureVectorBuffer fvb = new FeatureVectorBuffer();
		for(String feature : features) {
			if(!lm.containFeature(feature) && lm.isAllowNewFeatures()) {
				lm.addFeature(feature);
			}
			if(lm.containFeature(feature)) {
				fvb.addFeature(lm.getFeatureId(feature), 1.0);
			}
		}
		return fvb.toFeatureVector();
	}
	
	public static List<String> getLemmatizedUnigrams(
			List<Constituent> lemmas, int start, int end) {
		List<String> unigrams = new ArrayList<String>();
		for(int i = start; i <= end; ++i) {
			if(NumberUtils.isNumber(lemmas.get(i).getLabel()/*.replace(",", "")*/)) {
				unigrams.add("NUMBER");
			} else {
				unigrams.add(lemmas.get(i).getLabel());
			}
		}
		return unigrams;
	}

	public static List<String> getLemmatizedBigrams(
			List<Constituent> lemmas, int start, int end) {
		List<String> bigrams = new ArrayList<String>();
		List<String> unigrams = getLemmatizedUnigrams(lemmas, start, end);
		for(int i = 0; i < unigrams.size()-1; ++i) {
			bigrams.add(unigrams.get(i)+"_"+unigrams.get(i+1));
		}
		return bigrams;
	}
		
	public static List<String> getConjunctions(List<String> features) {
		List<String> conjunctions = new ArrayList<String>();
		for(String feature1 : features) {
			for(String feature2 : features) {
				conjunctions.add(feature1+"_"+feature2);
			}
		}
		return conjunctions;
	}
	
	public static List<String> getConjunctions(
			List<String> features1, List<String> features2) {
		List<String> conjunctions = new ArrayList<String>();
		for(String feature1 : features1) {
			for(String feature2 : features2) {
				conjunctions.add(feature1+"_"+feature2);
			}
		}
		return conjunctions;
	}
	
	public static IntPair getClosestMention(Set<IntPair> ipSet, IntPair key) {
		int minDist = 1000; 
		IntPair closest = null;
		for(IntPair ip : ipSet) {
			if(Math.abs(ip.getFirst()-key.getFirst()) < minDist) {
				minDist = Math.abs(ip.getFirst()-key.getFirst());
				closest = ip;
			}
		}
		return closest;
	}
	
	public static IntPair getSpanBetweenClosestMention(
			Set<IntPair> ipSet1, Set<IntPair> ipSet2) {
		int minDist = 1000; 
		IntPair closest1 = null;
		IntPair closest2 = null;
		for(IntPair ip1 : ipSet1) {
			for(IntPair ip2 : ipSet2) {
				if(Math.abs(ip1.getFirst()-ip2.getFirst()) < minDist) {
					minDist = Math.abs(ip1.getFirst()-ip2.getFirst());
					closest1 = ip1;
					closest2 = ip2;
				}
			}
		}
		return new IntPair(Math.min(closest1.getSecond(), closest2.getSecond()),
				Math.max(closest1.getFirst(), closest2.getFirst()));
	}
	

	public static List<Sentence> getQuestionSentences(TextAnnotation ta) {
		List<Sentence> questionSentences = new ArrayList<>();
		for(int i=0; i<ta.getNumberOfSentences(); i++) {
			if(ta.getSentence(i).getText().contains("?")) {
				questionSentences.add(ta.getSentence(i));
			} 
		}
		return questionSentences;
	}
	
	public static List<Constituent> getDependencyPath(
			TextAnnotation ta, List<Constituent> dependencyParse, int index) {
		List<Constituent> path = new ArrayList<>();
		Constituent leaf = null;
		for(Constituent cons : dependencyParse) {
			if(Tools.doesIntersect(cons.getSpan(), new IntPair(index, index+1))) {
				leaf = cons;
				break;
			}
		}
		path.add(leaf);
		while(leaf.getIncomingRelations().size() > 0) {
			leaf = leaf.getIncomingRelations().get(0).getSource();
			path.add(leaf);
		}
		return path;
	}
	
	public static List<Constituent> partialLemmas(
			List<Constituent> lemmas, int startPos, int endPos) {
		List<Constituent> partialLemmas = new ArrayList<>();
		for(Constituent cons : lemmas) {
			if(cons.getStartSpan()>=startPos && cons.getEndSpan()<=endPos) {
				partialLemmas.add(cons);
			}
		}
		return partialLemmas;
	}
	
	public static List<String> neighboringTokens(
			List<Constituent> lemmas, int pos, int window) {
		List<String> features = new ArrayList<String>();
		List<String> unigrams = getLemmatizedUnigrams(lemmas, 0, lemmas.size()-1);
		int index = -1;
		for(int i=0; i<lemmas.size(); i++) {
			Constituent cons = lemmas.get(i);
			if(pos >= cons.getStartSpan() && pos < cons.getEndSpan()) {
				index = i;
				break;
			}
		}
		int start = Math.max(0, index-window);
		int end = Math.min(index+window, lemmas.size()-1);
		for(int ngram = 0; ngram <= 2; ngram++) {
			for(int i=start; i<=end-ngram; i++) {
				features.add((i-index)+"_"+(i+ngram-index)+"_"+unigrams.get(i)+"_"+unigrams.get(i+ngram));
				features.add(unigrams.get(i)+"_"+unigrams.get(i+ngram));
			}
		}	
		return features;
	}
	
	public static List<Pair<String, IntPair>> getPartialSkeleton(
			List<Pair<String, IntPair>> skeleton, int startPos, int endPos) {
		List<Pair<String, IntPair>> partialSkeleton = new ArrayList<>();
		for(Pair<String, IntPair> pair : skeleton) {
			if(pair.getSecond().getFirst()>=startPos && pair.getSecond().getSecond()<=endPos) {
				partialSkeleton.add(pair);
			}
		}
		return partialSkeleton;
	}
	
 	public static List<String> neighboringSkeletonTokens(
			List<Pair<String, IntPair>> skeleton, int pos, int window) {
		List<String> features = new ArrayList<>();
		int index = -1;
		for(int i=0; i<skeleton.size(); i++) {
			Pair<String, IntPair> pair = skeleton.get(i);
			if(pos >= pair.getSecond().getFirst() && pos < pair.getSecond().getSecond()) {
				index = i;
				break;
			}
		}
		int start = Math.max(0, index-window);
		int end = Math.min(index+window, skeleton.size()-1);
		for(int ngram = 0; ngram <= 2; ngram++) {
			for(int i=start; i<=end-ngram; i++) {
				features.add((i-index)+"_"+(i+ngram-index)+"_"+skeleton.get(i).getFirst()+"_"+
						skeleton.get(i+ngram).getFirst());
				features.add(skeleton.get(i).getFirst()+"_"+skeleton.get(i+ngram).getFirst());
			}
		}	
		return features;
	}
}
