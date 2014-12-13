package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.bwaldvogel.liblinear.Feature;
import structure.Equation;
import structure.Operation;
import utils.FeatureExtraction;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class EquationFeatureExtractor extends AbstractFeatureGenerator implements
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1810851154558168679L;
	/**
	 * This function returns a feature vector \Phi(x,y) based on an
	 * instance-structure pair.
	 * 
	 * @return Feature Vector \Phi(x,y), where x is the input instance and y is
	 *         the output structure
	 */
	public Lexiconer lm = null;

	public EquationFeatureExtractor(Lexiconer lm) {
		this.lm = lm;
	}

	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		Blob blob = (Blob) arg0;
		Lattice lattice = (Lattice) arg1;
		List<String> features = new ArrayList<>();
		// Enumerate all positions
		for(int i=0; i<lattice.equations.size(); ++i) {
			Equation eq = lattice.equations.get(i);
			try {
				for(Pair<Operation, Double> pair : eq.A1) {
					features.addAll(getFeatures(blob, lattice, i, "A1", pair));
				}
				for(Pair<Operation, Double> pair : eq.A2) {
					features.addAll(getFeatures(blob, lattice, i, "A2", pair));
				}
				for(Pair<Operation, Double> pair : eq.B1) {
					features.addAll(getFeatures(blob, lattice, i, "B1", pair));
				}
				for(Pair<Operation, Double> pair : eq.B2) {
					features.addAll(getFeatures(blob, lattice, i, "B2", pair));
				}
				for(Pair<Operation, Double> pair : eq.C) {
					features.addAll(getFeatures(blob, lattice, i, "C", pair));
				}
				features.addAll(getFeatures(blob, lattice, i, "Op_E1", null));
				features.addAll(getFeatures(blob, lattice, i, "Op_E2", null));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	public List<String> getFeatures(
			Blob blob, Lattice lattice, int eqNo, String arrayName, Pair<Operation, Double> d) 
					throws Exception {
		List<String> feats = new ArrayList<>();
		if(arrayName.contains("A1")) {
			feats.addAll(getA1Features(blob, lattice, eqNo, d));
		}
		if(arrayName.contains("A2")) {
			feats.addAll(getA2Features(blob, lattice, eqNo, d));
		}	
		if(arrayName.contains("B1")) {
			feats.addAll(getB1Features(blob, lattice, eqNo, d));
		}	
		if(arrayName.contains("B2")) {
			feats.addAll(getB2Features(blob, lattice, eqNo, d));
		}	
		if(arrayName.contains("C")) {
			feats.addAll(getCFeatures(blob, lattice, eqNo, d));
		}		
		if(arrayName.contains("Op_E1")) {
			feats.addAll(getOpE1Features(blob, lattice, eqNo));
		}	
		if(arrayName.contains("Op_E2")) {
			feats.addAll(getOpE2Features(blob, lattice, eqNo));
		}	
		return feats;
	}
	
	private Collection<? extends String> getOpE2Features(Blob blob,
			Lattice lattice, int eqNo) throws Exception {
		List<String> features = new ArrayList<String>();
		Equation eq = lattice.equations.get(eqNo);
		String prefix = "Op_"+eq.operations.get(2)+"_"+eq.operations.get(3);
		features.add(prefix);
		return features;
	}

	private Collection<? extends String> getOpE1Features(Blob blob,
			Lattice lattice, int eqNo) throws Exception {
		List<String> features = new ArrayList<String>();
		Equation eq = lattice.equations.get(eqNo);
		String prefix = "Op_"+eq.operations.get(0)+"_"+eq.operations.get(1);
		features.add(prefix);
		return features;
	}

	private Collection<? extends String> getCFeatures(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "C_"+d.getFirst();
		features.add(prefix);
		for(String feature : getGlobalFeatures(blob)) {
			features.add(prefix+"_"+feature);
		}
		if(eqNo>0 && isPresent(d.getSecond(), "E3", lattice.equations.get(0))) {
			features.add("Already_Present");
		}
		return features;
	}

	private Collection<? extends String> getB2Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "AB2_"+d.getFirst();
		features.add(prefix);
		for(String feature : getGlobalFeatures(blob)) {
			features.add(prefix+"_"+feature);
		}
		if(eqNo>0 && isPresent(d.getSecond(), "E2", lattice.equations.get(0))) {
			features.add("Already_Present");
		}
		for(String feature : getPairwiseFeatures(
				lattice.equations.get(eqNo).B1, lattice.equations.get(eqNo).B2, blob, "B1", "B2")) {
			features.add(prefix+"_"+feature);
		}
		return features;
	}

	private Collection<? extends String> getB1Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "AB1_"+d.getFirst();
		features.add(prefix);
		for(String feature : getGlobalFeatures(blob)) {
			features.add(prefix+"_"+feature);
		}
		if(eqNo>0 && isPresent(d.getSecond(), "E2", lattice.equations.get(0))) {
			features.add("Already_Present");
		}
		for(String feature : getPairwiseFeatures(
				lattice.equations.get(eqNo).A1, lattice.equations.get(eqNo).B1, blob, "A1", "B1")) {
			features.add(prefix+"_"+feature);
		}
		return features;
	}

	private Collection<? extends String> getA2Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "AB2_"+d.getFirst();
		features.add(prefix);
		for(String feature : getGlobalFeatures(blob)) {
			features.add(prefix+"_"+feature);
		}
		if(eqNo>0 && isPresent(d.getSecond(), "E1", lattice.equations.get(0))) {
			features.add("Already_Present");
		}
		for(String feature : getPairwiseFeatures(
				lattice.equations.get(eqNo).A1, lattice.equations.get(eqNo).A2, blob, "A1", "A2")) {
			features.add(prefix+"_"+feature);
		}
		return features;
	}

	private Collection<? extends String> getA1Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "AB1_"+d.getFirst();
		features.add(prefix);
		for(String feature : getGlobalFeatures(blob)) {
			features.add(prefix+"_"+feature);
		}
		if(eqNo>0 && isPresent(d.getSecond(), "E1", lattice.equations.get(0))) {
			features.add("Already_Present");
		}
		return features;
	}

	public IFeatureVector getFeaturesVector(
			Blob blob, Lattice lattice, int eqNo, String arrayName, Pair<Operation, Double> d) 
					throws Exception {
		List<String> feats = getFeatures(blob, lattice, eqNo, arrayName, d);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
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
				blob.ta, blob.lemmas, 0, blob.ta.size()-1));
		features.addAll(FeatureExtraction.getLemmatizedBigrams(
				blob.ta, blob.lemmas, 0, blob.ta.size()-1));
		features.add("E1_size_"+Tools.uniqueNumbers(blob.clusterMap.get("E1")).size());
		features.add("E2_size_"+Tools.uniqueNumbers(blob.clusterMap.get("E2")).size());
		features.add("E3_size_"+Tools.uniqueNumbers(blob.clusterMap.get("E3")).size());
		features.add("QuestionSentence_"+FeatureExtraction.getQuestionSentences(blob.ta).size());
		return features;	
	}
	
	
	
	
	
	
	
}