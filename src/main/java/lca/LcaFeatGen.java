package lca;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import utils.FeatGen;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class LcaFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public LcaFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		LcaX x = (LcaX) arg0;
		LcaY y = (LcaY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	public static List<String> getFeatures(LcaX x, LcaY y) {
		List<String> features = new ArrayList<>();
		IntPair ip1, ip2;
		if(x.leaf1.label.equals("VAR")) {
			ip1 = x.candidateVars.get(x.leaf1.index);
		} else {
			QuantSpan qs = x.quantities.get(x.leaf1.index);
			ip1 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
					x.ta.getTokenIdFromCharacterOffset(qs.end));
		}
		if(x.leaf2.label.equals("VAR")) {
			ip2 = x.candidateVars.get(x.leaf2.index);
		} else {
			QuantSpan qs = x.quantities.get(x.leaf2.index);
			ip2 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
					x.ta.getTokenIdFromCharacterOffset(qs.end));
		}
		int min = Math.min(ip1.getSecond(), ip2.getSecond());
		int max = Math.max(ip1.getFirst(), ip2.getFirst());
		for(int i=min; i<max; ++i) {
			addFeature("MidUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
		}
		for(int i=min; i<max-1; ++i) {
			addFeature("MidBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase(), y, features);
		}
		return features;
	}

	public static void addFeature(String string, LcaY y, List<String> features) {
		features.add(string+"_"+y);
	}
}