package struct.lca;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import reader.DocReader;
import structure.Node;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class LcaDriver {
	
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
		trainModel("models/lca"+testFold+".save", train, testFold);
		return testModel("models/lca"+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) 
			throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			for(Map<String, List<Integer>> varTokens : 
					enumerateVarTokens(simulProb.varTokens)) {
				List<Node> nodes = new ArrayList<Node>();
				for(Node leaf : simulProb.equation.root.getLeaves()) {
					Node node = new Node(leaf);
					if(node.label.equals("VAR") && varTokens.containsKey(node.varId) &&
							varTokens.get(node.varId).size()>0) {
						node.index = varTokens.get(node.varId).get(0);
					}
					if(node.label.equals("NUM")) {
						for(int i=0; i<simulProb.quantities.size(); ++i) {
							if(Tools.safeEquals(Tools.getValue(
									simulProb.quantities.get(i)), node.value)) {
								node.index = i;
								break;
							}
						}
					}
					nodes.add(node);
				}
				LcaX x = new LcaX(simulProb, varTokens, nodes);
				LcaY y = new LcaY(simulProb);
				problem.addExample(x, y);
			}
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
			LcaX prob = (LcaX) sp.instanceList.get(i);
			LcaY gold = (LcaY) sp.goldStructureList.get(i);
			LcaY pred = (LcaY) model.infSolver.getBestStructure(
					model.wv, prob);
			total.add(prob.problemIndex);
			double goldWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, gold));
			double predWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, pred));
			if(goldWt > predWt) {
				System.out.println("PROBLEM HERE");
			}
			if(LcaY.getLoss(gold, pred) < 0.0001) {
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
				System.out.println("Loss : "+LcaY.getLoss(gold, pred));
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
		LcaFeatGen fg = new LcaFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new LcaInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
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
	public static void main(String args[]) throws Exception {
		LcaDriver.crossVal();
	}
}