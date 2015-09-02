package numoccur;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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
	private static int lexWindow=5;
	private static int posWindow=3;
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
		addPOSfeatures(features,x,y);
		addLexfeatures(features,x,y);
		addUnitfeatures(features,x,y);
		addOneOrTwoFeatures(features,x,y);
		addQuantPhraseFeatures(features,x,y);
		addGlobalFeatures(features,x,y);
		return features;
	}
	
	private static void addGlobalFeatures(List<String> features, NumoccurX x, NumoccurY y) {
		if(x.quantities.size() == 1) {
			addFeature("OneQuantityPresent", y, features);
		}
	}

	private static void addOneOrTwoFeatures(List<String> features, NumoccurX x, NumoccurY y) {
		Double val= Tools.getValue(x.quantities.get(x.quantIndex));
		if(Tools.safeEquals(val, 1.0))
		{
			addFeature("One", y, features);
		}
		if(Tools.safeEquals(val, 2.0))
		{
			addFeature("Two", y, features);
			QuantSpan qs = x.quantities.get(x.quantIndex);
			String quantPhrase = x.ta.getText().substring(qs.start, qs.end);
			if(quantPhrase.contains("twice") || quantPhrase.contains("Twice") || 
					quantPhrase.contains("times")) {
				addFeature("TwicePresent", y, features);
			}
		}
		
	}
	
	private static void addQuantPhraseFeatures(List<String> features, NumoccurX x, NumoccurY y) {
		QuantSpan qs = x.quantities.get(x.quantIndex);
		String quantPhrase = x.ta.getText().substring(qs.start, qs.end);
		String quantTokens[] = quantPhrase.split(" ");
		for(String str : quantTokens) {
			addFeature("QuantUnigram_"+str, y, features);
		}
		for(int i=0; i<quantTokens.length-1; ++i) {
			addFeature("QuantBigram_"+quantTokens[i]+"_"+quantTokens[i+1], y, features);
		}
		if(quantPhrase.contains("-")) addFeature("HyphenWord", y, features);
	}

	private static void addUnitfeatures(List<String> features, NumoccurX x, NumoccurY y) {
		String unit = Tools.getUnit(x.quantities.get(x.quantIndex));
		if(unit.length()!=0)
		{
			addFeature("UNIT_"+unit, y, features);
			if(unit.trim().equals("per"))
			{
				addFeature("UNIT_PER_1", y, features);
			}
			if(!StringUtils.isAlphanumeric(unit))
			{
				addFeature("UNIT_SYMBOL_1", y, features);
			}
		}
	}

	private static void addLexfeatures(List<String> features, NumoccurX x, NumoccurY y) {
		QuantSpan quant = x.quantities.get(x.quantIndex);
		int tok_index=x.ta.getTokenIdFromCharacterOffset(quant.start);
		
		String[] tokens = x.ta.getTokens();
		int window = lexWindow;
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
				addFeature(__id + __value,y,features);
			}
		}
	}

	private static void addPOSfeatures(List<String> features, NumoccurX x, NumoccurY y) {
		
		QuantSpan quant = x.quantities.get(x.quantIndex);
		int tok_index=x.ta.getTokenIdFromCharacterOffset(quant.start);
		
		int window = posWindow;
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
				addFeature(__id + __value,y,features);

			}
		}		
	}

	private static void addFeature(String string,
			NumoccurY y, List<String> features) {
		features.add(string+"_L"+y.numOccur);
	}
}