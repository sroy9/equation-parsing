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
		return getLossAugmentedBestStructure(wv, x, null);
	}

	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		Lattice l1 = (Lattice) arg1;
		Lattice l2 = (Lattice) arg2;
		float loss = 0.0f;
		for(int i=0; i<2; ++i) {
			Equation eq1 = l1.equations.get(i);
			Equation eq2 = l2.equations.get(i);
			for(Pair<Operation, Double> pair : eq1.A1) {
				if(!eq2.A1.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq1.A2) {
				if(!eq2.A2.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq1.B1) {
				if(!eq2.B1.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq1.B2) {
				if(!eq2.B2.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq1.C) {
				if(!eq2.C.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.A1) {
				if(!eq1.A1.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.A2) {
				if(!eq1.A2.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.B1) {
				if(!eq1.B1.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.B2) {
				if(!eq1.B2.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.C) {
				if(!eq1.C.contains(pair)) loss = loss + 1;
			}
			for(int j=0; j<5; j++) {
				if(eq1.operations.get(j) != eq2.operations.get(j)) {
					loss = loss + 1;
				}
			}
		}
		return loss;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector wv, IInstance arg1, IStructure arg2) throws Exception {
		Blob blob = (Blob) arg1;
		Lattice gold = (Lattice) arg2;
		Map<String, List<QuantSpan>> clusterMap = blob.simulProb.clusterMap;
		List<Pair<Lattice, Double>> tmpLatticeList = 
				new ArrayList<Pair<Lattice, Double>>();
		PairComparator<Lattice> latticePairComparator = new PairComparator<Lattice>() {
		};
		BoundedPriorityQueue<Pair<Lattice, Double>> beam = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(
						100, latticePairComparator);
		beam.add(new Pair<Lattice, Double>(new Lattice(), 0.0));
		// Enumerate all equations
		for(int i = 0; i < 2; i++) {
			// Transfer states from beam to tmpLatticeList
			Iterator<Pair<Lattice, Double>> it = beam.iterator();
			tmpLatticeList.clear();
			for(;it.hasNext();) {
				tmpLatticeList.add(it.next());
			}
			beam.clear();
			for(QuantSpan qs : clusterMap.get("E1")) {
				if(!occurTwice(qs, clusterMap.get("E1"))) continue;
				for(Pair<Lattice, Double> pair : tmpLatticeList) {
					Lattice tmpLattice = new Lattice(pair.getFirst());
					beam.add(new Pair<>(tmpLattice, pair.getSecond()));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A1.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A1", 
									new Pair<Operation, Double>(
									Operation.MUL, Tools.getValue(qs))))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A1.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A1", 
									new Pair<Operation, Double>(
									Operation.DIV, Tools.getValue(qs))))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A2.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A2", 
									new Pair<Operation, Double>(
									Operation.MUL, Tools.getValue(qs))))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A2.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A2", 
									new Pair<Operation, Double>(
									Operation.DIV, Tools.getValue(qs))))));
				}
				it = beam.iterator();
				tmpLatticeList.clear();
				for(;it.hasNext();) {
					tmpLatticeList.add(it.next());
				}
				beam.clear();
			}

			for(QuantSpan qs : clusterMap.get("E2")) {
				if(!occurTwice(qs, clusterMap.get("E2"))) continue;
				for(Pair<Lattice, Double> pair : tmpLatticeList) {
					Lattice tmpLattice = new Lattice(pair.getFirst());
					beam.add(new Pair<>(tmpLattice, pair.getSecond()));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B1.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B1", new Pair<Operation, Double>(
									Operation.MUL, Tools.getValue(qs))))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B1.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B1", new Pair<Operation, Double>(
									Operation.DIV, Tools.getValue(qs))))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B2.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B2", new Pair<Operation, Double>(
									Operation.MUL, Tools.getValue(qs))))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B2.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B2", new Pair<Operation, Double>(
									Operation.DIV, Tools.getValue(qs))))));
				}
				it = beam.iterator();
				tmpLatticeList.clear();
				for(;it.hasNext();) {
					tmpLatticeList.add(it.next());
				}
				beam.clear();
			}
			
			for(QuantSpan qs : clusterMap.get("E3")) {
				if(!occurTwice(qs, clusterMap.get("E3"))) continue;
				for(Pair<Lattice, Double> pair : tmpLatticeList) {
					Lattice tmpLattice = new Lattice(pair.getFirst());
					beam.add(new Pair<>(tmpLattice, pair.getSecond()));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).C.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "C", new Pair<Operation, Double>(
									Operation.MUL, Tools.getValue(qs))))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).C.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "C", new Pair<Operation, Double>(
									Operation.DIV, Tools.getValue(qs))))));
				}
				it = beam.iterator();
				tmpLatticeList.clear();
				for(;it.hasNext();) {
					tmpLatticeList.add(it.next());
				}
				beam.clear();
			}
			
			// Operation related to E1
			for(Pair<Lattice, Double> pair : tmpLatticeList) {
				List<Operation> one = new ArrayList<Operation>(Arrays.asList(
						Operation.ADD, Operation.SUB, 
						Operation.MUL, Operation.DIV));
				List<Operation> two = null;
				if(pair.getFirst().equations.get(i).A1.size() == 0) {
					one.add(Operation.NONE);
				}
				if(pair.getFirst().equations.get(i).A2.size() > 0) {
					two = Arrays.asList(Operation.ADD, Operation.SUB);
				} else {
					two = Arrays.asList(Operation.NONE);
				}
				for(Operation op1 : one) {
					for(Operation op2 : two) {
						Lattice tmpLattice = new Lattice(pair.getFirst());
						Equation tmpEq = tmpLattice.equations.get(i);
						tmpEq.operations.set(0, op1);
						tmpEq.operations.set(1, op2);
						beam.add(new Pair<Lattice, Double>(tmpLattice, pair.getSecond()+
								wv.dotProduct(featGen.getFeaturesVector(
										blob, tmpLattice, i, "Op_E1", null))));
					}
				}	
			}
			it = beam.iterator();
			tmpLatticeList.clear();
			for(;it.hasNext();) {
				tmpLatticeList.add(it.next());
			}
			beam.clear();
			
			// Operation related to E2
			for(Pair<Lattice, Double> pair : tmpLatticeList) {
				List<Operation> one = new ArrayList<Operation>(Arrays.asList(
						Operation.ADD, Operation.SUB, 
						Operation.MUL, Operation.DIV));
				List<Operation> two = null;
				if(pair.getFirst().equations.get(i).B1.size() == 0) {
					one.add(Operation.NONE);
				}
				if(pair.getFirst().equations.get(i).B2.size() > 0) {
					two = Arrays.asList(Operation.ADD, Operation.SUB);
				} else {
					two = Arrays.asList(Operation.NONE);
				}
				for(Operation op1 : one) {
					for(Operation op2 : two) {
						Lattice tmpLattice = new Lattice(pair.getFirst());
						Equation tmpEq = tmpLattice.equations.get(i);
						tmpEq.operations.set(2, op1);
						tmpEq.operations.set(3, op2);
						if(tmpEq.C.size() > 0) tmpEq.operations.set(4, Operation.SUB);
						else tmpEq.operations.set(4, Operation.NONE);
						if(isValid(i, tmpLattice, blob)) {
							beam.add(new Pair<Lattice, Double>(tmpLattice, pair.getSecond()+
									wv.dotProduct(featGen.getFeaturesVector(
											blob, tmpLattice, i, "Op_E2", null))));
						}
					}
				}	
			}
		}
