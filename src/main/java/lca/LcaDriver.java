package lca;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reader.DocReader;
import structure.Node;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
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
			for(Node leaf1 : simulProb.equation.root.getLeaves()) {
				for(Node node1 : enumerateVariableInstantiations(simulProb, leaf1)) {
					for(Node leaf2 : simulProb.equation.root.getLeaves()) {
						if(leaf1 == leaf2) continue;
						for(Node node2 : enumerateVariableInstantiations(simulProb, leaf2)) {
							LcaX x = new LcaX(simulProb, node1, node2);
							LcaY y = new LcaY(simulProb, node1, node2);
							problem.addExample(x, y);
						}
					}
				}
			}
		}
		return problem;
	}
	
	public static List<Node> enumerateVariableInstantiations(SimulProb simulProb, Node leaf) {
		List<Node> nodeList = new ArrayList<>();
		if(leaf.label.equals("VAR") && simulProb.varTokens.keySet().contains(leaf.varId)) {
			for(int id : simulProb.varTokens.get(leaf.varId)) {
				Node node = new Node(leaf);
				node.index = id;
				nodeList.add(node);
			}
		}
		if(leaf.label.equals("NUM")) {
			for(int i=0; i<simulProb.quantities.size(); ++i) {
				QuantSpan qs  = simulProb.quantities.get(i);
				if(Tools.safeEquals(Tools.getValue(qs), leaf.value)) {
					Node node = new Node(leaf);
					node.index = i;
					nodeList.add(node);
					break;
				}
			}
		}
		return nodeList;
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
				System.out.println("Nodes of interest : "+prob.leaf1+" : "+prob.leaf2);
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
		System.out.println("Strict Accuracy : ="+ (1-1.0*incorrect.size()/total.size()) + 
				" incorrect "+ incorrect.size() + " out of "+total.size());
		return (1-1.0*incorrect.size()/total.size());
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

	public static void main(String args[]) throws Exception {
		LcaDriver.crossVal();
	}
}