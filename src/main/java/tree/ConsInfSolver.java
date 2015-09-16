package tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import numoccur.NumoccurX;
import numoccur.NumoccurY;
import struct.lca.LcaX;
import structure.Node;
import structure.PairComparator;
import utils.FeatGen;
import utils.Tools;
import var.VarX;
import var.VarY;

import com.google.common.collect.MinMaxPriorityQueue;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLModel;

public class ConsInfSolver {
	
	public static double numOccurScale, varScale;
	
	public static TreeY getBestStructure(TreeX prob, SLModel numOccurModel, 
			SLModel varModel, SLModel lcaModel, TreeY gold) throws Exception {
		PairComparator<TreeY> pairComparator = 
				new PairComparator<TreeY>() {};
		MinMaxPriorityQueue<Pair<TreeY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		MinMaxPriorityQueue<Pair<TreeY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		TreeY seed = new TreeY();
		beam1.add(new Pair<TreeY, Double>(seed, 0.0));
		
		// Predict number of occurrences of each quantity
		for(int i=0; i<prob.quantities.size(); ++i) {
			for(Pair<TreeY, Double> pair : beam1) {
				for(int j=0; j<3; ++j) {
					List<String> features = numoccur.NumoccurFeatGen.getFeatures(
							new NumoccurX(prob, i),
							new NumoccurY(j));
					double score = numOccurScale*numOccurModel.wv.dotProduct(
							FeatGen.getFeatureVectorFromList(features, numOccurModel.lm));
					TreeY y = new TreeY(pair.getFirst());
					for(int k=0; k<j; ++k) {
						Node node = new Node("NUM", i, new ArrayList<Node>());
						node.value = Tools.getValue(prob.quantities.get(i));
						y.nodes.add(node);
					}
					beam2.add(new Pair<TreeY, Double>(y, pair.getSecond()+score));
				}
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		
		for(Pair<TreeY, Double> pair : beam1) {
			double score = numOccurScale*numOccurModel.wv.dotProduct(
					((struct.numoccur.NumoccurFeatGen) numOccurModel.featureGenerator).
					getGlobalFeatureVector(new struct.numoccur.NumoccurX(prob), 
							new struct.numoccur.NumoccurY(prob, pair.getFirst().nodes)));
			beam2.add(new Pair<TreeY, Double>(pair.getFirst(), pair.getSecond()+score));
		}
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();

		beam2.add(beam1.element());
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		
		// Grounding of variables
		for(Pair<TreeY, Double> pair : beam1) {
			for(int i=0; i<prob.candidateVars.size(); ++i) {
				TreeY y = new TreeY(pair.getFirst());
				Node node = new Node("VAR", i, new ArrayList<Node>());
				node.varId = "V1";
				y.nodes.add(node);
				y.varTokens.put("V1", new ArrayList<Integer>());
				y.varTokens.get("V1").add(i);
				beam2.add(new Pair<TreeY, Double>(y, pair.getSecond()+varScale*varModel.wv.dotProduct(
						varModel.featureGenerator.getFeatureVector(new VarX(prob), new VarY(y)))));
				for(int j=i; j<prob.candidateVars.size(); ++j) {
					y = new TreeY(pair.getFirst());
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
					beam2.add(new Pair<TreeY, Double>(y, 
							pair.getSecond()+varScale*varModel.wv.dotProduct(
							varModel.featureGenerator.getFeatureVector(new VarX(prob), new VarY(y)))));
				}
			}
		}
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();

		beam2.add(beam1.element());
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		
		// Equation generation
		struct.lca.LcaX x = new LcaX(prob, beam1.element().getFirst().varTokens, 
				beam1.element().getFirst().nodes);
		struct.lca.LcaY y = (struct.lca.LcaY) lcaModel.infSolver.getBestStructure(lcaModel.wv, x); 
		for(Pair<TreeY, Double> pair : beam1) {
			beam2.addAll(getBottomUpBestParse(prob, pair, lcaModel));
		}
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		if(struct.lca.LcaY.getLoss(y, new struct.lca.LcaY(beam1.element().getFirst())) > 0.001) {
			System.out.println("HOLA HERE");
			System.out.println("Gold : "+y);
			System.out.println("Pred : "+new struct.lca.LcaY(beam1.element().getFirst()));
		}
		return beam1.element().getFirst();
	}
	
	public static List<Pair<TreeY, Double>> getBottomUpBestParse(
			TreeX x, Pair<TreeY, Double> pair, SLModel lcaModel) {
		TreeY y = pair.getFirst();
		PairComparator<List<Node>> nodePairComparator = 
				new PairComparator<List<Node>>() {};
		MinMaxPriorityQueue<Pair<List<Node>, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(nodePairComparator)
				.maximumSize(50).create();
		MinMaxPriorityQueue<Pair<List<Node>, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(nodePairComparator)
				.maximumSize(50).create();
		int n = y.nodes.size();
		List<Node> init = new ArrayList<>();
		init.addAll(y.nodes);
		beam1.add(new Pair<List<Node>, Double>(init, pair.getSecond()));
		for(int i=1; i<=n-2; ++i) {
			for(Pair<List<Node>, Double> state : beam1) {
				beam2.addAll(enumerateSingleMerge(state, lcaModel, x, 
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
					state.getSecond()+getLcaScore(node, lcaModel, x, 
							pair.getFirst().varTokens, pair.getFirst().nodes)));
		}
		List<Pair<TreeY, Double>> results = new ArrayList<Pair<TreeY,Double>>();
		for(Pair<List<Node>, Double> b : beam2) {
			TreeY t = new TreeY(y);
			assert b.getFirst().size() == 1;
			t.equation.root = b.getFirst().get(0);
			results.add(new Pair<TreeY, Double>(t, b.getSecond()));
		}
		return results;
	}
	
	public static List<Pair<List<Node>, Double>> enumerateSingleMerge(
			Pair<List<Node>, Double> state, SLModel lcaModel, TreeX x, 
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
						nodeList.get(i), nodeList.get(j), lcaModel, x, varTokens, nodes)) {
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
	
	public static List<Pair<Node, Double>> enumerateMerge(
			Node node1, Node node2, SLModel lcaModel, TreeX x, 
			Map<String, List<Integer>> varTokens, List<Node> nodes) {
		List<Pair<Node, Double>> nextStates = new ArrayList<>();
		List<String> labels = Arrays.asList(
				"ADD", "SUB", "SUB_REV","MUL", "DIV", "DIV_REV");
		double mergeScore;
		for(String label : labels) {
			if(label.endsWith("REV")) {
				label = label.substring(0,3);
				Node node = new Node(label, -1, Arrays.asList(node2, node1));
				mergeScore = getLcaScore(node, lcaModel, x, varTokens, nodes);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			} else {
				Node node = new Node(label, -1, Arrays.asList(node1, node2));
				mergeScore = getLcaScore(node, lcaModel, x, varTokens, nodes);
				nextStates.add(new Pair<Node, Double>(node, mergeScore));
			}
		}
		return nextStates;
	}

	public static double getLcaScore(Node node, SLModel lcaModel, TreeX x, 
			Map<String, List<Integer>> varTokens, List<Node> nodes) {
		List<String> features = new ArrayList<String>();
		struct.lca.LcaX lcaX = new struct.lca.LcaX(x, varTokens, nodes);
		features.addAll(struct.lca.LcaFeatGen.getPairFeatures(lcaX, node));
		return lcaModel.wv.dotProduct(FeatGen.getFeatureVectorFromList(features, lcaModel.lm));
	}
	
}