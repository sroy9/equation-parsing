package template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.Equation;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class TemplateY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<Equation> equations;
	
	public TemplateY() {
		equations = new ArrayList<>();
	}
	
	public TemplateY(TemplateY other) {
		equations = new ArrayList<>();
		for(Equation eq : other.equations) {
			equations.add(new Equation(eq));
		}
	}
	
	public TemplateY(SimulProb prob) {
		equations = new ArrayList<>();
		for(Equation eq : prob.equations) {
			equations.add(eq);
		}
	}
	
	public static float getEquationLoss(TemplateY y1, TemplateY y2) {
		if(y1.equations.size() != y2.equations.size()) return 10.0f;
		if(y1.equations.size() == 1) return Equation.getLoss(
				y1.equations.get(0), y2.equations.get(0));
		if(y1.equations.size() == 2) {
			float loss1 = Equation.getLoss(y1.equations.get(0), y2.equations.get(0)) + 
					Equation.getLoss(y1.equations.get(1), y2.equations.get(1));
			float loss2 = Equation.getLoss(y1.equations.get(0), y2.equations.get(1)) + 
					Equation.getLoss(y1.equations.get(1), y2.equations.get(0));
			return Math.min(loss1, loss2);
		}
		return 10.0f;		
	}
	
	public static float getLoss(TemplateY y1, TemplateY y2) {
		return getEquationLoss(y1, y2);
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(equations);
	}
}