package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class RelationInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private RelationFeatGen featGen;
	public List<Map<String, Integer>> clusterTemplates;

	public RelationInfSolver(RelationFeatGen featGen, 
			List<Map<String, Integer>> templates) {
		this.featGen = featGen;
		this.clusterTemplates = templates;
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
		List<String> relations = null;
		return null;
	}
	
	public static List<Map<String, Integer>> extractClusterTemplates(SLProblem slProb) {
		List<Map<String, Integer>> clusterTemplates = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			RelationY gold = (RelationY) struct;
			Map<String, Integer> stats = new HashMap<>();
			stats.put("R1", 0);
			stats.put("R2", 0);
			stats.put("BOTH", 0);
			for(String relation : gold.relations) {
				if(!relation.equals("NONE")) {
					stats.put(relation, stats.get(relation)+1);
				}
			}
			boolean allow = true;
			for(Map<String, Integer> map : clusterTemplates) {
				if(stats.get("R1") == map.get("R1") && stats.get("R2") == map.get("R2") && 
						stats.get("BOTH") == map.get("BOTH")) {
					allow = false;
					break;
				}
				if(stats.get("R1") == map.get("R2") && stats.get("R2") == map.get("R1") && 
						stats.get("BOTH") == map.get("BOTH")) {
					allow = false;
					break;
				}
			}
			if(allow) clusterTemplates.add(stats);
		}
		return clusterTemplates;
	}
	
	public List<RelationY> enumerateClustersRespectingTemplates(RelationX x) {
		List<RelationY> candidates = new ArrayList<>();
		return candidates;
	}
}
