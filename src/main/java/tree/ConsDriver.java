package tree;

import java.util.ArrayList;
import java.util.List;

import reader.DocReader;
import structure.SimulProb;
import utils.Params;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;

public class ConsDriver {
	
	public static double crossVal() throws Exception {
		double acc = 0.0;
		for(int i=0;i<5;i++) {
			acc += doTuneTest(i);
		}
		System.out.println("5-fold CV : " + (acc/5));
		return (acc/5);
	}
	
	public static double doTuneTest(int testFold) throws Exception {
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
		SLModel numOccurModel = SLModel.loadModel("models/numoccur"+testFold+".save");
		SLModel varModel = SLModel.loadModel("models/var"+testFold+".save");
		SLModel lcaModel = SLModel.loadModel("models/lca"+testFold+".save");
		
		SLProblem train = getSP(trainProbs);
		SLProblem test = getSP(testProbs);
		tuneModel(numOccurModel, varModel, lcaModel, train);
		return testModel(numOccurModel, varModel, lcaModel, test, true);
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
		SLModel numOccurModel = SLModel.loadModel("models/numoccur"+testFold+".save");
		SLModel varModel = SLModel.loadModel("models/var"+testFold+".save");
		SLModel lcaModel = SLModel.loadModel("models/lca"+testFold+".save");
		
		SLProblem test = getSP(testProbs);
		return testModel(numOccurModel, varModel, lcaModel, test, true);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) 
			throws Exception {
		if(simulProbList == null) {
			simulProbList = 
					DocReader.readSimulProbFromBratDir(Params.annotationDir);
		}
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			TreeX x = new TreeX(simulProb);
			TreeY y = new TreeY(simulProb);
			problem.addExample(x, y);
		}
		return problem;
	}

	public static double testModel(SLModel numOccurModel, SLModel varModel, 
			SLModel lcaModel, SLProblem sp, boolean printMistakes) throws Exception {
		System.out.println("Testing with params : NumOccurScale "+ConsInfSolver.numOccurScale+
				" : VarScale "+ConsInfSolver.varScale);
		double acc = 0.0;
		for (int i = 0; i < sp.instanceList.size(); i++) {
			TreeX prob = (TreeX) sp.instanceList.get(i);
			TreeY gold = (TreeY) sp.goldStructureList.get(i);
			TreeY pred = ConsInfSolver.getBestStructure(prob, numOccurModel, varModel, lcaModel);
			if(TreeY.getLoss(gold, pred) < 0.0001) {
				acc += 1;
			} else if(printMistakes) {
				System.out.println(prob.problemIndex+" : "+prob.ta.getText());
				System.out.println("Quantities : "+prob.quantities);
				System.out.println("Gold : \n"+gold);
				System.out.println("Pred : \n"+pred);
				System.out.println("Loss : "+TreeY.getLoss(gold, pred));
			}
		}
		System.out.println("Accuracy : = " + acc + " / " + sp.instanceList.size() 
				+ " = " + (acc/sp.instanceList.size()));
		return (acc/sp.instanceList.size());
	}
	
	public static void tuneModel(SLModel numOccurModel, SLModel varModel, 
			SLModel lcaModel, SLProblem sp) throws Exception {
		double vals[] = {1.0, 100.0, 10000.0};
		double bestAccuracy = 0.0, bestNumOccurScale = 0.0, bestVarScale = 0.0;
		for(Double val1 : vals) {
			for(Double val2 : vals) {
				ConsInfSolver.numOccurScale = val1;
				ConsInfSolver.varScale = val2;
				double accuracy = testModel(numOccurModel, varModel, lcaModel, sp, false);
				if(accuracy > bestAccuracy) {
					bestAccuracy = accuracy;
					bestNumOccurScale = val1;
					bestVarScale = val2;
				}
			}
		}
		ConsInfSolver.numOccurScale = bestNumOccurScale;
		ConsInfSolver.varScale = bestVarScale;
	}
	
	public static void main(String args[]) throws Exception {
		ConsInfSolver.numOccurScale = 1000000000.0;
		ConsInfSolver.varScale = 1000.0;
		ConsDriver.doTest(0);
	}
}