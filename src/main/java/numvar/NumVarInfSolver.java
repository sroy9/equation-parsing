package numvar;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.MinMaxPriorityQueue;

import semparse.SemInfSolver;
import semparse.SemX;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.mit.jwi.morph.IStemmer;

public class NumVarInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private NumVarFeatGen featGen;

	public NumVarInfSolver(NumVarFeatGen featGen) throws Exception {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		NumVarY r1 = (NumVarY) arg1;
		NumVarY r2 = (NumVarY) arg2;
		return NumVarY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		NumVarX prob = (NumVarX) x;
		NumVarY gold = (NumVarY) goldStructure;
		NumVarY trueY = new NumVarY(true);
		NumVarY falseY = new NumVarY(false);
		Double score1 = 0.0 + wv.dotProduct(featGen.getFeatureVector(prob, trueY))+
				(goldStructure == null?0:NumVarY.getLoss(trueY, gold));
		Double score2 = 0.0 + wv.dotProduct(featGen.getFeatureVector(prob, falseY))+
				(goldStructure == null?0:NumVarY.getLoss(falseY, gold));
		return score1 > score2 ? trueY : falseY;
	}
}
