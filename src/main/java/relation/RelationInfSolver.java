package relation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
//	public Map<Integer, Set<Integer>> templateStats;
	public List<Map<String, Integer>> segTemplates;
	public SLModel equationModel;

	public RelationInfSolver(RelationFeatGen featGen, List<Map<String, Integer>> segTemplates, 
			int testFold) throws Exception {
		this.featGen = featGen;
		equationModel = SLModel.loadModel("sem"+testFold+".save");
//		templateStats = SemInfSolver.extractTemplateStats(
//				((SemInfSolver)equationModel.infSolver).templates);
		this.segTemplates = segTemplates;
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
	
	// argmax_h_i w^T \phi(x, h_i, y)
	public RelationY getBestLatentVariable(
			WeightVector wv, RelationX x, RelationY y) {
		y.relations.clear();
		Map<String, List<Double>> eqNumbers = new HashMap<String, List<Double>>();
		eqNumbers.put("R1", new ArrayList<Double>());
		eqNumbers.put("R2", new ArrayList<Double>());
		for(int i=0; i<y.equations.size(); ++i) {
			Equation eq = y.equations.get(i);
			for(List<Pair<Operation, Double>> pairList : eq.terms) {
				for(Pair<Operation, Double> pair : pairList) {
					eqNumbers.get("R"+(i+1)).add(pair.getSecond());
				}
			}
		}
		RelationY best = null;
		float bestScore = -Float.MAX_VALUE;
		for(RelationY ry : enumerateClustersRespectingEquations(x, y, eqNumbers)) {
			float score = wv.dotProduct(featGen.getFeatureVector(x, ry));
			if(score > bestScore) {
				bestScore = score;
				best = ry;
			}
		}
		System.out.println("BestLatentVar : "+best.equations.size());
		return best;
	}

	// argmax _ {y_i, h_i} w^T \phi(x, y_i, h_i)
	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		RelationX prob = (RelationX) x;
		RelationY gold = (RelationY) goldStructure;
		RelationY pred = null;
		PairComparator<RelationY> relationPairComparator = 
				new PairComparator<RelationY>() {};
		BoundedPriorityQueue<Pair<RelationY, Double>> beam1 = 
				new BoundedPriorityQueue<Pair<RelationY, Double>>(50, relationPairComparator);
		BoundedPriorityQueue<Pair<RelationY, Double>> beam2 = 
				new BoundedPriorityQueue<Pair<RelationY, Double>>(50, relationPairComparator);
		System.out.println("LossAugmentedBestStructure called");
		System.out.println(prob.problemIndex+" : "+prob.ta.getText());
		System.out.println(prob.quantities);
		for(RelationY y : enumerateClustersRespectingTemplates(prob, segTemplates)) {
			beam1.add(new Pair<RelationY, Double>(y, 0.0 + 
					wv.dotProduct(featGen.getRelationFeatureVector(prob, y))+
					(goldStructure == null?0:RelationY.getRelationLoss(y, gold))));
		}
		
		for(Pair<RelationY, Double> pair1 : beam1) {
			System.out.println("Relation : "+pair1.getFirst());
			List<SemX> semXs = SemX.extractEquationProbFromRelations(prob, pair1.getFirst());
			System.out.println("SemXs : " + semXs.size());
			if(semXs.size() == 1) {
				equationModel.infSolver.getBestStructure(equationModel.wv, semXs.get(0));
				List<Pair<SemY, Double>> list = ((SemInfSolver) equationModel.infSolver).beam;
				list = list.subList(0, Math.min(10, list.size()));
				for(Pair<SemY, Double> pair2 : list) {
					RelationY y = new RelationY(pair1.getFirst());
					y.equations.add(pair2.getFirst());
					beam2.add(new Pair<RelationY, Double>(y, pair1.getSecond() + 
							wv.dotProduct(featGen.getEquationFeatureVector(prob, y)) +
							RelationY.getLoss(y, gold)));
				}
			}
			if(semXs.size() == 2) {
				equationModel.infSolver.getBestStructure(equationModel.wv, semXs.get(0));
				List<Pair<SemY, Double>> list1 = new ArrayList<>();
				list1.addAll(((SemInfSolver) equationModel.infSolver).beam);
				list1 = list1.subList(0, Math.min(5, list1.size()));
				equationModel.infSolver.getBestStructure(equationModel.wv, semXs.get(1));
				List<Pair<SemY, Double>> list2 = new ArrayList<>();
				list2.addAll(((SemInfSolver) equationModel.infSolver).beam);
				list2 = list2.subList(0, Math.min(5, list2.size()));
 				for(Pair<SemY, Double> pair2 : list1) {				
					for(Pair<SemY, Double> pair3 : list2) {
						RelationY y = new RelationY(pair1.getFirst());
						y.equations.add(pair2.getFirst());
						y.equations.add(pair3.getFirst());
						beam2.add(new Pair<RelationY, Double>(y, pair1.getSecond() + 
								wv.dotProduct(featGen.getEquationFeatureVector(prob, y)) +
								RelationY.getLoss(y, gold)));
					}
				}
			}
		}
//		System.out.println(new Date()+" : inference done");
		if(beam2.size() > 0) pred = beam2.element().getFirst();
		System.out.println("BestLossAugmented : "+pred.equations.size());
		return pred;
	}
	
	public static Map<String, List<Double>> clusterMap(RelationX x, RelationY y) {
		List<QuantSpan> quantR1 = new ArrayList<>();
		List<QuantSpan> quantR2 = new ArrayList<>();
		for(int j=0; j<y.relations.size(); ++j) {
			String relation = y.relations.get(j);
			if(relation.equals("R1")) {
				quantR1.add(x.quantities.get(j));
			}
			if(relation.equals("R2")) {
				quantR2.add(x.quantities.get(j));
			}
			if(relation.equals("BOTH")) {
				quantR1.add(x.quantities.get(j));
				quantR2.add(x.quantities.get(j));
			}
		}
		Map<String, List<Double>> stats = new HashMap<>();
		stats.put("R1", Tools.uniqueNumbers(quantR1));
		stats.put("R2", Tools.uniqueNumbers(quantR2));
		return stats;
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
			RelationX x, List<Map<String, Integer>> segTemplates) {
		List<String> relations = Arrays.asList("R1", "R2", "BOTH", "NONE");
		List<RelationY> list1 = new ArrayList<>();
		list1.add(new RelationY());
		List<RelationY> list2 = new ArrayList<>();
//		System.out.println("NumQuant : "+x.quantities.size());
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
		System.out.println("Enumeration : "+list1.size());
		for(RelationY y : list1) {
			Map<String, Integer> stats = getStats(x, y);
//			System.out.println(Arrays.asList(stats));
			if(isTemplatePresent(segTemplates, stats)) {
				y.isOneVar = Tools.isOneVar(y.relations);
				list2.add(y);
			}
		}
//		System.out.println("Cluster Templates : "+clusterTemplates.size());
//		for(Map<String, Integer> template : clusterTemplates) {
//			System.out.println(Arrays.asList(template));
//		}
		System.out.println("After some pruning : "+list2.size());
		return list2;
	}
	
	public List<RelationY> enumerateClustersRespectingEquations(
			RelationX x, RelationY seed, Map<String, List<Double>> eqNumbers) {
//		System.out.println("Enumerating clusters respecting equations :");
//		System.out.println(x.problemIndex+" : "+x.ta.getText());
//		System.out.println(x.quantities);
//		System.out.println("EqNumbers : "+Arrays.asList(eqNumbers));
		seed.relations.clear();
		List<String> relations = Arrays.asList("R1", "R2", "BOTH", "NONE");
		List<RelationY> list1 = new ArrayList<>();
		list1.add(seed);
		List<RelationY> list2 = new ArrayList<>();
		for(int i=0; i<x.quantities.size(); ++i) {
			for(RelationY y : list1) {
				for(String relation : relations) {
					if(relation.equals("R1") && Tools.contains(
							eqNumbers.get("R1"), Tools.getValue(x.quantities.get(i)))) {
						RelationY yNew = new RelationY(y);
						yNew.relations.add(relation);
						list2.add(yNew);
					} else if(relation.equals("R2") && Tools.contains(
							eqNumbers.get("R2"), Tools.getValue(x.quantities.get(i)))) {
						RelationY yNew = new RelationY(y);
						yNew.relations.add(relation);
						list2.add(yNew);
					} else if(relation.equals("BOTH") && 
							Tools.contains(eqNumbers.get("R1"), Tools.getValue(x.quantities.get(i))) && 
							Tools.contains(eqNumbers.get("R2"), Tools.getValue(x.quantities.get(i)))) {
						RelationY yNew = new RelationY(y);
						yNew.relations.add(relation);
						list2.add(yNew);
					} else if(relation.equals("NONE")) {
						RelationY yNew = new RelationY(y);
						yNew.relations.add(relation);
						list2.add(yNew);
					}
				}
			}
			list1.clear();
			list1.addAll(list2);
			list2.clear();
		}
//		System.out.println("Enumeration : "+list1.size());
		for(RelationY y : list1) {
			Map<String, List<Double>> stats = clusterMap(x, y);
//			System.out.println("Candidate : "+Arrays.asList(stats));
			if(Tools.equals(stats.get("R1"), eqNumbers.get("R1")) && 
					Tools.equals(stats.get("R2"), eqNumbers.get("R2"))) {
				list2.add(y);
			} else if(Tools.equals(stats.get("R1"), eqNumbers.get("R2")) && 
					Tools.equals(stats.get("R2"), eqNumbers.get("R1"))) {
				list2.add(y);
			}
		}
//		System.out.println("After some pruning : "+list2.size());
		return list2;
	}
}
