package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class SemInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private SemFeatGen featGen;
	private List<SemY> templates;

	public SemInfSolver(SemFeatGen featGen, List<SemY> templates) {
		this.featGen = featGen;
		this.templates = templates;
	}

	public static List<SemY> extractTemplates(SLProblem slProb) {
		List<SemY> templates = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			SemY gold = (SemY) struct;
			SemY eq1 = new SemY(gold);
			for(int j=0; j<5; ++j) {
				for(int k=0; k<eq1.terms.get(j).size(); ++k) {
					eq1.terms.get(j).get(k).setSecond(null);
					eq1.emptySlots.add(new IntPair(j, k));
				}
			}
			boolean alreadyPresent = false;
			for(SemY eq2 : templates) {
				boolean diff = false;
				for(int j=0; j<5; ++j) {
					if(eq1.terms.get(j).size() != eq2.terms.get(j).size()) {
						diff = true; break;
					}
					if(diff) break;
					for(int k=0; k<eq1.terms.get(j).size(); k++) {
						if(eq1.terms.get(j).get(k).getFirst() != 
								eq2.terms.get(j).get(k).getFirst()) {
							diff = true; break;
						}
					}
					if(diff) break;
					for(int k=0; k<4; k++) {
						if(eq1.operations.get(k) != eq2.operations.get(k)) {
							diff = true; break;
						}
					}
					if(diff) break;
				}
				if(!diff) {
					alreadyPresent = true;
					break;
				}
			}
			if(!alreadyPresent) templates.add(eq1);
		}
		System.out.println("Number of templates : "+templates.size());
		return templates;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		SemY y1 = (SemY) arg1;
		SemY y2 = (SemY) arg2;
		return SemY.getLoss(y1, y2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		SemX blob = (SemX) x;
		SemY gold = (SemY) goldStructure;
		SemY pred = new SemY();
		System.out.println("Gold structure\n"+gold);

		PairComparator<SemY> semPairComparator = 
				new PairComparator<SemY>() {};
		BoundedPriorityQueue<Pair<SemY, Double>> beam1 = 
				new BoundedPriorityQueue<Pair<SemY, Double>>(50, semPairComparator);
		BoundedPriorityQueue<Pair<SemY, Double>> beam2 = 
				new BoundedPriorityQueue<Pair<SemY, Double>>(50, semPairComparator);
		Set<Double> availableNumbers = new HashSet<>();
		for(Integer i : blob.quantIndices) {
			Double d = Tools.getValue(blob.quantities.get(i));
			boolean alreadyThere = false;
			for(Double d1 : availableNumbers) {
				if(Tools.safeEquals(d, d1)) {
					alreadyThere = true;
				}
			}
			if(!alreadyThere) {
				availableNumbers.add(d);
			}
		}
		
		
		
		boolean wrongClusterting = false; 
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
			if(goldStructure != null && 
					!prediction.labelSet.labels.get(i).equals(gold.labelSet.labels.get(i))) {
				wrongClusterting = true;
			}
		}
		// Early update
		if(wrongClusterting) {
			prediction.clusters = gold.clusters;
			prediction.equations = gold.equations;
			System.out.println("Inferred : "+prediction);
			System.out.println("Score Predict : " + wv.dotProduct(featGen.getFeatureVector(blob, prediction)));
			System.out.println("Score Gold : "+wv.dotProduct(featGen.getFeatureVector(blob, gold)));
			return prediction;
		}
		
		// Infer equations, respecting clustering
		for(Lattice template : templates) {
			template.clusters  = prediction.clusters;
			template.labelSet = prediction.labelSet;
			beam1.add(new Pair<Lattice, Double>(template, 0.0));
		}
		
		for(Pair<Lattice, Double> pair : beam1) {
			for(Lattice lattice : enumerateEquations(pair.getFirst())) {
				beam2.add(new Pair<Lattice, Double>(lattice, 
						1.0*wv.dotProduct(featGen.getEquationFeatureVector(blob, lattice))));
			}
		}
		if(beam2.size() == 0) return prediction;
		System.out.println("Inferred : "+beam2.element().getFirst());
		return beam2.element().getFirst();
	}
	
	public List<Lattice> enumerateEquations(Lattice seed) {
		System.out.println("Seed : "+seed);
		List<Lattice> list1 = new ArrayList<>();
		list1.add(seed);
		List<Lattice> list2 = new ArrayList<>();
		for(int i=0; i<2; ++i) {
			for(int j=0; j<5; ++j) {
				for(int k=0; k<seed.equations.get(i).terms.get(j).size(); ++k) {
					// Found a slot, now enumerate
					for(Lattice lattice : list1) {
						for(Double d : Tools.uniqueNumbers(seed.clusters.get(j/2))) {
							Lattice newLattice = new Lattice(lattice);
							newLattice.equations.get(i).terms.get(j).get(k).setSecond(d);
							list2.add(newLattice);
						}
					}
					list1.clear();
					list1.addAll(list2);
					list2.clear();
				}
			}
		}
		System.out.println("Enumerated");
		for(Lattice lattice : list1) {
			System.out.println(lattice);
		}
		return list1;
	}
		
	
}
