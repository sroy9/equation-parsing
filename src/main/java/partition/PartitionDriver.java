package partition;

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

public class PartitionDriver {
	
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
		trainModel("models/partition"+testFold+".save", train);
		return testModel("models/partition"+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb prob : simulProbList) {
			for(int i=0; i<prob.triggers.size()-1; ++i) {
				int index1 = prob.triggers.get(i).getFirst();
				int index2 = prob.triggers.get(i+1).getFirst();
				if(prob.ta.getSentenceFromToken(index1) == 
						prob.ta.getSentenceFromToken(index2)) {
					PartitionX x = new PartitionX(prob, i, i+1);
					PartitionY y = new PartitionY(false);
					for(Pair<String, IntPair> pair : prob.nodes) {
						if(pair.getSecond().getFirst() <= i && 
								pair.getSecond().getSecond() > i+1) {
							y = new PartitionY(true);
							break;
						}
					}
					problem.addExample(x, y);
				}
			}
		}
		return problem;
	}

	private static double testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		Set<Integer> wrongProbIndex = new HashSet<>();
		Set<Integer> allProbIndex = new HashSet<>();
		double acc = 0.0;
		double total = sp.instanceList.size();
		for (int i = 0; i < sp.instanceList.size(); i++) {
			PartitionX prob = (PartitionX) sp.instanceList.get(i);
			PartitionY gold = (PartitionY) sp.goldStructureList.get(i);
			PartitionY pred = (PartitionY) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			allProbIndex.add(prob.problemIndex);
			if (PartitionY.getLoss(gold, pred) < 0.00001) {
				acc += 1.0;
			} else {
				wrongProbIndex.add(prob.problemIndex);
//				System.out.println("Gold : \n"+gold);
//				System.out.println("Gold weight : "+goldWt);
//				System.out.println("Pred : \n"+pred);
//				System.out.println("Pred weight : "+predWt);
//				System.out.println("Loss : "+PartitionY.getLoss(gold, pred));
			}
		}
		System.out.println("Accuracy : " + acc + " / " + total + " = "
				+ (acc / total));
		System.out.println("Problem Accuracy : " + (allProbIndex.size() - wrongProbIndex.size()) + 
				" / " + allProbIndex.size() + " = "
				+ ((allProbIndex.size() - wrongProbIndex.size())*1.0/allProbIndex.size()));
		return ((allProbIndex.size() - wrongProbIndex.size())*1.0/allProbIndex.size());
	}
	
	public static void trainModel(String modelPath, SLProblem train)
			throws Exception {
		SLModel model = new SLModel();
		Lexiconer lm = new Lexiconer();
		lm.setAllowNewFeatures(true);
		model.lm = lm;
		PartitionFeatGen fg = new PartitionFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new PartitionInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}
	
	public static void main(String args[]) throws Exception {
		PartitionDriver.doTrainTest(0);
	}
}
