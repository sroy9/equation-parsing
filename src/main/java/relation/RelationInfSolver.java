package relation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import semparse.SemInfSolver;
import semparse.SemX;
import semparse.SemY;
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
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.mit.jwi.morph.IStemmer;

public class RelationInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private RelationFeatGen featGen;
	public List<Map<String, Integer>> clusterTemplates;
	public SLModel equationModel;

	public RelationInfSolver(RelationFeatGen featGen, 
			List<Map<String, Integer>> templates, int testFold) throws Exception {
		this.featGen = featGen;
		this.clusterTemplates = templates;
		equationModel = SLModel.loadModel("sem"+testFold+".save");
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
		RelationY pred = new RelationY();
		PairComparator<RelationY> relationPairComparator = 
				new PairComparator<RelationY>() {};
		BoundedPriorityQueue<Pair<RelationY, Double>> beam1 = 
				new BoundedPriorityQueue<Pair<RelationY, Double>>(200, relationPairComparator);
		BoundedPriorityQueue<Pair<RelationY, Double>> beam2 = 
				new BoundedPriorityQueue<Pair<RelationY, Double>>(200, relationPairComparator);
		for(RelationY y : enumerateClustersRespectingTemplates(prob)) {
			beam1.add(new Pair<RelationY, Double>(y, 0.0 + 
					wv.dotProduct(featGen.getRelationFeatureVector(prob, y))));
		}
		for(Pair<RelationY, Double> pair1 : beam1) {
			List<SemX> semXs = SemX.extractEquationProbFromRelations(prob, pair1.getFirst());
			if(semXs.size() == 1) {
				equationModel.infSolver.getBestStructure(equationModel.wv, semXs.get(0));
				List<Pair<SemY, Double>> list = ((SemInfSolver) equationModel.infSolver).beam;
				list = list.subList(0, Math.min(10, list.size()));
				for(Pair<SemY, Double> pair2 : list) {
					RelationY y = new RelationY(pair1.getFirst());
					y.equations.add(pair2.getFirst());
					beam2.add(new Pair<RelationY, Double>(y, pair1.getSecond() + 
							wv.dotProduct(featGen.getEquationFeatureVector(prob, y))));
				}
			}
			if(semXs.size() == 2) {
				equationModel.infSolver.getBestStructure(equationModel.wv, semXs.get(0));
				List<Pair<SemY, Double>> list1 = ((SemInfSolver) equationModel.infSolver).beam;
				list1 = list1.subList(0, Math.min(5, list1.size()));
				equationModel.infSolver.getBestStructure(equationModel.wv, semXs.get(1));
				List<Pair<SemY, Double>> list2 = ((SemInfSolver) equationModel.infSolver).beam;
				list2 = list2.subList(0, Math.min(5, list2.size()));
 				for(Pair<SemY, Double> pair2 : list1) {				
					for(Pair<SemY, Double> pair3 : list2) {
						RelationY y = new RelationY(pair1.getFirst());
						y.equations.add(pair2.getFirst());
						y.equations.add(pair3.getFirst());
						beam2.add(new Pair<RelationY, Double>(y, pair1.getSecond() + 
								wv.dotProduct(featGen.getEquationFeatureVector(prob, y))));
					}
				}
			}
		}
		return beam2.element().getFirst();
	}
	
	public static List<Map<String, Integer>> extractClusterTemplates(SLProblem slProb) {
		List<Map<String, Integer>> clusterTemplates = new ArrayList<>();
		for(int i=0; i<slProb.goldStructureList.size(); ++i) {
			RelationX prob = (RelationX) slProb.instanceList.get(i);
			RelationY gold = (RelationY) slProb.goldStructureList.get(i);
			Map<String, Integer> stats = getStats(prob, gold);
			if(!isTemplatePresent(clusterTemplates, stats)) {
				clusterTemplates.add(stats);
			}
		}
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
			if(stats.get("R1") <= map.get("R1") && stats.get("R2") <= map.get("R2") && 
					stats.get("BOTH") <= map.get("BOTH")) {
				return true;
			}
			if(stats.get("R1") <= map.get("R2") && stats.get("R2") <= map.get("R1") && 
					stats.get("BOTH") <= map.get("BOTH")) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isTemplatePresent(
			List<Map<String, Integer>> templates, Map<String, Integer> stats) {
		for(Map<String, Integer> map : templates) {
			if(stats.get("R1") == map.get("R1") && stats.get("R2") == map.get("R2") && 
					stats.get("BOTH") == map.get("BOTH")) {
				return true;
			}
			if(stats.get("R1") == map.get("R2") && stats.get("R2") == map.get("R1") && 
					stats.get("BOTH") == map.get("BOTH")) {
				return true;
			}
		}
		return false;
	}
	
	public List<RelationY> enumerateClustersRespectingTemplates(RelationX x) {
		List<String> relations = Arrays.asList("R1", "R2", "BOTH", "NONE");
		List<RelationY> list1 = new ArrayList<>();
		list1.add(new RelationY());
		List<RelationY> list2 = new ArrayList<>();
		System.out.println("NumQuant : "+x.quantities.size());
		for(int i=0; i<x.quantities.size(); ++i) {
			for(RelationY y : list1) {
				for(String relation : relations) {
					RelationY yNew = new RelationY(y);
					yNew.relations.add(relation);
					if(isPossibleTemplate(clusterTemplates, getStats(x, yNew))) {
						list2.add(yNew);
					}
				}
			}
			list1.clear();
			list1.addAll(list2);
			list2.clear();
		}
		System.out.println("Enumeration : "+list1.size());
		for(RelationY y : list1) {
			Map<String, Integer> stats = getStats(x, y);
			if(isTemplatePresent(clusterTemplates, stats)) {
				y.isOneVar = Tools.isOneVar(y.relations);
				list2.add(y);
			}
		}
		System.out.println("After some pruning : "+list2.size());
		return list2;
	}
}
