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
	public Equation equation;
	
	public TemplateY() {
		equation = new Equation();
	}
	
	public TemplateY(TemplateY other) {
		equation = new Equation(other.equation);
	}
	
	public TemplateY(SimulProb prob) {
		equation = new Equation(prob.equation);
	}
	
	public static float getEquationLoss(TemplateY y1, TemplateY y2) {
		return Equation.getLoss(y1.equation, y2.equation);
	}
	
	public static float getLoss(TemplateY y1, TemplateY y2) {
		return getEquationLoss(y1, y2);
	}
	
	@Override
	public String toString() {
		return ""+equation;
	}
}