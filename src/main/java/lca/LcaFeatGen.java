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
		String prefix = "";
		if(x.leaf1.label.equals("VAR")) {
			prefix+="VAR";
			ip1 = x.candidateVars.get(x.leaf1.index);
		} else {
			prefix+="NUM";
			QuantSpan qs = x.quantities.get(x.leaf1.index);
			ip1 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
					x.ta.getTokenIdFromCharacterOffset(qs.end));
		}
		if(x.leaf2.label.equals("VAR")) {
			prefix+="VAR";
			ip2 = x.candidateVars.get(x.leaf2.index);
		} else {
			prefix+="NUM";
			QuantSpan qs = x.quantities.get(x.leaf2.index);
			ip2 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
					x.ta.getTokenIdFromCharacterOffset(qs.end));
		}
		if(ip1.getFirst() > ip2.getFirst()) prefix += "REV";
		if(ip1.getFirst() == ip2.getFirst() && ip1.getSecond() > ip2.getSecond()) prefix += "REV";
		
		int min = Math.min(ip1.getSecond(), ip2.getSecond());
		int max = Math.max(ip1.getFirst(), ip2.getFirst());
		int left = Math.min(ip1.getFirst(), ip2.getFirst());
		int right = Math.max(ip1.getSecond(), ip2.getSecond());
		for(int i=min; i<max; ++i) {
			addFeature(prefix+"_MidUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
		}
		for(int i=min; i<max-1; ++i) {
			addFeature(prefix+"_MidBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase(), y, features);
			addFeature(prefix+"_MidLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.posTags.get(i+1).getLabel(), y, features);
			addFeature(prefix+"_MidPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
					x.ta.getToken(i+1).toLowerCase(), y, features);
		}
		for(int i=Math.max(0, left-2); i<left; ++i) {
			addFeature(prefix+"_LeftUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
			addFeature(prefix+"_LeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase(), y, features);
		}
		for(int i=min; i<Math.min(x.ta.size()-1, min+2); ++i) {
			addFeature(prefix+"_LeftRightUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
			addFeature(prefix+"_LeftRightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase(), y, features);
		}
		for(int i=right; i<Math.min(x.ta.size()-1, right+2); ++i) {
			addFeature(prefix+"_RightUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
			addFeature(prefix+"_RightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase(), y, features);
		}
		for(int i=Math.max(0, max-2); i<max; ++i) {
			addFeature(prefix+"_RightLeftUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
			addFeature(prefix+"_RightLeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase(), y, features);
		}
		if(prefix.contains("NUMNUM")) {
			if(x.leaf1.value > x.leaf2.value) {
				addFeature(prefix+"_Desc", y, features);
			} else {
				addFeature(prefix+"_Asc", y, features);
			}
		}
		return features;
	}

	public static void addFeature(String string, LcaY y, List<String> features) {
		features.add(string+"_"+y);
	}
}