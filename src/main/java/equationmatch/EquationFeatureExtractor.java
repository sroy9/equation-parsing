package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Operation;
import utils.FeatureExtraction;
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
		try {
			features.addAll(extractFeatures(blob, lattice));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	private List<String> extractFeatures(Blob blob, Lattice l) throws Exception {
		List<String> feats = new ArrayList<>();
		feats.addAll(getStructureFeatures(blob, l));
		feats.addAll(getGlobalFeatures(blob, l));
		return feats;
	}

	public List<String> getStructureFeatures(Blob blob, Lattice l)
			throws Exception {
		List<String> features = new ArrayList<>();
		return features;
	}

	public List<String> getGlobalFeatures(Blob blob, Lattice l)
			throws Exception {
		List<String> features = new ArrayList<>();
		for (String feature : FeatureExtraction
				.getUnigrams(blob.simulProb.question)) {
			features.add("Unigram_" + feature);
		}
		for (String feature : FeatureExtraction
				.getBigrams(blob.simulProb.question)) {
			features.add("Bigram_" + feature);
		}
		return features;
	}

	Operation getOperation(Blob blob, String path1) {
		String[] parts = path1.split("_");
		return Operation.valueOf(parts[1]);
	}
}