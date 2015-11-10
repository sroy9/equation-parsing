package numoccur;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import numoccur.NumoccurX;
import numoccur.NumoccurY;

import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class NumoccurFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public NumoccurFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		NumoccurX x = (NumoccurX) arg0;
		NumoccurY y = (NumoccurY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(NumoccurX x, NumoccurY y) {
		List<String> features = new ArrayList<>();
		features.addAll(getGlobalFeatures(x, y));
		for(int i=0; i<x.quantities.size(); ++i) {
			features.addAll(getIndividualFeatures(x, i, y.numOccurList.get(i)));
		}
		return features;
	}
	
	public IFeatureVector getGlobalFeatureVector(NumoccurX x, NumoccurY y) {
		List<String> features = getGlobalFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getIndividualFeatureVector(NumoccurX x, int quantIndex, int numOccur) {
		List<String> features = getIndividualFeatures(x, quantIndex, numOccur);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getGlobalFeatures(NumoccurX x, NumoccurY y) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<x.ta.size(); ++i) {
			features.add(y+"_Unigram_"+x.ta.getToken(i).toLowerCase());
		}
		if(x.quantities.size() == 1) {
			features.add("OneQuantityPresent");
		}
		return features;
	}
	
	public static List<String> getIndividualFeatures(NumoccurX x, int quantIndex, int numOccur) {
		List<String> features = new ArrayList<>();
		features.addAll(getLexfeatures(x, quantIndex, numOccur));
		features.addAll(getPOSfeatures(x, quantIndex, numOccur));
		features.addAll(getQuantPhraseFeatures(x, quantIndex, numOccur));
		features.addAll(getOneOrTwoFeatures(x, quantIndex, numOccur));
		return features;
	}
	

	public static List<String> getOneOrTwoFeatures(NumoccurX x, int quantIndex, int numOccur) {
		List<String> features = new ArrayList<>();
		Double val= Tools.getValue(x.quantities.get(quantIndex));
		if(Tools.safeEquals(val, 1.0)) {
			features.add(numOccur+"_One");
		}
		if(Tools.safeEquals(val, 2.0)) {
			features.add(numOccur+"_Two");
			QuantSpan qs = x.quantities.get(quantIndex);
			String quantPhrase = x.ta.getText().substring(qs.start, qs.end);
			if(quantPhrase.contains("twice") || quantPhrase.contains("Twice") || 
					quantPhrase.contains("times")) {
				features.add(numOccur+"_TwicePresent");
			}
		}
		return features;
	}
	
	public static List<String> getQuantPhraseFeatures(NumoccurX x, int quantIndex, int numOccur) {
		List<String> features = new ArrayList<>();
		QuantSpan qs = x.quantities.get(quantIndex);
		String quantPhrase = x.ta.getText().substring(qs.start, qs.end);
		String quantTokens[] = quantPhrase.split(" ");
		for(String str : quantTokens) {
			features.add(numOccur+"_QuantUnigram_"+str);
		}
		for(int i=0; i<quantTokens.length-1; ++i) {
			features.add(numOccur+"_QuantBigram_"+quantTokens[i]+"_"+quantTokens[i+1]);
		}
		if(quantPhrase.contains("-")) features.add(numOccur+"_HyphenWord");
		return features;
	}

	public static List<String> getLexfeatures(NumoccurX x, int quantIndex, int numOccur) {
		List<String> features = new ArrayList<>();
		QuantSpan quant = x.quantities.get(quantIndex);
		int tok_index=x.ta.getTokenIdFromCharacterOffset(quant.start);
		String[] tokens = x.ta.getTokens();
		int window = 2;
		int before = Math.max(0, tok_index - window);
		int after = Math.min(tokens.length- 1, tok_index + window);
		String __id;
		String __value;
		String[] forms = new String[before + after + 1];
		int i = 0;
		for (int j = before; j <= after; j++) {
			forms[i++] = tokens[j];
		}
		for (int j = 0; j < window; j++) {
			for (i = 0; i < forms.length; i++) {
				StringBuilder f = new StringBuilder();
				for (int context = 0; context <= j
						&& i + context < forms.length; context++) {
					if (context != 0) {
						f.append("_");
					}
					f.append(forms[i + context]);
				}
				__id = ("LEX_"+i + "_" + j);
				__value = "_" + (f.toString());
				features.add(numOccur+__id + __value);
			}
		}
		return features;
	}

	public static List<String> getPOSfeatures(NumoccurX x, int quantIndex, int numOccur) {
		List<String> features = new ArrayList<>();
		QuantSpan quant = x.quantities.get(quantIndex);
		int tok_index=x.ta.getTokenIdFromCharacterOffset(quant.start);
		int window = 2;
		int before = Math.max(0, tok_index - window);
		int after = Math.min(x.posTags.size() - 1, tok_index + window);
		int i;
		String __id;
		String __value;
		String[] tags = new String[before + after + 1];
		i = 0;
		for (int j = before; j <= after; j++) {
			tags[i++] = x.posTags.get(j).getLabel();
		}
		for (int j = 0; j < window; j++) {
			for (i = 0; i < tags.length; i++) {
				StringBuilder f = new StringBuilder();
				for (int context = 0; context <= j && i + context < tags.length; context++) {
					if (context != 0) {
						f.append("_");
					}
					f.append(tags[i + context]);
				}
				__id = ("POS_"+i + "_" + j);
				__value = "_" + (f.toString());
				features.add(numOccur+__id + __value);
			}
		}	
		return features;	
	}
}