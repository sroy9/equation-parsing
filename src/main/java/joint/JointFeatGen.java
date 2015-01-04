package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import relation.RelationFeatGen;
import relation.RelationInfSolver;
import semparse.SemFeatGen;
import semparse.SemX;
import utils.FeatGen;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class JointFeatGen extends AbstractFeatureGenerator implements 
		Serializable {

	private static final long serialVersionUID = -3474103412622043440L;
	public Lexiconer lm = null;
	
	public JointFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
		JointX jointX = (JointX) x;
		JointY jointY = (JointY) y;
		List<String> features = getFeatures(jointX, jointY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public List<String> getFeatures(JointX x, JointY y) {
		List<String> features = new ArrayList<>();
		features.add("IsOneVar_"+y.isOneVar);
		List<SemX> semXs = SemX.extractEquationProbFromRelations(x.relationX, y.relationY);
		assert semXs.size() == y.semYs.size();
		for(int i=0; i<semXs.size(); ++i) {
			features.addAll(SemFeatGen.getFeatures(semXs.get(i), y.semYs.get(i)));	
		}
		features.addAll(solutionFeatures(x, y));
		return features;
	}

	public List<String> solutionFeatures(JointX x, JointY y) {
		List<String> features = new ArrayList<>();
		if(y.solns == null) {
			features.add("No_Solution");
			return features;
		}
		for(Double d : y.solns) {
			if(d>1) features.add("Positive>1");
			if(d-d.intValue() < 0.001 || d-d.intValue() > 0.999){
				features.add("Integer");
			}
		}
		return features;
	}
}
