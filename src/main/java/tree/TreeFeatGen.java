package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Node;
import utils.FeatGen;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class TreeFeatGen extends AbstractFeatureGenerator implements Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public TreeFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		TreeX x = (TreeX) arg0;
		TreeY y = (TreeY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public IFeatureVector getPairFeatureVector(TreeX x, Node node) {
		List<String> features = getPairFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(TreeX x, TreeY y) {
		List<String> features = new ArrayList<String>();
		for(Node node : y.equation.root.getAllSubNodes()) {
			if(node.children.size() == 2) {
				features.addAll(getPairFeatures(x, node));
			}
		}
		return features;
	}
	
	public static List<String> getPairFeatures(TreeX x, Node node) {
		List<String> features = new ArrayList<String>();
		if(node.children.size() == 0) {
			System.err.println("Pair Features called with a leaf : expected non-leaf");
		}
		if(node.children.get(0).getCharSpan().getFirst() == -1 ||
				node.children.get(1).getCharSpan().getFirst() == -1) {
			features.addAll(getNonProjectiveFeatures(node.children.get(0), 
					node.children.get(0), node.label, x));
		} else {
			features.addAll(getProjectiveFeatures(x, node));
		}
		return features;
	}
	
	public static List<String> getProjectiveFeatures(TreeX x, Node node) {
		IntPair ip1 = node.children.get(0).getNodeListSpan();
		IntPair ip2 = node.children.get(1).getNodeListSpan();
		String prePhrase = "";
		if(Math.min(ip1.getFirst(), ip2.getFirst())==0)  {
			prePhrase = x.ta.getText().toLowerCase().substring(
					0, x.nodes.get(Math.min(ip1.getFirst(), ip2.getFirst())).charIndex);
		} else {
			prePhrase = x.ta.getText().toLowerCase().substring(
					x.nodes.get(Math.min(ip1.getFirst(), ip2.getFirst())-1).charIndex, 
					x.nodes.get(Math.min(ip1.getFirst(), ip2.getFirst())).charIndex);
		}
		String midPhrase = x.ta.getText().toLowerCase().substring(
				x.nodes.get(Math.min(ip1.getSecond(), ip2.getSecond())).charIndex, 
				x.nodes.get(Math.max(ip1.getFirst(), ip2.getFirst())).charIndex);
		String leftToken = "";
		if(ip1.getFirst() <= ip2.getFirst() && node.children.get(0).equals("NUM")) {
			QuantSpan qs = x.quantities.get(node.children.get(0).index);
			leftToken = x.ta.getText().toLowerCase().substring(qs.start, qs.end);
		}
		if(ip2.getFirst() <= ip1.getFirst() && node.children.get(1).equals("NUM")) {
			QuantSpan qs = x.quantities.get(node.children.get(1).index);
			leftToken = x.ta.getText().toLowerCase().substring(qs.start, qs.end);
		}
		String label = node.label;
		if(ip1.getFirst() > ip2.getFirst() && node.label.endsWith("REV")) {
			label = node.label.substring(0,3);
		}
		List<String> features = new ArrayList<>();
		features.addAll(getRuleFeatures(prePhrase, midPhrase, leftToken, label));
		return features;
	}
	
	// Only for combining leaves 
	public static List<String> getNonProjectiveFeatures(Node leaf1, Node leaf2, String label, TreeX x) {
		if(leaf1.children.size() > 0 && leaf2.children.size() > 0) {
			System.err.println("Non Projective Features called with a non-leaf : expected leaf");
		}
		List<String> features = new ArrayList<>();
		IntPair ip1, ip2;
		String prefix = "";
		if(leaf1.label.equals("VAR")) {
			prefix+="VAR";
			ip1 = x.candidateVars.get(leaf1.index);
		} else {
			prefix+="NUM";
			QuantSpan qs = x.quantities.get(leaf1.index);
			ip1 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
					x.ta.getTokenIdFromCharacterOffset(qs.end));
		}
		if(leaf2.label.equals("VAR")) {
			prefix+="VAR";
			ip2 = x.candidateVars.get(leaf2.index);
		} else {
			prefix+="NUM";
			QuantSpan qs = x.quantities.get(leaf2.index);
			ip2 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
					x.ta.getTokenIdFromCharacterOffset(qs.end));
		}
		if(ip1.getFirst() > ip2.getFirst()) prefix += "REV";
		if(ip1.getFirst() == ip2.getFirst() && ip1.getSecond() > ip2.getSecond()) prefix += "REV";
		
		int min = x.ta.getTokenIdFromCharacterOffset(leaf1.charIndex)+1;
		int max = x.ta.getTokenIdFromCharacterOffset(leaf2.charIndex)-1;
		int left = x.ta.getTokenIdFromCharacterOffset(leaf1.charIndex)-1;
		int right = x.ta.getTokenIdFromCharacterOffset(leaf2.charIndex)+1;
		for(int i=min; i<max; ++i) {
			features.add(prefix+"_MidUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+label);
		}
		for(int i=min; i<max-1; ++i) {
			features.add(prefix+"_MidBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+label);
			features.add(prefix+"_MidLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.posTags.get(i+1).getLabel()+"_"+label);
			features.add(prefix+"_MidPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+label);
		}
		for(int i=Math.max(0, left-2); i<left; ++i) {
			features.add(prefix+"_LeftUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+label);
			features.add(prefix+"_LeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+label);
		}
		for(int i=min; i<Math.min(x.ta.size()-1, min+2); ++i) {
			features.add(prefix+"_LeftRightUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+label);
			features.add(prefix+"_LeftRightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+label);
		}
		for(int i=right; i<Math.min(x.ta.size()-1, right+2); ++i) {
			features.add(prefix+"_RightUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+label);
			features.add(prefix+"_RightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+label);
		}
		for(int i=Math.max(0, max-2); i<max; ++i) {
			features.add(prefix+"_RightLeftUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+label);
			features.add(prefix+"_RightLeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+label);
		}
		if(prefix.contains("NUMNUM")) {
			if(leaf1.value > leaf2.value) {
				features.add(prefix+"_Desc"+"_"+label);
			} else {
				features.add(prefix+"_Asc"+"_"+label);
			}
		}
		return features;
	}
	
	public static List<String> getRuleFeatures(String prePhrase, String midPhrase, 
			String leftToken, String operation) {
		List<String> features = new ArrayList<>();
		if((leftToken.contains("twice") || leftToken.contains("thrice") ||
				leftToken.contains("half")) && midPhrase.contains("as many")) {
			features.add(operation+"_DIVREV_RULE");
			return features;
		}
		if(leftToken.contains("thrice") || leftToken.contains("triple") ||
				leftToken.contains("double") || leftToken.contains("twice") ||
				leftToken.contains("half")) {
			features.add(operation+"_MUL_RULE");
			return features;
		}
		if(prePhrase.contains("sum of") || midPhrase.contains("added to") || 
				midPhrase.contains("plus") || midPhrase.contains("more than") || 
				midPhrase.contains("taller than") || midPhrase.contains("greater than")) {
			features.add(operation+"_ADD_RULE");
		}
		if(prePhrase.contains("difference of") || midPhrase.contains("exceeds") || 
				midPhrase.contains("minus")) {
			features.add(operation+"_SUB_RULE");
		}
		if(midPhrase.contains("subtracted") || midPhrase.contains("less than")) {
			features.add(operation+"_SUBREV_RULE");
		}
		if(prePhrase.contains("product of") || midPhrase.contains("times") || 
				midPhrase.contains("multiplied by") || midPhrase.contains("per")) {
			features.add(operation+"_MUL_RULE");
		}
		if(prePhrase.contains("ratio of")) {
			features.add(operation+"_DIV_RULE");
		}
		if(midPhrase.contains("as many")) {
			features.add(operation+"_DIVREV_RULE");
		}
		return features;
	}

	public static void addFeature(String string, TreeY y, List<String> features) {
		features.add(string+"_"+y);
	}
	
}