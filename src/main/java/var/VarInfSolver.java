package var;

import java.io.Serializable;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class VarInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private VarFeatGen featGen;

	public VarInfSolver(VarFeatGen featGen) 
			throws Exception {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		VarY r1 = (VarY) arg1;
		VarY r2 = (VarY) arg2;
		return VarY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		VarX prob = (VarX) x;
		double scoreTrue = wv.dotProduct(featGen.getFeatureVector(prob, new VarY(true)));
		double scoreFalse = wv.dotProduct(featGen.getFeatureVector(prob, new VarY(false)));
		if(scoreTrue > scoreFalse) {
			return new VarY(true);
		} else {
			return new VarY(false);
		}
	}
	
}
