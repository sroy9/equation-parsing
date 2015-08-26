package relevance;

import java.io.Serializable;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class RelInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private RelFeatGen featGen;

	public RelInfSolver(RelFeatGen featGen) 
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
		RelY r1 = (RelY) arg1;
		RelY r2 = (RelY) arg2;
		return RelY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		RelX prob = (RelX) x;
		double bestScore = -Double.MAX_VALUE;
		int relation = -1;
		for(int i=0; i<4; ++i) {
			double score = wv.dotProduct(featGen.getFeatureVector(prob, new RelY(i)));
			if(score > bestScore) {
				bestScore = score;
				relation = i;
			}
		}
		return new RelY(relation);
	}
	
}
