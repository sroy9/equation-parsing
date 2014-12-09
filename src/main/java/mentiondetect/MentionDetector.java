package mentiondetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import parser.DocReader;
import curator.NewCachingCurator;
import structure.Equation;
import structure.SimulProb;
import utils.Params;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.POSManager;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.POSTag;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.ViterbiInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class MentionDetector {
	
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
		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(
				Params.annotationDir);
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			VarSet varSet = new VarSet(simulProb);
			problem.addExample(varSet, varSet.getGold());
		}
		return problem;
	}
	
	private static void testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		double acc = 0.0;
		double total = 0.0;
		for (int i = 0; i < sp.instanceList.size(); i++) {
			LabelSet gold = (LabelSet) sp.goldStructureList.get(i);
			LabelSet prediction = (LabelSet) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			assert gold.labels.size() == prediction.labels.size();
			total += gold.labels.size();
			for(int j = 0; j < gold.labels.size(); ++j) {
				if (gold.labels.get(j).equals(prediction.labels.get(j))) {
					acc += 1.0;
				}
			}
		}
		System.out.println("Accuracy : " + acc + " / " + total + " = "
				+ (acc / total));
	}

	private static void trainModel(String modelPath, SLProblem train)
			throws Exception {
		SLModel model = new SLModel();
		Lexiconer lm = new Lexiconer();
		lm.setAllowNewFeatures(true);
		model.lm = lm;
		MentionFeatureExtractor fg = new MentionFeatureExtractor(lm);
		model.featureGenerator = fg;
		model.infSolver = new MentionInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}
	
	public SLProblem readStructuredData(List<SimulProb> simulProbList) 
			throws Exception {
		SLProblem slProblem = new SLProblem();
		for(SimulProb simulProb : simulProbList) {
			VarSet varSet = new VarSet(simulProb);
			LabelSet labelSet = varSet.getGold();
			slProblem.addExample(varSet, labelSet);
		}
		return slProblem;
	}
	
	public static void main(String args[]) throws Exception {
		MentionDetector.doTrainTest();
	}
}
