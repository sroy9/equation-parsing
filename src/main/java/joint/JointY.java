package joint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import learnInf.Driver;
import relation.RelationX;
import relation.RelationY;
import semparse.SemX;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class JointY implements IStructure{
	
	public RelationY relationY;
	public List<SemY> semYs;
	public List<Double> solns;
	public boolean isOneVar;
	
	public JointY(SimulProb prob) {
		relationY = new RelationY();
		for(String relation : prob.relations) {
			relationY.relations.add(relation);
		}
		semYs = new ArrayList<>();
		for(Equation eq : prob.equations) {
			semYs.add(new SemY(eq));
		}
		isOneVar = prob.isOneVar;
		solns = EquationSolver.solveSemYs(semYs);
	}
	
	public JointY(JointY other) {
		relationY = new RelationY();
		relationY.relations.addAll(other.relationY.relations);
		semYs = new ArrayList<>();
		for(Equation eq : other.semYs) {
			semYs.add(new SemY(eq));
		}
		isOneVar = other.isOneVar;
		solns = other.solns;
	}
	
	public JointY(RelationY relationY, List<SemY> semYs) {
		this.relationY = relationY;
		this.semYs = semYs;
		this.isOneVar = Tools.isOneVar(relationY.relations);
		solns = EquationSolver.solveSemYs(semYs);
	}
	
	public static float getLoss(JointY y1, JointY y2) {
		List<Double> soln1 = EquationSolver.solveSemYs(y1.semYs);
		List<Double> soln2 = EquationSolver.solveSemYs(y2.semYs);
		if(Driver.hasSameSolution(soln1, soln2)) {
			return 0.0f;
		} else {
			return 1.0f;
		}
	}
	
	@Override
	public String toString() {
		String str = "JointY\n";
		str += "Relations : "+Arrays.asList(relationY.relations)+"\n";
		str += "IsOneVar : "+isOneVar+"\n";
		for(SemY y : semYs) {
			str += "Equation : \n"+y+"\n";
		}
		str+="\n";
		return str;
	}
}
