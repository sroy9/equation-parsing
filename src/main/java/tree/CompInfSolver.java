package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Node;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class CompInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	public CompFeatGen featGen;

	public CompInfSolver(CompFeatGen featGen) throws Exception {
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
		MinMaxPriorityQueue<Pair<TreeY, Double>> beam = 
				getLossAugmentedBestKStructure(wv, x, goldStructure);
		return beam.element().getFirst();
	}
	
	public MinMaxPriorityQueue<Pair<TreeY, Double>> getLossAugmentedBestKStructure(
			WeightVector wv, IInstance x, IStructure goldStructure) throws Exception {
		TreeX prob = (TreeX) x;
		assert prob.nodes.size() > 2;
		PairComparator<TreeY> pairComparator = new PairComparator<TreeY>() {};
		MinMaxPriorityQueue<Pair<TreeY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		MinMaxPriorityQueue<Pair<TreeY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		TreeY seed = new TreeY();
		seed.varTokens = prob.varTokens;
		beam1.add(new Pair<TreeY, Double>(seed, 0.0));
		for(Pair<TreeY, Double> pair : beam1) {
			beam2.addAll(getBottomUpBestCkyParse(prob, pair, wv));
		}
		return beam2;
	}
	
	public List<Pair<TreeY, Double>> getBottomUpBestCkyParse(
			TreeX x, Pair<TreeY, Double> pair, WeightVector wv) {
		TreeY y = pair.getFirst();
		List<Pair<TreeY, Double>> results = new ArrayList<Pair<TreeY,Double>>();
//		Tools.visualizeNodeLocWithSynParse(x.ta, x.parse, x.nodes);
		for(Pair<Node, Double> b : getCkyBest(x.nodes, wv, x, false)) {
			TreeY t = new TreeY(y);
			t.equation.root = b.getFirst();
			results.add(new Pair<TreeY, Double>(t, b.getSecond()));
		}
		return results;
	}
	
	public List<Pair<Node, Double>> enumerateMerge(
			Node node1, Node node2, WeightVector wv, TreeX x) {
		List<Pair<Node, Double>> nextStates = new ArrayList<>();
		List<String> labels = Arrays.asList(
				"ADD", "SUB", "SUB_REV","MUL", "DIV", "DIV_REV");
		double mergeScore;
		String ruleOp = TreeFeatGen.getRuleOperation(node1, node2, x.ta, x.quantities, x.nodes);
		for(String label : labels) {
			if(ruleOp != null && !ruleOp.equals(label)) continue;
			if(label.endsWith("REV")) {
				label = label.substring(0,3);
				Node node = new Node(label, -1, Arrays.asList(node2, node1));
				mergeScore = getMergeScore(node, wv, x);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			} else {
				Node node = new Node(label, -1, Arrays.asList(node1, node2));
				mergeScore = getMergeScore(node, wv, x);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			}
		}
		return nextStates;
	}

	public double getMergeScore(Node node, WeightVector wv, TreeX x) {
		return wv.dotProduct(featGen.getNodeFeatureVector(x, node));
	}
	
	
//	public static boolean allowMerge(Node node1, Node node2) {
//		IntPair ip1 = node1.getNodeListSpan();
//		IntPair ip2 = node2.getNodeListSpan();
////		return true;
//		if((ip1.getSecond()+1)==ip2.getFirst() || (ip2.getSecond()+1)==ip1.getFirst()) {
//			return true;
//		}
//		return false;
//	}
	
	public MinMaxPriorityQueue<Pair<Node, Double>> getCkyBest(
			List<Node> leaves, WeightVector wv, TreeX x, boolean useSyntacticParse) {
		PairComparator<Node> nodeComparator = new PairComparator<Node>() {};
		int n = leaves.size();
		List<List<MinMaxPriorityQueue<Pair<Node, Double>>>> cky = 
				new ArrayList<List<MinMaxPriorityQueue<Pair<Node,Double>>>>();
		for(int i=0; i<=n; ++i) {
			cky.add(new ArrayList<MinMaxPriorityQueue<Pair<Node,Double>>>());
			for(int j=0; j<=n; ++j) {
				cky.get(i).add(MinMaxPriorityQueue.orderedBy(nodeComparator).
						maximumSize(5).create());
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
								if(useSyntacticParse && !doesSynParseAllow(x, leaves, node)) continue;
								cky.get(i).get(j).add(new Pair<Node, Double>(node, 
										pair1.getSecond()+pair2.getSecond()+getMergeScore(node, wv, x)));
								continue;
							}
							for(Pair<Node, Double> combinedPair : 
								enumerateMerge(pair1.getFirst(), pair2.getFirst(), wv, x)) {
								if(useSyntacticParse && !doesSynParseAllow(x, leaves, combinedPair.getFirst())) continue;
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

	public boolean doesSynParseAllow(TreeX x, List<Node> leaves, Node subTree) {
		Set<IntPair> spansSynParse = new HashSet<>();
		Set<IntPair> spansSubTree = new HashSet<>();
		for(Constituent cons : x.parse) {
			int start = -1, end = -1;
			for(int i=0; i<leaves.size(); ++i) {
				Node leaf = leaves.get(i);
				if(leaf.charIndex >= cons.getStartCharOffset()) {
					start = i;
					break;
				}
			}
			if(start == -1) continue;
			for(int i=start+1; i<leaves.size(); ++i) {
				Node leaf = leaves.get(i);
				if(leaf.charIndex > cons.getEndCharOffset()) {
					end = i-1;
					break;
				}
			}
			if(end == -1) continue;
			spansSynParse.add(new IntPair(start, end));
		}
		for(Node node : subTree.getAllSubNodes()) {
			spansSubTree.add(node.getNodeListSpan());
		}
		IntPair fullSpan = subTree.getNodeListSpan();
		for(IntPair span : spansSynParse) {
			if(fullSpan.getFirst() <= span.getFirst() && 
					span.getSecond() <= fullSpan.getSecond() &&
					!spansSubTree.contains(span)) {				
				return false;
			}
		}
		return true;
	}
	
}
