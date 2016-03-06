package lasttwo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Node;
import structure.PairComparator;
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
					if(y.nodes.size()>2) {
						beam2.add(new Pair<LasttwoY, Double>(y, pair.getSecond()+
								wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))));
					}
				}
			}
		}
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		// Equation generation
		for(Pair<LasttwoY, Double> pair : beam1) {
			Tools.populateAndSortByCharIndex(pair.getFirst().nodes, prob.ta, 
					prob.quantities, prob.candidateVars);
			beam2.addAll(getBottomUpBestCkyParse(prob, pair, wv));
		}
		return beam2.element().getFirst();
	}
	
	public List<Pair<LasttwoY, Double>> getBottomUpBestCkyParse(
			LasttwoX x, Pair<LasttwoY, Double> pair, WeightVector wv) {
		LasttwoY y = pair.getFirst();
		List<Pair<LasttwoY, Double>> results = new ArrayList<Pair<LasttwoY,Double>>();
		for(Pair<Node, Double> b : getCkyBest(y.nodes, y.varTokens, wv, x)) {
			LasttwoY t = new LasttwoY(y);
			t.equation.root = b.getFirst();
			results.add(new Pair<LasttwoY, Double>(t, pair.getSecond() + b.getSecond()));
		}
		return results;
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
	
	public MinMaxPriorityQueue<Pair<Node, Double>> getCkyBest(
			List<Node> leaves, Map<String, List<Integer>> varTokens, WeightVector wv, LasttwoX x) {
		PairComparator<Node> nodeComparator = new PairComparator<Node>() {};
		int n = leaves.size();
		List<List<MinMaxPriorityQueue<Pair<Node, Double>>>> cky = 
				new ArrayList<List<MinMaxPriorityQueue<Pair<Node,Double>>>>();
		for(int i=0; i<=n; ++i) {
			cky.add(new ArrayList<MinMaxPriorityQueue<Pair<Node,Double>>>());
			for(int j=0; j<=n; ++j) {
				cky.get(i).add(MinMaxPriorityQueue.orderedBy(nodeComparator).
						maximumSize(1).create());
				if(i==(j-1)) {
					cky.get(i).get(j).add(new Pair<Node, Double>(leaves.get(i), 0.0));
				}
			}
		}
		for(int j=2; j<=n; ++j) {
			for(int i=j-2; i>=0; --i) {
				for(int k=i+1; k<j; ++k) {
					for(Pair<Node, Double> pair1 : cky.get(i).get(k)) {
						for(Pair<Node, Double> pair2 : cky.get(k).get(j)) {
							if(i==0 && j==n) {
								Node node = new Node("EQ", -1, Arrays.asList(
										pair1.getFirst(), pair2.getFirst()));
								cky.get(i).get(j).add(new Pair<Node, Double>(node, 
										pair1.getSecond()+pair2.getSecond()+
										getMergeScore(node, wv, x, varTokens, leaves)));
								continue;
							}
							for(Pair<Node, Double> combinedPair : 
								enumerateMerge(pair1.getFirst(), pair2.getFirst(), wv, x, varTokens, leaves)) {
								cky.get(i).get(j).add(new Pair<Node, Double>(
										combinedPair.getFirst(), 
										combinedPair.getSecond()+pair1.getSecond()+pair2.getSecond()));	
							}
						}
					}
				}
			}
		}
		return cky.get(0).get(n);
	}
	
	public LasttwoY getLatentBestStructure(
			LasttwoX x, LasttwoY gold, WeightVector wv) {
		LasttwoY best = null;
		double bestScore = -Double.MAX_VALUE;
		for(Map<String, List<Integer>> varTokens : Tools.enumerateProjectiveVarTokens(
				gold.varTokens, gold.equation, x.ta, x.quantities, x.candidateVars)) {
			LasttwoY yNew = new LasttwoY(gold);
			Tools.populateNodesWithVarTokensInPlace(yNew.equation.root.getLeaves(), 
					varTokens, x.quantities);
			double score = wv.dotProduct(featGen.getFeatureVector(x, yNew));
			if(score > bestScore) {
				best = yNew;
			}
		}
		best.nodes = best.equation.root.getLeaves();
		Tools.populateAndSortByCharIndex(best.nodes, x.ta, x.quantities, x.candidateVars);
		return best;
	}
	
}
