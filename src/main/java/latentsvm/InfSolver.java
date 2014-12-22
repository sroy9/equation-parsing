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

	public InfSolver(FeatureExtractor featGen, List<Lattice> templates) {
		this.featGen = featGen;
		this.templates = templates;
	}

	public static List<Lattice> extractTemplates(SLProblem slProb) {
		List<Lattice> templates = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			Lattice gold = (Lattice) struct;
			System.out.println("Gold before : "+gold);
 			Lattice lattice = new Lattice(gold);
			for(int i=0; i<2; ++i) {
				Equation eq = lattice.equations.get(i);
				for(int j=0; j<5; ++j) {
					for(int k=0; k<eq.terms.get(j).size(); ++k) {
						eq.terms.get(j).get(k).setSecond(null);
					}
				}
			}
			boolean alreadyPresent = false;
			for(Lattice l : templates) {
				boolean diff = false;
				for(int i=0; i<2; i++) {
					Equation eq1 = l.equations.get(i);
					Equation eq2 = lattice.equations.get(i);
					for(int j=0; j<5; ++j) {
						if(eq1.terms.get(j).size() != eq2.terms.get(j).size()) {
							diff = true;
						}
						if(diff) break;
 						for(int k=0; k<eq1.terms.get(j).size(); k++) {
 							if(eq1.terms.get(j).get(k).getFirst() != 
 									eq2.terms.get(j).get(k).getFirst()) {
								diff = true;
							}
						}
						if(diff) break;
					}
					if(diff) break;
				}
				if(!diff) alreadyPresent = true;
			}
			if(!alreadyPresent) templates.add(lattice);
			System.out.println("Gold : "+gold );
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
		Lattice l1 = (Lattice) arg1;
		Lattice l2 = (Lattice) arg2;
		float loss = 0.0f;
		assert l1.labelSet.labels.size() == l2.labelSet.labels.size(); 
		System.out.println(l1.labelSet.labels.size() + " " + l2.labelSet.labels.size());
		int len = l1.labelSet.labels.size();
		for(int i=0; i<len; ++i) {
			if(!l1.labelSet.labels.get(i).equals(l2.labelSet.labels.get(i))) {
				loss+=1.0;
			}
		}
		if(!Driver.hasSameSolution(l1, l2)) loss += 10.0f; 
		return loss;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		Blob blob = (Blob) x;
		Lattice gold = (Lattice) goldStructure;
		Lattice prediction = new Lattice();
		System.out.println("Gold structure\n"+gold);

		PairComparator<Lattice> latticePairComparator = 
				new PairComparator<Lattice>() {};
		BoundedPriorityQueue<Pair<Lattice, Double>> beam1 = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(50, latticePairComparator);
		BoundedPriorityQueue<Pair<Lattice, Double>> beam2 = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(50, latticePairComparator);
		
		// Infer clustering
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
