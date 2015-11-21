package var;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reader.DocReader;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class VarDriver {
	
	public static void crossVal() throws Exception {
		double acc = 0.0;
		for(int i=0;i<5;i++) {
			acc += doTrainTest(i);
		}
		System.out.println("5-fold CV : " + (acc/5));
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
		trainModel("models/var"+testFold+".save", train, testFold);
		return testModel("models/var"+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) 
			throws Exception {
		if(simulProbList == null) {
			simulProbList = 
					DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			VarX x = new VarX(simulProb);
			VarY y = new VarY(simulProb);
			problem.addExample(x, y);
		}
		return problem;
	}

	public static double testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		Set<Integer> incorrect = new HashSet<>();
		Set<Integer> total = new HashSet<>();
		double acc = 0.0;
		for (int i = 0; i < sp.instanceList.size(); i++) {
			VarX prob = (VarX) sp.instanceList.get(i);
			VarY gold = (VarY) sp.goldStructureList.get(i);
			VarY pred = (VarY) model.infSolver.getBestStructure(
					model.wv, prob);
			total.add(prob.problemIndex);
			double goldWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, gold));
			double predWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, pred));
			if(goldWt > predWt) {
				System.out.println("PROBLEM HERE");
			}
			if(VarY.getLoss(gold, pred) < 0.0001) {
				acc += 1;
			} else {
				incorrect.add(prob.problemIndex);
				System.out.println(prob.problemIndex+" : "+prob.ta.getText());
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Gold : "+gold);
				print(prob, gold);
				System.out.println("Gold weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, gold)));
				System.out.println("Pred : "+pred);
				print(prob, pred);
				System.out.println("Pred weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, pred)));
				System.out.println("Loss : "+VarY.getLoss(gold, pred));
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
		VarFeatGen fg = new VarFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new VarInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		para.MAX_NUM_ITER = 5;
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
//		model.wv = learner.train(train);
		model.wv = latentSVMLearner(learner, train, 
				(VarInfSolver) model.infSolver, 5);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}

	public static WeightVector latentSVMLearner(
			Learner learner, SLProblem sp, VarInfSolver infSolver, 
			int maxIter) throws Exception {
		WeightVector wv = new WeightVector(7000);
		wv.setExtendable(true);
		for(int i=0; i<maxIter; ++i) {
			System.err.println("Latent SSVM : Iteration "+i);
			SLProblem newProb = new SLProblem();
			for(int j=0; j<sp.goldStructureList.size(); ++j) {
				VarX prob = (VarX) sp.instanceList.get(j);
				VarY gold = (VarY) sp.goldStructureList.get(j);
//				System.out.println("GetLatent : "+prob.problemIndex+" : "+gold);
				VarY bestLatent = infSolver.getLatentBestStructure(prob, gold, wv);
//				System.out.println("BestLatent : "+bestLatent);
				newProb.addExample(prob, bestLatent);
			}
//			System.out.println("Got all latent stuff");
//			for(int j=0; j<newProb.size(); ++j) {
//				TreeX prob = (TreeX) newProb.instanceList.get(j);
//				TreeY gold = (TreeY) newProb.goldStructureList.get(j);
//				System.out.println("X:"+prob.problemIndex+" Y:"+gold);
//			}
			System.err.println("Learning SSVM");
			wv = learner.train(newProb, wv);
			System.err.println("Done");
		}
		return wv;
	}
	
	public static void main(String args[]) throws Exception {
//		VarDriver.crossVal();
		VarDriver.doTrainTest(0);
		Tools.pipeline.closeCache();
		System.exit(0);;
	}
	
	public static void print(VarX x, VarY y) {
		for(String key : y.varTokens.keySet()) {
			String str = key +" : ";
			for(Integer index : y.varTokens.get(key)) {
				IntPair span = x.candidateVars.get(index);
				for(int i=span.getFirst(); i<span.getSecond(); ++i) {
					str += x.ta.getToken(i)+" ";
				}
				str += " ||| ";
			}
			System.out.println(str);
		}
	}
}
