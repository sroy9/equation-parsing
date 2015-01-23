package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.standardize.Ratio;
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
	
	public IFeatureVector getSpanFeatureVector(SemX x, SemY y) {
		List<String> features = spanFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getNodeFeatureVector(SemX x, IntPair span) {
		List<String> features = nodeFeatures(x, span);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getNodeTypeFeatureVector(SemX x, String nodeLabel, IntPair span) {
		List<String> features = nodeTypeFeatures(x, nodeLabel, span);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(SemX x, SemY y) {
		List<String> features = new ArrayList<>();
		features.addAll(spanFeatures(x, y));
		for(Pair<String, IntPair> pair : y.nodes) {
			if(pair.getFirst().equals("EQ")) continue;
			features.addAll(nodeFeatures(x, pair.getSecond()));
			features.addAll(nodeTypeFeatures(x, pair.getFirst(), pair.getSecond()));
		}
		return features;
	}

	public static List<String> spanFeatures(SemX x, SemY y) {
		List<String> features = new ArrayList<>();
		return features;
	}
	
	public static List<String> nodeFeatures(SemX x, IntPair span) {
		List<String> features = new ArrayList<>();
		return features;
	}
	
	public static List<String> nodeTypeFeatures(SemX x, String nodeLabel, IntPair span) {
		List<String> features = new ArrayList<>();
		return features;
	}
}