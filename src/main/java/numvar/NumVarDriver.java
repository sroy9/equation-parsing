package numvar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java_cup.internal_error;
import reader.DocReader;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class NumVarDriver {
	
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
		trainModel("models/numvar"+testFold+".save", train);
		return testModel("models/numvar"+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb prob : simulProbList) {
			NumVarX x = new NumVarX(prob);
			NumVarY y;
			if(prob.equations.size() == 1) {
				y = new NumVarY(true);
			} else {
				y = new NumVarY(false);
			}	
			problem.addExample(x, y);
		}
		return problem;
	}

	private static double testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		double acc = 0.0;
		double total = sp.instanceList.size();
		for (int i = 0; i < sp.instanceList.size(); i++) {
			NumVarX prob = (NumVarX) sp.instanceList.get(i);
			NumVarY gold = (NumVarY) sp.goldStructureList.get(i);
			NumVarY pred = (NumVarY) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			double goldWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, gold));
			double predWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, pred));
			if(goldWt > predWt) {
				System.out.println("PROBLEM HERE");
			}
			if (NumVarY.getLoss(gold, pred) < 0.00001) {
				acc += 1.0;
			} else {
//				System.out.println("Gold : \n"+gold);
//				System.out.println("Gold weight : "+goldWt);
//				System.out.println("Pred : \n"+pred);
//				System.out.println("Pred weight : "+predWt);
//				System.out.println("Loss : "+PartitionY.getLoss(gold, pred));
			}
		}
		System.out.println("Accuracy : " + acc + " / " + total + " = "
				+ (acc / total));
		return acc / total;
	}
	
	public static void trainModel(String modelPath, SLProblem train)
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
		NumVarDriver.doTrainTest(0);
	}
}
