package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.Node;
import utils.FeatGen;
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
		
	public static List<String> getFeatures(TreeX x, TreeY y) {
//		System.out.println("Features needed for "+y);
		List<String> features = new ArrayList<>();
		features.addAll(varTokenFeatures(x, y));
		for(Node subNode : y.equation.root.getAllSubNodes()) {
			if(subNode.children.size() == 2) {
				features.addAll(expressionFeatures(x, subNode));
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
		int midStart = Math.min(spanChild1.getSecond(), spanChild2.getSecond());
		int midEnd = Math.max(spanChild1.getFirst(), spanChild2.getFirst());
		if(spanChild1.getFirst() == spanChild2.getFirst()) {
			features.add(prefix + "_SAME");
		} else if(spanChild1.getFirst() > spanChild2.getFirst()) {
			features.add(prefix + "_DESC");
		} else {
			features.add(prefix + "_ASC");
		}
		// Mid token features
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
		for(int i=Math.min(spanChild1.getSecond(), spanChild2.getSecond())+1;
				i<Math.max(spanChild1.getFirst(), spanChild2.getFirst()); 
				++i) {
			features.add(prefix+"_MidUnigram_"+unigrams.get(i));
			if(i+1<Math.max(spanChild1.getFirst(), spanChild2.getFirst())) {
				features.add(prefix+"_MidBigram_"+unigrams.get(i)+"_"+unigrams.get(i+1));
			}
		}
		// Some tokens to the left
		for(int i=Math.min(spanChild1.getFirst(), spanChild2.getFirst())-1;
				i>Math.max(0, Math.min(spanChild1.getFirst(), spanChild2.getFirst())-5); 
				--i) {
			features.add(prefix+"_TokenLeft_"+unigrams.get(i));
			features.add(prefix+"_TokenLeft_"+unigrams.get(i)+"_"+unigrams.get(i+1));
		}
		return features;
	}

	public IFeatureVector getVarTokenFeatureVector(TreeX x, TreeY y) {
		List<String> features = varTokenFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
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