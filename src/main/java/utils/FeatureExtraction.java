package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class FeatureExtraction {
	
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
			if(NumberUtils.isNumber(lemmas.get(i).getLabel())) {
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
	
	public static IntPair getSpanBetweenClosestMention(Set<IntPair> ipSet1, Set<IntPair> ipSet2) {
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
}
