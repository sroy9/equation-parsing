package equationmatch;

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

	public static float getOperationLoss(Equation eq1, Equation eq2) {
		float loss = 0.0f;
		for (int i = 0; i < 5; i++) {
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
		int beamSize = 50, opFromEach = 5;
		PairComparator<Lattice> latticePairComparator = new PairComparator<Lattice>() {
		};
		PairComparator<Equation> equationPairComparator = new PairComparator<Equation>() {
		};
		BoundedPriorityQueue<Pair<Equation, Double>> equationNumberBeam = 
				new BoundedPriorityQueue<Pair<Equation, Double>>(beamSize, equationPairComparator);
		BoundedPriorityQueue<Pair<Equation, Double>> equationOperationBeam = 
				new BoundedPriorityQueue<Pair<Equation, Double>>(beamSize, equationPairComparator);
		BoundedPriorityQueue<Pair<Lattice, Double>> latticeBeam = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(beamSize, latticePairComparator);
		
		// Enumerate all equations
		for (Equation equation : enumerateEquationNumbers(blob)) {
			equationNumberBeam.add(new Pair<Equation, Double>(equation,
					1.0*wv.dotProduct(featGen.getNumberFeatureVector(blob, equation))));
		}
//		System.out.println("EquationNumberBeam : "+equationNumberBeam.size());
		
		for (Pair<Equation, Double> pair : equationNumberBeam) {
			BoundedPriorityQueue<Pair<Equation, Double>> opBeam = 
					new BoundedPriorityQueue<Pair<Equation, Double>>(opFromEach, equationPairComparator);
			for (Equation equation : enumerateEquationOperations(pair.getFirst(), blob)) {
				opBeam.add(new Pair<Equation, Double>(equation, 
						pair.getSecond() + 
						wv.dotProduct(featGen.getOperationFeatureVector(blob, equation))));
			}
			equationOperationBeam.addAll(opBeam);
		}
//		System.out.println("EquationOperationBeam : "+equationOperationBeam.size());
		
		// Enumerate all lattices
		for(Pair<Equation, Double> pair1 : equationOperationBeam) {
			if(pair1.getFirst().operations.get(0) == Operation.NONE || 
					pair1.getFirst().operations.get(2) == Operation.NONE) {
				Equation eq = new Equation();
				eq.operations = Arrays.asList(Operation.NONE, Operation.NONE, Operation.NONE, 
						Operation.NONE, Operation.NONE);
				Lattice lattice = new Lattice(Arrays.asList(pair1.getFirst(), eq));
				latticeBeam.add(new Pair<Lattice, Double>(lattice, 
						pair1.getSecond()+
						wv.dotProduct(featGen.getLatticeFeatureVector(blob, lattice))));
				continue;
			}
			for(Pair<Equation, Double> pair2 : equationOperationBeam) {
				if(pair1.equals(pair2)) continue;
				Lattice lattice = new Lattice(Arrays.asList(pair1.getFirst(), pair2.getFirst()));
				latticeBeam.add(new Pair<Lattice, Double>(lattice, 
						pair1.getSecond()+pair2.getSecond()+
						wv.dotProduct(featGen.getLatticeFeatureVector(blob, lattice))));
			}
		}
//		System.out.println("LatticeBeam : "+latticeBeam.size());
		prediction = latticeBeam.element().getFirst();
		return prediction;
	}

	public List<Equation> enumerateEquationNumbers(Blob blob) {
		List<Equation> seedList = new ArrayList<>();
		seedList.add(new Equation());
		List<Equation> equationList = new ArrayList<>();
		for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E1"))) {
			for (Equation seed : seedList) {
				Equation eq = new Equation(seed);
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.A1.add(new Pair<Operation, Double>(Operation.MUL, d));
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.A1.add(new Pair<Operation, Double>(Operation.DIV, d));
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.A1.add(new Pair<Operation, Double>(Operation.MUL, d));
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.A1.add(new Pair<Operation, Double>(Operation.DIV, d));
				equationList.add(eq);
			}
			seedList.clear();
			seedList.addAll(equationList);
			equationList.clear();
		}
		for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E2"))) {
			for (Equation seed : seedList) {
				Equation eq = new Equation(seed);
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.B1.add(new Pair<Operation, Double>(Operation.MUL, d));
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.B1.add(new Pair<Operation, Double>(Operation.DIV, d));
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.B2.add(new Pair<Operation, Double>(Operation.MUL, d));
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.B2.add(new Pair<Operation, Double>(Operation.DIV, d));
				equationList.add(eq);
			}
			seedList.clear();
			seedList.addAll(equationList);
			equationList.clear();
		}
		for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E3"))) {
			for (Equation seed : seedList) {
				Equation eq = new Equation(seed);
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.C.add(new Pair<Operation, Double>(Operation.MUL, d));
				equationList.add(eq);
				
				eq = new Equation(seed);
				eq.C.add(new Pair<Operation, Double>(Operation.DIV, d));
				equationList.add(eq);
			}
			seedList.clear();
			seedList.addAll(equationList);
			equationList.clear();
		}
		return seedList;
	}

	public List<Equation> enumerateEquationOperations(Equation eq, Blob blob) {
		List<Equation> equationList = new ArrayList<>();
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
							Equation e = new Equation(eq);
							e.operations.set(0, op1);
							e.operations.set(1, op2);
							e.operations.set(2, op3);
							e.operations.set(3, op4);
							e.operations.set(4, op5);
							equationList.add(e);
						}
					}
				}
			}
		}
//		System.out.println("LatticeList Before filtering : "+latticeList.size());
		// Filtering
		List<Equation> newEquationList = new ArrayList<>();
		for (Equation e : equationList) {
			if (e.operations.get(0) == Operation.MUL
					&& e.operations.get(2) != Operation.DIV) {
				continue;
			}
			if (e.operations.get(0) == Operation.DIV
					&& e.operations.get(2) != Operation.MUL) {
				continue;
			}
			if (e.operations.get(2) == Operation.MUL
					&& e.operations.get(0) != Operation.DIV) {
				continue;
			}
			if (e.operations.get(2) == Operation.DIV
					&& e.operations.get(0) != Operation.MUL) {
				continue;
			}
			if (e.operations.get(0) == Operation.NONE
					&& e.operations.get(2) == Operation.NONE) {
				continue;
			}
			newEquationList.add(e);
		}
//		System.out.println("LatticeList After filtering : "+newLatticeList.size());
		return newEquationList;
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
