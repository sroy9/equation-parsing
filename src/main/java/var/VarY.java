package var;

import java.io.Serializable;

import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class VarY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public boolean relevant;
	
	public VarY(VarY other) {
		relevant = other.relevant;
	}
	
	public VarY(boolean relevant) {
		this.relevant = relevant;
	}
	
	public VarY(SimulProb prob, int candidateVarIndex) {
		for(String key : prob.varTokens.keySet()) {
			if(prob.varTokens.get(key).contains(candidateVarIndex)) {
				relevant = true;
				return;
			}
		}
		relevant = false;
	}
	
	public static float getLoss(VarY gold, VarY pred) {
		if(gold.relevant == pred.relevant) return 0.0f;
		return 1.0f;
	}
	
	@Override
	public String toString() {
		return ""+relevant;
	}
}