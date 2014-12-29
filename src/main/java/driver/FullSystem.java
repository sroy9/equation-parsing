package driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.SLModel;
import relation.RelationX;
import relation.RelationY;
import semparse.SemX;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.SimulProb;

public class FullSystem {

	public static List<Double> getSolutions(SimulProb simulProb,
			SLModel relationModel, SLModel equationModel) throws Exception {
		List<Double> solutions = new ArrayList<>();
		simulProb.relations.clear();
		for(int i=0; i<simulProb.quantities.size(); ++i) {
			RelationX relationX = new RelationX(simulProb, i);
			simulProb.relations.add(
					((RelationY) relationModel.infSolver.getBestStructure(
					relationModel.wv, relationX)).relation);
		}
		SemX semX1 = new SemX(simulProb, "R1");
		SemY semY1 = (SemY) equationModel.infSolver.getBestStructure(
				equationModel.wv, semX1);
		SemX semX2 = new SemX(simulProb, "R2");
		SemY semY2 = (SemY) equationModel.infSolver.getBestStructure(
				equationModel.wv, semX2);
		List<Equation> eqList = new ArrayList<>();
		eqList.add(semY1);
		eqList.add(semY2);
		solutions = EquationSolver.solve(eqList);
		return solutions;
	}
	
	public Double getAccuracy(List<SimulProb> simulProbList) {
		return 0.0;
	}
	
}
