package semparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import partition.PartitionX;
import partition.PartitionY;
import reader.DocReader;
import structure.Equation;
import structure.EquationSolver;
import structure.KnowledgeBase;
import structure.Node;
import structure.Operation;
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
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMDCDSolver;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class SemDriver {
	
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
		trainModel("models/sem"+testFold+".save", train, testFold);
		return testModel("models/sem"+testFold+".save", test);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) 
			throws Exception {
		if(simulProbList == null) {
			simulProbList = 
					DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			List<IntPair> eqSpans = extractGoldEqSpans(simulProb);
			for(IntPair span : eqSpans) {
				SemX x = new SemX(simulProb, span);
				SemY y = new SemY(simulProb, span);
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
			SemX prob = (SemX) sp.instanceList.get(i);
			SemY gold = (SemY) sp.goldStructureList.get(i);
			SemY pred = (SemY) model.infSolver.getBestStructure(
					model.wv, prob);
			total.add(prob.problemIndex);
			double goldWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, gold));
			double predWt = model.wv.dotProduct(
					model.featureGenerator.getFeatureVector(prob, pred));
			if(goldWt > predWt) {
				System.out.println("PROBLEM HERE");
			}
			if(SemY.getLoss(gold, pred) < 0.0001) {
				acc += 1;
			} else {
				incorrect.add(prob.problemIndex);
				System.out.println(prob.problemIndex+" : "+prob.ta.getText());
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Triggers : " + prob.triggers);
				System.out.println("Eq Span : "+prob.eqSpan);
				System.out.println("Gold : \n"+gold);
				System.out.println("Gold weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, gold)));
				System.out.println("Pred : \n"+pred);
				System.out.println("Pred weight : "+model.wv.dotProduct(
						model.featureGenerator.getFeatureVector(prob, pred)));
				System.out.println("Loss : "+SemY.getLoss(gold, pred));
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
		SemFeatGen fg = new SemFeatGen(lm);
		model.featureGenerator = fg;
		model.infSolver = new SemInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}

	public static List<IntPair> extractGoldEqSpans(SimulProb prob) {
		List<IntPair> eqSpans = new ArrayList<IntPair>();
		for(Node node1 : prob.nodes) {
			boolean allow = true;
			for(Node node2 : prob.nodes) {
				if(Tools.doesContainNotEqual(node2.span, node1.span)) {
					allow = false;
				}
			}
			if(allow) eqSpans.add(node1.span);
		}
		return eqSpans;
	}
	
	
//	public static List<IntPair> extractGoldEqSpans(
//			SimulProb prob, Map<Integer, Boolean> partitions) {
//		int start, end=0;
//		List<IntPair> eqSpans = new ArrayList<>();
//		for(int i=0; i<prob.ta.size(); ++i) {
//			if(KnowledgeBase.mathIndicatorSet.contains(
//					prob.ta.getToken(i).toLowerCase())) {
////				System.out.println("Found indicator at "+i);
//				int minDist = Integer.MAX_VALUE;
//				int pivot = -1;
//				for(int j=end; j<prob.triggers.size(); j++) {
//					int dist = Math.abs(prob.triggers.get(j).index - i);
//					if(dist < minDist) {
//						minDist = dist;
//						pivot = j;
//					}
//				}
//				if(pivot == -1) continue;
////				System.out.println("Pivot found at "+pivot);
//				start = pivot; 
//				end = pivot+1;
//				for(int j=start-1; j>=0; --j) {
//					int index1 = prob.triggers.get(j).index;
//					int index2 = prob.triggers.get(j+1).index;
//					if(prob.ta.getSentenceFromToken(index1) == 
//							prob.ta.getSentenceFromToken(index2) && 
//							partitions.containsKey(j) &&
//							partitions.get(j) == false) {
//						start = j;
//					} else {
//						break;
//					}
//				}
//				for(int j=end; j<prob.triggers.size(); ++j) {
//					int index1 = prob.triggers.get(j-1).index;
//					int index2 = prob.triggers.get(j).index;
//					if(prob.ta.getSentenceFromToken(index1) == 
//							prob.ta.getSentenceFromToken(index2) && 
//							partitions.containsKey(j-1) &&
//							partitions.get(j-1) == false) {
//						end = j+1;
//					} else {
//						break;
//					}
//				}
//				eqSpans.add(new IntPair(start, end));
//				i = prob.triggers.get(end-1).index+1;
//				
//			}	
//		}
//		return eqSpans;
//	}
	
	public static Map<Integer, Boolean> extractGoldPartition(
			SimulProb simulProb) {
		Map<Integer, Boolean> partitions = new HashMap<Integer, Boolean>();
		for(int i=0; i<simulProb.triggers.size()-1; ++i) {
			int index1 = simulProb.triggers.get(i).index;
			int index2 = simulProb.triggers.get(i+1).index;
			if(simulProb.ta.getSentenceFromToken(index1) == 
					simulProb.ta.getSentenceFromToken(index2)) {
				partitions.put(i, true);
				for(Node pair : simulProb.nodes) {
					if(pair.span.getFirst() <= i && 
							pair.span.getSecond() >= i+1) {
						partitions.put(i, false);
						break;
					}
				}
			}
		}
		return partitions;
	}
	
	public static void main(String args[]) throws Exception {
		SemDriver.doTrainTest(0);
//		SemDriver.crossVal();
	}
}
