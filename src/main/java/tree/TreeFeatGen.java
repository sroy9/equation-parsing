package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lca.LcaX;
import lca.LcaY;
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
		if(node.children.size() == 2) {
			for(Node leaf1 : node.children.get(0).getLeaves()) {
				for(Node leaf2 : node.children.get(1).getLeaves()) {
					boolean allow = true;
					for(Node leaf : x.nodes) {
						if((leaf1.charIndex<leaf.charIndex && leaf.charIndex<leaf2.charIndex) || 
								(leaf2.charIndex<leaf.charIndex && leaf.charIndex<leaf1.charIndex)) {
							allow = false;
						}
					}
					if(!allow) continue;
					if(leaf1.charIndex <= leaf2.charIndex) {
						TreeX.LcaX lcaX = new TreeX.LcaX(x, leaf1, leaf2, prePhrase, midPhrase);
						TreeY.LcaY lcaY = new TreeY.LcaY(node.label);
						features.addAll(lca.TreeFeatGen.getFeatures(lcaX, lcaY));
					}
					if(leaf2.charIndex < leaf1.charIndex) {
						String label = node.label;
						if(label.equals("SUB") || label.equals("DIV")) label += "_REV";
						TreeX.LcaX lcaX = new TreeX.LcaX(x, leaf2, leaf1, prePhrase, midPhrase);
						TreeY.LcaY lcaY = new TreeY.LcaY(label);
						features.addAll(lca.TreeFeatGen.getFeatures(lcaX, lcaY));
					}
				}
			}
		}
		return features;
	}
	
	public static List<String> getLexicalFeatures(TreeX x, TreeY y) {
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
		
		int min = x.ta.getTokenIdFromCharacterOffset(x.leaf1.charIndex)+1;
		int max = x.ta.getTokenIdFromCharacterOffset(x.leaf2.charIndex)-1;
		int left = x.ta.getTokenIdFromCharacterOffset(x.leaf1.charIndex)-1;
		int right = x.ta.getTokenIdFromCharacterOffset(x.leaf2.charIndex)+1;
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
	
	public static List<String> getRuleFeatures(TreeX x, TreeY y) {
		List<String> features = new ArrayList<>();
		String prePhrase = x.prePhrase;
		String midPhrase = x.midPhrase;
		String leftToken = "";
		if(x.leaf1.label.equals("NUM")) {
			QuantSpan qs = x.quantities.get(x.leaf1.index);
			leftToken = x.ta.getText().toLowerCase().substring(qs.start, qs.end);
		}
		if((leftToken.contains("twice") || leftToken.contains("thrice") ||
				leftToken.contains("half")) && midPhrase.contains("as many")) {
			features.add(y.operation+"_DIVREV_RULE");
			return features;
		}
		if(leftToken.contains("thrice") || leftToken.contains("triple") ||
				leftToken.contains("double") || leftToken.contains("twice") ||
				leftToken.contains("half")) {
			features.add(y.operation+"_MUL_RULE");
			return features;
		}
		if(prePhrase.contains("sum of") || midPhrase.contains("added to") || 
				midPhrase.contains("plus") || midPhrase.contains("more than") || 
				midPhrase.contains("taller than") || midPhrase.contains("greater than")) {
			features.add(y.operation+"_ADD_RULE");
		}
		if(prePhrase.contains("difference of") || midPhrase.contains("exceeds") || 
				midPhrase.contains("minus")) {
			features.add(y.operation+"_SUB_RULE");
		}
		if(midPhrase.contains("subtracted") || midPhrase.contains("less than")) {
			features.add(y.operation+"_SUBREV_RULE");
		}
		if(prePhrase.contains("product of") || midPhrase.contains("times") || 
				midPhrase.contains("multiplied by") || midPhrase.contains("per")) {
			features.add(y.operation+"_MUL_RULE");
		}
		if(prePhrase.contains("ratio of")) {
			features.add(y.operation+"_DIV_RULE");
		}
		if(midPhrase.contains("as many")) {
			features.add(y.operation+"_DIVREV_RULE");
		}
		return features;
	}

	public static void addFeature(String string, TreeY y, List<String> features) {
		features.add(string+"_"+y);
	}
	
}