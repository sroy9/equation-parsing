package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import reader.DocReader;
import structure.Equation;
import structure.Node;
import structure.SimulProb;
import tree.TreeDriver;
import tree.TreeInfSolver;
import tree.TreeX;
import tree.TreeY;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class TemplateDriver {
	
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
		trainModel("models/template"+testFold+".save", train, testFold);
		return testModel("models/template"+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) 
			throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			TemplateX x = new TemplateX(simulProb);
			TemplateY y = new TemplateY(simulProb);
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
			TemplateX prob = (TemplateX) sp.instanceList.get(i);
			TemplateY gold = (TemplateY) sp.goldStructureList.get(i);
			TemplateY pred = (TemplateY) model.infSolver.getBestStructure(
					model.wv, prob);
			total.add(prob.problemIndex);
			double goldWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, gold));
			double predWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, pred));
			if(goldWt > predWt) {
				System.out.println("PROBLEM HERE");
			}
			if(TemplateY.getLoss(gold, pred) < 0.0001) {
				acc += 1;
			} else {
				incorrect.add(prob.problemIndex);
				System.out.println(prob.problemIndex+" : "+prob.ta.getText());
				System.out.println("Skeleton : "+Tools.skeletonString(prob.skeleton));
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Gold : \n"+gold);
				System.out.println("Gold weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, gold)));
				System.out.println("Pred : \n"+pred);
				System.out.println("Pred weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, pred)));
				System.out.println("Loss : "+TemplateY.getLoss(gold, pred));
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
		TemplateFeatGen fg = new TemplateFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new TemplateInfSolver(fg, extractTemplates(train));
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
//		model.wv = learner.train(train);
		model.wv = latentSVMLearner(learner, train, 
				(TemplateInfSolver) model.infSolver, 5);
		model.wv = learner.train(train);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}
	
	public static void main(String args[]) throws Exception {
		TemplateDriver.doTrainTest(0);
	}
	
	public static List<TemplateY> extractTemplates(SLProblem slProb) {
		List<TemplateY> templates = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			TemplateY gold = new TemplateY((TemplateY) struct);
			for(Node node : gold.equation.root.getLeaves()) {
				if(node.label.equals("NUM")) {
					node.value = 0.0;
				}
			}
			gold.varTokens.clear();
			boolean alreadyPresent = false;
			for(int i=0; i< templates.size(); ++i) { 
				double loss = Math.min(Node.getLoss(gold.equation.root, 
						templates.get(i).equation.root, true),
						Node.getLoss(gold.equation.root, 
								templates.get(i).equation.root, false));
				if(loss < 0.0001) {
					alreadyPresent = true;
					break;
				}
			}
			if(!alreadyPresent) {
				templates.add(gold);
			}
		}
		System.out.println("Number of templates : "+templates.size());
		return templates;
	}
	
	public static WeightVector latentSVMLearner(
			Learner learner, SLProblem sp, TemplateInfSolver infSolver, 
			int maxIter) throws Exception {
		WeightVector wv = new WeightVector(7000);
		wv.setExtendable(true);
		for(int i=0; i<maxIter; ++i) {
			System.err.println("Latent SSVM : Iteration "+i);
			SLProblem newProb = new SLProblem();
			for(int j=0; j<sp.goldStructureList.size(); ++j) {
				TemplateX prob = (TemplateX) sp.instanceList.get(j);
				TemplateY gold = (TemplateY) sp.goldStructureList.get(j);
//				System.out.println("GetLatent : "+prob.problemIndex+" : "+gold);
				TemplateY bestLatent = infSolver.getLatentBestStructure(prob, gold, wv);
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
	
}
