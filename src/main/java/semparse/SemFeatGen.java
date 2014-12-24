package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import utils.FeatureExtraction;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
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
		List<String> features = new ArrayList<>();
		features.addAll(getFeatures(x, y));
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	// Cluster Features
	public IFeatureVector getFeatureVector(SemX x, SemY y) {
		List<String> feats = getFeatures(x, y);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getFeatures(SemX x, SemY y) {
		List<String> features = new ArrayList<>();
		return features;
	}
	// Utility functions
	
		
	public List<String> globalFeatures(SemX blob) {
		List<String> features = new ArrayList<>();
		return features;	
	}
	
}