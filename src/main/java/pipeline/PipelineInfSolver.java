package pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import joint.JointX;
import joint.JointY;
import numoccur.NumoccurX;
import numoccur.NumoccurY;
import structure.Node;
import structure.PairComparator;
import utils.FeatGen;
import utils.Tools;
import var.VarX;
import var.VarY;

import com.google.common.collect.MinMaxPriorityQueue;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLModel;

public class PipelineInfSolver {
	
//	public static double numOccurScale, varScale;
	
	public static JointY getBestStructure(JointX prob, SLModel numOccurModel, 
			SLModel varModel, SLModel lcaModel, JointY gold) throws Exception {
		PairComparator<JointY> pairComparator = 
				new PairComparator<JointY>() {};
		MinMaxPriorityQueue<Pair<JointY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		MinMaxPriorityQueue<Pair<JointY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		JointY seed = new JointY();
		beam1.add(new Pair<JointY, Double>(seed, 0.0));
		
		// Predict number of occurrences of each quantity
		for(int i=0; i<prob.quantities.size(); ++i) {
			for(Pair<JointY, Double> pair : beam1) {
				for(int j=0; j<3; ++j) {
					List<String> features = numoccur.NumoccurFeatGen.getFeatures(
							new NumoccurX(prob, i),
							new NumoccurY(j));
					double score = numOccurModel.wv.dotProduct(
							FeatGen.getFeatureVectorFromList(features, numOccurModel.lm));
					JointY y = new JointY(pair.getFirst());
					for(int k=0; k<j; ++k) {
						Node node = new Node("NUM", i, new ArrayList<Node>());
						node.value = Tools.getValue(prob.quantities.get(i));
						y.nodes.add(node);
					}
					beam2.add(new Pair<JointY, Double>(y, pair.getSecond()+score));
				}
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		if(PipelineDriver.useSPforNumoccur) {
			for(Pair<JointY, Double> pair : beam1) {
				double score = numOccurModel.wv.dotProduct(
						((numoccur.NumoccurFeatGen) numOccurModel.featureGenerator).
						getGlobalFeatureVector(new numoccur.NumoccurX(prob), 
								new numoccur.NumoccurY(prob, pair.getFirst().nodes)));
				beam2.add(new Pair<JointY, Double>(pair.getFirst(), pair.getSecond()+score));
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		beam2.add(beam1.element());
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		
		// Grounding of variables
		for(Pair<JointY, Double> pair : beam1) {
			for(int i=0; i<prob.candidateVars.size(); ++i) {
				JointY y = new JointY(pair.getFirst());
				Node node = new Node("VAR", i, new ArrayList<Node>());
				node.varId = "V1";
				y.nodes.add(node);
				y.varTokens.put("V1", new ArrayList<Integer>());
				y.varTokens.get("V1").add(i);
				y.coref = false;
				beam2.add(new Pair<JointY, Double>(y, pair.getSecond()+varModel.wv.dotProduct(
						varModel.featureGenerator.getFeatureVector(new VarX(prob), new VarY(y)))));
				for(int j=i; j<prob.candidateVars.size(); ++j) {
					y = new JointY(pair.getFirst());
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
					beam2.add(new Pair<JointY, Double>(y, 
							pair.getSecond()+varModel.wv.dotProduct(
							varModel.featureGenerator.getFeatureVector(new VarX(prob), new VarY(y)))));
					y = new JointY(pair.getFirst());
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
					beam2.add(new Pair<JointY, Double>(y, 
							pair.getSecond()+varModel.wv.dotProduct(
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
		for(Pair<JointY, Double> pair : beam1) {
			beam2.addAll(getBottomUpBestParse(prob, pair, lcaModel));
		}
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		return beam1.element().getFirst();
	}
	
	public static List<Pair<JointY, Double>> getBottomUpBestParse(
			JointX x, Pair<JointY, Double> pair, SLModel lcaModel) {
		JointY y = pair.getFirst();
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
		List<Pair<JointY, Double>> results = new ArrayList<Pair<JointY,Double>>();
		for(Pair<List<Node>, Double> b : beam2) {
			JointY t = new JointY(y);
			assert b.getFirst().size() == 1;
			t.equation.root = b.getFirst().get(0);
			results.add(new Pair<JointY, Double>(t, b.getSecond()));
		}
		return results;
	}
	
	public static List<Pair<List<Node>, Double>> enumerateSingleMerge(
			Pair<List<Node>, Double> state, SLModel lcaModel, JointX x, 
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
			Node node1, Node node2, SLModel lcaModel, JointX x, 
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

	public static double getLcaScore(Node node, SLModel lcaModel, JointX x, 
			Map<String, List<Integer>> varTokens, List<Node> nodes) {
		List<String> features = new ArrayList<String>();
		tree.TreeX lcaX = new tree.TreeX(x, varTokens, nodes);
//		if(ConsDriver.useSPforLCA) {
			features.addAll(tree.TreeFeatGen.getPairFeatures(lcaX, node));
//		} else {
//			features.addAll(struct.lca.LcaFeatGen.getPairFeaturesWithoutGlobalPrefix(lcaX, node));
//		}
		return lcaModel.wv.dotProduct(FeatGen.getFeatureVectorFromList(features, lcaModel.lm));
	}
	
}