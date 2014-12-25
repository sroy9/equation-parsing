package relation;

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

public class RelationDriver {
	
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
	public static void doTrainTest() throws Exception {
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
			for(int i=0; i<simulProb.quantities.size(); ++i) {
				RelationX relationX = new RelationX(simulProb, i);
				RelationY relationY = new RelationY(simulProb, i);
				problem.addExample(relationX, relationY);
			}
		}
		return problem;
	}

	private static void testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		double loss = 0.0;
		double total = 0.0;
		double acc = 0.0;
		for (int i = 0; i < sp.instanceList.size(); i++) {
			RelationX x = (RelationX) sp.instanceList.get(i);
			RelationY gold = (RelationY) sp.goldStructureList.get(i);
			RelationY pred = (RelationY) model.infSolver.getBestStructure(
					model.wv, x);
			if(RelationY.getLoss(gold, pred) < 0.000001) {
				acc += 1;
			}
			loss += RelationY.getLoss(gold, pred);
			total += gold.relations.size();
			printPrediction(x, gold, pred);
		}
		System.out.println("Structural Accuracy : = " + (1.0-(loss/total)));
		System.out.println("Accuracy : = " + (acc/sp.instanceList.size()));
	}
	
	private static void trainModel(String modelPath, SLProblem train)
			throws Exception {
		SLModel model = new SLModel();
		Lexiconer lm = new Lexiconer();
		lm.setAllowNewFeatures(true);
		model.lm = lm;
		RelationFeatGen fg = new RelationFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new RelationInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}
	
	public static void printPrediction(RelationX x, RelationY gold, RelationY pred) {
		System.out.println("***************************");
		System.out.println("Text : " + x.ta.getText());
		System.out.println("Quantities : " + Arrays.asList(x.quantities));
		System.out.println("Gold : " + Arrays.asList(gold.relations));
		System.out.println("Pred : " + Arrays.asList(pred.relations));
	}
	
	public static void main(String args[]) throws Exception {
		RelationDriver.doTrainTest();
	}
}
