package semparse;

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

public class SemDriver {
	
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
		double trainFrac = 0.8;
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
			SemX semX = new SemX(simulProb, "R1");
			SemY semY = new SemY(simulProb.equations.get(0));
			if(semY.emptySlots.size() > 0) {
				problem.addExample(semX, semY);	
			}
			semX = new SemX(simulProb, "R2");
			semY = new SemY(simulProb.equations.get(1));
			if(semY.emptySlots.size() > 0) {
				problem.addExample(semX, semY);	
			}
		}
		return problem;
	}

	private static void testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		double acc = 0.0;
		double total = sp.instanceList.size();
		for (int i = 0; i < sp.instanceList.size(); i++) {
			SemX prob = (SemX) sp.instanceList.get(i);
			SemY gold = (SemY) sp.goldStructureList.get(i);
			SemY pred = (SemY) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			System.out.println("Text : "+prob.ta.getText());
			System.out.println("Skeleton : "+Tools.skeletonString(prob.skeleton));
			System.out.println("Gold : \n"+gold);
			System.out.println("Gold weight : "+model.wv.dotProduct(model.featureGenerator.getFeatureVector(prob, gold)));
			System.out.println("Pred : \n"+pred);
			System.out.println("Pred weight : "+model.wv.dotProduct(model.featureGenerator.getFeatureVector(prob, pred)));
			System.out.println("Loss : "+SemY.getLoss(gold, pred));
			if (SemY.getLoss(gold, pred) < 0.00001) {
				acc += 1.0;
			}
		}
		System.out.println("Accuracy : " + acc + " / " + total + " = "
				+ (acc / total));
	}
	
	private static void trainModel(String modelPath, SLProblem train)
			throws Exception {
		SLModel model = new SLModel();
		Lexiconer lm = new Lexiconer();
		lm.setAllowNewFeatures(true);
		model.lm = lm;
		SemFeatGen fg = new SemFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new SemInfSolver(fg, SemInfSolver.extractTemplates(train));
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}
	
	public static void main(String args[]) throws Exception {
		SemDriver.doTrainTest();
	}
}
