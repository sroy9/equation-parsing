package relation;

import utils.Params;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class LatentSVM {

	public static WeightVector learn(SLProblem problem, AbstractInferenceSolver infSolver, 
			AbstractFeatureGenerator fg, int numInnerIters, int numOuterIters) throws Exception {
		SLParameters params = new SLParameters();
		params.loadConfigFile(Params.spConfigFile);
		params.MAX_NUM_ITER = numInnerIters;
		params.PROGRESS_REPORT_ITER = 1;
		System.err.println("Running LatentSVM with "+numInnerIters+" inner and "+numOuterIters+" outer iterations");
		Learner learner = LearnerFactory.getLearner(infSolver, fg, params);
		WeightVector w = new WeightVector(8000);
		w.setExtendable(true);
		for (int outerIter = 0; outerIter < numOuterIters; outerIter++) {
			System.err.println("Iteration Outer : "+outerIter);
			problem = runLatentStructureInference(problem, w, infSolver);
			System.err.println("Gold extracted for latent");
			learner = LearnerFactory.getLearner(infSolver, fg, params);
			w = learner.train(problem);
		}
		System.out.println("LatentSVM learning complete");
		return w;
	}

	private static SLProblem runLatentStructureInference(
			SLProblem problem, WeightVector w, AbstractInferenceSolver infSolver)
			throws Exception {
		SLProblem p = new SLProblem();
		for (int i = 0; i < problem.size(); i++) {
			IInstance x = problem.instanceList.get(i);
			IStructure gold = problem.goldStructureList.get(i);
			IStructure y = ((RelationInfSolver)infSolver).getBestLatentVariable(
					w, (RelationX) x, (RelationY) gold);
			p.instanceList.add(x);
			p.goldStructureList.add(y);
		}
		return p;
	}
}
