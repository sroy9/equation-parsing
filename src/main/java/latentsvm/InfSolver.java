package latentsvm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class InfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private FeatureExtractor featGen;

	public InfSolver(FeatureExtractor featGen) {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}

	public static float getOperationLoss(Equation eq1, Equation eq2) {
		float loss = 0.0f;
		for (int i = 0; i < 4; i++) {
			if (eq1.operations.get(i) != eq2.operations.get(i)) {
				loss += 1.0;
			}
		}
		return loss;
	}

	public static float getNumberLoss(Equation eq1, Equation eq2) {
		float loss = 0.0f;
		loss += getSymmetricDifference(eq1.A1, eq2.A1);
		loss += getSymmetricDifference(eq1.A2, eq2.A2);
		loss += getSymmetricDifference(eq1.B1, eq2.B1);
		loss += getSymmetricDifference(eq1.B2, eq2.B2);
		loss += getSymmetricDifference(eq1.C, eq2.C);
		return loss;
	}

	public static float getSymmetricDifference(List<Pair<Operation, Double>> list1,
			List<Pair<Operation, Double>> list2) {
		float loss = 0.0f;
		for (Pair<Operation, Double> pair1 : list1) {
			boolean found = false;
			for (Pair<Operation, Double> pair2 : list2) {
				if (pair1.getFirst() == pair2.getFirst()
						&& Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) {
					found = true;
					break;
				}
			}
			if (!found) loss += 1.0;
		}
		for (Pair<Operation, Double> pair2 : list2) {
			boolean found = false;
			for (Pair<Operation, Double> pair1 : list1) {
				if (pair1.getFirst() == pair2.getFirst()
						&& Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) {
					found = true;
					break;
				}
			}
			if (!found) loss += 1.0;
		}
		return loss;
	}

	public static float getLoss(Lattice l1, Lattice l2) {
		Equation eq11 = l1.equations.get(0);
		Equation eq12 = l1.equations.get(1);
		Equation eq21 = l2.equations.get(0);
		Equation eq22 = l2.equations.get(1);
		float loss1 = getNumberLoss(eq11, eq21) + getOperationLoss(eq11, eq21) 
				+ getNumberLoss(eq12, eq22) + getOperationLoss(eq12, eq22);
		float loss2 = getNumberLoss(eq11, eq22) + getOperationLoss(eq11, eq22) 
				+ getNumberLoss(eq12, eq21) + getOperationLoss(eq12, eq21);
		return Math.min(loss1, loss2);
	}
	
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		Lattice l1 = (Lattice) arg1;
		Lattice l2 = (Lattice) arg2;
		return getLoss(l1, l2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		Blob blob = (Blob) x;
		Lattice gold = (Lattice) goldStructure;
		Lattice prediction = null;
		int beamSize = 10;
		PairComparator<Lattice> latticePairComparator = new PairComparator<Lattice>() {
		};
		return prediction;
	}

	public static boolean isAllNumbersUsed(Lattice lattice, Blob blob) {
		for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E1"))) {
			boolean found = false;
			for (int j = 0; j < 2; ++j) {
				Equation e = lattice.equations.get(j);
				for (Pair<Operation, Double> pair : e.A1) {
					if (Tools.safeEquals(pair.getSecond(), d)) {
						found = true;
					}
				}
				for (Pair<Operation, Double> pair : e.A2) {
					if (Tools.safeEquals(pair.getSecond(), d)) {
						found = true;
					}
				}
			}
			if(!found) return false;
		}
		for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E2"))) {
			boolean found = false;
			for (int j = 0; j < 2; ++j) {
				Equation e = lattice.equations.get(j);
				for (Pair<Operation, Double> pair : e.B1) {
					if (Tools.safeEquals(pair.getSecond(), d)) {
						found = true;
					}
				}
				for (Pair<Operation, Double> pair : e.B2) {
					if (Tools.safeEquals(pair.getSecond(), d)) {
						found = true;
					}
				}
			}
			if(!found) return false;
		}
		for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E3"))) {
			boolean found = false;
			for (int j = 0; j < 2; ++j) {
				Equation e = lattice.equations.get(j);
				for (Pair<Operation, Double> pair : e.C) {
					if (Tools.safeEquals(pair.getSecond(), d)) {
						found = true;
					}
				}
			}
			if(!found) return false;
		}		
		return true;
	}
}