//		System.out.println("Gold choice : \n"+gold);
//		System.out.println("Best Choice : \n"+beam.element().getFirst());
		return beam.element().getFirst();
	}

	private boolean isValid(int i, Lattice lattice, Blob blob) {
		if(i==1 && lattice.equations.get(i).operations.get(0) == Operation.NONE &&
				lattice.equations.get(i).operations.get(2) == Operation.NONE) {
			return false;
		}
		if(i==1 && EquationSolver.solve(lattice) == null) {
			return false;
		}
		Equation eq = lattice.equations.get(i);
		if((eq.operations.get(0)==Operation.ADD || eq.operations.get(0)==Operation.SUB) &&
				(eq.operations.get(2)==Operation.MUL || eq.operations.get(2)==Operation.DIV)) {
			return false;
		}
		if((eq.operations.get(0)==Operation.MUL || eq.operations.get(0)==Operation.DIV) &&
				(eq.operations.get(2)==Operation.ADD || eq.operations.get(2)==Operation.SUB)) {
			return false;
		}
		if(i==1 && !isAllNumbersUsed(lattice, blob)) return false;
		return true;
	}

	private boolean occurTwice(QuantSpan qs, List<QuantSpan> list) {
		for(QuantSpan item : list) {
			if(Tools.safeEquals(Tools.getValue(qs), Tools.getValue(item))) {
				if(qs.start == item.start) return true;
				else return false;
			}
		}
		return true;
	}
	
	private boolean isAllNumbersUsed(Lattice lattice, Blob blob) {
		for(String entity : blob.clusterMap.keySet()) {
			for(QuantSpan qs : blob.clusterMap.get(entity)) {
				boolean found = false;
				if(entity.equals("E1")) {
					for(int j=0; j<lattice.equations.size(); ++j) {
						Equation e = lattice.equations.get(j);
						for(Pair<Operation, Double> pair : e.A1) {
							if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
								found = true;
							}
						}	
						for(Pair<Operation, Double> pair : e.A2) {
							if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
								found = true;
							}
						}	
					}
				}
				if(entity.equals("E2")) {
					for(int j=0; j<lattice.equations.size(); ++j) {
						Equation e = lattice.equations.get(j);
						for(Pair<Operation, Double> pair : e.B1) {
							if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
								found = true;
							}
						}	
						for(Pair<Operation, Double> pair : e.B2) {
							if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
								found = true;
							}
						}	
					}
				}
				if(entity.equals("E3")) {
					for(int j=0; j<lattice.equations.size(); ++j) {
						Equation e = lattice.equations.get(j);
						for(Pair<Operation, Double> pair : e.C) {
							if(Tools.safeEquals(pair.getSecond(), Tools.getValue(qs))) {
								found = true;
							}
						}	
					}
				}
				if(!found) return false;
			}
		}
		return true;
	}
}
