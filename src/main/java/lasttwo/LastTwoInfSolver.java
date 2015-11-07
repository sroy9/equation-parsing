package lasttwo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Node;
import structure.PairComparator;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class LastTwoInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	public LastTwoFeatGen featGen;

	public LastTwoInfSolver(LastTwoFeatGen featGen) {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		LastTwoY r1 = (LastTwoY) arg1;
		LastTwoY r2 = (LastTwoY) arg2;
		return LastTwoY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		LastTwoX prob = (LastTwoX) x;
		PairComparator<LastTwoY> pairComparator = 
				new PairComparator<LastTwoY>() {};
		MinMaxPriorityQueue<Pair<LastTwoY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(20).create();
		MinMaxPriorityQueue<Pair<LastTwoY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(20).create();
		LastTwoY seed = new LastTwoY();
		seed.nodes.addAll(prob.nodes);
		beam1.add(new Pair<LastTwoY, Double>(seed, 0.0));
		
		// Grounding of variables
		for(Pair<LastTwoY, Double> pair : beam1) {
			for(int i=0; i<prob.candidateVars.size(); ++i) {
				LastTwoY y = new LastTwoY(pair.getFirst());
				Node node = new Node("VAR", i, new ArrayList<Node>());
				node.varId = "V1";
				y.nodes.add(node);
				y.varTokens.put("V1", new ArrayList<Integer>());
				y.varTokens.get("V1").add(i);
				y.coref = false;
				beam2.add(new Pair<LastTwoY, Double>(y, pair.getSecond()+
						wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))));
				for(int j=i; j<prob.candidateVars.size(); ++j) {
					y = new LastTwoY(pair.getFirst());
					node = new Node("VAR", i, new ArrayList<Node>());
					node.varId = "V1";
					y.nodes.add(node);
					node = new Node("VAR", j, new ArrayList<Node>());
					node.varId = "V2";
					y.nodes.add(node);
					y.varTokens.put("V1", new ArrayList<Integer>());
					y.varTokens.put("V2", new ArrayList<Integer>());
					y.varTokens.get("V1").add(i);
					y.varTokens.get("V2").add(j);
					y.coref = false;
					beam2.add(new Pair<LastTwoY, Double>(y, pair.getSecond()+
							wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))));
					y = new LastTwoY(pair.getFirst());
					node = new Node("VAR", i, new ArrayList<Node>());
					node.varId = "V1";
					y.nodes.add(node);
					node = new Node("VAR", j, new ArrayList<Node>());
					node.varId = "V2";
					y.nodes.add(node);
					y.varTokens.put("V1", new ArrayList<Integer>());
					y.varTokens.put("V2", new ArrayList<Integer>());
					y.varTokens.get("V1").add(i);
					y.varTokens.get("V2").add(j);
					y.coref = true;
					beam2.add(new Pair<LastTwoY, Double>(y, pair.getSecond()+
							wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))));
				}
			}
		}
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		
		// Equation generation
		for(Pair<LastTwoY, Double> pair : beam1) {
			beam2.addAll(getBottomUpBestParse(prob, pair, wv));
		}
		return beam2.element().getFirst();
	}
	
	public List<Pair<LastTwoY, Double>> getBottomUpBestParse(
			LastTwoX x, Pair<LastTwoY, Double> pair, WeightVector wv) {
		LastTwoY y = pair.getFirst();
		PairComparator<List<Node>> nodePairComparator = 
				new PairComparator<List<Node>>() {};
		MinMaxPriorityQueue<Pair<List<Node>, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(nodePairComparator)
				.maximumSize(5).create();
		MinMaxPriorityQueue<Pair<List<Node>, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(nodePairComparator)
				.maximumSize(5).create();
		int n = y.nodes.size();
		List<Node> init = new ArrayList<>();
		init.addAll(y.nodes);
		beam1.add(new Pair<List<Node>, Double>(init, pair.getSecond()));
		for(int i=1; i<=n-2; ++i) {
			for(Pair<List<Node>, Double> state : beam1) {
				beam2.addAll(enumerateSingleMerge(state, wv, x, 
						pair.getFirst().varTokens, pair.getFirst().nodes));
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		for(Pair<List<Node>, Double> state : beam1) {
			if(state.getFirst().size() != 2) continue;
			Node node = new Node("EQ", -1, Arrays.asList(
					state.getFirst().get(0), state.getFirst().get(1)));
			beam2.add(new Pair<List<Node>, Double>(Arrays.asList(node), 
					state.getSecond()+ 
					getLcaScore(node, wv, x, pair.getFirst().varTokens, pair.getFirst().nodes)));
		}
		List<Pair<LastTwoY, Double>> results = new ArrayList<Pair<LastTwoY,Double>>();
		for(Pair<List<Node>, Double> b : beam2) {
			LastTwoY t = new LastTwoY(y);
			assert b.getFirst().size() == 1;
			t.equation.root = b.getFirst().get(0);
			results.add(new Pair<LastTwoY, Double>(t, b.getSecond()));
		}
		return results;
	}
	
	public List<Pair<List<Node>, Double>> enumerateSingleMerge(
			Pair<List<Node>, Double> state, WeightVector wv, LastTwoX x,
			Map<String, List<Integer>> varTokens, List<Node> nodes) {
		List<Pair<List<Node>, Double>> nextStates = new ArrayList<>();
		List<Node> nodeList = state.getFirst();
		if(nodeList.size() == 1) {
			List<Pair<List<Node>, Double>> tmpNodeList = 
					new ArrayList<Pair<List<Node>, Double>>();
			tmpNodeList.add(state);
			return tmpNodeList;
		}
		double initScore = state.getSecond();
		for(int i=0; i<nodeList.size(); ++i) {
			for(int j=i+1; j<nodeList.size(); ++j) {
				List<Node> tmpNodeList = new ArrayList<Node>();
				tmpNodeList.addAll(nodeList);
				tmpNodeList.remove(i);
				tmpNodeList.remove(j-1);
				for(Pair<Node, Double> pair : enumerateMerge(
						nodeList.get(i), nodeList.get(j), wv, x, varTokens, nodes)) {
					List<Node> newNodeList = new ArrayList<Node>();
					newNodeList.addAll(tmpNodeList);
					newNodeList.add(pair.getFirst());
					nextStates.add(new Pair<List<Node>, Double>(newNodeList, 
							initScore + pair.getSecond()));
				}
			}
		}
		return nextStates;
	}
	
	public List<Pair<Node, Double>> enumerateMerge(
			Node node1, Node node2, WeightVector wv, LastTwoX x, 
			Map<String, List<Integer>> varTokens, List<Node> nodes) {
		List<Pair<Node, Double>> nextStates = new ArrayList<>();
		List<String> labels = Arrays.asList(
				"ADD", "SUB", "SUB_REV","MUL", "DIV", "DIV_REV");
		double mergeScore;
		for(String label : labels) {
			if(label.endsWith("REV")) {
				label = label.substring(0,3);
				Node node = new Node(label, -1, Arrays.asList(node2, node1));
				mergeScore = getLcaScore(node, wv, x, varTokens, nodes);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			} else {
				Node node = new Node(label, -1, Arrays.asList(node1, node2));
				mergeScore = getLcaScore(node, wv, x, varTokens, nodes);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			}
		}
		return nextStates;
	}
	
	public double getLcaScore(Node node, WeightVector wv, LastTwoX x, 
			Map<String, List<Integer>> varTokens, List<Node> nodes) {
		List<String> features = new ArrayList<String>();
		struct.lca.LcaX lcaX = new struct.lca.LcaX(x, varTokens, nodes);
		features.addAll(struct.lca.LcaFeatGen.getPairFeatures(lcaX, node));
		return wv.dotProduct(FeatGen.getFeatureVectorFromList(features, featGen.lm));
	}
	
	public LastTwoY getLatentBestStructure(
			LastTwoX x, LastTwoY gold, WeightVector wv) {
		LastTwoY best = null;
		double bestScore = -Double.MAX_VALUE;
		if(gold.varTokens.keySet().size() == 1) {
			for(Integer tokenIndex : gold.varTokens.get("V1")) {
				LastTwoY yNew = new LastTwoY(gold);
				yNew.varTokens.get("V1").clear();
				yNew.varTokens.get("V1").add(tokenIndex);
				for(Node node : yNew.equation.root.getLeaves()) {
					if(node.label.equals("VAR")) {
						node.index = tokenIndex;
					}
					if(node.label.equals("NUM")) {
						for(int i=0; i<x.quantities.size(); ++i) {
							if(Tools.safeEquals(node.value, 
									Tools.getValue(x.quantities.get(i)))) {
								node.index = i;
								break;
							}
						}
					}
				}
				double score = wv.dotProduct(featGen.getFeatureVector(x, yNew));
				if(score > bestScore) {
					best = yNew;
				}
			}
		}
		if(gold.varTokens.keySet().size() == 2) {
			for(Integer tokenIndex1 : gold.varTokens.get("V1")) {
				for(Integer tokenIndex2 : gold.varTokens.get("V2")) {
					LastTwoY yNew = new LastTwoY(gold);
					yNew.varTokens.get("V1").clear();
					yNew.varTokens.get("V1").add(tokenIndex1);
					yNew.varTokens.get("V2").clear();
					yNew.varTokens.get("V2").add(tokenIndex2);
					for(Node node : yNew.equation.root.getLeaves()) {
						if(node.label.equals("VAR") && node.varId.equals("V1")) {
							node.index = tokenIndex1;
						}
						if(node.label.equals("VAR") && node.varId.equals("V2")) {
							node.index = tokenIndex2;
						}
						if(node.label.equals("NUM")) {
							for(int i=0; i<x.quantities.size(); ++i) {
								if(Tools.safeEquals(node.value, 
										Tools.getValue(x.quantities.get(i)))) {
									node.index = i;
									break;
								}
							}
						}
						
					}
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
	
	@Override
	public Object clone() {
		return new LastTwoInfSolver(featGen);
	}
}
