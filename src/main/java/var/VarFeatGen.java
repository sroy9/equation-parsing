package var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tree.TreeFeatGen;
import utils.FeatGen;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
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
		List<IntPair> candidates = new ArrayList<IntPair>();
		String prefix = "";
		for(String key : y.varTokens.keySet()) {
			assert y.varTokens.get(key).size() < 2;
			if(y.varTokens.get(key).size() == 0) continue;
			candidates.add(x.candidateVars.get(y.varTokens.get(key).get(0)));
		}
		if(candidates.size() == 2) {
			prefix+="TwoVariables";
			if(candidates.get(0) == candidates.get(1)) {
				prefix+="SameSpan";
			}
		}
		for(IntPair candidate : candidates) {
			for(int i=candidate.getFirst(); i<candidate.getSecond(); ++i) {
				features.add(prefix+"_VarUnigram_"+x.ta.getToken(i).toLowerCase());
				features.add(prefix+"_VarPOSUnigram_"+x.posTags.get(i).getLabel());
			}
			for(int i=candidate.getFirst(); i<candidate.getSecond()-1; ++i) {
				features.add(prefix+"_VarBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.ta.getToken(i+1).toLowerCase());
				features.add(prefix+"_VarLexPOSBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.posTags.get(i+1).getLabel());
				features.add(prefix+"_VarPOSLexBigram_"+x.posTags.get(i).getLabel()+"_"+
						x.ta.getToken(i+1).toLowerCase());
			}
			if(candidate.getFirst() == 0) {
				features.add(prefix+"_StartOfSentence");
			}
//			int left = candidate.getFirst();
//			int right = candidate.getSecond();
//			for(int i=Math.max(0, left-2); i<left; ++i) {
//				features.add(prefix+"_LeftUnigram_"+x.ta.getToken(i).toLowerCase());
//				features.add(prefix+"_LeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//						x.posTags.get(i+1).getLabel());
//				features.add(prefix+"_LeftBigram_"+x.posTags.get(i).getLabel()+"_"+
//						x.ta.getToken(i+1).toLowerCase());
//			}
//			for(int i=right; i<Math.min(x.ta.size()-1, right+2); ++i) {
//				features.add(prefix+"_RightUnigram_"+x.ta.getToken(i).toLowerCase());
//				features.add(prefix+"_RightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//						x.posTags.get(i+1).getLabel());
//				features.add(prefix+"_RightBigram_"+x.posTags.get(i).getLabel()+"_"+
//						x.ta.getToken(i+1).toLowerCase());
//			}
		}
		// Global features
//		for(int i=0; i<x.ta.size()-1; ++i) {
//			features.add(prefix+"_"+x.ta.getToken(i).toLowerCase()+"_"+x.posTags.get(i+1).getLabel());
//			features.add(prefix+"_"+x.posTags.get(i).getLabel()+"_"+x.ta.getToken(i+1).toLowerCase());
//		}
//		if(candidates.size() == 2) {
//			String secondPhrase = getString(x.ta, candidates.get(1));
//			if(secondPhrase.contains("the number") || secondPhrase.contains("same")) {
//				features.add(prefix+"_COREF");
//			}
//		}
		if(candidates.size() == 2) {
			int left = Math.min(candidates.get(0).getSecond(), candidates.get(1).getSecond());
			int right = Math.max(candidates.get(0).getFirst(), candidates.get(1).getFirst());
			for(int i=left; i<right; ++i) {
				features.add(prefix+"_MidUnigram_"+x.ta.getToken(i).toLowerCase());
				features.add(prefix+"_MidPOS_"+x.posTags.get(i).getLabel());
			}
			for(int i=left; i<right; ++i) {
				features.add(prefix+"_MidBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.ta.getToken(i+1).toLowerCase());
				features.add(prefix+"_MidLexPOSBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.posTags.get(i+1).getLabel());
				features.add(prefix+"_MidPOSLexBigram_"+x.posTags.get(i).getLabel()+"_"+
						x.ta.getToken(i+1).toLowerCase());
			}
		}
		
		return features;
	}
	
	public static List<String> getRuleFeatures(VarX x, VarY y) {
		List<String> feats = new ArrayList<>();
		List<Integer> charIndices = new ArrayList<>();
		List<Integer> varIndices = new ArrayList<>();
		for(String key : y.varTokens.keySet()) {
			IntPair span = x.candidateVars.get(y.varTokens.get(key).get(0));
			varIndices.add((x.ta.getTokenCharacterOffset(span.getFirst()).getFirst()+
					x.ta.getTokenCharacterOffset(span.getSecond()-1).getSecond())/2);
		}
		charIndices.addAll(varIndices);
		for(QuantSpan qs : x.quantities) {
			charIndices.add((qs.start+qs.end)/2);
		}
		Collections.sort(charIndices);
		String str = x.ta.getText().toLowerCase();
		for(int i=0; i<charIndices.size(); ++i) {
			if(varIndices.contains(charIndices.get(i)) && i<(charIndices.size()-1)) {
				String op = TreeFeatGen.getRuleOperation(getPrePhrase(str, charIndices, i, i+1), 
						getLeftToken(x.ta, charIndices, i, i+1), 
						getMidToken(str, charIndices, i, i+1), 
						getRightToken(x.ta, charIndices, i, i+1), 
						getPostPhrase(str, charIndices, i, i+1));
				if(op!=null) feats.add("RuleOpFound");
			}
			if(varIndices.contains(charIndices.get(i)) && i>0) {
				String op = TreeFeatGen.getRuleOperation(getPrePhrase(str, charIndices, i-1, i), 
						getLeftToken(x.ta, charIndices, i-1, i), 
						getMidToken(str, charIndices, i-1, i), 
						getRightToken(x.ta, charIndices, i-1, i), 
						getPostPhrase(str, charIndices, i-1, i));
				if(op!=null) feats.add("RuleOpFound");
			}
		}
		return feats;
	}
	
	public static String getPrePhrase(String str, List<Integer> charIndices, int i, int j) {
		if(i==0) return str.substring(0, charIndices.get(i));
		return str.substring(charIndices.get(i-1), charIndices.get(i));
	}
	
	public static String getLeftToken(TextAnnotation ta, List<Integer> charIndices, int i, int j) {
		return ta.getToken(ta.getTokenIdFromCharacterOffset(charIndices.get(i))).toLowerCase();
	}
	
	public static String getRightToken(TextAnnotation ta, List<Integer> charIndices, int i, int j) {
		return ta.getToken(ta.getTokenIdFromCharacterOffset(charIndices.get(j))).toLowerCase();
	}
	
	public static String getMidToken(String str, List<Integer> charIndices, int i, int j) {
		return str.substring(charIndices.get(i), charIndices.get(j));
	}
	
	public static String getPostPhrase(String str, List<Integer> charIndices, int i, int j) {
		if(j==(charIndices.size()-1)) return str.substring(charIndices.get(j));
		return str.substring(charIndices.get(j), charIndices.get(j+1));
	}
	
	public static String getString(TextAnnotation ta, IntPair ip) {
		String str = "";
		for(int i=ip.getFirst(); i<ip.getSecond(); ++i) {
			str += ta.getToken(i).toLowerCase()+" ";
		}
		return str.trim();
	}
}