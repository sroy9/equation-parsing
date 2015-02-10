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
	
	public static List<String> getFeatures(SemX x, SemY y) {
		List<String> features = new ArrayList<>();
		features.addAll(spanFeatures(x, y));
		for(Integer i : y.partitions.keySet()) {
			features.addAll(partitionFeatures(x, y, i));
		}
		for(Pair<String, IntPair> pair : y.nodes) {
			List<Pair<String, IntPair>> pattern = 
					Lexicon.getNodeString(x, y.nodes, pair.getSecond());
			List<IntPair> divs = new ArrayList<>();
			if(pattern.size() > 1) { 
				for(Pair<String, IntPair> p : pattern) {
					if(p.getFirst().equals("EXPR")) divs.add(p.getSecond());
				}
			}
			features.addAll(expressionFeatures(x, pair.getSecond().getFirst(), 
					pair.getSecond().getSecond(), divs, pair.getFirst()));
		}
		return features;
	}

	public IFeatureVector getSpanFeatureVector(SemX x, SemY y) {
		List<String> features = spanFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> spanFeatures(SemX x, SemY y) {
		List<String> features = new ArrayList<>();
		for(IntPair span : y.spans) {
			features.addAll(singleSpanFeatures(x, span));
		}
		if(y.spans.size() == 2) {
			if(x.ta.getSentenceFromToken(y.spans.get(0).getFirst()).getSentenceId() !=
				x.ta.getSentenceFromToken(y.spans.get(1).getFirst()).getSentenceId()) {
				features.add("SpanMid_DiffSentence");
			} else for(int i=y.spans.get(0).getSecond(); i<y.spans.get(1).getFirst(); ++i) {
				features.add("SpanMid_Unigram_"+x.ta.getToken(i));
			}
		}
		return features;
	}
	
	public static List<String> singleSpanFeatures(SemX x, IntPair span) {
		List<String> features = new ArrayList<>();
		String prefix = "SingleSpan";
		features.add(prefix+"_StartUnigram_"+x.lemmas.get(span.getFirst()));
		features.add(prefix + "_StartBigram_" + x.lemmas.get(span.getFirst()) 
				+ "_" + x.lemmas.get(span.getFirst()+1));
		features.add(prefix+"_EndUnigram_"+x.lemmas.get(span.getSecond()-1));
		if(x.ta.size() > span.getSecond()) {
			features.add(prefix+"_EndBigram_"+x.lemmas.get(span.getSecond()-1)+"_"+
					x.lemmas.get(span.getSecond()));
		}
		if(span.getFirst() == 0 || x.lemmas.get(span.getFirst()-1).equals(".")) {
			features.add(prefix + "_StartBeginningOfSentence");
		}
		if(span.getSecond() == x.ta.size() || x.lemmas.get(span.getSecond()).equals(".")) {
			features.add(prefix + "_EndWithEndOfSentence");
		}
		if((span.getFirst() == 0 || x.lemmas.get(span.getFirst()-1).equals(".")) && 
				(span.getSecond() == x.ta.size() || x.lemmas.get(span.getSecond()).equals("."))) {
			features.add(prefix + "_FullSentence");
		}
		return features;
	}

	public IFeatureVector getExpressionFeatureVector(SemX x, int i, int j,
			List<IntPair> division, String label) {
		List<String> features = expressionFeatures(x, i, j, division, label);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> expressionFeatures(SemX x, int i, int j,
			List<IntPair> division, String label) {
		List<String> features = new ArrayList<>();
		return features;
	}

	public IFeatureVector getPartitionFeatureVector(SemX x, SemY y, int i) {
		List<String> features = partitionFeatures(x, y, i);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> partitionFeatures(SemX x, SemY y, int i) {
		List<String> features = new ArrayList<>();
		return features;
	}
}