package lasttwo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import reader.DocReader;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class LasttwoDriver {
	
	public static String prefix = "models/lasttwo";
	
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
		trainModel(prefix+testFold+".save", train, testFold);
//		testModel(prefix+testFold+".save", train);
		return testModel(prefix+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) 
			throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			LasttwoX x = new LasttwoX(simulProb);
			LasttwoY y = new LasttwoY(simulProb);
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
			System.out.println("---------------------------------------------------");
			LasttwoX prob = (LasttwoX) sp.instanceList.get(i);
			LasttwoY gold = (LasttwoY) sp.goldStructureList.get(i);
			LasttwoY pred = (LasttwoY) model.infSolver.getBestStructure(model.wv, prob);
			System.out.println("VarScore : "+pred.varScore);
			total.add(prob.problemIndex);
			double goldWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, gold));
			double predWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, pred));
			if(goldWt > predWt) {
				System.out.println("PROBLEM HERE");
			}
			if(LasttwoY.getLoss(gold, pred) < 0.0001) {
				acc += 1;
			} else {
				incorrect.add(prob.problemIndex);
				System.out.println(prob.problemIndex+" : "+prob.ta.getText());
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Gold : \n"+gold);
				System.out.println("Gold weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, gold)));
				System.out.println("Pred : \n"+pred);
				System.out.println("Pred weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, pred)));
				System.out.println("VarScore : "+model.wv.dotProduct(
						((LasttwoFeatGen)model.featureGenerator).getVarTokenFeatureVector(prob, pred)));
				System.out.println("Loss : "+LasttwoY.getLoss(gold, pred));
			}
			System.out.println("---------------------------------------------------");
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
		LasttwoFeatGen fg = new LasttwoFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new LasttwoInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		para.MAX_NUM_ITER = 5;
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = latentSVMLearner(learner, train, 
				(LasttwoInfSolver) model.infSolver, 5, null);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}
	
	public static List<Map<String, List<Integer>>> enumerateVarTokens(
			Map<String, List<Integer>> seed) {
		List<Map<String, List<Integer>>> mapList = new ArrayList<>();
		List<Integer> v1 = seed.get("V1");
		List<Integer> v2 = seed.get("V2");
		if(v1 != null && v1.size() > 0 && (v2 == null || v2.size() == 0)) {
			for(Integer i : v1) {
				Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
				map.put("V1", Arrays.asList(i));
				mapList.add(map);
			}
		}
		if(v1 != null && v1.size() > 0 && v2 != null && v2.size() > 0) {
			for(Integer i : v1) {
				for(Integer j : v2) {
					Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
					map.put("V1", Arrays.asList(i));
					map.put("V2", Arrays.asList(j));
					mapList.add(map);
				}
			}
		}
		return mapList;
	}
	
	public static WeightVector latentSVMLearner(
			Learner learner, SLProblem sp, LasttwoInfSolver infSolver, 
			int numIter, WeightVector initialWv) throws Exception {
		WeightVector wv = initialWv;
		if(wv == null) {
			wv = new WeightVector(7000);
			wv.setExtendable(true);
		}
		for(int i=0; i<numIter; ++i) {
			System.err.println("Latent SSVM : Iteration "+i);
			SLProblem newProb = new SLProblem();
			for(int j=0; j<sp.goldStructureList.size(); ++j) {
				LasttwoX prob = (LasttwoX) sp.instanceList.get(j);
				LasttwoY gold = (LasttwoY) sp.goldStructureList.get(j);
				LasttwoY bestLatent = infSolver.getLatentBestStructure(prob, gold, wv);
				newProb.addExample(prob, bestLatent);
			}
			System.err.println("Learning SSVM");
			wv = learner.train(newProb, wv);
			System.err.println("Done");
		}
		return wv;
	}
	
	public static void main(String args[]) throws Exception {
		LasttwoDriver.doTrainTest(0);
		Tools.pipeline.closeCache();
		System.exit(0);
	}
}
