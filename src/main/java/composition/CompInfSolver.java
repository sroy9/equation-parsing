package composition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.MinMaxPriorityQueue;

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

public class CompInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private CompFeatGen featGen;
	public List<CompY> equationTemplates;
	public List<Pair<CompY, Double>> beam;

	public CompInfSolver(CompFeatGen featGen, List<CompY> templates) {
		this.featGen = featGen;
		this.equationTemplates = templates;
		beam = new ArrayList<Pair<CompY, Double>>();
	}

	public static List<CompY> extractTemplates(SLProblem slProb) {
		List<CompY> templates = new ArrayList<>();
		Map<Integer, Integer> stats = new HashMap<Integer, Integer>();
		for(IStructure struct : slProb.goldStructureList) {
			CompY gold = (CompY) struct;
			CompY eq1 = new CompY(gold);
			for(int j=0; j<5; ++j) {
				for(int k=0; k<eq1.terms.get(j).size(); ++k) {
					eq1.terms.get(j).get(k).setSecond(null);
				}
			}
			boolean alreadyPresent = false;
			for(CompY eq2 : templates) {
				if(CompY.getLoss(eq1, eq2) < 0.0001) alreadyPresent = true; 
			}
			if(!alreadyPresent) {
				eq1.templateNo = templates.size();
				if(!stats.containsKey(eq1.emptySlots.size())) {
					stats.put(eq1.emptySlots.size(), 0);
				}
				stats.put(eq1.emptySlots.size(), stats.get(eq1.emptySlots.size())+1);
				templates.add(eq1);
			}
		}
		System.out.println("Number of templates : "+templates.size());
		System.out.println("Stats : "+Arrays.asList(stats));
		return templates;
	}

	public static Map<Integer, Set<Integer>> extractTemplateStats(List<CompY> templates) {
		Map<Integer, Set<Integer>> stats = new HashMap<>();
		stats.put(1, new HashSet<Integer>());
		stats.put(2, new HashSet<Integer>());
		for(CompY y : templates) {
			if(y.isOneVar) stats.get(1).add(y.emptySlots.size());
			else stats.get(2).add(y.emptySlots.size());
		}
		return stats;
	}
	
 	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		CompY y1 = (CompY) arg1;
		CompY y2 = (CompY) arg2;
		return CompY.getLoss(y1, y2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		CompX blob = (CompX) x;
		CompY gold = (CompY) goldStructure;
		CompY pred = null;
		PairComparator<CompY> semPairComparator = 
				new PairComparator<CompY>() {};
		MinMaxPriorityQueue<Pair<CompY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).maximumSize(200).create();
		MinMaxPriorityQueue<Pair<CompY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).maximumSize(200).create();
		beam = new ArrayList<Pair<CompY, Double>>();
		
		Set<Double> availableNumbers = new HashSet<Double>();
		for(Double d : Tools.uniqueNumbers(blob.relationQuantities)) {
			availableNumbers.add(d);
		}
		for(CompY template : equationTemplates) {
			if(availableNumbers.size() == template.emptySlots.size() && 
					template.isOneVar == blob.isOneVar) {
				beam1.add(new Pair<CompY, Double>(template, 0.0));
			}
		}
		for(Pair<CompY, Double> pair : beam1) {
			for(CompY y : enumerateSemYs(availableNumbers, pair.getFirst())) {
				Double score = pair.getSecond() + 
						wv.dotProduct(featGen.getFeatureVector(blob, y)) + 
						(goldStructure == null ? 0.0 : CompY.getLoss(y, gold));
				beam2.add(new Pair<CompY, Double>(y, score));		
			}
		}
		// If beam2 is empty, you are doing something wrong
		pred = beam2.element().getFirst();
		
		int size = 10, i=0;
		while(beam2.size()>0 && i<size) {
			++i;
			beam.add(beam2.poll());
		}
		return pred;
	}
	
	public List<CompY> enumerateSemYs(Set<Double> availableNumbers, CompY seed) {
		List<CompY> list1 = new ArrayList<>();
		list1.add(seed);
		List<CompY> list2 = new ArrayList<>();
		for(IntPair slot : seed.emptySlots) {
			for(CompY y1 : list1) {
				for(Double d : availableNumbers) {
					CompY y = new CompY(y1);
					y.terms.get(slot.getFirst()).get(slot.getSecond()).setSecond(d);
					list2.add(y);
				}
			}
			list1.clear();
			list1.addAll(list2);
			list2.clear();
		}
		// Ensure that same number not used twice
		for(CompY y : list1) {
			boolean allow = true;
			for(int i=0; i<seed.emptySlots.size(); ++i) {
				IntPair slot1 = seed.emptySlots.get(i);
				for(int j=i+1; j<seed.emptySlots.size(); ++j) {
					IntPair slot2 = seed.emptySlots.get(j);
					if(Tools.safeEquals(y.terms.get(slot1.getFirst())
							.get(slot1.getSecond()).getSecond(), 
							y.terms.get(slot2.getFirst())
							.get(slot2.getSecond()).getSecond())) {
						allow = false;
						break;
					}
				}
				if(!allow) break;
			}
			if(allow) list2.add(y);
		}
		return list2;
	}
		
	
}
