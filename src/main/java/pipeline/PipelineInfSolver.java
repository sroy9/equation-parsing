package pipeline;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import joint.JointX;
import joint.JointY;
import numoccur.NumoccurInfSolver;
import numoccur.NumoccurX;
import numoccur.NumoccurY;
import structure.Node;
import tree.CompInfSolver;
import tree.TreeX;
import tree.TreeY;
import utils.Tools;
import var.VarInfSolver;
import var.VarX;
import var.VarY;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLModel;

public class PipelineInfSolver {
	
	public static JointY getBestStructure(JointX prob, SLModel numOccurModel, 
			SLModel varModel, SLModel treeModel) throws Exception {
		// Predict number of occurrences of each quantity
		NumoccurX numX = new NumoccurX(prob);
//		NumoccurY numY = (NumoccurY) numOccurModel.infSolver.getBestStructure(numOccurModel.wv, numX);
 		MinMaxPriorityQueue<Pair<NumoccurY, Double>> beam1 = 
 				((NumoccurInfSolver) numOccurModel.infSolver).
 				getLossAugmentedBestStructureTopK(numOccurModel.wv, numX, null);
 		List<Pair<NumoccurY, Double>> list1 = new ArrayList<>();
 		while(beam1.size()>0) {
 			list1.add(beam1.poll());
 		}
 		// Grounding of variables
 		VarX varX = new VarX(prob);
// 		VarY varY = (VarY) varModel.infSolver.getBestStructure(varModel.wv, varX);
 		MinMaxPriorityQueue<Pair<VarY, Double>> beam2 = ((VarInfSolver) varModel.infSolver).
 				getLossAugmentedBestStructureTopK(varModel.wv, varX, null);
 		List<Pair<VarY, Double>> list2 = new ArrayList<>();
 		while(beam2.size()>0) {
 			list2.add(beam2.poll());
 		}
 		JointY best = null;
 		double bestScore = -Double.MAX_VALUE;
 		for(Pair<NumoccurY, Double> pair1 : list1.subList(0, Math.min(1, list1.size()))) {
 			for(Pair<VarY, Double> pair2 : list2.subList(0, Math.min(1, list2.size()))) {
 				JointY y = new JointY();
 				for(int i=0; i<prob.quantities.size(); ++i) {
 					for(int k=0; k<pair1.getFirst().numOccurList.get(i); ++k) {
 						Node node = new Node("NUM", i, new ArrayList<Node>());
 						node.value = Tools.getValue(prob.quantities.get(i));
 						y.nodes.add(node);
 					}
 				}
 				y.varTokens = pair2.getFirst().varTokens;
 				for(String key : y.varTokens.keySet()) {
 					Node node = new Node("VAR", y.varTokens.get(key).get(0), new ArrayList<Node>());
 					node.varId = key;
 					y.nodes.add(node);
 				}
 				Tools.populateAndSortByCharIndex(y.nodes, prob.ta, prob.quantities, prob.candidateVars);
 				// Equation generation
 				TreeX treeX = new TreeX(prob, y.varTokens, y.nodes);
 				Pair<TreeY, Double> pair3 = ((CompInfSolver) treeModel.infSolver).getLossAugmentedBestKStructure(
 						treeModel.wv, treeX, null).element();
 				y.equation = pair3.getFirst().equation;
 				double score = pair1.getSecond() + pair2.getSecond() + pair3.getSecond();
 				if(score > bestScore) {
 					best = y;
 				} 				
 			}
 		}
		return best;
	}
	
}