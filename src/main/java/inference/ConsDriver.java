package inference;

import java.util.ArrayList;
import java.util.List;

import reader.DocReader;
import structure.SimulProb;
import tree.TreeX;
import tree.TreeY;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;

public class ConsDriver {
	
	public static boolean useSPforNumoccur = true, useSPforLCA = false;
	
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
		SLModel numOccurModel = null, lcaModel = null;
		SLModel varModel = SLModel.loadModel("models/var"+testFold+".save");
		if(useSPforNumoccur) {
			numOccurModel = SLModel.loadModel("models/numoccurStruct"+testFold+".save");
		} else {
			numOccurModel = SLModel.loadModel("models/numoccur"+testFold+".save");
		}
		if(useSPforLCA) {
			lcaModel = SLModel.loadModel("models/lcaStruct"+testFold+".save");
		} else {
			lcaModel = SLModel.loadModel("models/lca"+testFold+".save");
		}
		SLProblem test = getSP(testProbs);
		return testModel(numOccurModel, varModel, lcaModel, test, true);
	}
	
	public static SLProblem getSP(List<SimulProb> simulProbList) 
			throws Exception {
		if(simulProbList == null) {
			simulProbList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
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
		double acc = 0.0;
		for (int i = 0; i < sp.instanceList.size(); i++) {
			TreeX prob = (TreeX) sp.instanceList.get(i);
			TreeY gold = (TreeY) sp.goldStructureList.get(i);
			TreeY pred = ConsInfSolver.getBestStructure(prob, numOccurModel, varModel, lcaModel, gold);
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
	
	public static void main(String args[]) throws Exception {
		if(args[0].equals("SP")) {
			ConsDriver.useSPforNumoccur = true; 
		} else {
			ConsDriver.useSPforNumoccur = false;
		}
		if(args[1].equals("SP")) {
			ConsDriver.useSPforLCA = true; 
		} else {
			ConsDriver.useSPforLCA = false;
		}
		ConsDriver.crossVal();
		Tools.pipeline.closeCache();
	}
}
