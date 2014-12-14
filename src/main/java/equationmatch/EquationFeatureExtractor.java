package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import structure.Equation;
import structure.Operation;
import utils.FeatureExtraction;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class EquationFeatureExtractor extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;

	public EquationFeatureExtractor(Lexiconer lm) {
		this.lm = lm;
	}
	
	public IFeatureVector getOperationFeatureVector(
			Blob blob, Lattice lattice, int eqNo) throws Exception {
		List<String> feats = getOperationFeatures(blob, lattice, eqNo);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public IFeatureVector getNumberFeatureVector(
			Blob blob, Lattice lattice, int eqNo) throws Exception {
		List<String> feats = getNumberFeatures(blob, lattice, eqNo);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}

	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		Blob blob = (Blob) arg0;
		Lattice lattice = (Lattice) arg1;
		List<String> features = new ArrayList<>();
		// Enumerate all positions
		for(int i=0; i<2; ++i) {
			features.addAll(getNumberFeatures(blob, lattice, i));
			features.addAll(getOperationFeatures(blob, lattice, i));
		}
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	public List<String> getOperationFeatures(Blob blob, Lattice lattice, int eqNo) {
		List<String> features = new ArrayList<>();
		return features;
	}
	
	public List<String> getNumberFeatures(Blob blob, Lattice lattice, int eqNo) {
		List<String> features = new ArrayList<>();
		return features;
	}
	
	
	public List<IntPair> getRelevantSpans(
			Blob blob, String arrayName, Double d) {
		List<IntPair> relevantSpans = new ArrayList<IntPair>();
		if(arrayName.equals("A1") || arrayName.equals("A2") || arrayName.contains("E1")) {
			for(QuantSpan qs : blob.clusterMap.get("E1")) {
				if(Tools.safeEquals(d, Tools.getValue(qs))) {
					relevantSpans.add(new IntPair(qs.start, qs.end));
					break;
				}
			}
		}
		if(arrayName.equals("B1") || arrayName.equals("B2") || arrayName.contains("E2")) {
			for(QuantSpan qs : blob.clusterMap.get("E2")) {
				if(Tools.safeEquals(d, Tools.getValue(qs))) {
					relevantSpans.add(new IntPair(qs.start, qs.end));
					break;
				}
			}
		}
		if(arrayName.equals("C")) {
			for(QuantSpan qs : blob.clusterMap.get("E3")) {
				if(Tools.safeEquals(d, Tools.getValue(qs))) {
					relevantSpans.add(new IntPair(qs.start, qs.end));
					break;
				}
			}
		}
		return relevantSpans;
	}
	
	public boolean isPresent(Double d, String entity, Equation eq) {
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
	
	public List<String> getPairwiseFeatures(List<Pair<Operation, Double>> list1,
			List<Pair<Operation, Double>> list2, Blob blob, String arrayName1, String arrayName2) {
		List<String> features = new ArrayList<>();
		for(Pair<Operation, Double> pair1 : list1) {
			for(IntPair span1 : getRelevantSpans(blob, arrayName1, pair1.getSecond())) {
				for(Pair<Operation, Double> pair2 : list2) {
					for(IntPair span2 : getRelevantSpans(
							blob, arrayName2, pair2.getSecond())) {
						features.addAll(getPairwiseFeatures(span1, span2, blob));
					}
				}
			}
		}
		return features;
	}
	
	public List<String> getPairwiseFeatures(IntPair span1, IntPair span2, Blob blob) {
		List<String> features = new ArrayList<>();
		int pos1 = blob.ta.getTokenIdFromCharacterOffset(span1.getFirst());
		int pos2 = blob.ta.getTokenIdFromCharacterOffset(span1.getSecond());
		int sent1 = blob.ta.getSentenceFromToken(pos1).getSentenceId();
		int sent2 = blob.ta.getSentenceFromToken(pos2).getSentenceId();
		if(sent1 == sent2) {
			features.add("SameSentence");
			for(int i=Math.min(pos1, pos2)+1; i<Math.max(pos1, pos2); i++) {
				features.add("WordsInBetween_"+blob.ta.getToken(i));
			}
		} else {
			features.add("DifferentSentence");
		}
		return features;
	}
	
	public List<String> getGlobalFeatures(Blob blob) {
		List<String> features = new ArrayList<>();
		features.add("NumberOfSentences_"+blob.ta.getNumberOfSentences());
		features.addAll(FeatureExtraction.getLemmatizedUnigrams(
				blob.lemmas, 0, blob.ta.size()-1));
		features.addAll(FeatureExtraction.getLemmatizedBigrams(
				blob.lemmas, 0, blob.ta.size()-1));
		features.add("E1_size_"+Tools.uniqueNumbers(blob.clusterMap.get("E1")).size());
		features.add("E2_size_"+Tools.uniqueNumbers(blob.clusterMap.get("E2")).size());
		features.add("E3_size_"+Tools.uniqueNumbers(blob.clusterMap.get("E3")).size());
		features.add("QuestionSentence_"+FeatureExtraction.getQuestionSentences(blob.ta).size());
		return features;	
	}
	
	public List<String> nearbyTokens(
			IntPair span, TextAnnotation ta, List<Constituent> lemmas, int window) {
		List<String> features = new ArrayList<>();
		int startPos = ta.getTokenIdFromCharacterOffset(span.getFirst());
		int endPos = ta.getTokenIdFromCharacterOffset(span.getSecond());
		List<String> unigrams = new ArrayList<>();
		for(int i=Math.max(startPos-window,0); i<=Math.min(endPos+window,ta.size()-1); i++) {
			if(NumberUtils.isNumber(ta.getToken(i))) {
				unigrams.add("NUMBER");
			} else {
				unigrams.add(lemmas.get(i).getLabel());
			}
		}
		for(int i=0; i<unigrams.size(); i++) {
			features.add("Nearby_"+unigrams.get(i));
		}
		for(int i=0; i<unigrams.size()-1; i++) {
			features.add("Nearby_"+unigrams.get(i)+"_"+unigrams.get(i+1));
		}
		return features;
	}
	
	
	
	
	
	
	
}