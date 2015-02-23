package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Node;
import structure.PairComparator;
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
		
		return pred;
	}
	
	public Pair<String, List<Node>> getBottomUpBestParse(TreeX x, WeightVector wv) {
		
		List<Node> nodes = new ArrayList<>();
		List<String> labels = null;
		int n = x.eqSpan.getSecond() - x.eqSpan.getFirst();
		
		// Initializing of CKY beam
		PairComparator<Node> nodePairComparator = 
				new PairComparator<Node>() {};
		List<List<MinMaxPriorityQueue<Pair<Node, Double>>>> dpMat = 
				new ArrayList<>();
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
				if(i+1 == j && triggers.get(i).label.equals("NUMBER")) {
					labels = new ArrayList<String>(
							Arrays.asList("EXPR", "ADD", "SUB", "MUL", "DIV", "NULL"));
				} else if(i+1 == j && triggers.get(i).label.equals("OP")) {
					labels = new ArrayList<String>(
							Arrays.asList("OP", "ADD", "SUB", "DIV"));
				} else {
					labels = new ArrayList<String>(
							Arrays.asList("ADD", "SUB", "MUL", "DIV"));
				}
				if(i==0 && j==n) {
					labels.remove("EXPR");
					labels.remove("OP");
					labels.add("EQ");
				}
				for(String label : labels) {
					for(List<IntPair> division : enumerateDivisions(x, i, j)) { 
						for(List<Pair<Node, Double>> childrenPairList : 
							enumerateChildrenPairs(dpMat, division)) {
							double score = 0.0;
							List<Node> children = new ArrayList<>();
							for(Pair<Node, Double> childrenPair : childrenPairList) {
								score += childrenPair.getSecond();
								children.add(childrenPair.getFirst());
							}
							score += 1.0*wv.dotProduct(featGen.getExpressionFeatureVector(
									x, i, j, children, label));
							dpMat.get(i).get(j).add(new Pair<Node, Double>(
									new Node(label, new IntPair(i, j), children), score));
						}
					}
				}
			}
		}
		List<Node> queue = new ArrayList<Node>();
		queue.add(dpMat.get(0).get(n).element().getFirst());
		while(queue.size() > 0) {
			Node expr = queue.get(0);
			nodes.add(expr);
			queue.remove(0);
			for(Node child : expr.children) {
				queue.add(child);
			}
		}
		for(Node node : nodes) {
			node.span.setFirst(node.span.getFirst()+x.eqSpan.getFirst());
			node.span.setSecond(node.span.getSecond()+x.eqSpan.getFirst());
		}
		return new Pair<String, List<Node>>("", nodes);
	}

	public List<List<Pair<Node, Double>>> enumerateChildrenPairs(
			List<List<MinMaxPriorityQueue<Pair<Node, Double>>>> dpMat,
			List<IntPair> division) {
		List<List<Pair<Node, Double>>> childrenList = new ArrayList<>();
		List<List<Pair<Node, Double>>> tmpList = new ArrayList<>();
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

	public static List<List<IntPair>> enumerateDivisions(
			TreeX x, int start, int end) {
		List<List<IntPair>> divisions = new ArrayList<>();
		if(start+1 == end) {
			divisions.add(new ArrayList<IntPair>());
			return divisions;
		}
		for(int i=start+1; i<end; ++i) {
			List<IntPair> div = Arrays.asList(
					new IntPair(start, i), new IntPair(i, end));
			divisions.add(div);
		}
		for(int i=start+1; i<end-1; ++i) {
			for(int j=i+1; j<end; ++j) {
				List<IntPair> div = Arrays.asList(new IntPair(start, i), 
						new IntPair(i, j), new IntPair(j, end));
				if((i-start == 1 && x.triggers.get(start).label.equals("OP")) ||
						(j-i == 1 && x.triggers.get(i).label.equals("OP")) ||
						(end-j == 1 && x.triggers.get(j).label.equals("OP"))) {
					divisions.add(div);
				}
			}
		}
		return divisions;
	}
	
	public static String getEqString(TreeX x, Node node) {
		String str = "";
		if(node.span.getFirst()+1 == node.span.getSecond()) {
			if(node.label.equals("EXPR")) str = 
					""+x.triggers.get(node.span.getFirst()).num;
			if(node.label.equals("ADD")) str = "V+V";
			if(node.label.equals("SUB")) str = "V-V";
			if(node.label.equals("MUL")) str = 
					x.triggers.get(node.span.getFirst()).num+"*V";
			if(node.label.equals("DIV") && 
					x.triggers.get(node.span.getFirst()).num == null) {
				str = "V/V";
			}
			if(node.label.equals("DIV") && 
					x.triggers.get(node.span.getFirst()).num != null) {
				str = "V/"+x.triggers.get(node.span.getFirst()).num;
			}
		} else {
			List<Integer> locs = new ArrayList<>();
			int count = 0;
			List<String> childrenStrings = new ArrayList<>();
			for(int i=0; i<node.children.size(); ++i) {
				String childStr = getEqString(x, node.children.get(i));
				childrenStrings.add(childStr);
				if(!childStr.equals("")) {
					locs.add(i);
					count++;
				}
			}
			if(count == 1) {
				String childStr = childrenStrings.get(locs.get(0));
				if(node.label.equals("ADD")) str = "V+"+childStr;
				if(node.label.equals("SUB")) str = "V-"+childStr;
				if(node.label.equals("MUL")) str = "V*"+childStr;
				if(node.label.equals("DIV")) str = "V/"+childStr;
			}
			if(count == 2) {
				String childStr1 = childrenStrings.get(locs.get(1));
				String childStr2 = childrenStrings.get(locs.get(0));
				if(node.label.equals("EQ")) str = childStr1+"="+childStr2;
				if(node.label.equals("ADD")) str = childStr1+"+"+childStr2;
				if(node.label.equals("SUB")) str = childStr1+"-"+childStr2;
				if(node.label.equals("MUL")) str = childStr1+"*"+childStr2;
				if(node.label.equals("DIV")) str = childStr1+"/"+childStr2;
			}
		}
		return str;
	}
	
	public static String postProcessEqString(String str) {
		int count = 0;
		String newStr = "";
		for(int i=0; i<str.length(); ++i) {
			if(str.charAt(i) == 'V') {
				count++;
				newStr += "V"+count;
			} else {
				newStr += str.charAt(i);
			}
		}
		if(!newStr.contains("=")) {
			count++;
			newStr = newStr+"=V"+count;
		}
		return newStr;
	}
	
}
