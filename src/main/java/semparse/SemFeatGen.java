package semparse;

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

public class SemFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public SemFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		SemX x = (SemX) arg0;
		SemY y = (SemY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getExpressionFeatureVector(SemX x, int i, int j,
			List<Node> children, String label) {
		List<String> features = expressionFeatures(x, i, j, children, label);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	public static List<String> getFeatures(SemX x, SemY y) {
		List<String> features = new ArrayList<>();
		for(Node node : y.nodes) {
			features.addAll(expressionFeatures(
					x, node.span.getFirst(), node.span.getSecond(), 
					node.children, node.label));
		}
		return features;
	}
	
	public static List<String> expressionFeatures(SemX x, int start, int end,
			List<Node> children, String label) {
		List<String> features = new ArrayList<>();
		int startIndex = x.triggers.get(start).index;
		String prefix = label + "_" + ((start+1 == end) ? "Leaf" : "");
		if(start+1 == end) {
			features.add(prefix+"_Label_"+x.triggers.get(start).label);
			if(x.triggers.get(start).label.equals("OP")) {
				features.add(prefix + "_Token_" + 
						x.ta.getToken(startIndex).toLowerCase());
			}
//			features.add(prefix + "_Token+1_" + 
//					x.ta.getToken(startIndex+1).toLowerCase());
		} else {
			
			
		}
		return features;
	}
}