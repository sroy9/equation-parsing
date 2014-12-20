package latentsvm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class InfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private FeatureExtractor featGen;
	private List<Lattice> templates;

	public InfSolver(FeatureExtractor featGen, SLProblem slProb) {
		this.featGen = featGen;
		this.templates = extractTemplates(slProb);
	}

	private List<Lattice> extractTemplates(SLProblem slProb) {
		List<Lattice> templates = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			Lattice lattice = (Lattice) struct;
			for(int i=0; i<2; ++i) {
				Equation eq = lattice.equations.get(i);
				for(Pair<Operation, Double> pair : eq.A1) {
					pair.setSecond(null);
				}
				for(Pair<Operation, Double> pair : eq.A2) {
					pair.setSecond(null);
				}
				for(Pair<Operation, Double> pair : eq.B1) {
					pair.setSecond(null);
				}
				for(Pair<Operation, Double> pair : eq.B2) {
					pair.setSecond(null);
				}
				for(Pair<Operation, Double> pair : eq.C) {
					pair.setSecond(null);
				}
			}
			if(!templates.contains(lattice)) templates.add(lattice);
		}
		System.out.println("Number of templates : "+templates.size());
		return templates;
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
				if (pair1.getFirst() == pair2.getFirst() && 
						Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) {
					found = true;
					break;
				}
			}
			if (!found) loss += 1.0;
		}
		for (Pair<Operation, Double> pair2 : list2) {
			boolean found = false;
			for (Pair<Operation, Double> pair1 : list1) {
				if (pair1.getFirst() == pair2.getFirst() && 
						Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) {
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
		Lattice prediction = new Lattice();

		PairComparator<Lattice> latticePairComparator = 
				new PairComparator<Lattice>() {};
		BoundedPriorityQueue<Pair<Lattice, Double>> beam1 = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(50, latticePairComparator);
		BoundedPriorityQueue<Pair<Lattice, Double>> beam2 = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(50, latticePairComparator);
		
		// Infer clustering
		List<String> labels = Arrays.asList("E1", "E2", "E3", "NONE");
		for(int i=0; i<blob.quantities.size(); ++i) {
			double maxScore = -Double.MAX_VALUE;
			String bestLabel = null;
			for(String label : labels) {
				prediction.labelSet.addLabel(label);
				double score = wv.dotProduct(
						featGen.getClusterFeatureVector(blob, prediction.labelSet, i));
				if(score > maxScore) {
					maxScore = score;
					bestLabel = label;
				}
				prediction.labelSet.removeLast();
			}
			prediction.labelSet.addLabel(bestLabel);
		}
		// Create a cluster map
		Map<String, List<QuantSpan>> clusterMap = new HashMap<>();
		clusterMap.put("E1", new ArrayList<QuantSpan>());
		clusterMap.put("E2", new ArrayList<QuantSpan>());
		clusterMap.put("E3", new ArrayList<QuantSpan>());
		for(int i=0; i<blob.quantities.size(); ++i) {
			if(!prediction.labelSet.labels.get(i).equals("NONE")) {
				clusterMap.get(prediction.labelSet.labels.get(i)).add(
						blob.quantities.get(i));
			}
		}
		
		// Infer equations, respecting clustering
		for(Lattice template : templates) {
			template.labelSet = prediction.labelSet;
			beam1.add(new Pair<Lattice, Double>(template, 0.0));
		}
		
		for(Pair<Lattice, Double> pair : beam1) {
			boolean emptySlotFound = false;
			Lattice lattice = pair.getFirst();
			for(int i=0; i<2; ++i) {
				Equation eq = lattice.equations.get(i);
				for(int j=0; j<eq.A1.size(); ++j) {
					Pair<Operation, Double> term = eq.A1.get(j);
					if(term.getSecond() == null) {
						emptySlotFound = true;
						for(Double d : Tools.uniqueNumbers(clusterMap.get("E1"))) {
							Lattice newLattice = new Lattice(lattice);
							newLattice.equations.get(i).A1.get(j).setSecond(d);
							beam2.add(new Pair<Lattice, Double>(newLattice, 
									pair.getSecond()+
									wv.dotProduct(featGen.getEquationFeatureVector(
											blob, newLattice, i, "A1", j))));
						}
						break;
					}
				}
				if(emptySlotFound) break;
				for(int j=0; j<eq.A2.size(); ++j) {
					Pair<Operation, Double> term = eq.A2.get(j);
					if(term.getSecond() == null) {
						emptySlotFound = true;
						for(Double d : Tools.uniqueNumbers(clusterMap.get("E1"))) {
							Lattice newLattice = new Lattice(lattice);
							newLattice.equations.get(i).A2.get(j).setSecond(d);
							beam2.add(new Pair<Lattice, Double>(newLattice, 
									pair.getSecond()+
									wv.dotProduct(featGen.getEquationFeatureVector(
											blob, newLattice, i, "A2", j))));
						}
						break;
					}
				}
				if(emptySlotFound) break;
				for(int j=0; j<eq.B1.size(); ++j) {
					Pair<Operation, Double> term = eq.B1.get(j);
					if(term.getSecond() == null) {
						emptySlotFound = true;
						for(Double d : Tools.uniqueNumbers(clusterMap.get("E2"))) {
							Lattice newLattice = new Lattice(lattice);
							newLattice.equations.get(i).B1.get(j).setSecond(d);
							beam2.add(new Pair<Lattice, Double>(newLattice, 
									pair.getSecond()+
									wv.dotProduct(featGen.getEquationFeatureVector(
											blob, newLattice, i, "B1", j))));
						}
						break;
					}
				}
				if(emptySlotFound) break;
				for(int j=0; j<eq.B2.size(); ++j) {
					Pair<Operation, Double> term = eq.B2.get(j);
					if(term.getSecond() == null) {
						emptySlotFound = true;
						for(Double d : Tools.uniqueNumbers(clusterMap.get("E2"))) {
							Lattice newLattice = new Lattice(lattice);
							newLattice.equations.get(i).B2.get(j).setSecond(d);
							beam2.add(new Pair<Lattice, Double>(newLattice, 
									pair.getSecond()+
									wv.dotProduct(featGen.getEquationFeatureVector(
											blob, newLattice, i, "B2", j))));
						}
						break;
					}
				}
				if(emptySlotFound) break;
				for(int j=0; j<eq.C.size(); ++j) {
					Pair<Operation, Double> term = eq.C.get(j);
					if(term.getSecond() == null) {
						emptySlotFound = true;
						for(Double d : Tools.uniqueNumbers(clusterMap.get("E3"))) {
							Lattice newLattice = new Lattice(lattice);
							newLattice.equations.get(i).C.get(j).setSecond(d);
							beam2.add(new Pair<Lattice, Double>(newLattice, 
									pair.getSecond()+
									wv.dotProduct(featGen.getEquationFeatureVector(
											blob, newLattice, i, "C", j))));
						}
						break;
					}
				}
				if(emptySlotFound) break;
			}
			if(!emptySlotFound) beam2.add(pair);
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		return beam1.element().getFirst();
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
