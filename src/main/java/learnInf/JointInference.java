package learnInf;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import relation.RelationInfSolver;
import relation.RelationX;
import relation.RelationY;
import semparse.SemInfSolver;
import semparse.SemX;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLModel;

public class JointInference {
	
	public static Double getSolutionScore(List<Double> solutions) {
		Double score = 0.0;
		if(solutions == null) return 0.0;
		for(Double d : solutions) {
			if(d>0) {
				score += 10.0;
				if(d-d.intValue() < 0.001 || d-d.intValue() > 0.99){
					score += 10.0;
				}
			}
		}
		return score;
	}
	
	public static List<Double> constrainedInference(SimulProb simulProb,
			SLModel relationModel, SLModel equationModel) throws Exception {
		PairComparator<List<Double>> solnComparator = new PairComparator<List<Double>>() {};
		MinMaxPriorityQueue<Pair<List<Double>, Double>> beam = 
				MinMaxPriorityQueue.orderedBy(solnComparator).maximumSize(10).create();
		simulProb.relations.clear();
		RelationX relationX = new RelationX(simulProb);
		relationModel.infSolver.getBestStructure(relationModel.wv, relationX);
		for(Pair<RelationY, Double> pair1 : ((RelationInfSolver)relationModel.infSolver).beam) {
			List<SemX> clusters = SemX.extractEquationProbFromRelations(relationX, pair1.getFirst());
			if(clusters.size() == 1) {
				equationModel.infSolver.getBestStructure(equationModel.wv, clusters.get(0));
				for(Pair<SemY, Double> pair2 : ((SemInfSolver) equationModel.infSolver).beam) {
					List<SemY> equations = new ArrayList<>();
					equations.add(pair2.getFirst());
					List<Double> solns = EquationSolver.solveSemYs(equations);
					beam.add(new Pair<List<Double>, Double>(solns, 
							getSolutionScore(solns) + pair1.getSecond() + pair2.getSecond()));
				}
			} 
			if(clusters.size() == 2) {
				equationModel.infSolver.getBestStructure(equationModel.wv, clusters.get(0));
				List<Pair<SemY, Double>> list1 = new ArrayList<>();
				list1.addAll(((SemInfSolver) equationModel.infSolver).beam);
				equationModel.infSolver.getBestStructure(equationModel.wv, clusters.get(1));
				List<Pair<SemY, Double>> list2 = new ArrayList<>();
				list2.addAll(((SemInfSolver) equationModel.infSolver).beam);
				for(Pair<SemY, Double> pair2 : list1) {
					for(Pair<SemY, Double> pair3 : list2) {
						List<SemY> equations = new ArrayList<>();
						equations.add(pair2.getFirst());
						equations.add(pair3.getFirst());
						List<Double> solns = EquationSolver.solveSemYs(equations);
						beam.add(new Pair<List<Double>, Double>(solns, 
								getSolutionScore(solns) + pair1.getSecond() + pair2.getSecond() + 
								pair3.getSecond()));
						
					}
				}
			}
		}
		return beam.element().getFirst();
	}

}
