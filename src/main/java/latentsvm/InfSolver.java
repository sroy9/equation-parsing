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
				for(int j=0; j<5; ++j) {
					for(int k=0; k<eq.terms.get(j).size(); ++k) {
						eq.terms.get(j).get(k).setSecond(null);
					}
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
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		return 0.0f;
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
					prediction.labelSet.labels.get(i) != gold.labelSet.labels.get(i)) {
				wrongClusterting = true;
			}
		}
		
		// Early update
		if(wrongClusterting) prediction.labelSet = gold.labelSet;
		
		// Create a cluster map
		for(int i=0; i<blob.quantities.size(); ++i) {
			String label = prediction.labelSet.labels.get(i);
			if(label.equals("E1")) prediction.clusters.get(0).add(
						blob.quantities.get(i));
			if(label.equals("E2")) prediction.clusters.get(1).add(
					blob.quantities.get(i));
			if(label.equals("E3")) prediction.clusters.get(2).add(
					blob.quantities.get(i));
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
		return beam2.element().getFirst();
	}
	
	public List<Lattice> enumerateEquations(Lattice seed) {
		List<Lattice> list1 = Arrays.asList(seed);
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
		return list1;
	}
		
	
}
