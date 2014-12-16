package equationmatch;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import parser.DocReader;
import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class EquationMatcher {
	
	public static void crossVal() throws Exception {
		SLProblem problem = getSP();
		List<Pair<SLProblem, SLProblem>> folds = problem.splitDataToNFolds(
				5, new Random());
		for(int i=0;i<folds.size();i++) {
			Pair<SLProblem, SLProblem> fold = folds.get(i);
			SLProblem train = fold.getFirst();
			SLProblem test = fold.getSecond();
			trainModel("cvFold"+i+".model",train);
			testModel("cvFold"+i+".model", test);
		}
		
	}
	public static void doTrainTest() throws Exception
	{
		SLProblem problem=getSP();
		double trainFrac = 0.5;
		Pair<SLProblem, SLProblem> trainTest = problem
				.splitTrainTest((int) (trainFrac * problem.size()));
		SLProblem train = trainTest.getFirst();
		SLProblem test = trainTest.getSecond();
		trainModel("model.save", train);
		testModel("model.save", test);
	}

	private static SLProblem getSP() throws Exception {
		DocReader dr = new DocReader();
		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(Params.annotationDir);
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			Blob blob = new Blob(simulProb);
			problem.addExample(blob, blob.getGold());
		}
		return problem;
	}

	private static void testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		double acc = 0.0;
		double structAcc = 0.0;
		double total = sp.instanceList.size();
		for (int i = 0; i < sp.instanceList.size(); i++) {
			Blob blob = (Blob) sp.instanceList.get(i);
			Lattice gold = (Lattice) sp.goldStructureList.get(i);
			Lattice prediction = (Lattice) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			System.out.println(blob.simulProb.index+" : "+blob.simulProb.question);
			System.out.println("Gold : \n" + gold);
			System.out.println("Predicted : \n" + prediction);
			structAcc += getStructAcc(prediction, gold);
			if (hasSameSolution(prediction, gold)) {
				acc += 1.0;
			}
		}
		System.out.println("Structural Accuracy : " + structAcc + " / " + total + " = "
				+ (structAcc / total));
		System.out.println("Accuracy : " + acc + " / " + total + " = "
				+ (acc / total));
	}

	private static double getStructAcc(Lattice prediction, Lattice gold) {
		double acc1 = 0.0, acc2 = 0.0, total = 0.0;
		for(int i=0; i<2; ++i) {
			Equation eqPred1 = prediction.equations.get(i);
			Equation eqPred2 = prediction.equations.get(1-i);
			Equation eqGold = gold.equations.get(i);
			total+=6.0;
			if(isEqual(eqPred1.A1, eqGold.A1)) {
				acc1 += 1.0;
			}
			if(isEqual(eqPred1.A2, eqGold.A2)) {
				acc1 += 1.0;
			}
			if(isEqual(eqPred1.B1, eqGold.B1)) {
				acc1 += 1.0;
			}
			if(isEqual(eqPred1.B2, eqGold.B2)) {
				acc1 += 1.0;
			}
			if(isEqual(eqPred1.C, eqGold.C)) {
				acc1 += 1.0;
			}
			if(Arrays.asList(eqPred1.operations).toString().equals(
					Arrays.asList(eqGold.operations))){
				acc1 += 1.0;
			}
			if(isEqual(eqPred2.A1, eqGold.A1)) {
				acc2 += 1.0;
			}
			if(isEqual(eqPred2.A2, eqGold.A2)) {
				acc2 += 1.0;
			}
			if(isEqual(eqPred2.B1, eqGold.B1)) {
				acc2 += 1.0;
			}
			if(isEqual(eqPred2.B2, eqGold.B2)) {
				acc2 += 1.0;
			}
			if(isEqual(eqPred2.C, eqGold.C)) {
				acc2 += 1.0;
			}
			if(Arrays.asList(eqPred2.operations).toString().equals(
					Arrays.asList(eqGold.operations))){
				acc2 += 1.0;
			}
		}
		return Math.max(acc1, acc2)/total;
	}
	
	private static boolean isEqual(List<Pair<Operation, Double>> a1,
			List<Pair<Operation, Double>> a2) {
		if(a1.size() != a2.size()) {
			return false;
		}
		for(Pair<Operation, Double> pair1 : a1) {
			boolean found = false;
			for(Pair<Operation, Double> pair2 : a2) {
				if(pair1.getFirst() == pair2.getFirst() &&
						Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) {
					found = true;
					break;
				}
			}
			if(!found) return false;
		}
		return true;
	}
	private static boolean hasSameSolution(Lattice prediction, Lattice gold) {
		List<Double> solutions1 = EquationSolver.solve(prediction);
		List<Double> solutions2 = EquationSolver.solve(gold);
		if(solutions1 == null || solutions2 == null) return false;
		if(solutions1.size() != solutions2.size()) return false;		
		if(solutions1.size() == 1) {
			if(Tools.safeEquals(solutions1.get(0), solutions2.get(0))) {
				return true; 
			}
		}	
		if(solutions1.size() == 2) {
			if(Tools.safeEquals(solutions1.get(0), solutions2.get(0)) &&
					Tools.safeEquals(solutions1.get(1), solutions2.get(1)) ) {
				return true; 
			}
			if(Tools.safeEquals(solutions1.get(0), solutions2.get(1)) &&
					Tools.safeEquals(solutions1.get(1), solutions2.get(0)) ) {
				return true; 
			}
		}
		return false;
	}
	
	private static void trainModel(String modelPath, SLProblem train)
			throws Exception {
		SLModel model = new SLModel();
		Lexiconer lm = new Lexiconer();
		lm.setAllowNewFeatures(true);
		model.lm = lm;
		EquationFeatureExtractor fg = new EquationFeatureExtractor(lm);
		model.featureGenerator = fg;
		model.infSolver = new EquationInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}
	
	public static void main(String args[]) throws Exception {
		EquationMatcher.doTrainTest();
	}
}
