package template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Node;
import utils.FeatGen;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class TemplateFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public TemplateFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		TemplateX x = (TemplateX) arg0;
		TemplateY y = (TemplateY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	
	public IFeatureVector getAlignmentFeatureVector(
			TemplateX x, TemplateY y, List<Node> leaves, int slotNo) {
		List<String> features = alignmentFeatures(x, y, leaves, slotNo);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	public static List<String> getFeatures(TemplateX x, TemplateY y) {
		List<String> features = new ArrayList<>();
		List<Node> leaves = y.equation.root.getLeaves();
		for(int i=0; i<leaves.size(); ++i) {
			features.addAll(alignmentFeatures(x, y, leaves, i));
		}
		return features;
	}
	
	public static List<String> alignmentFeatures(
			TemplateX x, TemplateY y, List<Node> leaves, int slotNo) {
		List<String> features = new ArrayList<>();
		features.addAll(singleSlotFeatures(x, y, leaves, slotNo));
		for(int i=0; i<slotNo; ++i) {
			features.addAll(slotPairFeatures(x, y, leaves, i, slotNo));
		}
		return features;
	}
	
	public static List<String> singleSlotFeatures(
			TemplateX x, TemplateY y, List<Node> leaves, int slotNo) {
		List<String> features = new ArrayList<>();
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
		String prefix = getSlotSignature(y, leaves, slotNo);
		int tokenIndex = leaves.get(slotNo).tokenIndex;
		features.add(prefix+"_Unigram_"+unigrams.get(tokenIndex));
		return features;
	}
	
	public static List<String> slotPairFeatures(
			TemplateX x, TemplateY y, List<Node> leaves, int slot1, int slot2) {
		List<String> features = new ArrayList<>();
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
		String prefix1 = getSlotSignature(y, leaves, slot1);
		String prefix2 = getSlotSignature(y, leaves, slot2);
		int tokenIndex1 = leaves.get(slot1).tokenIndex;
		int tokenIndex2 = leaves.get(slot2).tokenIndex;
		String prefix = prefix1+"_"+prefix2;
		for(int i=Math.min(tokenIndex1, tokenIndex2)+1;
				i<Math.max(tokenIndex1, tokenIndex2); ++i) {
			features.add(prefix+"_MidUnigram_"+unigrams.get(i));
			features.add(prefix+"_MidBigram_"+unigrams.get(i)+"_"+unigrams.get(i+1));
		}
		return features;
	}
	
	public static String getSlotSignature(TemplateY y, List<Node> leaves, int slotNo) {
		String signature = "";
		if(leaves.get(slotNo).label.equals("VAR")) signature += "VAR";
		if(leaves.get(slotNo).label.equals("NUM")) signature += "NUM";
//		for(Node node : y.equation.root.getAllSubNodes()) {
//			if(node.children.size() == 2 && (node.children.get(0).equals(leaves.get(slotNo)) || 
//					node.children.get(1).equals(leaves.get(slotNo)))) {
//				if(node.label.equals("ADD") || node.equals("SUB") || node.label.equals("EQ")) {
//					signature += "IND";
//				} else {
//					signature += "DEP";
//				}
//				break;
//			}
//		}
		return signature;
	}

	public static Node getParentOfLeaf(Node root, Node someChild) {
		for(Node node : root.getAllSubNodes()) {
			if(node.children.size() == 2 && (node.children.get(0).label.equals(someChild.label) && 
					node.children.get(0).tokenIndex == someChild.tokenIndex)) {
				return node;
			}
			if(node.children.size() == 2 && (node.children.get(1).label.equals(someChild.label) && 
					node.children.get(1).tokenIndex == someChild.tokenIndex)) {
				return node;
			}
		}
		return null;
	}
	
	public static Node getCommonParent(Node root, Node child1, Node child2) {
		Node commonParent = null;
		for(Node node : root.getAllSubNodes()) {
			List<Node> leaves1 = node.getLeaves();
			List<Node> leaves2 = node.children.get(0).getLeaves();
			List<Node> leaves3 = node.children.get(1).getLeaves();
			
			
		}
		return null;
	}
}