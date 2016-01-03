package var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class VarInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	public VarFeatGen featGen;

	public VarInfSolver(VarFeatGen featGen) throws Exception {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		VarY r1 = (VarY) arg1;
		VarY r2 = (VarY) arg2;
		return VarY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		MinMaxPriorityQueue<Pair<VarY, Double>> beam = 
				getLossAugmentedBestStructureTopK(wv, x, goldStructure);
		return beam.element().getFirst();
	}	
		
	public MinMaxPriorityQueue<Pair<VarY, Double>> getLossAugmentedBestStructureTopK(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		VarX prob = (VarX) x;
		PairComparator<VarY> pairComparator = new PairComparator<VarY>() {};
		MinMaxPriorityQueue<Pair<VarY, Double>> beam = 
				MinMaxPriorityQueue.orderedBy(pairComparator).maximumSize(200).create();
		// Grounding of variables
		for(int i=0; i<prob.candidateVars.size(); ++i) {
			VarY y = new VarY();
			y.varTokens.put("V1", new ArrayList<Integer>());
			y.varTokens.get("V1").add(i);
			y.coref = false;
			beam.add(new Pair<VarY, Double>(y, 
					1.0*wv.dotProduct(featGen.getFeatureVector(prob, y))));
			for(int j=i; j<prob.candidateVars.size(); ++j) {
				if(Tools.doesContainNotEqual(prob.candidateVars.get(i), prob.candidateVars.get(j)) ||
						Tools.doesContainNotEqual(prob.candidateVars.get(j), prob.candidateVars.get(i))) {
					continue;
				}
				y = new VarY();
				y.varTokens.put("V1", new ArrayList<Integer>());
				y.varTokens.put("V2", new ArrayList<Integer>());
				y.varTokens.get("V1").add(i);
				y.varTokens.get("V2").add(j);
				y.coref = corefRule(prob.ta, prob.candidateVars.get(i), prob.candidateVars.get(j));
				beam.add(new Pair<VarY, Double>(y, 
						1.0*wv.dotProduct(featGen.getFeatureVector(prob, y))));
			}
		}
		return beam;
	}
		
	public VarY getLatentBestStructure(VarX x, VarY gold, WeightVector wv) {
		VarY best = null;
		double bestScore = -Double.MAX_VALUE;
		for(Map<String, List<Integer>> varTokens : Tools.enumerateVarTokens(gold.varTokens)) {
			VarY yNew = new VarY();
			yNew.varTokens = varTokens;
			double score = wv.dotProduct(featGen.getFeatureVector(x, yNew));
			if(score > bestScore) {
				best = yNew;
			}
		}
		return best;
	}
	
	public static boolean corefRule(TextAnnotation ta, IntPair ip1, IntPair ip2) {
		String str1 = VarFeatGen.getString(ta, ip1);
		String str2 = VarFeatGen.getString(ta, ip2);
		if(str1.contains("a number") && str2.contains("the number")) {
			return true;
		}
		if(str2.contains("same number")) return true;
		if(str1.equals(str2) && !str1.contains("Two") && !str1.contains("two") && 
				!str1.contains("2 ")) return true;
		return false;
	}
	
}
