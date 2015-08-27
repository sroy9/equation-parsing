package template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.Node;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
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
	
	public IFeatureVector getGlobalFeatureVector(TemplateX x, TemplateY y) {
		List<String> features = globalFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getVarTokenFeatureVector(TemplateX x, TemplateY y) {
		List<String> features = varTokenFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public IFeatureVector getNumFeatureVector(TemplateX x, TemplateY y) {
		List<String> features = numFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	public static List<String> getFeatures(TemplateX x, TemplateY y) {
//		System.out.println("Features needed for ");
//		System.out.println("X : "+x.problemIndex + " Quants : "+Arrays.asList(x.quantities));
//		System.out.println("Y : "+y);
		List<String> features = new ArrayList<>();
		features.addAll(globalFeatures(x, y));
		features.addAll(varTokenFeatures(x, y));
		features.addAll(numFeatures(x, y));
		return features;
	}
	
	public static List<String> globalFeatures(TemplateX x, TemplateY y) {
		List<String> features = new ArrayList<>();
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
		features.add("TokenIndex_"+y.templateId);
		for(int i=0; i<unigrams.size(); ++i) {
			features.add("TokenIndex_Unigram_"+y.templateId+"_"+unigrams.get(i));
			if(i>0) features.add("TokenIndex_Bigram_"+y.templateId+"_"+
							unigrams.get(i-1)+" "+unigrams.get(i));
		}
		return features;
		
	}
	
	public static List<String> varTokenFeatures(TemplateX x, TemplateY y) {
		List<String> features = new ArrayList<>();
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
//		for(String key : y.varTokens.keySet()) {
//			int index = y.varTokens.get(key).get(0);
//			features.add("VarToken_"+unigrams.get(index));
//			if(index-1>0) {
//				features.add("VarToken_-1_"+unigrams.get(index-1)); 
//				features.add("VarToken_"+unigrams.get(index-1)+"_"+unigrams.get(index));
//			}
//			if(index+1<x.ta.size()) {
//				features.add("VarToken_+1_"+unigrams.get(index+1)); 
//				features.add("VarToken_"+unigrams.get(index)+"_"+unigrams.get(index+1));
//			}
//		}
		features.addAll(varTokenFeaturesCopy(x, y));
		return features;
		
	}
	
	public static List<String> numFeatures(TemplateX x, TemplateY y) {
		List<String> features = new ArrayList<>();
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
		for(Node leaf : y.equation.root.getLeaves()) {
			if(leaf.label.equals("VAR")) continue;
			QuantSpan qs = null;
			for(int i=0; i<x.quantities.size(); ++i) {
//				System.out.println("Matching "+Tools.getValue(
//						x.quantities.get(i))+"_" +leaf.value);
				if(Tools.safeEquals(Tools.getValue(
						x.quantities.get(i)), leaf.value)) {
					qs = x.quantities.get(i);
					break;
				}
			}
			String prefix = getSlotSignature(y, leaf);
			if(qs == null) System.out.println("PROBLEM in numFeatures");
			// Features here
			if(Tools.safeEquals(Tools.getValue(qs), 1.0)) {
				features.add(prefix+"_NUM_1.0");
			}
			if(Tools.safeEquals(Tools.getValue(qs), 2.0)) {
				features.add(prefix+"_NUM_2.0");
			}
			if(Tools.getUnit(qs).contains("percent")) {
				features.add(prefix+"_Unit_percent");
			}
			if(Tools.getUnit(qs).contains("US$")) {
				features.add(prefix+"_Unit_US$");
			}
////			 Some tokens to the left
//			for(int i=leaf.tokenIndex-1; i>Math.max(0, leaf.tokenIndex-5); --i) {
//				features.add(prefix+"_TokenLeft_"+unigrams.get(i));
//				features.add(prefix+"_TokenLeft_"+unigrams.get(i)+"_"+unigrams.get(i+1));
//			}
////			 Some tokens to the right
//			for(int i=leaf.tokenIndex+1; i<Math.min(x.ta.size()-1, leaf.tokenIndex+3); ++i) {
//				features.add(prefix+"_TokenRight_"+unigrams.get(i));
//				features.add(prefix+"_TokenRight_"+unigrams.get(i)+"_"+unigrams.get(i+1));
//			}
			
//			Node parent = getParentOfLeaf(y.equation.root, leaf);
//			features.addAll(expressionFeaturesCopy(x, parent));
		}
		
		for(Node node : y.equation.root.getAllSubNodes()) {
			if(node.children.size() == 0) continue;
			features.addAll(expressionFeaturesCopy(x, node));
		}
		
		
		return features;
	}
	
	public static List<String> expressionFeaturesCopy(TemplateX x, Node node) {
		List<String> features = new ArrayList<>();
		String prefix = node.label;
		IntPair span = node.getSpanningTokenIndices();
		IntPair spanChild1 = node.children.get(0).getSpanningTokenIndices();
		IntPair spanChild2 = node.children.get(1).getSpanningTokenIndices();
		int leftStart = Math.min(spanChild1.getFirst(), spanChild2.getFirst());
		int rightEnd = Math.max(spanChild1.getSecond(), spanChild2.getSecond());
		int midStart = Math.min(spanChild1.getSecond(), spanChild2.getSecond());
		int midEnd = Math.max(spanChild1.getFirst(), spanChild2.getFirst());
		
		if(node.label.equals("SUB") || node.label.equals("DIV")) {
			if(spanChild1.getFirst() > spanChild2.getFirst()) {
				prefix += "_DESC";
			} else {
				prefix += "_ASC";
			}
		}
		
		// Mid token features
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
		for(int i=midStart+1; i<midEnd; ++i) {
			features.add(prefix+"_MidUnigram_"+unigrams.get(i));
			if(i+1<Math.max(spanChild1.getFirst(), spanChild2.getFirst())) {
				features.add(prefix+"_MidBigram_"+unigrams.get(i)+"_"+unigrams.get(i+1));
			}
		}
		
		// Some tokens to the left
		for(int i=leftStart-1; i>Math.max(0, leftStart-5); --i) {
			features.add(prefix+"_TokenLeft_"+unigrams.get(i));
			features.add(prefix+"_TokenLeft_"+unigrams.get(i)+"_"+unigrams.get(i+1));
		}
		
		// Some tokens to the right
		for(int i=rightEnd+1; i<Math.min(x.ta.size()-1, rightEnd+3); ++i) {
			features.add(prefix+"_TokenRight_"+unigrams.get(i));
			features.add(prefix+"_TokenRight_"+unigrams.get(i)+"_"+unigrams.get(i+1));
		}
		
		// Children features, whether they are number or variable, is a percentage or US$
		if(node.children.get(0).label.equals("VAR") || 
				node.children.get(1).label.equals("VAR")) {
			features.add(prefix+"_OneChildIsVar");
			if(node.children.get(0).label.equals("VAR")) {
				features.add(prefix+"_OneChildIsVar_"+unigrams.get(span.getFirst()));
			} else {
				features.add(prefix+"_OneChildIsVar_"+unigrams.get(span.getSecond()));
			}
		}
		if(node.children.get(0).label.equals("NUM") || 
				node.children.get(1).label.equals("NUM")) {
			features.add(prefix+"_OneChildIsNum");
			Node numNode;
			if(node.children.get(0).label.equals("NUM")) {
				features.add(prefix+"_OneChildIsVar_"+unigrams.get(span.getFirst()));
				numNode = node.children.get(0);
			} else {
				features.add(prefix+"_OneChildIsVar_"+unigrams.get(span.getSecond()));
				numNode = node.children.get(1);
			}
			for(int i=0; i<x.quantities.size(); ++i) {
				if(Tools.safeEquals(Tools.getValue(x.quantities.get(i)), numNode.value)) {
					if(Tools.getUnit(x.quantities.get(i)).equals("percent")) {
						features.add(prefix+"_Child_Percentage");
					}
					if(Tools.getUnit(x.quantities.get(i)).equals("US$")) {
						features.add(prefix+"_Child_US$");
					}
					break;
				}
			}
		}
		
		// Subtraction : which one is greater than the other
		if(node.label.equals("SUB") && node.children.get(0).label.equals("NUM")
				&& node.children.get(1).label.equals("NUM")) {
			if(node.children.get(0).value > node.children.get(1).value) {
				features.add(prefix+"_SubtractingGreaterFromSmaller");
			} else {
				features.add(prefix+"_SubtractingSmallerFromGreater");
			}
		}
		return features;
	}
	
	public static List<String> varTokenFeaturesCopy(TemplateX x, TemplateY y) {
		List<String> features = new ArrayList<>();
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
		for(String key : y.varTokens.keySet()) {
			int index = y.varTokens.get(key).get(0);
			features.add("VarToken_"+unigrams.get(index));
			if(index-1>0) features.add("VarToken_-1_"+unigrams.get(index-1)); 
		}
		if(y.varTokens.size() == 2) {
			
		}
		return features;
		
	}
	
	public static String getSlotSignature(TemplateY y, Node someLeaf) {
		String signature = "";
		if(someLeaf.label.equals("VAR")) signature += "VAR";
		if(someLeaf.label.equals("NUM")) signature += "NUM";
		signature += "_"+getParentOfLeaf(y.equation.root, someLeaf).label;
		return signature;
	}

	public static Node getParentOfLeaf(Node root, Node someChild) {
		for(Node node : root.getAllSubNodes()) {
			if(node.children.size() == 2 && (node.children.get(0).label.equals(someChild.label) && 
					node.children.get(0).index == someChild.index)) {
				return node;
			}
			if(node.children.size() == 2 && (node.children.get(1).label.equals(someChild.label) && 
					node.children.get(1).index == someChild.index)) {
				return node;
			}
		}
		return null;
	}
	
}