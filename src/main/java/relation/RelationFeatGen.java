package relation;

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

public class RelationFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;

	public RelationFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		RelationX blob = (RelationX) arg0;
		RelationY relationY= (RelationY) arg1;
		List<String> features = new ArrayList<>();
		for(int i=0; i<relationY.relations.size(); ++i) {
			features.addAll(getFeatures(blob, relationY, i));
		}
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	// Cluster Features
	public IFeatureVector getFeatureVector(
			RelationX blob, RelationY labelSet, int index) {
		List<String> feats = getFeatures(blob, labelSet, index);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getFeatures(
			RelationX blob, RelationY labelSet, int index) {
		List<String> features = new ArrayList<>();
		String prefix = labelSet.relations.get(index);
		QuantSpan qs = blob.quantities.get(index);
//		System.out.println("Cluster Features");
		for(String feature : singleFeatures(new IntPair(qs.start, qs.end), blob)) {
			features.add(prefix+"_"+feature);
//			System.out.println(prefix+"_"+feature);
		}
		return features;
	}
	
	public List<IntPair> getRelevantSpans(
			RelationX blob, int index, Double d, List<List<QuantSpan>> clusters) {
		List<IntPair> relevantSpans = new ArrayList<IntPair>();
		for(QuantSpan qs : clusters.get(index/2)) {
			if(Tools.safeEquals(d, Tools.getValue(qs))) {
				relevantSpans.add(new IntPair(qs.start, qs.end));
			}
		}
		return relevantSpans;
	}
	
	
	public List<String> globalFeatures(RelationX blob) {
		List<String> features = new ArrayList<>();
		return features;	
	}
	
	public List<String> singleFeatures(IntPair span, RelationX blob) {
		List<String> features = new ArrayList<>();
		int pos = blob.ta.getTokenIdFromCharacterOffset(span.getFirst());
		Sentence sent = blob.ta.getSentenceFromToken(pos);
		String tokens[] = new String[blob.ta.size()];
		for(int i=0; i<blob.ta.size(); ++i) {
			if(NumberUtils.isNumber(blob.ta.getToken(i))) {
				tokens[i] = "NUMBER";
			} else if(blob.ta.getToken(i).contains("$") 
					|| blob.ta.getToken(i).contains("dollar") 
					|| blob.ta.getToken(i).contains("cents")) {
				tokens[i] = "MONEY_UNIT";
			} else {
				tokens[i] = blob.lemmas.get(i).getLabel();
			}
		}
		// Nearby unigrams and bigrams
		for(int i = Math.max(pos-3, sent.getStartSpan());
				i <= Math.min(pos+3, sent.getEndSpan()-1); ++i) {
			features.add("Unigram_"+(i-pos)+"_"+tokens[i]);
			features.add("Unigram_"+tokens[i]);
		}
		for(int i = Math.max(pos-3, sent.getStartSpan());
				i <= Math.min(pos+3, sent.getEndSpan()-1)-1; ++i) {
			features.add("Bigram_"+(i-pos)+"_"+tokens[i]+"_"+tokens[i+1]);
			features.add("Bigram_"+tokens[i]+"_"+tokens[i+1]);
		}
		return features;
	}
}