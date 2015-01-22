package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MinMaxPriorityQueue;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class RelationInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private RelationFeatGen featGen;
	public List<Map<String, Integer>> segTemplates;
	public List<Pair<RelationY, Double>> beam;

	public RelationInfSolver(RelationFeatGen featGen, 
			List<Map<String, Integer>> segTemplates, 
			int testFold) throws Exception {
		this.featGen = featGen;
		this.segTemplates = segTemplates;
		beam = new ArrayList<>();
		System.out.println("Segmentation Templates : " +Arrays.asList(segTemplates));
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		RelationY r1 = (RelationY) arg1;
		RelationY r2 = (RelationY) arg2;
		return RelationY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		RelationX prob = (RelationX) x;
		RelationY gold = (RelationY) goldStructure;
		RelationY pred = null;
		PairComparator<RelationY> relationPairComparator = 
				new PairComparator<RelationY>() {};
		MinMaxPriorityQueue<Pair<RelationY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(relationPairComparator)
				.maximumSize(200).create();
		MinMaxPriorityQueue<Pair<RelationY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(relationPairComparator)
				.maximumSize(200).create();
		
		// Number of Variables and Relation labels
		for(boolean isOneVar : Arrays.asList(true, false)) {
			for(RelationY y : enumerateClustersRespectingTemplates(
					prob, segTemplates, isOneVar)) {
				Double score = 0.0 + 
						wv.dotProduct(featGen.getNumVarFeatureVector(prob, y)) +
						wv.dotProduct(featGen.getRelationFeatureVector(prob, y)) +
						(goldStructure == null? 0 : 
							(RelationY.getNumVarLoss(y, gold) + 
									RelationY.getRelationLoss(y, gold)));
				beam1.add(new Pair<RelationY, Double>(y, score));
			}
		}

		// Equation Span
		
		
		if(beam1.size() > 0) pred = beam1.element().getFirst();
		int size = 10, i=0;
		beam.clear();
		while(beam1.size()>0 && i<size) {
			++i;
			beam.add(beam1.poll());
		}
		return pred;
	}
	
	public static List<Map<String, Integer>> extractSegTemplates(SLProblem slProb) {
		List<Map<String, Integer>> clusterTemplates = new ArrayList<>();
		for(int i=0; i<slProb.goldStructureList.size(); ++i) {
			RelationX prob = (RelationX) slProb.instanceList.get(i);
			RelationY gold = (RelationY) slProb.goldStructureList.get(i);
			Map<String, Integer> stats = getStats(prob, gold);
			if(!isTemplatePresent(clusterTemplates, stats)) {
				clusterTemplates.add(stats);
			}
		}
		System.out.println("Number of templates : " + clusterTemplates.size());
		return clusterTemplates;
	}
	
	public static Map<String, Integer> getStats(RelationX x, RelationY y) {
		List<QuantSpan> quantR1 = new ArrayList<>();
		List<QuantSpan> quantR2 = new ArrayList<>();
		List<QuantSpan> quantBOTH = new ArrayList<>();
		for(int j=0; j<y.relations.size(); ++j) {
			String relation = y.relations.get(j);
			if(relation.equals("R1")) {
				quantR1.add(x.quantities.get(j));
			}
			if(relation.equals("R2")) {
				quantR2.add(x.quantities.get(j));
			}
			if(relation.equals("BOTH")) {
				quantBOTH.add(x.quantities.get(j));
			}
		}
		Map<String, Integer> stats = new HashMap<>();
		stats.put("R1", Tools.uniqueNumbers(quantR1).size());
		stats.put("R2", Tools.uniqueNumbers(quantR2).size());
		stats.put("BOTH", Tools.uniqueNumbers(quantBOTH).size());
		return stats;
	}
	
	public static boolean isPossibleTemplate(
			List<Map<String, Integer>> templates, Map<String, Integer> stats) {
		for(Map<String, Integer> map : templates) {
			if(stats.get("R1").intValue() <= map.get("R1").intValue() && 
					stats.get("R2").intValue() <= map.get("R2").intValue() && 
					stats.get("BOTH").intValue() <= map.get("BOTH").intValue()) {
				return true;
			}
			if(stats.get("R1").intValue() <= map.get("R2").intValue() && 
					stats.get("R2").intValue() <= map.get("R1").intValue() && 
					stats.get("BOTH").intValue() <= map.get("BOTH").intValue()) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isTemplatePresent(
			List<Map<String, Integer>> templates, Map<String, Integer> stats) {
		for(Map<String, Integer> map : templates) {
			if(stats.get("R1").equals(map.get("R1")) && 
					stats.get("R2").equals(map.get("R2")) && 
					stats.get("BOTH").equals(map.get("BOTH"))) {
				return true;
			}
			if(stats.get("R1").equals(map.get("R2")) && 
					stats.get("R2").equals(map.get("R1")) && 
					stats.get("BOTH").equals(map.get("BOTH"))) {
				return true;
			}
		}
		return false;
	}
	
	public List<RelationY> enumerateClustersRespectingTemplates(
			RelationX x, List<Map<String, Integer>> segTemplates, boolean isOneVar) {
		List<String> relations = Arrays.asList("R1", "R2", "BOTH", "NONE");
		List<RelationY> list1 = new ArrayList<>();
		list1.add(new RelationY());
		List<RelationY> list2 = new ArrayList<>();
		for(int i=0; i<x.quantities.size(); ++i) {
			for(RelationY y : list1) {
				for(String relation : relations) {
					RelationY yNew = new RelationY(y);
					yNew.relations.add(relation);
					if(isPossibleTemplate(segTemplates, getStats(x, yNew))) {
						list2.add(yNew);
					}
				}
			}
			list1.clear();
			list1.addAll(list2);
			list2.clear();
		}
		for(RelationY y : list1) {
			Map<String, Integer> stats = getStats(x, y);
			if(isTemplatePresent(segTemplates, stats)) {
				y.isOneVar = Tools.isOneVar(y.relations);
				if(isOneVar == y.isOneVar) {
					list2.add(y);
				}
			}
		}
		return list2;
	}
}
