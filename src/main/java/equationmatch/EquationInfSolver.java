package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class EquationInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private EquationFeatureExtractor featGen;

	public EquationInfSolver(EquationFeatureExtractor featGen) {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}

	public static float getOperationLoss(Lattice l1, Lattice l2, int eqNo) {
		float loss = 0.0f;
		Equation eq1 = l1.equations.get(eqNo);
		Equation eq2 = l2.equations.get(eqNo);
		for (int i = 0; i < 5; i++) {
			if (eq1.operations.get(i) != eq2.operations.get(i)) {
				loss += 1.0;
			}
		}
		return loss;
	}

	public static float getNumberLoss(Lattice l1, Lattice l2, int eqNo) {
		float loss = 0.0f;
		Equation eq1 = l1.equations.get(eqNo);
		Equation eq2 = l2.equations.get(eqNo);
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
						&& Tools.safeEquals(pair1.getSecond(),
								pair2.getSecond())) {
					found = true;
					break;
				}
			}
			if (!found)
				loss += 1.0;
		}
		for (Pair<Operation, Double> pair2 : list2) {
			boolean found = false;
			for (Pair<Operation, Double> pair1 : list1) {
				if (pair1.getFirst() == pair2.getFirst()
						&& Tools.safeEquals(pair1.getSecond(),
								pair2.getSecond())) {
					found = true;
					break;
				}
			}
			if (!found)
				loss += 1.0;
		}
		return loss;
	}

	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		Lattice l1 = (Lattice) arg1;
		Lattice l2 = (Lattice) arg2;
		float loss = 0.0f;
		for (int i = 0; i < 2; ++i) {
			loss += getNumberLoss(l1, l2, i);
			loss += getOperationLoss(l1, l2, i);
		}
		return loss;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		Blob blob = (Blob) x;
		Lattice gold = (Lattice) goldStructure;
		Lattice prediction = null;
		List<Pair<Lattice, Double>> tmpLatticeList = 
				new ArrayList<Pair<Lattice, Double>>();
		PairComparator<Lattice> latticePairComparator = new PairComparator<Lattice>() {
		};
		BoundedPriorityQueue<Pair<Lattice, Double>> beam = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(
				50, latticePairComparator);
		tmpLatticeList.add(new Pair<Lattice, Double>(new Lattice(), 0.0));
		beam.clear();
		Iterator<Pair<Lattice, Double>> it;
		// Enumerate all equations
		for (int i = 0; i < 2; i++) {
			for (Pair<Lattice, Double> pair : tmpLatticeList) {
				for (Lattice lattice : enumerateEquationNumbers(
						pair.getFirst(), i, blob)) {
					beam.add(new Pair<Lattice, Double>(lattice, 
							pair.getSecond()
							+ (goldStructure == null ? 0 : getNumberLoss(lattice, gold, i))
							+ wv.dotProduct(featGen.getNumberFeatureVector(
									blob, lattice, i))));
				}
			}
			tmpLatticeList.clear();
			it = beam.iterator();
			for (; it.hasNext();) {
				tmpLatticeList.add(it.next());
			}
			System.out.println("Beam Size After Number Enumeration: "+beam.size());
			beam.clear();
			for (Pair<Lattice, Double> pair : tmpLatticeList) {
				for (Lattice lattice : enumerateEquationOperations(
						pair.getFirst(), i, blob)) {
					beam.add(new Pair<Lattice, Double>(lattice, 
							pair.getSecond()
							+ (goldStructure == null ? 0 : getOperationLoss(lattice, gold, i))
							+ wv.dotProduct(featGen.getNumberFeatureVector(
									blob, lattice, i))));
				}
			}
			tmpLatticeList.clear();
			it = beam.iterator();
			for (; it.hasNext();) {
				tmpLatticeList.add(it.next());
			}
			System.out.println("Beam Size After Operation Enumeration: "+beam.size());
			prediction = beam.element().getFirst();
			beam.clear();
		}
		return prediction;
	}

	public List<Lattice> enumerateEquationNumbers(Lattice startSeed, int eqNo,
			Blob blob) {
		List<Lattice> seedList = new ArrayList<>();
		seedList.add(startSeed);
		List<Lattice> latticeList = new ArrayList<>();
		System.out.println("E1Size : "+blob.clusterMap.get("E1").size());
		for (Lattice seed : seedList) {
			for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E1"))) {
				Lattice lattice = new Lattice(seed);
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).A1.add(new Pair<Operation, Double>(
						Operation.MUL, d));
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).A1.add(new Pair<Operation, Double>(
						Operation.DIV, d));
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).A2.add(new Pair<Operation, Double>(
						Operation.MUL, d));
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).A2.add(new Pair<Operation, Double>(
						Operation.DIV, d));
				latticeList.add(lattice);
			}
		}
		System.out.println("LatticeListSize : "+latticeList.size());
		if(latticeList.size() > 0) { 
			seedList.clear();
			seedList.addAll(latticeList);
			latticeList.clear();
		}
		System.out.println("E2Size : "+blob.clusterMap.get("E2").size());
		for (Lattice seed : seedList) {
			for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E2"))) {
				Lattice lattice = new Lattice(seed);
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).B1.add(new Pair<Operation, Double>(
						Operation.MUL, d));
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).B1.add(new Pair<Operation, Double>(
						Operation.DIV, d));
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).B2.add(new Pair<Operation, Double>(
						Operation.MUL, d));
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).B2.add(new Pair<Operation, Double>(
						Operation.DIV, d));
				latticeList.add(lattice);
			}
		}
		System.out.println("LatticeListSize : "+latticeList.size());
		if(latticeList.size() > 0) { 
			seedList.clear();
			seedList.addAll(latticeList);
			latticeList.clear();
		}
		System.out.println("E3Size : "+blob.clusterMap.get("E3").size());
		for (Lattice seed : seedList) {
			for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E3"))) {
				Lattice lattice = new Lattice(seed);
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).C.add(new Pair<Operation, Double>(
						Operation.MUL, d));
				latticeList.add(lattice);
				lattice = new Lattice(seed);
				lattice.equations.get(eqNo).C.add(new Pair<Operation, Double>(
						Operation.DIV, d));
				latticeList.add(lattice);
			}
		}
		System.out.println("LatticeListSize : "+latticeList.size());
		if(latticeList.size() > 0) { 
			seedList.clear();
			seedList.addAll(latticeList);
			latticeList.clear();
		}
		return seedList;
	}

	public List<Lattice> enumerateEquationOperations(Lattice startSeed,
			int eqNo, Blob blob) {
		List<Lattice> seedList = new ArrayList<>();
		seedList.add(startSeed);
		List<Lattice> latticeList = new ArrayList<>();
		for (Lattice seed : seedList) {
			Equation eq = seed.equations.get(eqNo);
			List<Operation> one = Arrays.asList(Operation.ADD, Operation.SUB,
					Operation.MUL, Operation.DIV, Operation.NONE);
			List<Operation> three = Arrays.asList(Operation.ADD, Operation.SUB,
					Operation.MUL, Operation.DIV, Operation.NONE);
			List<Operation> two = null, four = null, five = null;
			if (eq.A2.size() == 0) {
				two = Arrays.asList(Operation.NONE);
			} else {
				two = Arrays.asList(Operation.ADD, Operation.SUB);
			}
			if (eq.B2.size() == 0) {
				four = Arrays.asList(Operation.NONE);
			} else {
				four = Arrays.asList(Operation.ADD, Operation.SUB);
			}
			if (eq.C.size() == 0) {
				five = Arrays.asList(Operation.NONE);
			} else {
				five = Arrays.asList(Operation.SUB);
			}
			for (Operation op1 : one) {
				for (Operation op2 : two) {
					for (Operation op3 : three) {
						for (Operation op4 : four) {
							for (Operation op5 : five) {
								Lattice lattice = new Lattice(seed);
								Equation e = lattice.equations.get(eqNo);
								e.operations.set(0, op1);
								e.operations.set(1, op2);
								e.operations.set(2, op3);
								e.operations.set(3, op4);
								e.operations.set(4, op5);
								latticeList.add(lattice);
							}
						}
					}
				}
			}
		}
		// Filtering
		List<Lattice> newLatticeList = new ArrayList<>();
		for (Lattice lattice : latticeList) {
			Equation eq = lattice.equations.get(eqNo);
			if (eq.operations.get(0) == Operation.MUL
					&& eq.operations.get(2) != Operation.DIV) {
				continue;
			}
			if (eq.operations.get(0) == Operation.DIV
					&& eq.operations.get(2) != Operation.MUL) {
				continue;
			}
			if (eq.operations.get(2) == Operation.MUL
					&& eq.operations.get(0) != Operation.DIV) {
				continue;
			}
			if (eq.operations.get(2) == Operation.DIV
					&& eq.operations.get(0) != Operation.MUL) {
				continue;
			}
			if (eq.operations.get(0) == Operation.NONE
					&& eq.operations.get(2) == Operation.NONE) {
				continue;
			}
			newLatticeList.add(lattice);
		}
		return newLatticeList;
	}

	private boolean fullEqSystemValidity(Lattice lattice) {
		if (EquationSolver.solve(lattice) == null) {
			return false;
		}
		if (lattice.equations.get(0).operations.get(0) == Operation.NONE
				|| lattice.equations.get(0).operations.get(2) == Operation.NONE) {
			for (Operation op : lattice.equations.get(1).operations) {
				if (op != Operation.NONE)
					return false;
			}
		}
		// if(i==1 && !isAllNumbersUsed(lattice, blob)) return false;
		return true;
	}

	private boolean isAllNumbersUsed(Lattice lattice, Blob blob) {
		for (String entity : blob.clusterMap.keySet()) {
			for (QuantSpan qs : blob.clusterMap.get(entity)) {
				boolean found = false;
				if (entity.equals("E1")) {
					for (int j = 0; j < lattice.equations.size(); ++j) {
						Equation e = lattice.equations.get(j);
						for (Pair<Operation, Double> pair : e.A1) {
							if (Tools.safeEquals(pair.getSecond(),
									Tools.getValue(qs))) {
								found = true;
							}
						}
						for (Pair<Operation, Double> pair : e.A2) {
							if (Tools.safeEquals(pair.getSecond(),
									Tools.getValue(qs))) {
								found = true;
							}
						}
					}
				}
				if (entity.equals("E2")) {
					for (int j = 0; j < lattice.equations.size(); ++j) {
						Equation e = lattice.equations.get(j);
						for (Pair<Operation, Double> pair : e.B1) {
							if (Tools.safeEquals(pair.getSecond(),
									Tools.getValue(qs))) {
								found = true;
							}
						}
						for (Pair<Operation, Double> pair : e.B2) {
							if (Tools.safeEquals(pair.getSecond(),
									Tools.getValue(qs))) {
								found = true;
							}
						}
					}
				}
				if (entity.equals("E3")) {
					for (int j = 0; j < lattice.equations.size(); ++j) {
						Equation e = lattice.equations.get(j);
						for (Pair<Operation, Double> pair : e.C) {
							if (Tools.safeEquals(pair.getSecond(),
									Tools.getValue(qs))) {
								found = true;
							}
						}
					}
				}
				if (!found)
					return false;
			}
		}
		return true;
	}
}
