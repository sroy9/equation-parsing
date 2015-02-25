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
	
	public static List<String> getUnigrams(TextAnnotation ta) {
		List<String> unigrams = new ArrayList<>();
		for(int i=0; i<ta.size(); ++i) {
			if(NumberUtils.isNumber(ta.getToken(i).toLowerCase().replace(",", ""))) {
				unigrams.add("NUMBER");
			} else {
				unigrams.add(ta.getToken(i).toLowerCase());
			}
		}
		return unigrams;
	}
	
	public static List<String> getLemmatizedUnigrams(
			List<Constituent> lemmas, int start, int end) {
		List<String> unigrams = new ArrayList<String>();
		for(int i = start; i <= end; ++i) {
			if(NumberUtils.isNumber(lemmas.get(i).getLabel().replace(",", ""))) {
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
	
}
