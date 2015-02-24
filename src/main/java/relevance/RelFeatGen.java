package relevance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.Node;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class RelFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	private static int lexWindow=5;
	private static int posWindow=5;
	public RelFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		RelX x = (RelX) arg0;
		RelY y = (RelY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	public static List<String> getFeatures(RelX x, RelY y) {
		List<String> features = new ArrayList<>();
		addPOSfeatures(features,x,y,x.quantIndex);
		addLexfeatures(features,x,y,x.quantIndex);
		addUnitfeatures(features,x,y,x.quantIndex);
		addNextTokenfeatures(features,x,y,x.quantIndex);
//		IsOneAnotherFeature(features,x,y,x.quantIndex);
		return features;
	}

	private static void addNextTokenfeatures(List<String> features, RelX x,
			RelY y, int quantIndex) {
		
	}

	private static void addUnitfeatures(List<String> features, RelX x, RelY y,
			int quantIndex) {
		String unit = Tools.getUnit(x.quantities.get(x.quantIndex));
		if(unit.length()!=0)
		{
			addFeature("UNIT_"+unit, y, features);
			if(unit.contains(" per "))
			{
				addFeature("UNIT_PER_"+1, y, features);
			}
		}
	}

	private static void addLexfeatures(List<String> features, RelX x, RelY y,
			int index) {
		String[] tokens = x.ta.getTokens();
		int window = lexWindow;
		int before = Math.max(0, index - window);
		int after = Math.min(tokens.length- 1, index + window);
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

	private static void addPOSfeatures(List<String> features, RelX x, RelY y, int index) {
		int window = posWindow;
		int before = Math.max(0, index - window);
		int after = Math.min(x.posTags.size() - 1, index + window);
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
			RelY y, List<String> features) {
		features.add(string+"_L"+y.decision);
	}
}