package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
		features.addAll(getE1Features(blob, lattice));
		features.addAll(getE2Features(blob, lattice));
		features.addAll(getE3Features(blob, lattice));
		features.addAll(getOperationFeatures(blob, lattice));
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	// Operation Features
	public IFeatureVector getOperationFeatureVector(
			Blob blob, Lattice lattice) throws Exception {
		List<String> feats = getOperationFeatures(blob, lattice);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getOperationFeatures(Blob blob, Lattice lattice) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<2; i++) {
			Equation eq = lattice.equations.get(i);
			String prefix = eq.A1.size()+"_"+eq.A2.size()+"_"+eq.B1.size()+"_"+
					eq.B2.size()+"_"+eq.C.size();
			features.add(prefix);
			for(Pair<Operation, Double> pair : eq.A1) {
				for(String feature : singleFeatures(pair.getSecond(), "E1", blob)) {
					features.add(prefix+"_A1_"+pair.getFirst()+"_"+feature);
				}
			}
			for(Pair<Operation, Double> pair : eq.B1) {
				for(String feature : singleFeatures(pair.getSecond(), "E2", blob)) {
					features.add(prefix+"_B1_"+pair.getFirst()+"_"+feature);
				}
			}
			for(Pair<Operation, Double> pair : eq.C) {
				for(String feature : singleFeatures(pair.getSecond(), "E3", blob)) {
					features.add(prefix+"_C_"+pair.getFirst()+"_"+feature);
				}
			}
		}
		return features;
	}

	// Number Features
	public IFeatureVector getE1FeatureVector(
			Blob blob, Lattice lattice) throws Exception {
		List<String> feats = getE1Features(blob, lattice);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getE1Features(Blob blob, Lattice lattice) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<2; i++) {
			Equation eq = lattice.equations.get(i);
			for(Pair<Operation, Double> pair : eq.A2) {
				for(String feature : singleFeatures(pair.getSecond(), "E1", blob)) {
					features.add("A2_"+pair.getFirst()+"_"+feature);
				}
			}
			for(Pair<Operation, Double> pair : eq.A1) {
				for(String feature : singleFeatures(pair.getSecond(), "E1", blob)) {
					features.add("A1_"+pair.getFirst()+"_"+feature);
				}
			}
		}
		return features;
	}
	
	public IFeatureVector getE2FeatureVector(
			Blob blob, Lattice lattice) throws Exception {
		List<String> feats = getE2Features(blob, lattice);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getE2Features(Blob blob, Lattice lattice) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<2; i++) {
			Equation eq = lattice.equations.get(i);
			String prefix = eq.A1.size()+"_"+eq.A2.size();
			for(Pair<Operation, Double> pair : eq.B2) {
				for(String feature : singleFeatures(pair.getSecond(), "E2", blob)) {
					features.add(prefix+"_B2_"+pair.getFirst()+"_"+feature);
				}
			}
			for(Pair<Operation, Double> pair : eq.B1) {
				for(String feature : singleFeatures(pair.getSecond(), "E2", blob)) {
					features.add(prefix+"_B1_"+pair.getFirst()+"_"+feature);
				}
			}
		}
		return features;
	}
	
	public IFeatureVector getE3FeatureVector(
			Blob blob, Lattice lattice) throws Exception {
		List<String> feats = getE3Features(blob, lattice);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getE3Features(Blob blob, Lattice lattice) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<2; i++) {
			Equation eq = lattice.equations.get(i);
			String prefix = eq.A1.size()+"_"+eq.A2.size()+"_"+eq.B1.size()+"_"+eq.B2.size();
			for(Pair<Operation, Double> pair : eq.C) {
				for(String feature : singleFeatures(pair.getSecond(), "E3", blob)) {
					features.add(prefix+"_C_"+pair.getFirst()+"_"+feature);
				}
			}
		}
		return features;
	}
	
	// Utility functions
	
	public List<IntPair> getRelevantSpans(
			Blob blob, String arrayName, Double d) {
		List<IntPair> relevantSpans = new ArrayList<IntPair>();
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
			Double d1, String arrayName1, Double d2, String arrayName2, Blob blob) {
		List<String> features = new ArrayList<>();
		List<IntPair> spans1 = getRelevantSpans(blob, arrayName1, d1);
		List<IntPair> spans2 = getRelevantSpans(blob, arrayName2, d2);
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
	
	public List<String> singleFeatures(Double d, String arrayName, Blob blob) {
		List<String> features = new ArrayList<>();
		
		// Ready the data structures
		List<IntPair> spans = getRelevantSpans(blob, arrayName, d);
		if(spans.size() == 0) {
			System.out.println("Number not found : "+d);
			System.out.println(blob.simulProb.index+" :Text : "+blob.ta);
			System.out.println("Quantities : "+Arrays.asList(blob.quantities));
		}
		int pos = blob.ta.getTokenIdFromCharacterOffset(spans.get(0).getFirst());
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