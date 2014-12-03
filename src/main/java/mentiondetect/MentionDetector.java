package mentiondetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lbj.EquationFeatureManager;
import lbj.ExpressionFeatureManager;
import parser.DocReader;
import curator.NewCachingCurator;
import structure.Clustering;
import structure.Equation;
import structure.Expression;
import structure.LabelSet;
import structure.Mention;
import structure.SimulProb;
import structure.VarSet;
import utils.Params;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.POSManager;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.POSTag;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.ViterbiInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class MentionDetector {
	
	public void train (
			List<SimulProb> simulProbList, String configFilePath, String modelFile) 
					throws Exception {
		SLModel model = new SLModel();
		model.lm = new Lexiconer();	
		model.lm.setAllowNewFeatures(true);
		SLProblem sp = readStructuredData(simulProbList);
		// initialize the inference solver
		FeatureExtractor fe = new FeatureExtractor (model.lm);
		model.infSolver = new InferenceSolver(fe);
		SLParameters para = new SLParameters();
		para.loadConfigFile(configFilePath);
		Learner learner = LearnerFactory.getLearner(
				model.infSolver, fe, para);
		model.wv = learner.train(sp);
		model.saveModel(modelFile);
	}
	
	public SLProblem readStructuredData(List<SimulProb> simulProbList) 
			throws Exception {
		SLProblem slProblem = new SLProblem();
		for(SimulProb simulProb : simulProbList) {
			TextAnnotation ta = new TextAnnotation("", "", simulProb.question);
			for(int i = 0 ; i < ta.getNumberOfSentences(); ++i) {
				VarSet varSet = new VarSet(simulProb, i);
				LabelSet labelSet = varSet.getGold();
				slProblem.addExample(varSet, labelSet);
				System.out.println("**********************");
				for(int j=0; j<varSet.ta.size(); j++) {
					System.out.print("["+varSet.ta.getToken(j)
							+" - "+labelSet.labels.get(j)+"]");
				}
				System.out.println();
			}
		}
		return slProblem;
	}
	
	public void test(List<SimulProb> simulProbList, String modelFile) 
			throws Exception {
		SLModel model = SLModel.loadModel(modelFile);
		SLProblem sp = readStructuredData(simulProbList);
		model.lm.setAllowNewFeatures(false);

		double baseLineErrors = 0;
		double correct = 0.0;
		double total = 0.0;

		for (int i = 0; i < sp.instanceList.size(); i++) {
			VarSet varSet = (VarSet) sp.instanceList.get(i);
			LabelSet gold = (LabelSet) sp.goldStructureList.get(i);
			LabelSet prediction = (LabelSet) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			assert gold.labels.size() == prediction.labels.size();
			total += gold.labels.size();
			for(int j = 0; j < gold.labels.size(); j++) {
				if(gold.labels.get(j).equals(prediction.labels.get(j))) {
					correct += 1;
				}
				if(!"O".equals(gold.labels.get(j))) {
					baseLineErrors += 1;
				}
			}
			System.out.println("Gold");
			for(int j=0; j<varSet.ta.size(); j++) {
				System.out.print("["+gold.labels.get(j)+" : "
						+varSet.ta.getToken(j)+"] ");
			}
			System.out.println();
			System.out.println("Predict");
			for(int j=0; j<varSet.ta.size(); j++) {
				System.out.print("["+prediction.labels.get(j)+" : "
						+varSet.ta.getToken(j)+"] ");
			}
			System.out.println();
			
		}
		System.out.println("Baseline = "+(1.0 - (baseLineErrors/total)));
		System.out.println("Acc = " + correct + " / " + total + " = " 
					+ (correct*1.0/total));
	}
	
	public static void main(String args[]) throws Exception {
		DocReader dr = new DocReader();
		List<SimulProb> train = dr.readSimulProbFromBratDir(
					Params.annotationDir, 0.0, 0.8);
		List<SimulProb> test = dr.readSimulProbFromBratDir(
				Params.annotationDir, 0.8, 1.0);
		MentionDetector mentionDetector = new MentionDetector();
		mentionDetector.train(train, Params.spConfigFile, Params.spModelFile);
		mentionDetector.test(test, Params.spModelFile);
		
	}
}
