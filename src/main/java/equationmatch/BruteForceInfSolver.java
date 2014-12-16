package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class BruteForceInfSolver extends AbstractInferenceSolver 
implements Serializable {
	private static final long serialVersionUID = 5253748728743334706L;
	private EquationFeatureExtractor featGen;
	
	public BruteForceInfSolver(EquationFeatureExtractor featGen) {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector weight, IInstance ins) {
		return getLossAugmentedBestStructure(weight, ins, null);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) {
		Blob blob = (Blob) ins;
		Lattice gold = (Lattice) goldStructure;
		Lattice pred = null;
		Double predScore = -Double.MAX_VALUE, score;
		for(Lattice lattice : enumerateLattices(blob)) {
			score = weight.dotProduct(featGen.getFeatureVector(blob, lattice))*1.0
					+ (goldStructure == null ? 0 : getLoss(blob, gold, lattice));
			if(score > predScore) {
				predScore = score;
				pred = lattice;
			}
		}
		return pred;
	}

	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {
		Lattice l1 = (Lattice) gold;
		Lattice l2 = (Lattice) pred;
		return EquationInfSolver.getLoss(l1, l2);
	}

	public static List<Lattice> enumerateLattices(Blob blob) {
		List<Lattice> latticeList = new ArrayList<>();
		List<Equation> equationNumberList = 
				EquationInfSolver.enumerateEquationNumbers(blob);
		List<Equation> equationOpList = new ArrayList<>();
		for(Equation eq : equationNumberList) {
			equationOpList.addAll(
					EquationInfSolver.enumerateEquationOperations(eq));
		}
		for(int i=0; i<equationOpList.size(); ++i) {
			Equation eq1 = equationOpList.get(i);
			if(eq1.operations.get(0) == Operation.NONE || 
					eq1.operations.get(2) == Operation.NONE) {
				Equation eq = new Equation();
				eq.operations = Arrays.asList(
						Operation.NONE, Operation.NONE, Operation.NONE, 
						Operation.NONE, Operation.NONE);
				Lattice lattice = new Lattice(Arrays.asList(eq1, eq));
				latticeList.add(lattice);
				continue;
			}
			for(int j=i+1; j<equationOpList.size(); ++j) {
				Equation eq2 = equationOpList.get(j);
				Lattice lattice = new Lattice(Arrays.asList(eq1, eq2));
				latticeList.add(lattice);
			}
		}
		List<Lattice> newLatticeList = new ArrayList<>();
		for(Lattice lattice : latticeList) {
			if(!EquationInfSolver.isAllNumbersUsed(lattice, blob)) continue;
			if(EquationSolver.solve(lattice) == null) continue;
			newLatticeList.add(lattice);
		}
		return newLatticeList;
	}

}
