package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Node;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class TreeInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	public TreeFeatGen featGen;

	public TreeInfSolver(TreeFeatGen featGen) 
			throws Exception {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		TreeY r1 = (TreeY) arg1;
		TreeY r2 = (TreeY) arg2;
		return TreeY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		TreeX prob = (TreeX) x;
//		System.out.println("Inference called with "+prob.problemIndex);
		// Get best equation trees
		PairComparator<TreeY> pairComparator = 
				new PairComparator<TreeY>() {};
		MinMaxPriorityQueue<Pair<TreeY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(pairComparator)
				.maximumSize(50).create();
		MinMaxPriorityQueue<Pair<TreeY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(pairComparator)
				.maximumSize(50).create();
//		System.out.println("InfSolver called for problem : "+prob.problemIndex);
		
		TreeY seed = new TreeY();
		for(Integer i : prob.relevantQuantIndices) {
			Node node = new Node("NUM", 
					prob.ta.getTokenIdFromCharacterOffset(
							prob.quantities.get(i).start), 
							new ArrayList<Node>());
			node.value = Tools.getValue(prob.quantities.get(i));
			seed.nodes.add(node);
		}
		
		// Grounding of variables
		for(int i=0; i<prob.candidateVars.size(); ++i) {
			TreeY y = new TreeY(seed);
			Node node = new Node("VAR", i, new ArrayList<Node>());
			node.varId = "V1";
			y.nodes.add(node);
			y.varTokens.put("V1", new ArrayList<Integer>());
			y.varTokens.get("V1").add(i);
			beam1.add(new Pair<TreeY, Double>(y, 
					1.0*wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))));
			for(int j=i; j<prob.candidateVars.size(); ++j) {
				y = new TreeY(seed);
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
				beam1.add(new Pair<TreeY, Double>(y, 
						1.0*wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))));
			}
		}
//		System.out.println("Beam size : "+beam1.size());
		
		// Equation generation
		for(Pair<TreeY, Double> pair : beam1) {
			beam2.add(getBottomUpBestParse(prob, pair, wv));
		}
//		System.out.println("Output from inference : "+beam2.element().getFirst());
		return beam2.element().getFirst();
	}
	
	public Pair<TreeY, Double> getBottomUpBestParse(
			TreeX x, Pair<TreeY, Double> pair, WeightVector wv) {
		TreeY y = pair.getFirst();
		Collections.sort(y.nodes, new Comparator<Node>() {
		    @Override
		    public int compare(Node a, Node b) {
		    		return (int) Math.signum(a.index - b.index);
		    }
		});
		PairComparator<List<Node>> nodePairComparator = 
				new PairComparator<List<Node>>() {};
		MinMaxPriorityQueue<Pair<List<Node>, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(nodePairComparator)
				.maximumSize(200).create();
		MinMaxPriorityQueue<Pair<List<Node>, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(nodePairComparator)
				.maximumSize(200).create();
		int n = y.nodes.size();
		List<Node> init = new ArrayList<>();
		init.addAll(y.nodes);
		
		beam1.add(new Pair<List<Node>, Double>(init, pair.getSecond()));
		for(int i=1; i<=n-2; ++i) {
			for(Pair<List<Node>, Double> state : beam1) {
				beam2.addAll(enumerateSingleMerge(state, wv, x));
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		for(Pair<List<Node>, Double> state : beam1) {
			Node node = new Node("EQ", -1, Arrays.asList(
					state.getFirst().get(0), state.getFirst().get(1)));
			beam2.add(new Pair<List<Node>, Double>(Arrays.asList(node), 
					state.getSecond()+wv.dotProduct(featGen.getExpressionFeatureVector(x, node))));
		}
		List<Node> nodes = beam2.element().getFirst();
		double finalScore = beam2.element().getSecond(); 
		assert nodes.size() == 1;
		y.equation.root = nodes.get(0);
		return new Pair<TreeY, Double>(y, finalScore);
	}
	
	public List<Pair<List<Node>, Double>> enumerateSingleMerge(
			Pair<List<Node>, Double> state, WeightVector wv, TreeX x) {
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
						nodeList.get(i), nodeList.get(j), wv, x)) {
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
			Node node1, Node node2, WeightVector wv, TreeX x) {
		List<Pair<Node, Double>> nextStates = new ArrayList<>();
		List<String> labels = Arrays.asList(
				"ADD", "SUB", "SUB_REV","MUL", "DIV", "DIV_REV");
		double mergeScore;
		for(String label : labels) {
			if(label.endsWith("REV")) {
				label = label.substring(0,3);
				Node node = new Node(label, -1, Arrays.asList(node2, node1));
				mergeScore = getScore(node, wv, x);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			} else {
				Node node = new Node(label, -1, Arrays.asList(node1, node2));
				mergeScore = getScore(node, wv, x);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			}
		}
		return nextStates;
	}

	public double getScore(Node node, WeightVector wv, TreeX x) {
		return wv.dotProduct(featGen.getExpressionFeatureVector(x, node));
	}
	
	public TreeY getLatentBestStructure(
			TreeX x, TreeY gold, WeightVector wv) {
		TreeY best = null;
		double bestScore = -Double.MAX_VALUE;
		if(gold.varTokens.keySet().size() == 1) {
			for(Integer tokenIndex : gold.varTokens.get("V1")) {
				TreeY yNew = new TreeY(gold);
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
								node.index = x.ta.getTokenIdFromCharacterOffset(
												x.quantities.get(i).start);
								break;
							}
						}
					}
				}
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
					TreeY yNew = new TreeY(gold);
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
									node.index = x.ta.getTokenIdFromCharacterOffset(
													x.quantities.get(i).start);
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
		return best;
	}
	
}
