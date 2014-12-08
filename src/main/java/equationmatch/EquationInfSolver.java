package equationmatch;

import java.io.Serializable;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * Brute force search over all 64 possible eqns.
 * @author upadhya3
 *
 */
public class EquationInfSolver extends AbstractInferenceSolver 
implements Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private EquationFeatureExtractor featGen;
	
	public EquationInfSolver(EquationFeatureExtractor featGen) {
		this.featGen=featGen;
	}
	
	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return null;
	}

	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		Lattice l1 = (Lattice) arg1;
		Lattice l2 = (Lattice) arg2;
		if(l1.equals(l2))
		{
			return 0;
		}
		else
		{
//			System.out.println("LOSS!");
			return 1;
		}
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector arg0,
			IInstance arg1, IStructure arg2) throws Exception {
		return getBestStructure(arg0, arg1);
	}

}
