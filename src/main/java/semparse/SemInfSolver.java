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

public class SemInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private SemFeatGen featGen;
	public List<SemY> equationTemplates;
	public List<Pair<SemY, Double>> beam;

	public SemInfSolver(SemFeatGen featGen, List<SemY> templates) {
		this.featGen = featGen;
		this.equationTemplates = templates;
		beam = new ArrayList<Pair<SemY, Double>>();
	}

	public static List<SemY> extractTemplates(SLProblem slProb) {
		List<SemY> templates = new ArrayList<>();
		Map<Integer, Integer> stats = new HashMap<Integer, Integer>();
		for(IStructure struct : slProb.goldStructureList) {
			SemY gold = (SemY) struct;
			SemY eq1 = new SemY(gold);
			for(int j=0; j<5; ++j) {
				for(int k=0; k<eq1.terms.get(j).size(); ++k) {
					eq1.terms.get(j).get(k).setSecond(null);
				}
			}
			boolean alreadyPresent = false;
			for(SemY eq2 : templates) {
				if(SemY.getLoss(eq1, eq2) < 0.0001) alreadyPresent = true; 
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

	public static Map<Integer, Set<Integer>> extractTemplateStats(List<SemY> templates) {
		Map<Integer, Set<Integer>> stats = new HashMap<>();
		stats.put(1, new HashSet<Integer>());
		stats.put(2, new HashSet<Integer>());
		for(SemY y : templates) {
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
		SemY y1 = (SemY) arg1;
		SemY y2 = (SemY) arg2;
		return SemY.getLoss(y1, y2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		SemX blob = (SemX) x;
		SemY gold = (SemY) goldStructure;
		SemY pred = null;
		PairComparator<SemY> semPairComparator = 
				new PairComparator<SemY>() {};
		MinMaxPriorityQueue<Pair<SemY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).maximumSize(200).create();
		MinMaxPriorityQueue<Pair<SemY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).maximumSize(200).create();
		beam = new ArrayList<Pair<SemY, Double>>();
		
		Set<Double> availableNumbers = new HashSet<Double>();
		for(Double d : Tools.uniqueNumbers(blob.relationQuantities)) {
			availableNumbers.add(d);
		}
		for(SemY template : equationTemplates) {
			if(availableNumbers.size() == template.emptySlots.size() && 
					template.isOneVar == blob.isOneVar) {
				beam1.add(new Pair<SemY, Double>(template, 0.0));
			}
		}
		for(Pair<SemY, Double> pair : beam1) {
			for(SemY y : enumerateSemYs(availableNumbers, pair.getFirst())) {
				Double score = pair.getSecond() + 
						wv.dotProduct(featGen.getFeatureVector(blob, y)) + 
						(goldStructure == null ? 0.0 : SemY.getLoss(y, gold));
				beam2.add(new Pair<SemY, Double>(y, score));		
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
	
	public List<SemY> enumerateSemYs(Set<Double> availableNumbers, SemY seed) {
		List<SemY> list1 = new ArrayList<>();
		list1.add(seed);
		List<SemY> list2 = new ArrayList<>();
		for(IntPair slot : seed.emptySlots) {
			for(SemY y1 : list1) {
				for(Double d : availableNumbers) {
					SemY y = new SemY(y1);
					y.terms.get(slot.getFirst()).get(slot.getSecond()).setSecond(d);
					list2.add(y);
				}
			}
			list1.clear();
			list1.addAll(list2);
			list2.clear();
		}
		// Ensure that same number not used twice
		for(SemY y : list1) {
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
