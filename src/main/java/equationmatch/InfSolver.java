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
		int beamSize = 10;
		PairComparator<Lattice> latticePairComparator = new PairComparator<Lattice>() {
		};
		BoundedPriorityQueue<Pair<Lattice, Double>> latticeE1Beam = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(beamSize, latticePairComparator);
		BoundedPriorityQueue<Pair<Lattice, Double>> latticeE2Beam = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(beamSize, latticePairComparator);
		BoundedPriorityQueue<Pair<Lattice, Double>> latticeE3Beam = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(beamSize, latticePairComparator);
		BoundedPriorityQueue<Pair<Lattice, Double>> latticeOpBeam = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(beamSize, latticePairComparator);
		
		for(Lattice lattice : enumerateE1(blob, new Lattice())) {
			latticeE1Beam.add(new Pair<Lattice, Double>(
					lattice, wv.dotProduct(featGen.getE1FeatureVector(blob, lattice))*1.0));
		}
		for (Pair<Lattice, Double> pair : latticeE1Beam) {
			for (Lattice lattice : enumerateE2(blob, pair.getFirst())) {
				latticeE2Beam.add(new Pair<Lattice, Double>(lattice, 
						pair.getSecond() + 
						wv.dotProduct(featGen.getE2FeatureVector(blob, lattice))));
			}
		}
		for (Pair<Lattice, Double> pair : latticeE2Beam) {
			for (Lattice lattice : enumerateE3(blob, pair.getFirst())) {
				latticeE3Beam.add(new Pair<Lattice, Double>(lattice, 
						pair.getSecond() + 
						wv.dotProduct(featGen.getE3FeatureVector(blob, lattice))));
			}
		}
		for (Pair<Lattice, Double> pair : latticeE3Beam) {
			for (Lattice lattice : enumerateLatticeOperations(pair.getFirst())) {
				latticeOpBeam.add(new Pair<Lattice, Double>(lattice, 
						pair.getSecond() + 
						wv.dotProduct(featGen.getOperationFeatureVector(blob, lattice))));
			}
		}
		prediction = latticeOpBeam.element().getFirst();
		return prediction;
	}

	public static List<Lattice> enumerateE1(Blob blob, Lattice startSeed) {
		List<Lattice> seedList = new ArrayList<Lattice>();
		seedList.add(startSeed);
		List<Lattice> latticeList = new ArrayList<>();
		for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E1"))) {
			for(int i=0; i<2; i++) {
				for (Lattice seed : seedList) {
					Lattice lattice = new Lattice(seed);
					latticeList.add(lattice);
					
					lattice = new Lattice(seed);
					lattice.equations.get(i).A1.add(
							new Pair<Operation, Double>(Operation.MUL, d));
					latticeList.add(lattice);
					
					lattice = new Lattice(seed);
					lattice.equations.get(i).A1.add(
							new Pair<Operation, Double>(Operation.DIV, d));
					latticeList.add(lattice);
					
					lattice = new Lattice(seed);
					lattice.equations.get(i).A2.add(
							new Pair<Operation, Double>(Operation.MUL, d));
					latticeList.add(lattice);
					
					lattice = new Lattice(seed);
					lattice.equations.get(i).A2.add(
							new Pair<Operation, Double>(Operation.DIV, d));
					latticeList.add(lattice);
				}
				seedList.clear();
				seedList.addAll(latticeList);
				latticeList.clear();
			}
			for(Lattice seed : seedList) {
				if(FeatureExtractor.isPresent(d, "E1", seed.equations.get(0)) ||
						FeatureExtractor.isPresent(d, "E1", seed.equations.get(1))) {
					latticeList.add(seed);
				}
			}
			seedList.clear();
			seedList.addAll(latticeList);
			latticeList.clear();
		}
		return seedList;
	}
	
	public static List<Lattice> enumerateE2(Blob blob, Lattice startSeed) {
		List<Lattice> seedList = new ArrayList<Lattice>();
		seedList.add(startSeed);
		List<Lattice> latticeList = new ArrayList<>();
		for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E2"))) {
			for(int i=0; i<2; i++) {
				for (Lattice seed : seedList) {
					Lattice lattice = new Lattice(seed);
					latticeList.add(lattice);
					lattice = new Lattice(seed);
					lattice.equations.get(i).B1.add(
							new Pair<Operation, Double>(Operation.MUL, d));
					latticeList.add(lattice);
					
					lattice = new Lattice(seed);
					lattice.equations.get(i).B1.add(
							new Pair<Operation, Double>(Operation.DIV, d));
					latticeList.add(lattice);
					
					lattice = new Lattice(seed);
					lattice.equations.get(i).B2.add(
							new Pair<Operation, Double>(Operation.MUL, d));
					latticeList.add(lattice);
					
					lattice = new Lattice(seed);
					lattice.equations.get(i).B2.add(
							new Pair<Operation, Double>(Operation.DIV, d));
					latticeList.add(lattice);
				}
				seedList.clear();
				seedList.addAll(latticeList);
				latticeList.clear();
			}
			for(Lattice seed : seedList) {
				if(FeatureExtractor.isPresent(d, "E2", seed.equations.get(0)) ||
						FeatureExtractor.isPresent(d, "E2", seed.equations.get(1))) {
					latticeList.add(seed);
				}
			}
			seedList.clear();
			seedList.addAll(latticeList);
			latticeList.clear();
		}
		return seedList;
	}
	
	public static List<Lattice> enumerateE3(Blob blob, Lattice startSeed) {
		List<Lattice> seedList = new ArrayList<Lattice>();
		seedList.add(startSeed);
		List<Lattice> latticeList = new ArrayList<>();
		for (Double d : Tools.uniqueNumbers(blob.clusterMap.get("E3"))) {
			for(int i=0; i<2; i++) {
				for (Lattice seed : seedList) {
					Lattice lattice = new Lattice(seed);
					latticeList.add(lattice);
					
					lattice = new Lattice(seed);
					lattice.equations.get(i).C.add(
							new Pair<Operation, Double>(Operation.MUL, d));
					latticeList.add(lattice);
					
					lattice = new Lattice(seed);
					lattice.equations.get(i).C.add(
							new Pair<Operation, Double>(Operation.DIV, d));
					latticeList.add(lattice);
				}
				seedList.clear();
				seedList.addAll(latticeList);
				latticeList.clear();
			}
			for(Lattice seed : seedList) {
				if(FeatureExtractor.isPresent(d, "E3", seed.equations.get(0)) ||
						FeatureExtractor.isPresent(d, "E3", seed.equations.get(1))) {
					latticeList.add(seed);
				}
			}
			seedList.clear();
			seedList.addAll(latticeList);
			latticeList.clear();
		}
		return seedList;
	}

	public List<Lattice> enumerateLatticeOperations(Lattice seed) {
		List<Lattice> latticeList = new ArrayList<>();
		for(Equation eq1 : enumerateEquationOperations(seed.equations.get(0))) {
			if(eq1.operations.get(0) == Operation.NONE || 
					eq1.operations.get(2) == Operation.NONE) {
				Equation eq2 = new Equation(seed.equations.get(1));
				if(eq2.A1.size() == 0  && eq2.A2.size() == 0  && eq2.B1.size() == 0  && 
						eq2.B2.size() == 0  && eq2.C.size() == 0) {
					eq2.operations = Arrays.asList(Operation.NONE, Operation.NONE, 
							Operation.NONE, Operation.NONE, Operation.NONE);
					Lattice lattice = new Lattice(Arrays.asList(eq1, eq2));
					if(EquationSolver.solve(lattice) != null) {
						latticeList.add(lattice);
					}
				}
				continue;
			}
			for(Equation eq2 : enumerateEquationOperations(seed.equations.get(1))) {
				Lattice lattice = new Lattice(Arrays.asList(eq1, eq2));
				if(EquationSolver.solve(lattice) != null) {
					latticeList.add(lattice);
				}
			}
		}
		return latticeList;
	}
	
	public static List<Equation> enumerateEquationOperations(Equation eq) {
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
