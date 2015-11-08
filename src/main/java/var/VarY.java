package var;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lasttwo.LastTwoY;
import structure.SimulProb;
import tree.TreeY;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class VarY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Map<String, List<Integer>> varTokens;
	public boolean coref;
	
	public VarY() {
		varTokens = new HashMap<String, List<Integer>>();
		coref = false;
	}
	
	public VarY(VarY other) {
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(other.varTokens);
		coref = other.coref;
	}
	
	public VarY(SimulProb prob) {
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(prob.varTokens);
		coref = prob.coref;
	}
	
	public VarY(TreeY prob) {
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(prob.varTokens);
		coref = prob.coref;
	}
	
	public VarY(LastTwoY prob) {
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(prob.varTokens);
		coref = prob.coref;
	}
	
	public static float getLoss(VarY gold, VarY pred) {
		if(pred.varTokens.get("V1").size() > 1 || 
				(pred.varTokens.containsKey("V2") && pred.varTokens.get("V2").size() > 1)) {
			System.err.println("Error in TreeY getLoss() function");
		}
		float loss1 = SimulProb.getVarTokenLoss(gold.varTokens, gold.coref, 
				pred.varTokens, pred.coref, true);
		float loss2 = SimulProb.getVarTokenLoss(gold.varTokens, gold.coref, 
				pred.varTokens, pred.coref, false);
		return Math.min(loss1, loss2);
	}
	
	@Override
	public String toString() {
		return "VarTokens : "+Arrays.asList(varTokens) +" Coref : "+coref;
	}
}