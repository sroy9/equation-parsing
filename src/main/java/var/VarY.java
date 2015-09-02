package var;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class VarY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Map<String, List<Integer>> varTokens;
	
	public VarY() {
		varTokens = new HashMap<String, List<Integer>>();
	}
	
	public VarY(VarY other) {
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(other.varTokens);
	}
	
	public VarY(SimulProb prob) {
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(prob.varTokens);
	}
	
	public static float getLoss(VarY gold, VarY pred) {
		if(pred.varTokens.get("V1").size() > 1 || 
				(pred.varTokens.containsKey("V2") && pred.varTokens.get("V2").size() > 1)) {
			System.err.println("Error in TreeY getLoss() function");
		}
		float loss1 = SimulProb.getVarTokenLoss(gold.varTokens, pred.varTokens, true);
		float loss2 = SimulProb.getVarTokenLoss(gold.varTokens, pred.varTokens, false);
		return Math.min(loss1, loss2);
	}
	
	@Override
	public String toString() {
		return "VarTokens : "+Arrays.asList(varTokens);
	}
}