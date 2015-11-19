package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Node;
import utils.FeatGen;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
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

	public IFeatureVector getNodeFeatureVector(TreeX x, Node node) {
		List<String> features = getNodeFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(TreeX x, TreeY y) {
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
			features.addAll(getNonProjectiveFeatures(x, node));
//			features.add("NON_PROJECTIVE");
		} else if(ip1.getFirst()!=-1 && ip2.getFirst()!=-1 && 
				(((ip1.getSecond()+1)==ip2.getFirst()) || ((ip2.getSecond()+1)==ip1.getFirst()))) {
//			features.add("PROJECTIVE");
			features.addAll(getNonProjectiveFeatures(x, node));
		} else {
			features.add("NOT_ALLOWED_STUFF");
		}
//		node.feats = new ArrayList<String>();
//		node.feats.addAll(features);
		return features;
	}
	
	public static List<String> getProjectiveFeatures(TreeX x, Node node) {
		List<String> features = new ArrayList<>();
		Node leaf1 = node.children.get(0);
		Node leaf2 = node.children.get(1);
		IntPair ip1 = leaf1.getNodeListSpan();
		IntPair ip2 = leaf2.getNodeListSpan();
		String ruleOp = getRuleOperation(node.children.get(0), node.children.get(1), x.ta, x.quantities, x.nodes);
		if(ruleOp != null) {
			features.add("RULE_TRIGGERED");
			if(node.children.get(0).children.size() == 0 && node.children.get(1).children.size() == 0) {
				features.add("LEAVES_MERGED_"+ruleOp);
			}
		} else {
			String op = node.label;
			if(ip1.getFirst() > ip2.getFirst() && (node.label.equals("SUB") || node.label.equals("DIV"))) {
				op += "_REV";
			}
			IntPair span1 = leaf1.getCharSpan();
			IntPair span2 = leaf2.getCharSpan();
			int min = x.ta.getTokenIdFromCharacterOffset(Math.min(span1.getFirst(), span2.getFirst()))+1;
			int max = x.ta.getTokenIdFromCharacterOffset(Math.max(span1.getSecond(), span2.getSecond()))-1;
			int left = x.ta.getTokenIdFromCharacterOffset(Math.min(leaf1.charIndex, leaf2.charIndex))-1;
			int right = x.ta.getTokenIdFromCharacterOffset(Math.max(leaf1.charIndex, leaf2.charIndex))+1;
			if(Math.abs(min-max)<=1) {
				features.add("MidUnigram_NonExistent_"+op);
			}
			for(int i=min; i<=max; ++i) {
				features.add("MidUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
			}
			for(int i=min; i<=max-1; ++i) {
				features.add("MidLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.posTags.get(i+1).getLabel()+"_"+op);
				features.add("MidPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
						x.ta.getToken(i+1).toLowerCase()+"_"+op);
			}
			String prefix = "";
			for(int i=Math.max(0, left-2); i<left; ++i) {
				features.add(prefix+"_LeftUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
				features.add(prefix+"_LeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.posTags.get(i+1).getLabel()+"_"+op);
				features.add(prefix+"_LeftBigram_"+x.posTags.get(i).getLabel()+"_"+
						x.ta.getToken(i+1).toLowerCase()+"_"+op);
			}
			for(int i=min; i<Math.min(x.ta.size()-1, min+2); ++i) {
				features.add(prefix+"_LeftRightUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
				features.add(prefix+"_LeftRightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.posTags.get(i+1).getLabel()+"_"+op);
				features.add(prefix+"_LeftRightBigram_"+x.posTags.get(i).getLabel()+"_"+
						x.ta.getToken(i+1).toLowerCase()+"_"+op);
			}
			for(int i=right; i<Math.min(x.ta.size()-1, right+2); ++i) {
				features.add(prefix+"_RightUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
				features.add(prefix+"_RightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.posTags.get(i+1).getLabel()+"_"+op);
				features.add(prefix+"_RightBigram_"+x.posTags.get(i).getLabel()+"_"+
						x.ta.getToken(i+1).toLowerCase()+"_"+op);
			}
			for(int i=Math.max(0, max-2); i<=max; ++i) {
				features.add(prefix+"_RightLeftUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
				features.add(prefix+"_RightLeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.posTags.get(i+1).getLabel()+"_"+op);
				features.add(prefix+"_RightLeftBigram_"+x.posTags.get(i).getLabel()+"_"+
						x.ta.getToken(i+1).toLowerCase()+"_"+op);
			}
			if(leaf1.label.equals("NUM") && leaf2.label.equals("NUM")) {
				if(leaf1.value > leaf2.value) {
					features.add(prefix+"_Desc"+"_"+op);
				} else {
					features.add(prefix+"_Asc"+"_"+op);
				}
			}
		}
		return features;
	}
	
	// Only for combining leaves 
	public static List<String> getNonProjectiveFeatures(TreeX x, Node node) {
		Node leaf1 = node.children.get(0);
		Node leaf2 = node.children.get(1);
		List<String> features = new ArrayList<>();
		String prefix = "";
		if(leaf1.label.equals("VAR")) {
			prefix+="VAR";
		} else {
			prefix+="NUM";
		}
		if(leaf2.label.equals("VAR")) {
			prefix+="VAR";
		} else {
			prefix+="NUM";
		}
		if(prefix.equals("NUMVAR")) prefix = "VARNUM";
		int min = x.ta.getTokenIdFromCharacterOffset(Math.min(leaf1.charIndex, leaf2.charIndex))+1;
		int max = x.ta.getTokenIdFromCharacterOffset(Math.max(leaf1.charIndex, leaf2.charIndex))-1;
		int left = x.ta.getTokenIdFromCharacterOffset(Math.min(leaf1.charIndex, leaf2.charIndex))-1;
		int right = x.ta.getTokenIdFromCharacterOffset(Math.max(leaf1.charIndex, leaf2.charIndex))+1;
		String op = node.label;
		if(leaf1.charIndex > leaf2.charIndex && (node.label.equals("SUB") || node.label.equals("DIV"))) {
			op += "_REV";
		}
		for(int i=Math.max(0, left-2); i<left; ++i) {
			features.add(prefix+"_LeftUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
			features.add(prefix+"_LeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.posTags.get(i+1).getLabel()+"_"+op);
			features.add(prefix+"_LeftBigram_"+x.posTags.get(i).getLabel()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+op);
		}
		for(int i=min; i<Math.min(x.ta.size()-1, min+2); ++i) {
			features.add(prefix+"_LeftRightUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
			features.add(prefix+"_LeftRightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
			x.posTags.get(i+1).getLabel()+"_"+op);
			features.add(prefix+"_LeftRightBigram_"+x.posTags.get(i).getLabel()+"_"+
			x.ta.getToken(i+1).toLowerCase()+"_"+op);
		}
		for(int i=right; i<Math.min(x.ta.size()-1, right+2); ++i) {
			features.add(prefix+"_RightUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
			features.add(prefix+"_RightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
					x.posTags.get(i+1).getLabel()+"_"+op);
			features.add(prefix+"_RightBigram_"+x.posTags.get(i).getLabel()+"_"+
					x.ta.getToken(i+1).toLowerCase()+"_"+op);
		}
		for(int i=Math.max(0, max-2); i<=max; ++i) {
			features.add(prefix+"_RightLeftUnigram_"+x.ta.getToken(i).toLowerCase()+"_"+op);
			features.add(prefix+"_RightLeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
			x.posTags.get(i+1).getLabel()+"_"+op);
			features.add(prefix+"_RightLeftBigram_"+x.posTags.get(i).getLabel()+"_"+
			x.ta.getToken(i+1).toLowerCase()+"_"+op);
		}
		return features;
	}
	
	public static String getRuleOperation(Node node1, Node node2, 
			TextAnnotation ta, List<QuantSpan> quantities, List<Node> leaves) {
		// Extracting the right information from text
		IntPair ip1 = node1.getNodeListSpan();
		IntPair ip2 = node2.getNodeListSpan();
		if(!(ip1.getFirst()!=-1 && ip2.getFirst()!=-1 && (((ip1.getSecond()+1)==ip2.getFirst()) || 
				((ip2.getSecond()+1)==ip1.getFirst())))) {
			return null;
		}
		String prePhrase = "";
		if(Math.min(ip1.getFirst(), ip2.getFirst())==0)  {
			prePhrase = ta.getText().toLowerCase().substring(
					0, leaves.get(Math.min(ip1.getFirst(), ip2.getFirst())).charIndex);
		} else {
			prePhrase = ta.getText().toLowerCase().substring(
					leaves.get(Math.min(ip1.getFirst(), ip2.getFirst())-1).charIndex, 
					leaves.get(Math.min(ip1.getFirst(), ip2.getFirst())).charIndex);
		}
		String midPhrase = ta.getText().toLowerCase().substring(
				leaves.get(Math.min(ip1.getSecond(), ip2.getSecond())).charIndex, 
				leaves.get(Math.max(ip1.getFirst(), ip2.getFirst())).charIndex);
		String postPhrase = "";
		if(Math.max(ip1.getSecond(), ip2.getSecond())==(leaves.size()-1))  {
			postPhrase = ta.getText().toLowerCase().substring(
					leaves.get(Math.max(ip1.getSecond(), ip2.getSecond())).charIndex);
		} else {
			postPhrase = ta.getText().toLowerCase().substring(
					leaves.get(Math.max(ip1.getSecond(), ip2.getSecond())).charIndex, 
					leaves.get(Math.max(ip1.getSecond(), ip2.getSecond())+1).charIndex);
		}
		String leftToken = "";
		if(ip1.getFirst() <= ip2.getFirst() && node1.label.equals("NUM")) {
			QuantSpan qs = quantities.get(node1.index);
			leftToken = ta.getText().toLowerCase().substring(qs.start, qs.end);
		}
		if(ip2.getFirst() <= ip1.getFirst() && node2.label.equals("NUM")) {
			QuantSpan qs = quantities.get(node2.index);
			leftToken = ta.getText().toLowerCase().substring(qs.start, qs.end);
		}
		String rightToken = "";
		if(ip1.getFirst() <= ip2.getFirst() && node2.label.equals("NUM")) {
			QuantSpan qs = quantities.get(node2.index);
			rightToken = ta.getText().toLowerCase().substring(qs.start, qs.end);
		}
		if(ip2.getFirst() <= ip1.getFirst() && node1.label.equals("NUM")) {
			QuantSpan qs = quantities.get(node1.index);
			rightToken = ta.getText().toLowerCase().substring(qs.start, qs.end);
		}
		String op = getRuleOperation(prePhrase, leftToken, midPhrase, rightToken, postPhrase);
		if(op!=null && ip1.getFirst() > ip2.getFirst() && 
				(op.equals("SUB") || op.equals("DIV"))) {
			op += "_REV";
		} else if(op!=null && ip1.getFirst() > ip2.getFirst() && 
				(op.equals("SUB_REV") || op.equals("DIV_REV"))) {
			op = op.substring(0,3);
		}
		return op;
		
	}
	
	public static String getRuleOperation(String prePhrase, String leftToken,
			String midPhrase, String rightToken, String postPhrase) {
		// Applying rules, priority last to first
		if(rightToken.contains("thrice") || rightToken.contains("triple") || 
				rightToken.contains("twice") || rightToken.contains("double") || 
				rightToken.contains("half") || postPhrase.contains("times") || 
				prePhrase.contains("times")) {
			return null;
		}
		String op = null;
		if(prePhrase.contains("sum of") && (midPhrase.contains("and") || midPhrase.equals(""))) {
			op = "ADD";
		}
		if(midPhrase.contains("added to") || 
				midPhrase.contains("plus") || midPhrase.contains("more than") || 
				midPhrase.contains("taller than") || midPhrase.contains("greater than") ||
				midPhrase.contains("larger than") || midPhrase.contains("faster than") ||
				midPhrase.contains("longer than") || midPhrase.contains("increased")) {
			op = "ADD";
		}
		if((midPhrase.contains("more than") || midPhrase.contains("taller than") || 
				midPhrase.contains("greater than") || midPhrase.contains("larger than") || 
				midPhrase.contains("faster than") || midPhrase.contains("longer than")) &&
				postPhrase.contains(" by ")) {
			op = "SUB";
		}
		if(prePhrase.contains("difference") && (midPhrase.contains(" and ") || midPhrase.equals(""))) {
			op = "SUB";
		}
		if(midPhrase.contains("exceeds") || midPhrase.contains("minus") || midPhrase.contains("decreased")) {
			op = "SUB";
		}
		if(midPhrase.contains("subtracted") || midPhrase.contains("less than") || 
				midPhrase.contains("smaller than") || midPhrase.contains("slower than") ||
				midPhrase.contains("shorter than")) {
			op = "SUB_REV";
		}
		if(midPhrase.contains("multiplied by")) {
			op = "MUL";
		}
		if(prePhrase.contains("product of") && midPhrase.contains(" and ")) {
			op = "MUL";
		}
		if(prePhrase.contains("ratio of")) {
			op = "DIV";
		}
		if((leftToken.contains("thrice") || leftToken.contains("triple") ||
				leftToken.contains("double") || leftToken.contains("twice") ||
				leftToken.contains("half") || midPhrase.contains("times"))) {
			op = "MUL";
		}
		if((leftToken.contains("thrice") || leftToken.contains("triple") ||
				leftToken.contains("double") || leftToken.contains("twice") ||
				leftToken.contains("half") || midPhrase.contains("times")) && 
				postPhrase.contains(" as ") && midPhrase.contains(" as ")) {
			op = "DIV_REV";
		}
		return op;
	}
	
}