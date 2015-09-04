package tree;

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
			}
		}
		return features;
	}
	
	public static List<String> expressionFeatures(TreeX x, Node node) {
		List<String> features = new ArrayList<>();
		return features;
	}
	
	public static List<String> varTokenFeatures(TreeX x, TreeY y) {
		List<String> features = new ArrayList<>();
		return features;
	}
}