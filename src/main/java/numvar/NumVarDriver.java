package numvar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import parser.DocReader;
import semparse.SemInfSolver;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.SimulProb;
import utils.FeatGen;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMDCDSolver;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class NumVarDriver {
	
	public static void crossVal() throws Exception {
		double acc = 0.0;
		for(int i=0;i<5;i++) {
			acc += doTrainTest(i);
		}
		System.out.println("5-fold CV : " + (acc/5));
	}
	
	public static double doTrainTest(int testFold) throws Exception {
		List<List<Integer>> folds = DocReader.extractFolds();
		List<SimulProb> simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		List<SimulProb> trainProbs = new ArrayList<>();
		List<SimulProb> testProbs = new ArrayList<>();
		for(SimulProb simulProb : simulProbList) {
			if(folds.get(testFold).contains(simulProb.index)) {
				testProbs.add(simulProb);
			} else /*if(FeatGen.getQuestionSentences(simulProb.ta).size() < 2) */{
				trainProbs.add(simulProb);
			}
		}
		SLProblem train = getSP(trainProbs);
		SLProblem test = getSP(testProbs);
		trainModel("models/numvar"+testFold+".save", train, testFold);
		return testModel("models/numvar"+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			NumVarX relationX = new NumVarX(simulProb);
			NumVarY relationY = new NumVarY(simulProb);
			problem.addExample(relationX, relationY);
		}
		return problem;
	}

	public static double testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		double acc = 0.0;
		for (int i = 0; i < sp.instanceList.size(); i++) {
			NumVarX prob = (NumVarX) sp.instanceList.get(i);
			NumVarY gold = (NumVarY) sp.goldStructureList.get(i);
			NumVarY pred = null;
			if(FeatGen.getQuestionSentences(prob.ta).size() < 2) {
				pred = (NumVarY) model.infSolver.getBestStructure(model.wv, prob);
			} else {
				pred = new NumVarY(false);
			}
			if(NumVarY.getLoss(gold, pred) < 0.0001) {
				acc += 1;
			} else {
				System.out.println(prob.problemIndex+" : "+prob.ta.getText());
				System.out.println("Skeleton : "+Tools.skeletonString(prob.skeleton));
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Gold : \n"+gold);
				System.out.println("Gold weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, gold)));
				System.out.println("Pred : \n"+pred);
				System.out.println("Pred weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, pred)));
				System.out.println("Loss : "+NumVarY.getLoss(gold, pred));
			}
		}
		System.out.println("Accuracy : = " + acc + " / " + sp.instanceList.size() 
				+ " = " + (acc/sp.instanceList.size()));
		return (acc/sp.instanceList.size());
	}
	
	public static void trainModel(String modelPath, SLProblem train, int testFold) 
			throws Exception {
		SLModel model = new SLModel();
		Lexiconer lm = new Lexiconer();
		lm.setAllowNewFeatures(true);
		model.lm = lm;
		NumVarFeatGen fg = new NumVarFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new NumVarInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}
	
	public static void main(String args[]) throws Exception {
//		RelationDriver.doTrainTest(0);
		NumVarDriver.crossVal();
	}
}
