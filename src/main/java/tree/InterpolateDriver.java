package tree;

import java.util.ArrayList;
import java.util.List;

import reader.DocReader;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;

public class InterpolateDriver {
	
	public static double crossVal() throws Exception {
		double acc = 0.0;
		for(int i=0;i<5;i++) {
			acc += doTest(i);
		}
		System.out.println("5-fold CV : " + (acc/5));
		return (acc/5);
	}
	
	public static double doTest(int testFold) throws Exception {
		List<List<Integer>> folds = DocReader.extractFolds();
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir);
		List<SimulProb> testProbs = new ArrayList<>();
		for(SimulProb simulProb : simulProbList) {
			if(folds.get(testFold).contains(simulProb.index)) {
				testProbs.add(simulProb);
			}
		}
		SLModel	composeModel = SLModel.loadModel("models/compose"+testFold+".save");
		SLModel nonComposeModel = SLModel.loadModel("models/nonCompose"+testFold+".save");
		SLProblem test = TreeDriver.getSP(testProbs);
		return testModel(composeModel, nonComposeModel, test, true);
	}

	public static double testModel(SLModel composeModel, SLModel nonComposeModel, 
			SLProblem sp, boolean printMistakes) throws Exception {
		double acc = 0.0;
		for (int i = 0; i < sp.instanceList.size(); i++) {
			TreeX prob = (TreeX) sp.instanceList.get(i);
			TreeY gold = (TreeY) sp.goldStructureList.get(i);
			TreeY pred = InterpolateInfSolver.getBestTree(
					prob, composeModel, nonComposeModel);
//			if(Equation.getLoss(gold.equation, pred.equation, true) < 0.0001 || 
//					Equation.getLoss(gold.equation, pred.equation, false) < 0.0001) {
			if(TreeY.getLoss(gold, pred) < 0.0001) {
				acc += 1;
				System.out.println(prob.problemIndex+" : "+prob.ta.getText());
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Gold : \n"+gold);
				System.out.println("Pred : \n"+pred);
				System.out.println("Loss : "+TreeY.getLoss(gold, pred));
			} else if(printMistakes) {
//				System.out.println(prob.problemIndex+" : "+prob.ta.getText());
//				System.out.println("Quantities : "+prob.quantities);
//				System.out.println("Gold : \n"+gold);
//				System.out.println("Pred : \n"+pred);
//				System.out.println("Loss : "+JointY.getLoss(gold, pred));				
			}
		}
		System.out.println("Accuracy : = " + acc + " / " + sp.instanceList.size() 
				+ " = " + (acc/sp.instanceList.size()));
		return (acc/sp.instanceList.size());
	}
	
	public static void main(String args[]) throws Exception {
		InterpolateDriver.doTest(0);
		Tools.pipeline.closeCache();
		System.exit(0);
	}
}
