package pipeline;

import java.util.ArrayList;
import java.util.List;

import joint.JointX;
import joint.JointY;
import reader.DocReader;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;

public class PipelineDriver {
	
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
		SLModel	numOccurModel = SLModel.loadModel("models/numoccur"+testFold+".save");
		SLModel varModel = SLModel.loadModel("models/var"+testFold+".save");
		SLModel treeModel = SLModel.loadModel("models/tree"+testFold+".save");
		SLProblem test = getSP(testProbs);
		return testModel(numOccurModel, varModel, treeModel, test, true);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) 
			throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			JointX x = new JointX(simulProb);
			JointY y = new JointY(simulProb);
			problem.addExample(x, y);
		}
		return problem;
	}

	public static double testModel(SLModel numOccurModel, SLModel varModel, 
			SLModel treeModel, SLProblem sp, boolean printMistakes) throws Exception {
		double acc = 0.0;
		for (int i = 0; i < sp.instanceList.size(); i++) {
			JointX prob = (JointX) sp.instanceList.get(i);
			JointY gold = (JointY) sp.goldStructureList.get(i);
			JointY pred = PipelineInfSolver.getBestStructure(
					prob, numOccurModel, varModel, treeModel);
//			if(Equation.getLoss(gold.equation, pred.equation, true) < 0.0001 || 
//					Equation.getLoss(gold.equation, pred.equation, false) < 0.0001) {
			if(JointY.getLoss(gold, pred) < 0.0001) {
				acc += 1;
				System.out.println(prob.problemIndex+" : "+prob.ta.getText());
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Gold : \n"+gold);
				System.out.println("Pred : \n"+pred);
				System.out.println("Loss : "+JointY.getLoss(gold, pred));
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
		PipelineDriver.crossVal();
		Tools.pipeline.closeCache();
		System.exit(0);
	}
}
