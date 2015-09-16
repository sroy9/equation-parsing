package tree;

import java.util.ArrayList;
import java.util.List;

import reader.DocReader;
import struct.numoccur.NumoccurX;
import struct.numoccur.NumoccurY;
import structure.Node;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import var.VarX;
import var.VarY;
import edu.illinois.cs.cogcomp.sl.core.SLModel;

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
		SLModel numOccurModel = null, lcaModel = null;
		SLModel varModel = SLModel.loadModel("models/var"+testFold+".save");
		numOccurModel = SLModel.loadModel("models/numoccurStruct"+testFold+".save");
		lcaModel = SLModel.loadModel("models/lcaStruct"+testFold+".save");
		return testModel(numOccurModel, varModel, lcaModel, testProbs, true);
	}

	public static double testModel(SLModel numOccurModel, SLModel varModel, 
			SLModel lcaModel, List<SimulProb> probs, boolean printMistakes) throws Exception {
		double acc = 0.0, numAcc = 0.0, varAcc = 0.0, lcaAcc = 0.0;
		for (SimulProb prob : probs) {
			struct.numoccur.NumoccurX numX = new NumoccurX(prob);
			struct.numoccur.NumoccurY numGold = new NumoccurY(prob);
			struct.numoccur.NumoccurY numPred = (struct.numoccur.NumoccurY)
					numOccurModel.infSolver.getBestStructure(numOccurModel.wv, numX);
			
			VarX varX = new VarX(prob);
			VarY varGold = new VarY(prob);
			VarY varPred = (VarY) varModel.infSolver.getBestStructure(varModel.wv, varX);
			
			List<Node> nodes = new ArrayList<Node>();
			for(Node leaf : prob.equation.root.getLeaves()) {
				Node node = new Node(leaf);
				if(node.label.equals("VAR") && varPred.varTokens.containsKey(node.varId) &&
						varPred.varTokens.get(node.varId).size()>0) {
					node.index = varPred.varTokens.get(node.varId).get(0);
				}
				if(node.label.equals("NUM")) {
					for(int i=0; i<prob.quantities.size(); ++i) {
						if(Tools.safeEquals(Tools.getValue(prob.quantities.get(i)), node.value)) {
							node.index = i;
							break;
						}
					}
				}
				nodes.add(node);
			}
			
			struct.lca.LcaX lcaX = new struct.lca.LcaX(prob, varPred.varTokens, nodes);
			struct.lca.LcaY lcaGold = new struct.lca.LcaY(prob);
			struct.lca.LcaY lcaPred = (struct.lca.LcaY) 
					lcaModel.infSolver.getBestStructure(lcaModel.wv, lcaX);
			
			if(struct.numoccur.NumoccurY.getLoss(numGold, numPred) < 0.0001 
					&& VarY.getLoss(varGold, varPred) < 0.0001
					&& struct.lca.LcaY.getLoss(lcaGold, lcaPred) < 0.0001) {
				acc += 1;
			}
			if(struct.numoccur.NumoccurY.getLoss(numGold, numPred) < 0.0001) {
				numAcc += 1;
			}
			if(VarY.getLoss(varGold, varPred) < 0.0001) {
				varAcc += 1;
			}
			if(struct.lca.LcaY.getLoss(lcaGold, lcaPred) < 0.0001) {
				lcaAcc += 1;
			}
		}
		System.out.println("Number Accuracy : = " + numAcc + " / " + probs.size()
				+ " = " + (numAcc/probs.size()));
		System.out.println("Var Accuracy : = " + varAcc + " / " + probs.size()
				+ " = " + (varAcc/probs.size()));
		System.out.println("Lca Accuracy : = " + lcaAcc + " / " + probs.size()
				+ " = " + (lcaAcc/probs.size()));
		System.out.println("Accuracy : = " + acc + " / " + probs.size()
				+ " = " + (acc/probs.size()));
		return (acc/probs.size());
	}
	
	public static void main(String args[]) throws Exception {
		PipelineDriver.doTest(0);
	}
}
