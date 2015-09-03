package equation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Node;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class EquationFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public EquationFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		EquationX x = (EquationX) arg0;
		EquationY y = (EquationY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getExpressionFeatureVector(EquationX x, Node node) {
		List<String> features = expressionFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public static List<String> getFeatures(EquationX x, EquationY y) {
		List<String> features = new ArrayList<>();
		for(Node subNode : y.equation.root.getAllSubNodes()) {
			if(subNode.children.size() == 2) {
				features.addAll(expressionFeatures(x, subNode));
			}
		}
		return features;
	}
	
	public static List<String> expressionFeatures(EquationX x, Node node) {
		List<String> features = new ArrayList<>();
		String prefix = node.label;
		IntPair span = x.getSpanningTokenIndices(node);
		Node child1 = node.children.get(0);
		Node child2 = node.children.get(1);
		IntPair spanChild1 = x.getSpanningTokenIndices(child1);
		IntPair spanChild2 = x.getSpanningTokenIndices(child2);
		int leftStart = Math.min(spanChild1.getFirst(), spanChild2.getFirst());
		int rightEnd = Math.max(spanChild1.getSecond(), spanChild2.getSecond());
		int midStart = Math.min(spanChild1.getSecond(), spanChild2.getSecond());
		int midEnd = Math.max(spanChild1.getFirst(), spanChild2.getFirst());
		
		if(child1.label.equals("VAR") || child2.label.equals("VAR")) {
			prefix += "_VAR"; 
		}
		if(child1.label.equals("NUM") || child2.label.equals("NUM")) {
			prefix += "_NUM"; 
		}
		
		// Mid token features
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
		for(int i=midStart+1; i<midEnd; ++i) {
			features.add(prefix+"_MidUnigram_"+unigrams.get(i));
			if(i+1<Math.max(spanChild1.getFirst(), spanChild2.getFirst())) {
				features.add(prefix+"_MidBigram_"+unigrams.get(i)+"_"+unigrams.get(i+1));
			}
		}
		
//		// Some tokens to the left
//		for(int i=leftStart-1; i>Math.max(0, leftStart-5); --i) {
//			features.add(prefix+"_TokenLeft_"+unigrams.get(i));
//			features.add(prefix+"_TokenLeft_"+unigrams.get(i)+"_"+unigrams.get(i+1));
//		}
//		
//		// Some tokens to the right
//		for(int i=rightEnd+1; i<Math.min(x.ta.size()-1, rightEnd+3); ++i) {
//			features.add(prefix+"_TokenRight_"+unigrams.get(i));
//			features.add(prefix+"_TokenRight_"+unigrams.get(i)+"_"+unigrams.get(i+1));
//		}
//		
//		Node numNode;
//		if(node.children.get(0).label.equals("NUM")) {
//			features.add(prefix+"_OneChildIsVar_"+unigrams.get(span.getFirst()));
//			numNode = node.children.get(0);
//		} else {
//			features.add(prefix+"_OneChildIsVar_"+unigrams.get(span.getSecond()-1));
//			numNode = node.children.get(1);
//		}
//		for(int i=0; i<x.quantities.size(); ++i) {
//			if(Tools.safeEquals(Tools.getValue(x.quantities.get(i)), numNode.value)) {
//				if(Tools.getUnit(x.quantities.get(i)).equals("percent")) {
//					features.add(prefix+"_Child_Percentage");
//				}
//				if(Tools.getUnit(x.quantities.get(i)).equals("US$")) {
//					features.add(prefix+"_Child_US$");
//				}
//				break;
//			}
//		}
		
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
}