package semparse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parser.DocReader;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class SemDriver {
	
	public static void crossVal() throws Exception {
		double crossValAcc = 0.0;
		for(int i=0;i<5;i++) {
			crossValAcc += doTrainTest(i);
		}
		System.out.println("5-fold CV : "+(crossValAcc/5));
	}
	
	public static double doTrainTest(int testFold) throws Exception {
		List<List<Integer>> folds = DocReader.extractFolds();
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir);
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
		trainModel("models/sem"+testFold+".save", train);
		return testModel("models/sem"+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			for(int i=0; i<simulProb.equations.size(); ++i) {
				SemX semX = new SemX(simulProb);
				SemY semY = new SemY(simulProb);
				problem.addExample(semX, semY);
			}
		}
		return problem;
	}

	private static double testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		double acc = 0.0;
		double nonEmptyAcc = 0.0;
		double nonEmptyTot = 0.0;
		double total = sp.instanceList.size();
		for (int i = 0; i < sp.instanceList.size(); i++) {
			SemX prob = (SemX) sp.instanceList.get(i);
			SemY gold = (SemY) sp.goldStructureList.get(i);
			SemY pred = (SemY) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			double goldWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, gold));
			double predWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, pred));
			if(goldWt > predWt) {
				System.out.println("PROBLEM HERE");
			}
			if(gold.nodes.size() != 0) nonEmptyTot += 1.0;
			if (SemY.getLoss(gold, pred) < 0.00001) {
				acc += 1.0;
				if(gold.nodes.size() != 0) nonEmptyAcc += 1.0; 
			} else {
				System.out.println("Text : "+prob.ta.getText());
				System.out.println("Skeleton : "+Tools.skeletonString(prob.skeleton));
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Gold : \n"+gold);
				System.out.println("Gold weight : "+goldWt);
				System.out.println("Pred : \n"+pred);
				System.out.println("Pred weight : "+predWt);
				System.out.println("Loss : "+SemY.getLoss(gold, pred));
			}
		}
		System.out.println("Accuracy : " + acc + " / " + total + " = "
				+ (acc / total));
		System.out.println("Non Empty Accuracy : " + nonEmptyAcc + " / " + 
				nonEmptyTot + " = " + (nonEmptyAcc / nonEmptyTot));
		return acc / total;
	}
	
	public static void trainModel(String modelPath, SLProblem train)
			throws Exception {
		SLModel model = new SLModel();
		Lexiconer lm = new Lexiconer();
		lm.setAllowNewFeatures(true);
		model.lm = lm;
		SemFeatGen fg = new SemFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new SemInfSolver(fg);
//		return;
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}
	
	public static void main(String args[]) throws Exception {
//		SemDriver.crossVal();
		SemDriver.doTrainTest(0);
	}
}
