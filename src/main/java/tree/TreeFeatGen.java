package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import structure.KnowledgeBase;
import structure.Node;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class TreeFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public TreeFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		TreeX x = (TreeX) arg0;
		TreeY y = (TreeY) arg1;
//		System.out.println("Feature being extracted");
		List<String> features = getFeatures(x, y);
//		System.out.println("Feature already extracted");
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getExpressionFeatureVector(TreeX x, Node node) {
		List<String> features = expressionFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public IFeatureVector getVarTokenFeatureVector(TreeX x, TreeY y) {
		List<String> features = varTokenFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public static List<String> getFeatures(TreeX x, TreeY y) {
		List<String> features = new ArrayList<>();
		features.addAll(varTokenFeatures(x, y));
		for(Node subNode : y.equation.root.getAllSubNodes()) {
			if(subNode.children.size() == 2) {
				features.addAll(expressionFeatures(x, subNode));
//				features.addAll(kbFeatures(x, y.varTokens, subNode));
			}
		}
		return features;
	}
	
	public static List<String> expressionFeatures(TreeX x, Node node) {
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
	
	public static List<String> varTokenFeatures(TreeX x, TreeY y) {
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
}