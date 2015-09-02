package var;

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

public class VarFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	private static int lexWindow=5;
	private static int posWindow=3;
	
	public VarFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		VarX x = (VarX) arg0;
		VarY y = (VarY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	public static List<String> getFeatures(VarX x, VarY y) {
		List<String> features = new ArrayList<>();
		addPOSfeatures(features,x,y);
		addLexfeatures(features,x,y);
		addUnitfeatures(features,x,y);
		addOneOrTwoFeatures(features,x,y);
		addQuantPhraseFeatures(features,x,y);
		addGlobalFeatures(features,x,y);
		return features;
	}
	
	private static void addGlobalFeatures(List<String> features, VarX x, VarY y) {
		if(x.quantities.size() == 1) {
			addFeature("OneQuantityPresent", y, features);
		}
	}

	private static void addLexfeatures(List<String> features, VarX x, VarY y) {
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

	private static void addPOSfeatures(List<String> features, VarX x, VarY y) {
		
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

	private static void addFeature(String string, VarY y, List<String> features) {
		features.add(string+"_L"+y.relevant);
	}
}