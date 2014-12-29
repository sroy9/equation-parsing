package driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import parser.DocReader;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import relation.RelationDriver;
import relation.RelationX;
import relation.RelationY;
import semparse.SemDriver;
import semparse.SemX;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class FullSystem {

	public static List<Double> getSolutions(SimulProb simulProb,
			SLModel relationModel, SLModel equationModel) throws Exception {
		List<Double> solutions = new ArrayList<>();
		simulProb.relations.clear();
		for(int i=0; i<simulProb.quantities.size(); ++i) {
			RelationX relationX = new RelationX(simulProb, i);
			simulProb.relations.add(
					((RelationY) relationModel.infSolver.getBestStructure(
					relationModel.wv, relationX)).relation);
		}
		SemX semX1 = new SemX(simulProb, "R1");
		SemY semY1 = (SemY) equationModel.infSolver.getBestStructure(
				equationModel.wv, semX1);
		simulProb.equations.clear();
		simulProb.equations.add(semY1);
		if(semY1 == null) return null;
		if(semY1.operations.get(0) == Operation.NONE || 
				semY1.operations.get(2) == Operation.NONE) {
			simulProb.equations.add(new Equation());
		} else {
			SemX semX2 = new SemX(simulProb, "R2");
			SemY semY2 = (SemY) equationModel.infSolver.getBestStructure(
					equationModel.wv, semX2);
			if(semY2 == null) {
				semY2 = new SemY();
				semY2.operations.set(0, Operation.ADD);
				semY2.operations.set(2, Operation.ADD);
			}
			simulProb.equations.add(semY2);
		}
		solutions = EquationSolver.solve(simulProb.equations);
		return solutions;
	}
	
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
		double acc = 0.0, relationAcc = 0.0;
		for(SimulProb simulProb : simulProbList) {
			SimulProb test = new SimulProb(-1);
			test.question = simulProb.question;
			test.quantities = simulProb.quantities;
			test.chunks = simulProb.chunks;
			test.lemmas = simulProb.lemmas;
			test.parse = simulProb.parse;
			test.posTags = simulProb.posTags;
			test.skeleton = simulProb.skeleton;
//			test.relations = simulProb.relations;
			test.ta = simulProb.ta;
			
			List<Double> solutions1 = EquationSolver.solve(simulProb.equations);
			List<Double> solutions2 = getSolutions(test, relationModel, equationModel);

			if((""+Arrays.asList(simulProb.relations)).equals(""+Arrays.asList(test.relations))) {
				relationAcc += 1.0;
			}
			if(hasSameSolution(solutions1, solutions2)) {
				acc += 1.0;
			} else {
				System.out.println(simulProb.index + " : " + simulProb.question);
				System.out.println("Quantities : "+simulProb.quantities);
				System.out.println("Gold Relations : "+Arrays.asList(simulProb.relations));
				System.out.println("Predict Relations : "+Arrays.asList(test.relations));
				if((""+Arrays.asList(simulProb.relations)).equals(""+Arrays.asList(test.relations))) {
					System.out.println("Relation Correct");
				}
				System.out.println("Gold Equations : "+Arrays.asList(simulProb.equations));
				System.out.println("Predict Equations : "+Arrays.asList(test.equations));
				System.out.println("Gold Solutions : "+Arrays.asList(solutions1));
				System.out.println("Predict Solutions : "+Arrays.asList(solutions2));
			}
		}

		System.out.println("Relation Accuracy : "+relationAcc+" / "+simulProbList.size() + 
				" = " + (relationAcc / simulProbList.size()));
		
		System.out.println("Accuracy : "+acc+" / "+simulProbList.size() + 
				" = " + (acc / simulProbList.size()));
		return 0.0;
	}
	
	public static void doTrainTest(String relationModel, String equationModel) 
			throws Exception {
		List<SimulProb> train = DocReader.readSimulProbFromBratDir(
				Params.annotationDir, 0.0, 0.8);
		List<SimulProb> test = DocReader.readSimulProbFromBratDir(
				Params.annotationDir, 0.8, 1.0);
//		RelationDriver.trainModel(relationModel, RelationDriver.getSP(train));
//		SemDriver.trainModel(equationModel, SemDriver.getSP(train));
		computeAccuracy(test, SLModel.loadModel(relationModel), SLModel.loadModel(equationModel));
	}
	
	public static void main(String args[]) throws Exception {
		doTrainTest("relationModel.save", "equationModel.save");
	}
	
}
