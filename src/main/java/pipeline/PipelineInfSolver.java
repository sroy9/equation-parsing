package pipeline;

import java.util.ArrayList;

import joint.JointX;
import joint.JointY;
import numoccur.NumoccurX;
import numoccur.NumoccurY;
import structure.Node;
import tree.TreeX;
import tree.TreeY;
import utils.Tools;
import var.VarX;
import var.VarY;
import edu.illinois.cs.cogcomp.sl.core.SLModel;

public class PipelineInfSolver {
	
	public static JointY getBestStructure(JointX prob, SLModel numOccurModel, 
			SLModel varModel, SLModel treeModel, JointY gold) throws Exception {
		JointY y = new JointY();
		// Predict number of occurrences of each quantity
		NumoccurX numX = new NumoccurX(prob);
		NumoccurY numY = (NumoccurY) numOccurModel.infSolver.getBestStructure(numOccurModel.wv, numX);
 		for(int i=0; i<prob.quantities.size(); ++i) {
			for(int k=0; k<numY.numOccurList.get(i); ++k) {
				Node node = new Node("NUM", i, new ArrayList<Node>());
				node.value = Tools.getValue(prob.quantities.get(i));
				y.nodes.add(node);
			}
		}
 		// Grounding of variables
 		VarX varX = new VarX(prob);
 		VarY varY = (VarY) varModel.infSolver.getBestStructure(varModel.wv, varX);
		y.varTokens = varY.varTokens;
		y.coref = varY.coref;
 		for(String key : y.varTokens.keySet()) {
 			Node node = new Node("VAR", y.varTokens.get(key).get(0), new ArrayList<Node>());
 			node.varId = key;
 			y.nodes.add(node);
 		}
 		Tools.populateAndSortByCharIndex(y.nodes, prob.ta, prob.quantities, 
 				prob.candidateVars, y.coref);
		// Equation generation
		TreeX treeX = new TreeX(prob, y.varTokens, y.nodes);
		TreeY treeY = (TreeY) treeModel.infSolver.getBestStructure(treeModel.wv, treeX);
		y.equation = treeY.equation;
		return y;
	}
	
}