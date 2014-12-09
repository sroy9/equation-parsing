package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Equation;
import structure.Operation;
import utils.FeatureExtraction;
import utils.Tools;
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
			try {
				features.addAll(getFeatures(blob, lattice, i, "A1"));
				features.addAll(getFeatures(blob, lattice, i, "A2"));
				features.addAll(getFeatures(blob, lattice, i, "B1"));
				features.addAll(getFeatures(blob, lattice, i, "B2"));
				features.addAll(getFeatures(blob, lattice, i, "C"));
				features.addAll(getFeatures(blob, lattice, i, "Op"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	public List<String> getFeatures(
			Blob blob, Lattice lattice, int eqNo, String arrayName) 
					throws Exception {
		List<String> feats = new ArrayList<>();
		feats.addAll(getNeighboringWordFeatures(blob, lattice, eqNo, arrayName));
		return feats;
	}

	public IFeatureVector getFeaturesVector(
			Blob blob, Lattice lattice, int eqNo, String arrayName) throws Exception {
		List<String> feats = getFeatures(blob, lattice, eqNo, arrayName);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getNeighboringWordFeatures(
			Blob blob, Lattice lattice, int eqNo, String arrayName) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = arrayName + "_" + eqNo;
		List<QuantSpan> relevantQuantSpans = getRelevantQuantSpan(
				blob, lattice, eqNo, arrayName);
		for(QuantSpan qs : relevantQuantSpans) {
			int pos = blob.ta.getTokenIdFromCharacterOffset(qs.start);
			for(String feature : FeatureExtraction.getFormPP(blob.ta, pos, 2)) {
				features.add(prefix + "_" + feature);
			}
			for(String feature : FeatureExtraction.getMixed(blob.ta, blob.posTags, pos, 2)) {
				features.add(prefix + "_" + feature);
			}
			for(String feature : FeatureExtraction.getPOSWindowPP(blob.posTags, pos, 2)) {
				features.add(prefix + "_" + feature);
			}
		}
		return features;
	}
	
	public List<QuantSpan> getRelevantQuantSpan(
			Blob blob, Lattice lattice, int eqNo, String arrayName) {
		List<QuantSpan> relevantQuantSpans = new ArrayList<QuantSpan>();
		if(arrayName.equals("A1")) {
			for(QuantSpan qs : blob.clusterMap.get("E1")) {
				for(Pair<Operation, Double> pair : lattice.equations.get(eqNo).A1) {
					if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
						relevantQuantSpans.add(qs);
						break;
					}
				}
			}
		}
		if(arrayName.equals("A2")) {
			for(QuantSpan qs : blob.clusterMap.get("E1")) {
				for(Pair<Operation, Double> pair : lattice.equations.get(eqNo).A2) {
					if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
						relevantQuantSpans.add(qs);
						break;
					}
				}
			}
		}
		if(arrayName.equals("B1")) {
			for(QuantSpan qs : blob.clusterMap.get("E2")) {
				for(Pair<Operation, Double> pair : lattice.equations.get(eqNo).B1) {
					if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
						relevantQuantSpans.add(qs);
						break;
					}
				}
			}
		}
		if(arrayName.equals("B2")) {
			for(QuantSpan qs : blob.clusterMap.get("E2")) {
				for(Pair<Operation, Double> pair : lattice.equations.get(eqNo).B2) {
					if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
						relevantQuantSpans.add(qs);
						break;
					}
				}
			}
		}
		if(arrayName.equals("C")) {
			for(QuantSpan qs : blob.clusterMap.get("E3")) {
				for(Pair<Operation, Double> pair : lattice.equations.get(eqNo).C) {
					if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
						relevantQuantSpans.add(qs);
						break;
					}
				}
			}
		}
		return relevantQuantSpans;
	}
}