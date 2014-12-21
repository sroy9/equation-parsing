package latentsvm;

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

public class FeatureExtractor extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;

	public FeatureExtractor(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		Blob blob = (Blob) arg0;
		Lattice lattice = (Lattice) arg1;
		List<String> features = new ArrayList<>();
		for(int i=0; i<lattice.labelSet.labels.size(); ++i) {
			features.addAll(getClusterFeatures(blob, lattice.labelSet, i));
		}
		for(int i=0; i<2; i++) {
			Equation eq = lattice.equations.get(i);
			for(int j=0; j<eq.A1.size(); ++j) {
				features.addAll(getEquationFeatures(
						blob, lattice, i, "A1", j, eq.A1.get(j).getSecond()));
			}
			for(int j=0; j<eq.A2.size(); ++j) {
				features.addAll(getEquationFeatures(
						blob, lattice, i, "A2", j, eq.A2.get(j).getSecond()));
			}
			for(int j=0; j<eq.B1.size(); ++j) {
				features.addAll(getEquationFeatures(
						blob, lattice, i, "B1", j, eq.B1.get(j).getSecond()));
			}
			for(int j=0; j<eq.B2.size(); ++j) {
				features.addAll(getEquationFeatures(
						blob, lattice, i, "B2", j, eq.B2.get(j).getSecond()));
			}
			for(int j=0; j<eq.C.size(); ++j) {
				features.addAll(getEquationFeatures(
						blob, lattice, i, "C", j, eq.C.get(j).getSecond()));
			}
		}
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	// Cluster Features
	public IFeatureVector getClusterFeatureVector(
			Blob blob, LabelSet labelSet, int index) {
		List<String> feats = getClusterFeatures(blob, labelSet, index);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getClusterFeatures(
			Blob blob, LabelSet labelSet, int index) {
		List<String> features = new ArrayList<>();
		String prefix = labelSet.labels.get(index);
		QuantSpan qs = blob.quantities.get(index);
		for(String feature : singleFeatures(new IntPair(qs.start, qs.end), blob)) {
			features.add(prefix+"_"+feature);
		}
		return features;
	}
	
	// Equation Features
	public IFeatureVector getEquationFeatureVector(
			Blob blob, Lattice lattice, int eqNo, String arrayName, int index, Double d) throws Exception {
		List<String> feats = getEquationFeatures(blob, lattice, eqNo, arrayName, index, d);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getEquationFeatures(
			Blob blob, Lattice lattice, int eqNo, String arrayName, int index, Double d) {
		List<String> features = new ArrayList<>();
		String prefix = "";
		if(arrayName.endsWith("1")) prefix = "AB1";
		if(arrayName.endsWith("2")) prefix = "AB2";
		if(arrayName.equals("C")) prefix = "C";
		for(String feature : singleFeatures(d, arrayName, blob, lattice)) {
			features.add(prefix+"_"+feature);
		}
		return features;
	}
	
	// Utility functions
	
	public List<IntPair> getRelevantSpans(
			Blob blob, String arrayName, Double d, Map<String, List<QuantSpan>> clusterMap) {
		List<IntPair> relevantSpans = new ArrayList<IntPair>();
		if(arrayName.equals("A1") || arrayName.equals("A2") 
				|| arrayName.contains("E1")) {
			for(QuantSpan qs : clusterMap.get("E1")) {
				if(Tools.safeEquals(d, Tools.getValue(qs))) {
					relevantSpans.add(new IntPair(qs.start, qs.end));
				}
			}
		}
		if(arrayName.equals("B1") || arrayName.equals("B2") 
				|| arrayName.contains("E2")) {
			for(QuantSpan qs : clusterMap.get("E2")) {
				if(Tools.safeEquals(d, Tools.getValue(qs))) {
					relevantSpans.add(new IntPair(qs.start, qs.end));
				}
			}
		}
		if(arrayName.equals("C") || arrayName.equals("E3")) {
			for(QuantSpan qs : clusterMap.get("E3")) {
				if(Tools.safeEquals(d, Tools.getValue(qs))) {
					relevantSpans.add(new IntPair(qs.start, qs.end));
				}
			}
		}
		return relevantSpans;
	}
	
	public static boolean isPresent(Double d, String entity, Equation eq) {
		if(entity.equals("E1")) {
			for(Pair<Operation, Double> pair : eq.A1) {
				if(Tools.safeEquals(d, pair.getSecond())) {
					return true;
				}
			}
			for(Pair<Operation, Double> pair : eq.A2) {
				if(Tools.safeEquals(d, pair.getSecond())) {
					return true;
				}
			}
		}
		if(entity.equals("E2")) {
			for(Pair<Operation, Double> pair : eq.B1) {
				if(Tools.safeEquals(d, pair.getSecond())) {
					return true;
				}
			}
			for(Pair<Operation, Double> pair : eq.B2) {
				if(Tools.safeEquals(d, pair.getSecond())) {
					return true;
				}
			}
		}
		if(entity.equals("E3")) {
			for(Pair<Operation, Double> pair : eq.C) {
				if(Tools.safeEquals(d, pair.getSecond())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<String> pairwiseFeatures(
			Double d1, String arrayName1, Double d2, String arrayName2, Blob blob, Lattice lattice) {
		List<String> features = new ArrayList<>();
		List<IntPair> spans1 = getRelevantSpans(blob, arrayName1, d1, lattice.clusterMap);
		List<IntPair> spans2 = getRelevantSpans(blob, arrayName2, d2, lattice.clusterMap);
		int pos1 = blob.ta.getTokenIdFromCharacterOffset(spans1.get(0).getFirst());
		int pos2 = blob.ta.getTokenIdFromCharacterOffset(spans2.get(0).getFirst());
		int sent1 = blob.ta.getSentenceFromToken(pos1).getSentenceId();
		int sent2 = blob.ta.getSentenceFromToken(pos2).getSentenceId();
		if(sent1 == sent2) {
			features.add("SameSentence");
			for(int i=Math.min(pos1, pos2)+1; i<Math.max(pos1, pos2); i++) {
				features.add("WordsInBetween_"+blob.ta.getToken(i));
			}
			for(int i=Math.min(pos1, pos2)+1; i<Math.max(pos1, pos2)-1; i++) {
				features.add("WordsInBetween_"+blob.ta.getToken(i)+"_"+blob.ta.getToken(i+1));
			}
		} else {
			features.add("DifferentSentence");
		}
		return features;
	}
	
	public List<String> globalFeatures(Blob blob) {
		List<String> features = new ArrayList<>();
		return features;	
	}
	
	public List<String> singleFeatures(Double d, String arrayName, Blob blob, Lattice lattice) {
		// Ready the data structures
		List<IntPair> spans = getRelevantSpans(blob, arrayName, d, lattice.clusterMap);
		if(spans.size() == 0) {
			System.out.println("Number not found : "+d);
			System.out.println(blob.simulProb.index+" :Text : "+blob.ta);
			System.out.println("Quantities : "+Arrays.asList(blob.quantities));
		}
		return singleFeatures(spans.get(0), blob);
	}
	
	public List<String> singleFeatures(IntPair span, Blob blob) {
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