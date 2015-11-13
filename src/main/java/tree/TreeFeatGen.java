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
		List<String> features = getNodeFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(TreeX x, TreeY y) {
//		System.out.println("Text : "+x.ta.getText());
//		System.out.println("NodeList");
//		for(Node node : y.equation.root.getLeaves()) {
//			System.out.println(node+" "+node.getNodeListSpan()+" "+node.getCharSpan());
//		}
		List<String> features = new ArrayList<String>();
		for(Node node : y.equation.root.getAllSubNodes()) {
			if(node.children.size() == 2) {
				features.addAll(getNodeFeatures(x, node));
			}
		}
		return features;
	}
	
	public static List<String> getNodeFeatures(TreeX x, Node node) {
		List<String> features = new ArrayList<String>();
		if(node.children.size() == 0) {
			System.err.println("Pair Features called with a leaf : expected non-leaf");
		}
//		System.out.println("Pair features called with : "+node);
		Node node1 = node.children.get(0);
		Node node2 = node.children.get(1);
		IntPair ip1 = node1.getNodeListSpan();
		IntPair ip2 = node2.getNodeListSpan();
		if(node1.children.size()==0 && node2.children.size()==0 && 
				(!node1.projection || !node2.projection)) {
//			System.out.println("Chosen non-projective");
			features.addAll(getNonProjectiveFeatures(x, node));
		} else if(ip1.getFirst()!=-1 && ip2.getFirst()!=-1 && 
				(ip1.getSecond()+1==ip2.getFirst() || ip2.getSecond()+1==ip1.getFirst())) {
//			System.out.println("Chosen projective");
			features.addAll(getProjectiveFeatures(x, node));
		}
		return features;
	}
	
	public static List<String> getProjectiveFeatures(TreeX x, Node node) {
		List<String> features = new ArrayList<>();
		IntPair ip1 = node.children.get(0).getNodeListSpan();
		IntPair ip2 = node.children.get(1).getNodeListSpan();
		if(Math.min(ip1.getSecond(), ip2.getSecond()) > Math.max(ip1.getFirst(), ip2.getFirst())) {
			return features;
		}
//		System.out.println("NodeListSpan "+node.children.get(0)+" "+ip1);
//		System.out.println("NodeListSpan "+node.children.get(1)+" "+ip2);
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
		String op = node.label;
		if(ip1.getFirst() > ip2.getFirst() && (node.label.equals("SUB") || node.label.equals("DIV"))) {
			op += "_REV";
		}
//		System.out.println("PrePhrase : "+prePhrase+" MidPhrase : "+midPhrase+" LeftToken : "+leftToken);
		features.addAll(getRuleFeatures(prePhrase, midPhrase, leftToken, op));
		

		IntPair span1 = node.children.get(0).getCharSpan();
		IntPair span2 = node.children.get(1).getCharSpan();
		int min = x.ta.getTokenIdFromCharacterOffset(Math.min(span1.getFirst(), span2.getFirst()))+1;
		int max = x.ta.getTokenIdFromCharacterOffset(Math.max(span1.getSecond(), span2.getSecond()))-1;
		int left = x.ta.getTokenIdFromCharacterOffset(Math.min(span1.getSecond(), span2.getSecond()))-1;
		int right = x.ta.getTokenIdFromCharacterOffset(Math.max(span1.getFirst(), span2.getFirst()))+1;
		for(int i=min; i<=max; ++i) {
			features.add("MidUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
		}
		for(int i=min; i<=max-1; ++i) {
			features.add("MidBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+op);
			features.add("MidLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.posTags.get(i+1).getLabel()+"_"+op);
			features.add("MidPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+op);
		}
		return features;
	}
	
	// Only for combining leaves 
	public static List<String> getNonProjectiveFeatures(TreeX x, Node node) {
		Node leaf1 = node.children.get(0);
		Node leaf2 = node.children.get(1);
		if(leaf1.children.size() > 0 && leaf2.children.size() > 0) {
			System.err.println("Non Projective Features called with a non-leaf : expected leaf");
		}
		List<String> features = new ArrayList<>();
		String prefix = "";
		int min = x.ta.getTokenIdFromCharacterOffset(Math.min(leaf1.charIndex, leaf2.charIndex))+1;
		int max = x.ta.getTokenIdFromCharacterOffset(Math.max(leaf1.charIndex, leaf2.charIndex))-1;
		int left = x.ta.getTokenIdFromCharacterOffset(Math.min(leaf1.charIndex, leaf2.charIndex))-1;
		int right = x.ta.getTokenIdFromCharacterOffset(Math.max(leaf1.charIndex, leaf2.charIndex))+1;
		String op = node.label;
		if(leaf1.charIndex > leaf2.charIndex && (node.label.equals("SUB") || node.label.equals("DIV"))) {
			op += "_REV";
		}
//		for(int i=min; i<max; ++i) {
//			features.add(prefix+"_MidUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
//		}
//		for(int i=min; i<max-1; ++i) {
//			features.add(prefix+"_MidBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.ta.getToken(i+1).toLowerCase()+"_"+op);
//			features.add(prefix+"_MidLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.posTags.get(i+1).getLabel()+"_"+op);
//			features.add(prefix+"_MidPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
//					x.ta.getToken(i+1).toLowerCase()+"_"+op);
//		}
		for(int i=Math.max(0, left-2); i<left; ++i) {
			features.add(prefix+"_LeftUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
			features.add(prefix+"_LeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+op);
		}
		for(int i=min; i<Math.min(x.ta.size()-1, min+2); ++i) {
			features.add(prefix+"_LeftRightUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
			features.add(prefix+"_LeftRightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+op);
		}
		for(int i=right; i<Math.min(x.ta.size()-1, right+2); ++i) {
			features.add(prefix+"_RightUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
			features.add(prefix+"_RightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+op);
		}
		for(int i=Math.max(0, max-2); i<max; ++i) {
			features.add(prefix+"_RightLeftUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
			features.add(prefix+"_RightLeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+op);
		}
		if(prefix.contains("NUMNUM")) {
			if(leaf1.value > leaf2.value) {
				features.add(prefix+"_Desc"+"_"+op);
			} else {
				features.add(prefix+"_Asc"+"_"+op);
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
	
}