package driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import de.bwaldvogel.liblinear.Model;
import parser.DocReader;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import relation.RelationDriver;
import relation.RelationX;
import relation.RelationY;
import semparse.SemDriver;
import semparse.SemX;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class FullSystem {

	public static boolean hasSameSolution (List<Double> soln1, List<Double> soln2) {
		if(soln1 == null || soln2 == null) return false;
		if(soln1.size() != soln2.size()) return false;
		for(Double d1 : soln1) {
			boolean found = false;
			for(Double d2 : soln2) {
				if(Tools.safeEquals(d1, d2)) {
					found = true;
					break;
				}
			}
			if(!found) return false;
		}
		return true;
	}
	
	public static Double computeAccuracy(List<SimulProb> simulProbList, 
			SLModel relationModel, SLModel equationModel) throws Exception {
		double acc = 0.0;
		for(SimulProb simulProb : simulProbList) {
			SimulProb test = new SimulProb(-1);
			test.question = simulProb.question;
			test.quantities = simulProb.quantities;
			test.chunks = simulProb.chunks;
			test.lemmas = simulProb.lemmas;
			test.parse = simulProb.parse;
			test.posTags = simulProb.posTags;
			test.skeleton = simulProb.skeleton;
			test.ta = simulProb.ta;
			
			List<Double> solutions1 = EquationSolver.solve(simulProb.equations);
			List<Double> solutions2 = JointInference.constrainedInference(
					test, relationModel, equationModel);
			if(hasSameSolution(solutions1, solutions2)) {
				acc += 1.0;
			} else {
				System.out.println(simulProb.index + " : " + simulProb.question);
				System.out.println("Quantities : "+simulProb.quantities);
//				System.out.println("Gold Relations : "+Arrays.asList(simulProb.relations));
//				System.out.println("Predict Relations : "+Arrays.asList(test.relations));
//				if((""+Arrays.asList(simulProb.relations)).equals(""+Arrays.asList(test.relations))) {
//					System.out.println("Relation Correct");
//				}
//				System.out.println("Gold Equations : "+Arrays.asList(simulProb.equations));
//				System.out.println("Predict Equations : "+Arrays.asList(test.equations));
				System.out.println("Gold Solutions : "+Arrays.asList(solutions1));
				System.out.println("Predict Solutions : "+Arrays.asList(solutions2));
			}
		}
		System.out.println("Accuracy : "+acc+" / "+simulProbList.size() + 
				" = " + (acc / simulProbList.size()));
		return (acc / simulProbList.size());
	}
	
	public static double doTest(int testFold) throws Exception {
		List<List<Integer>> folds = DocReader.extractFolds();
		List<SimulProb> simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		List<SimulProb> testProbs = new ArrayList<>();
		for(SimulProb simulProb : simulProbList) {
			if(folds.get(testFold).contains(simulProb.index)) {
				testProbs.add(simulProb);
			}
		}
		SLModel relationModel = SLModel.loadModel("rel"+testFold+".save");
		SLModel equationModel = SLModel.loadModel("sem"+testFold+".save");
		return computeAccuracy(testProbs, relationModel, equationModel);
	}
	
	public static void crossVal() throws Exception {
		double acc = 0.0;
		for(int i=0;i<5;i++) {
			acc += doTest(i);
		}
		System.out.println("5-fold CV : " + (acc/5));
	}
	
	public static void main(String args[]) throws Exception {
		doTest(0);
	}
}
