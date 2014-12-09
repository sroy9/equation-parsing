package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Equation;
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
		// Enumerate all positions
		for(int i=0; i<lattice.equations.size(); ++i) {
			Equation eq = lattice.equations.get(i);
			try {
				features.addAll(getFeatures(blob, lattice, i, "A1"));
				features.addAll(getFeatures(blob, lattice, i, "A2"));
				features.addAll(getFeatures(blob, lattice, i, "B1"));
				features.addAll(getFeatures(blob, lattice, i, "B2"));
				features.addAll(getFeatures(blob, lattice, i, "C"));
				features.addAll(getFeatures(blob, lattice, i, "Op"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	public List<String> getFeatures(
			Blob blob, Lattice l, int eqNo, String arrayName) 
					throws Exception {
		List<String> feats = new ArrayList<>();
		// All feature functions go here
		return feats;
	}

	public IFeatureVector getFeaturesVector(
			Blob blob, Lattice lattice, int eqNo, String arrayName) {
		List<String> feats = new ArrayList<>();
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
}