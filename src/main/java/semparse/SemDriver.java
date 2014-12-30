package semparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
		for(int i=0;i<5;i++) {
			doTrainTest(i);
		}
	}
	
	public static void doTrainTest(int testFold) throws Exception {
		List<List<Integer>> folds = DocReader.extractFolds();
		List<SimulProb> simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		List<SimulProb> trainProbs = new ArrayList<>();
		List<SimulProb> testProbs = new ArrayList<>();
		for(SimulProb simulProb : simulProbList) {
			if(folds.get(testFold).contains(simulProb.index)) {
				testProbs.add(simulProb);
			} else {
				trainProbs.add(simulProb);
			}
		}
		SLProblem train = getSP(trainProbs);
		SLProblem test = getSP(testProbs);
		trainModel("sem"+testFold+".save", train);
		testModel("sem"+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
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
		Set<Integer> incorrect = new HashSet<>();
		Set<Integer> tot = new HashSet<>();  
		for (int i = 0; i < sp.instanceList.size(); i++) {
			SemX prob = (SemX) sp.instanceList.get(i);
			SemY gold = (SemY) sp.goldStructureList.get(i);
			SemY pred = (SemY) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			tot.add(prob.problemIndex);
			if (SemY.getLoss(gold, pred) < 0.00001) {
				acc += 1.0;
			} else {
				incorrect.add(prob.problemIndex);
				System.out.println("Text : "+prob.ta.getText());
				System.out.println("Skeleton : "+Tools.skeletonString(prob.skeleton));
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Gold : \n"+gold);
				System.out.println("Gold weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, gold)));
				System.out.println("Pred : \n"+pred);
				System.out.println("Pred weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, pred)));
				System.out.println("Loss : "+SemY.getLoss(gold, pred));
			}
		}
		System.out.println("Accuracy : " + acc + " / " + total + " = "
				+ (acc / total));
		System.out.println("Problem Accuracy : = 1 - " + incorrect.size() + "/" 
				+ tot.size() 
				+ " = " + (1-(incorrect.size()*1.0/tot.size())));
	}
	
	public static void trainModel(String modelPath, SLProblem train)
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
		SemDriver.crossVal();
	}
}
