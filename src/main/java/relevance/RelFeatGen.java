package relevance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.Node;
import utils.FeatGen;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class RelFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
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
		return features;
	}
	private static void addPOSfeatures(List<String> features, RelX x, RelY y, int index) {
		int window = 3;
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