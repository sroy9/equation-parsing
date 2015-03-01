package template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import structure.Equation;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class TemplateY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Equation equation;
	public Map<String, List<Integer>> varTokens;
	public int templateId;
	
	public TemplateY() {
		equation = new Equation();
		varTokens = new HashMap<String, List<Integer>>();
	}
	
	public TemplateY(TemplateY other) {
		equation = new Equation(other.equation);
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(other.varTokens);
	}
	
	public TemplateY(SimulProb prob) {
		equation = new Equation(prob.equation);
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(prob.varTokens);
	}
	
	public static float getLoss(TemplateY gold, TemplateY pred) {
		if(pred.varTokens.get("V1").size() > 1 || 
				(pred.varTokens.containsKey("V2") && pred.varTokens.get("V2").size() > 1)) {
			System.err.println("Error in TreeY getLoss() function");
		}
		float loss1 = Equation.getLoss(gold.equation, pred.equation, true) + 
				SimulProb.getVarTokenLoss(gold.varTokens, pred.varTokens, true);
		float loss2 = Equation.getLoss(gold.equation, pred.equation, false) + 
				SimulProb.getVarTokenLoss(gold.varTokens, pred.varTokens, false);
		return Math.min(loss1, loss2);
	}
	
	
	@Override
	public String toString() {
		return "Equation : "+equation +" VarToken : "+Arrays.asList(varTokens);
	}
}