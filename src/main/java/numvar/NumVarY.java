package numvar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import learnInf.Driver;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class NumVarY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public boolean isOneVar;
	
	public NumVarY(boolean isOneVar) {
		this.isOneVar = isOneVar;
	}
	
	public NumVarY(NumVarY other) {
		isOneVar = other.isOneVar;
	}
	
	public NumVarY(SimulProb prob) {
		isOneVar = prob.isOneVar;
	}
	
	public static float getLoss(NumVarY r1, NumVarY r2) {
		return r1.isOneVar == r2.isOneVar ? 0 : 1;
	}
	
	@Override
	public String toString() {
		return ""+isOneVar;
	}
}