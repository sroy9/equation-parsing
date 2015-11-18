package lasttwo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Node;
import structure.PairComparator;
import tree.CompInfSolver;
import tree.TreeFeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class LasttwoInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	public LasttwoFeatGen featGen;

	public LasttwoInfSolver(LasttwoFeatGen featGen) throws Exception {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		LasttwoY r1 = (LasttwoY) arg1;
		LasttwoY r2 = (LasttwoY) arg2;
		return LasttwoY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		LasttwoX prob = (LasttwoX) x;
		PairComparator<LasttwoY> pairComparator = 
				new PairComparator<LasttwoY>() {};
		MinMaxPriorityQueue<Pair<LasttwoY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		MinMaxPriorityQueue<Pair<LasttwoY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		LasttwoY seed = new LasttwoY();
		seed.nodes.addAll(prob.nodes);
		beam1.add(new Pair<LasttwoY, Double>(seed, 0.0));
		// Grounding of variables
		for(Pair<LasttwoY, Double> pair : beam1) {
			for(int i=0; i<prob.candidateVars.size(); ++i) {
				LasttwoY y = new LasttwoY(pair.getFirst());
				Node node = new Node("VAR", i, new ArrayList<Node>());
				node.varId = "V1";
				y.nodes.add(node);
				y.varTokens.put("V1", new ArrayList<Integer>());
				y.varTokens.get("V1").add(i);
				y.coref = false;
				if(y.nodes.size() > 2) {
					beam2.add(new Pair<LasttwoY, Double>(y, pair.getSecond()+
							wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))));
				}
				for(int j=i; j<prob.candidateVars.size(); ++j) {
					if(Tools.doesContainNotEqual(prob.candidateVars.get(i), prob.candidateVars.get(j)) ||
							Tools.doesContainNotEqual(prob.candidateVars.get(j), prob.candidateVars.get(i))) {
						continue;
					}
					y = new LasttwoY(pair.getFirst());
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
					if(y.nodes.size()>2) {
						beam2.add(new Pair<LasttwoY, Double>(y, pair.getSecond()+
								wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))));
					}
					y = new LasttwoY(pair.getFirst());
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
					beam2.add(new Pair<LasttwoY, Double>(y, pair.getSecond()+
							wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))));
				}
			}
		}
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		// Equation generation
		for(Pair<LasttwoY, Double> pair : beam1) {
			Tools.populateAndSortByCharIndex(pair.getFirst().nodes, prob.ta, 
					prob.quantities, prob.candidateVars, pair.getFirst().coref);
			beam2.addAll(getBottomUpBestParse(prob, pair, wv));
		}
		System.out.println("PredSolver : "+beam2.element().getSecond());
		return beam2.element().getFirst();
	}
	
	public List<Pair<LasttwoY, Double>> getBottomUpBestParse(
			LasttwoX x, Pair<LasttwoY, Double> pair, WeightVector wv) {
		LasttwoY y = pair.getFirst();
		PairComparator<List<Node>> nodePairComparator = new PairComparator<List<Node>>() {};
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
				beam2.addAll(enumerateSingleMerge(state, wv, x, pair.getFirst().varTokens, y.nodes));
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		for(Pair<List<Node>, Double> state : beam1) {
			if(state.getFirst().size() != 2) {
//				System.err.println("Penultimate list should have 2 nodes, found "+state.getFirst().size());
				continue;
			}
			Node node = new Node("EQ", -1, Arrays.asList(
					state.getFirst().get(0), state.getFirst().get(1)));
			beam2.add(new Pair<List<Node>, Double>(Arrays.asList(node), 
					state.getSecond()+ 
					getMergeScore(node, wv, x, pair.getFirst().varTokens, pair.getFirst().nodes)));
		}
		List<Pair<LasttwoY, Double>> results = new ArrayList<Pair<LasttwoY,Double>>();
		for(Pair<List<Node>, Double> b : beam2) {
			LasttwoY t = new LasttwoY(y);
			if(b.getFirst().size() != 1){
//				System.err.println("Final list should have only 1 node, found "+b.getFirst().size());
			}
			t.equation.root = b.getFirst().get(0);
			results.add(new Pair<LasttwoY, Double>(t, b.getSecond()));
		}
		return results;
	}
	
	public List<Pair<List<Node>, Double>> enumerateSingleMerge(
			Pair<List<Node>, Double> state, WeightVector wv, LasttwoX x,
			Map<String, List<Integer>> varTokens, List<Node> nodes) {
		List<Pair<List<Node>, Double>> nextStates = new ArrayList<>();
		List<Node> nodeList = state.getFirst();
		if(nodeList.size() == 1) {
//			System.err.println("List should not have size 1 here");
			List<Pair<List<Node>, Double>> tmpNodeList = 
					new ArrayList<Pair<List<Node>, Double>>();
			tmpNodeList.add(state);
			return tmpNodeList;
		}
		double initScore = state.getSecond();
		for(int i=0; i<nodeList.size(); ++i) {
			for(int j=i+1; j<nodeList.size(); ++j) {
				if(!CompInfSolver.allowMerge(nodeList.get(i), nodeList.get(j))) continue;
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
			Node node1, Node node2, WeightVector wv, LasttwoX x, 
			Map<String, List<Integer>> varTokens, List<Node> nodes) {
		List<Pair<Node, Double>> nextStates = new ArrayList<>();
		List<String> labels = Arrays.asList(
				"ADD", "SUB", "SUB_REV","MUL", "DIV", "DIV_REV");
		double mergeScore;
		String ruleOp = TreeFeatGen.getRuleOperation(node1, node2, x.ta, x.quantities, nodes);
		for(String label : labels) {
			if(ruleOp != null && !ruleOp.equals(label)) continue;
			if(label.endsWith("REV")) {
				label = label.substring(0,3);
				Node node = new Node(label, -1, Arrays.asList(node2, node1));
				mergeScore = getMergeScore(node, wv, x, varTokens, nodes);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			} else {
				Node node = new Node(label, -1, Arrays.asList(node1, node2));
				mergeScore = getMergeScore(node, wv, x, varTokens, nodes);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			}
		}
		return nextStates;
	}
	
	public double getMergeScore(Node node, WeightVector wv, LasttwoX x, 
			Map<String, List<Integer>> varTokens, List<Node> leaves) {
		return wv.dotProduct(featGen.getNodeFeatureVector(x, varTokens, leaves, node));
	}
	
	public LasttwoY getLatentBestStructure(
			LasttwoX x, LasttwoY gold, WeightVector wv) {
		LasttwoY best = null;
		double bestScore = -Double.MAX_VALUE;
		if(gold.varTokens.keySet().size() == 1) {
			for(Integer tokenIndex : gold.varTokens.get("V1")) {
				LasttwoY yNew = new LasttwoY(gold);
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
					LasttwoY yNew = new LasttwoY(gold);
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
		best.nodes = best.equation.root.getLeaves();
		best.coref = gold.coref;
		Tools.populateAndSortByCharIndex(best.nodes, x.ta, x.quantities, x.candidateVars, best.coref);
		return best;
	}
	
}
