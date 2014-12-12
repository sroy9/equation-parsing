package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
			feats.addAll(getOpE1Features(blob, lattice, eqNo, d));
		}	
		if(arrayName.contains("Op_E2")) {
			feats.addAll(getOpE2Features(blob, lattice, eqNo, d));
		}	
		return feats;
	}
	
	private Collection<? extends String> getOpE2Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		Equation eq = lattice.equations.get(eqNo);
		String prefix = "OpE2_"+eqNo+"_"+eq.operations.get(2)+"_"+eq.operations.get(3);
		features.add(prefix);
//		if(eq.B1.size() == 0) {
//			features.add(prefix+"_B1_Size_0");
//		}
//		if(eq.B2.size() == 0) {
//			features.add(prefix+"_B2_Size_0");
//		}
//		List<IntPair> spans = new ArrayList<>();
//		for(Pair<Operation, Double> pair : eq.B2) {
//			spans.addAll(getRelevantSpans(blob, lattice, eqNo, "B2", pair.getSecond()));
//		}
//		for(IntPair span : spans) {
//			int pos = blob.ta.getTokenIdFromCharacterOffset(span.getFirst());
//			for(String feature : FeatureExtraction.getMixed(blob.ta, blob.posTags, pos, 2)) {
//				features.add(prefix+"_Neighbors_"+feature);
//			}
//			for(String feature : blob.ta.getSentenceFromToken(pos).getTokens()) {
//				features.add(prefix+"_Sentence_"+feature);
//			}
//		}
		return features;
	}

	private Collection<? extends String> getOpE1Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		Equation eq = lattice.equations.get(eqNo);
		String prefix = "OpE1_"+eqNo+"_"+eq.operations.get(0)+"_"+eq.operations.get(1);
		features.add(prefix);
		if(eq.A1.size() == 0) {
			features.add(prefix+"_A1_Size_0");
		}
		if(eq.A2.size() == 0) {
			features.add(prefix+"_A2_Size_0");
		}
		List<IntPair> spans = new ArrayList<>();
		for(Pair<Operation, Double> pair : eq.A2) {
			spans.addAll(getRelevantSpans(blob, lattice, eqNo, "A2", pair.getSecond()));
		}
		for(IntPair span : spans) {
			int pos = blob.ta.getTokenIdFromCharacterOffset(span.getFirst());
			for(String feature : FeatureExtraction.getMixed(blob.ta, blob.posTags, pos, 2)) {
				features.add(prefix+"_Neighbors_"+feature);
			}
			for(String feature : blob.ta.getSentenceFromToken(pos).getTokens()) {
				features.add(prefix+"_Sentence_"+feature);
			}
		}
		return features;
	}

	private Collection<? extends String> getCFeatures(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "C_"+eqNo+"_"+d.getFirst()+"_"+lattice.equations.get(eqNo).C.size();
		features.add(prefix);
		if(eqNo > 0) {
			for(Pair<Operation, Double> pair : lattice.equations.get(0).C) {
				if(Tools.safeEquals(pair.getSecond(), d.getSecond())) {
					features.add(prefix+"_Already used in previous equation");
					break;
				}
			}
		}
		return features;
	}

	private Collection<? extends String> getB2Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "B2_"+eqNo+"_"+d.getFirst();
		features.add(prefix);
		if(eqNo > 0) {
			for(Pair<Operation, Double> pair : lattice.equations.get(0).B2) {
				if(Tools.safeEquals(pair.getSecond(), d.getSecond())) {
					features.add(prefix+"_Already used in previous equation");
					break;
				}
			}
		}
		return features;
	}

	private Collection<? extends String> getB1Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		Equation eq = lattice.equations.get(eqNo);
		String prefix = "B1_"+eqNo+"_"+d.getFirst();
		features.add(prefix);
		List<IntPair> spans = getRelevantSpans(blob, lattice, eqNo, "B1", d.getSecond());
		if(spans.size() > 1) features.add(prefix+"_MentionedTwice");
		features.add(prefix+"_A1Size_"+eq.A1.size());
		features.add(prefix+"_A2Size_"+eq.A2.size());
		features.add(prefix+"_Op_"+eq.operations.get(0)+"_"+eq.operations.get(1));
		for(IntPair span : spans) {
			int pos = blob.ta.getTokenIdFromCharacterOffset(span.getFirst());
			for(String feature : FeatureExtraction.getMixed(blob.ta, blob.posTags, pos, 2)) {
				features.add(prefix+"_Neighbors_"+feature);
			}
			int sentId = blob.ta.getSentenceFromToken(pos).getSentenceId();
			for(Pair<Operation, Double> pair : eq.A1) {
				for(IntPair span1 : getRelevantSpans(blob, lattice, eqNo, "A1", pair.getSecond())) {
					int pos1 = blob.ta.getTokenIdFromCharacterOffset(span1.getFirst());
					int sentId1 = blob.ta.getSentenceFromToken(pos1).getSentenceId();
					if(sentId1 == sentId) {
						features.add(prefix+"_SameSentenceWithA1"+pair.getFirst());
					}
				}
			}
			
		}
		
		if(eqNo > 0) {
			for(Pair<Operation, Double> pair : lattice.equations.get(0).B1) {
				if(Tools.safeEquals(pair.getSecond(), d.getSecond())) {
					features.add(prefix+"_Already used in previous equation");
					break;
				}
			}
		}
		return features;
	}

	private Collection<? extends String> getA2Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "A2_"+eqNo+"_"+d.getFirst();
		features.add(prefix);
		List<IntPair> spans = getRelevantSpans(blob, lattice, eqNo, "A2", d.getSecond());
		for(IntPair span : spans) {
			int pos = blob.ta.getTokenIdFromCharacterOffset(span.getFirst());
			for(String feature : FeatureExtraction.getMixed(blob.ta, blob.posTags, pos, 2)) {
				features.add(prefix+"_Neighbors_"+feature);
			}
			for(String feature : blob.ta.getSentenceFromToken(pos).getTokens()) {
				features.add(prefix+"_Sentence_"+feature);
			}
		}
		if(eqNo > 0) {
			for(Pair<Operation, Double> pair : lattice.equations.get(0).A2) {
				if(Tools.safeEquals(pair.getSecond(), d.getSecond())) {
					features.add(prefix+"_Already used in previous equation");
					break;
				}
			}
		}
		return features;
	}

	private Collection<? extends String> getA1Features(Blob blob,
			Lattice lattice, int eqNo, Pair<Operation, Double> d) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "A1_"+eqNo+"_"+d.getFirst()+"_"+blob.clusterMap.get("E1").size();
		features.add(prefix);
		List<IntPair> spans = getRelevantSpans(blob, lattice, eqNo, "A1", d.getSecond());
		if(spans.size() > 1) features.add(prefix+"_MentionedTwice");
		for(IntPair span : spans) {
			int pos = blob.ta.getTokenIdFromCharacterOffset(span.getFirst());
			for(String feature : FeatureExtraction.getMixed(blob.ta, blob.posTags, pos, 2)) {
				features.add(prefix+"_Neighbors_"+feature);
			}
			for(String feature : blob.ta.getSentenceFromToken(pos).getTokens()) {
				features.add(prefix+"_Sentence_"+feature);
			}
		}
		if(eqNo > 0) {
			for(Pair<Operation, Double> pair : lattice.equations.get(0).A1) {
				if(Tools.safeEquals(pair.getSecond(), d.getSecond())) {
					features.add(prefix+"_Already used in previous equation");
					break;
				}
			}
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
			Blob blob, Lattice lattice, int eqNo, String arrayName, Double d) {
		List<IntPair> relevantSpans = new ArrayList<IntPair>();
		if(arrayName.equals("A1") || arrayName.contains("E1")) {
			for(QuantSpan qs : blob.clusterMap.get("E1")) {
				if(Tools.safeEquals(d, Tools.getValue(qs))) {
					relevantSpans.add(new IntPair(qs.start, qs.end));
					break;
				}
			}
		}
		if(arrayName.equals("A2") || arrayName.contains("E1")) {
			for(QuantSpan qs : blob.clusterMap.get("E1")) {
				if(Tools.safeEquals(d, Tools.getValue(qs))) {
					relevantSpans.add(new IntPair(qs.start, qs.end));
					break;
				}
			}
		}
		if(arrayName.equals("B1") || arrayName.contains("E2")) {
			for(QuantSpan qs : blob.clusterMap.get("E2")) {
				if(Tools.safeEquals(d, Tools.getValue(qs))) {
					relevantSpans.add(new IntPair(qs.start, qs.end));
					break;
				}
			}
		}
		if(arrayName.equals("B2") || arrayName.contains("E2")) {
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
}