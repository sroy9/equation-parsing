package var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
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
			if(allowVar(prob, y)) beam.add(new Pair<VarY, Double>(y, 
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
				y.coref = false;
				if(allowVar(prob, y)) beam.add(new Pair<VarY, Double>(y, 
						1.0*wv.dotProduct(featGen.getFeatureVector(prob, y))));
				y = new VarY();
				y.varTokens.put("V1", new ArrayList<Integer>());
				y.varTokens.put("V2", new ArrayList<Integer>());
				y.varTokens.get("V1").add(i);
				y.varTokens.get("V2").add(j);
				y.coref = true;
				if(allowVar(prob, y)) beam.add(new Pair<VarY, Double>(y, 
						1.0*wv.dotProduct(featGen.getFeatureVector(prob, y))));
			}
		}
		return beam.element().getFirst();
	}
		
	public VarY getLatentBestStructure(VarX x, VarY gold, WeightVector wv) {
		VarY best = null;
		double bestScore = -Double.MAX_VALUE;
		if(gold.varTokens.keySet().size() == 1) {
			for(Integer tokenIndex : gold.varTokens.get("V1")) {
				VarY yNew = new VarY(gold);
				yNew.varTokens.get("V1").clear();
				yNew.varTokens.get("V1").add(tokenIndex);
				double score = wv.dotProduct(
						featGen.getFeatureVector(x, yNew));
				if(score > bestScore) {
					best = yNew;
				}
			}
		}
		if(gold.varTokens.keySet().size() == 2) {
			for(Integer tokenIndex1 : gold.varTokens.get("V1")) {
				for(Integer tokenIndex2 : gold.varTokens.get("V2")) {
					VarY yNew = new VarY(gold);
					yNew.varTokens.get("V1").clear();
					yNew.varTokens.get("V1").add(tokenIndex1);
					yNew.varTokens.get("V2").clear();
					yNew.varTokens.get("V2").add(tokenIndex2);
					double score = wv.dotProduct(featGen.getFeatureVector(x, yNew));
					if(score > bestScore) {
						best = yNew;
					}
				}
			}
		}
		if(best == null) return gold;
		best.coref = gold.coref;
		return best;
	}
	
	public static boolean allowVar(VarX x, VarY y) {
		List<Integer> charIndices = new ArrayList<>();
		List<Integer> varIndices = new ArrayList<>();
		for(String key : y.varTokens.keySet()) {
			IntPair span = x.candidateVars.get(y.varTokens.get(key).get(0));
			varIndices.add((x.ta.getTokenCharacterOffset(span.getFirst()).getFirst()+
					x.ta.getTokenCharacterOffset(span.getSecond()-1).getSecond())/2);
		}
		charIndices.addAll(varIndices);
		for(QuantSpan qs : x.quantities) {
			charIndices.add((qs.start+qs.end)/2);
		}
		Collections.sort(charIndices);
		String str = x.ta.getText().toLowerCase();
//		System.out.println(str);
//		System.out.println(Arrays.asList(charIndices));
//		for(Integer i : charIndices) {
//			System.out.print(str.substring(i, Math.min(i+5, str.length()))+" ");
//		}
//		System.out.println();
		// 2 to the right
		int index = str.indexOf("ratio of");
		if(index>-1 && index>charIndices.get(charIndices.size()-2)) {
			return false;
		}
		index = str.indexOf("sum of");
		if(index>-1 && index>charIndices.get(charIndices.size()-2)) {
			return false;
		}
		index = str.indexOf("difference");
		if(index>-1 && index>charIndices.get(charIndices.size()-2)) {
			return false; 
		}
		index = str.indexOf("product of");
		if(index>-1 && index>charIndices.get(charIndices.size()-2)) {
			return false;
		}
		// 1 on each side
		index = str.indexOf(" plus ");
		if(index>-1 && (index>charIndices.get(charIndices.size()-1)
				|| index<charIndices.get(0))) {
			return false;
		}
		index = str.indexOf(" minus ");
		if(index>-1 && (index>charIndices.get(charIndices.size()-1)
				|| index<charIndices.get(0))) {
			return false;
		}
		index = str.indexOf(" exceed ");
		if(index>-1 && (index>charIndices.get(charIndices.size()-1)
				|| index<charIndices.get(0))) {
			return false;
		}
		index = str.indexOf(" added to");
		if(index>-1 && (index>charIndices.get(charIndices.size()-1)
				|| index<charIndices.get(0))) {
			return false;
		}
		index = str.indexOf(" subtracted from ");
		if(index>-1 && (index>charIndices.get(charIndices.size()-1)
				|| index<charIndices.get(0))) {
			return false;
		}
		index = str.indexOf(" than ");
		if(index>-1 && (index>charIndices.get(charIndices.size()-1)
				|| index<charIndices.get(0))) {
			return false;
		}
		index = str.indexOf(" times ");
		if(index>-1 && (index>charIndices.get(charIndices.size()-1)
				|| index<charIndices.get(0))) {
			return false;
		}
		// 1 to the right
		index = str.indexOf(" thrice ");
		if(index>-1 && index>charIndices.get(charIndices.size()-1)) {
			return false;
		}
		index = str.indexOf(" twice ");
		if(index>-1 && index>charIndices.get(charIndices.size()-1)) {
			return false;
		}
		index = str.indexOf(" double ");
		if(index>-1 && index>charIndices.get(charIndices.size()-1)) {
			return false;
		}
		index = str.indexOf(" half ");
		if(index>-1 && index>charIndices.get(charIndices.size()-1)) {
			return false;
		}
		index = str.indexOf(" triple ");
		if(index>-1 && index>charIndices.get(charIndices.size()-1)) {
			return false;
		}
		return true;
	}
	
}
