package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Equation;
import structure.Node;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class TreeInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private TreeFeatGen featGen;

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
		TreeY pred = new TreeY();
		// Get best equation trees
		PairComparator<TreeY> pairComparator = 
				new PairComparator<TreeY>() {};
		MinMaxPriorityQueue<Pair<TreeY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(pairComparator)
				.maximumSize(200).create();
		MinMaxPriorityQueue<Pair<TreeY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(pairComparator)
				.maximumSize(200).create();
		
		// Grounding of variables
		for(int i=0; i<prob.ta.size(); ++i) {
//			if(prob.posTags.get(i).getLabel().startsWith("N") || 
//					prob.posTags.get(i).getLabel().startsWith("V") ||
//					prob.posTags.get(i).getLabel().startsWith("J") ||
//					prob.posTags.get(i).getLabel().equals("CD")) {
				TreeY y = new TreeY();
				Node node = new Node("VAR", i, null, new ArrayList<Node>());
				node.varId = "V1";
				y.nodes.add(node);
				y.varTokens.put("V1", new ArrayList<Integer>());
				y.varTokens.get("V1").add(i);
				beam1.add(new Pair<TreeY, Double>(y, 
						1.0*wv.dotProduct(featGen.getVarTokenFeatureVector(y))));
				
				for(int j=i; j<prob.ta.size(); ++j) {
//					if(prob.posTags.get(j).getLabel().startsWith("N") || 
//							prob.posTags.get(j).getLabel().startsWith("V") ||
//							prob.posTags.get(j).getLabel().startsWith("J") ||
//							prob.posTags.get(j).getLabel().equals("CD")) {
						y = new TreeY();
						node = new Node("VAR", i, null, new ArrayList<Node>());
						node.varId = "V1";
						y.nodes.add(node);
						node = new Node("VAR", j, null, new ArrayList<Node>());
						node.varId = "V2";
						y.nodes.add(node);
						y.varTokens.put("V1", new ArrayList<Integer>());
						y.varTokens.put("V2", new ArrayList<Integer>());
						y.varTokens.get("V1").add(i);
						y.varTokens.get("V2").add(j);
						beam1.add(new Pair<TreeY, Double>(y, 
								1.0*wv.dotProduct(featGen.getVarTokenFeatureVector(y))));
//					}
				}
//			}
		}
		
		// Relevant Quantity Detection
		for(Pair<TreeY, Double> pair : beam1) {
			for(int i=0; i<prob.quantities.size(); ++i) {
				beam2.add(new Pair<TreeY, Double>(pair.getFirst(), pair.getSecond() +
						1.0*wv.dotProduct(featGen.getQuantityFeatureVector(i))));
				TreeY y = new TreeY(pair.getFirst());
				Node node = new Node("NUM", 
						prob.ta.getTokenIdFromCharacterOffset(
								prob.quantities.get(i).start), 
								null,
								new ArrayList<Node>());
				node.value = Tools.getValue(prob.quantities.get(i));
				y.nodes.add(node);
				beam2.add(new Pair<TreeY, Double>(y, pair.getSecond()));
			}
		}
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		
		// Equation generation
		for(Pair<TreeY, Double> pair : beam1) {
			beam2.add(getBottomUpBestParse(prob, pair, wv));
		}
		return beam2.element().getFirst();
	}
	
	public Pair<TreeY, Double> getBottomUpBestParse(
			TreeX x, Pair<TreeY, Double> pair, WeightVector wv) {
		TreeY y = pair.getFirst();
		Collections.sort(y.nodes, new Comparator<Node>() {
		    @Override
		    public int compare(Node a, Node b) {
		    		return (int) Math.signum(a.tokenIndex - b.tokenIndex);
		    }
		});
		
		// Initializing of CKY beam
		PairComparator<Node> nodePairComparator = 
				new PairComparator<Node>() {};
		List<List<MinMaxPriorityQueue<Pair<Node, Double>>>> dpMat = 
				new ArrayList<>();
		int n = y.nodes.size();		
		for(int i=0; i<=n; i++) {
			dpMat.add(new ArrayList<MinMaxPriorityQueue<Pair<Node, Double>>>());
			for(int j=0; j<=n; ++j) {
				dpMat.get(i).add(MinMaxPriorityQueue.orderedBy(nodePairComparator)
						.maximumSize(50).create());
			}
		}
		
		// CKY Beam Search
		for(int j=1; j<=n; ++j) {
			for(int i=j-1; i>=0; --i) {
				List<String> labels = new ArrayList<>();
				if(i+1 == j) {
					y.nodes.get(i).span = new IntPair(i, j);
					dpMat.get(i).get(j).add(new Pair<Node, Double>(y.nodes.get(i), 0.0));
					continue;
				} else if(i == 0 && j == n) {
					labels.add("EQ");
				} else {
					labels.addAll(Arrays.asList("ADD", "SUB", "MUL", "DIV"));
				}
				for(String label : labels) {
					for(int k=i+1; k<j; ++k) {
						for(List<Pair<Node, Double>> childrenPairList : 
							enumerateChildrenPairs(dpMat, i, k, j)) {
							double score = 0.0;
							List<Node> children = new ArrayList<>();
							for(Pair<Node, Double> childrenPair : childrenPairList) {
								score += childrenPair.getSecond();
								children.add(childrenPair.getFirst());
							}
							score += 1.0*wv.dotProduct(featGen.getExpressionFeatureVector(
									x, i, j, children, label));
							dpMat.get(i).get(j).add(new Pair<Node, Double>(
									new Node(label, -1, new IntPair(i, j), children), score));
						}
					}
				}
			}
		}
		TreeY pred = new TreeY(pair.getFirst());
		pred.equation = new Equation();
		pred.equation.root = dpMat.get(0).get(n).element().getFirst();
		return new Pair<TreeY, Double>(pred, pair.getSecond() + 
				dpMat.get(0).get(n).element().getSecond());
	}

	public List<List<Pair<Node, Double>>> enumerateChildrenPairs(
			List<List<MinMaxPriorityQueue<Pair<Node, Double>>>> dpMat,
			int i, int k, int j) {
		List<List<Pair<Node, Double>>> childrenList = new ArrayList<>();
		List<List<Pair<Node, Double>>> tmpList = new ArrayList<>();
		List<IntPair> division = Arrays.asList(new IntPair(i, k), new IntPair(k, j));
		childrenList.add(new ArrayList<Pair<Node, Double>>());
		
		for(IntPair ip : division) {
			for(Pair<Node, Double> pair : dpMat.get(ip.getFirst())
					.get(ip.getSecond())) {
				for(List<Pair<Node, Double>> list : childrenList) {
					List<Pair<Node, Double>> newList = new ArrayList<>();
					newList.addAll(list);
					newList.add(pair);
					tmpList.add(newList);
				}
			}
			childrenList.clear();
			childrenList.addAll(tmpList);
			tmpList.clear();
		}
		return childrenList;
	}
	
}
