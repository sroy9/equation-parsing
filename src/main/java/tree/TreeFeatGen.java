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
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getExpressionFeatureVector(TreeX x, int i, int j,
			List<Node> children, String label) {
		List<String> features = expressionFeatures(x, i, j, children, label);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	public static List<String> getFeatures(TreeX x, TreeY y) {
		List<String> features = new ArrayList<>();
		for(Node node : y.nodes) {
			features.addAll(expressionFeatures(
					x, node.span.getFirst(), node.span.getSecond(), 
					node.children, node.label));
		}
		return features;
	}
	
	public static List<String> expressionFeatures(TreeX x, int start, int end,
			List<Node> children, String label) {
		List<String> features = new ArrayList<>();
		return features;
	}
}