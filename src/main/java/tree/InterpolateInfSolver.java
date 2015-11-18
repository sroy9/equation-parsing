//package tree;
//
//import com.google.common.collect.MinMaxPriorityQueue;
//import structure.PairComparator;
//import tree.TreeX;
//import tree.TreeY;
//import edu.illinois.cs.cogcomp.core.datastructures.Pair;
//import edu.illinois.cs.cogcomp.sl.core.SLModel;
//
//public class InterpolateInfSolver {
//	
//	public static TreeY getBestTree(TreeX prob, SLModel composeModel, 
//			SLModel nonComposeModel) throws Exception {
//		double lambda = 100.0;
//		MinMaxPriorityQueue<Pair<TreeY, Double>> beam1 = 
//				((CompInfSolver)composeModel.infSolver).
//				getLossAugmentedBestKStructure(composeModel.wv, prob, null);
//		MinMaxPriorityQueue<Pair<TreeY, Double>> beam2 = 
//				((NoncompInfSolver)nonComposeModel.infSolver).
//				getLossAugmentedBestKStructure(nonComposeModel.wv, prob, null);
//		PairComparator<TreeY> pairComparator = 
//				new PairComparator<TreeY>() {};
//		MinMaxPriorityQueue<Pair<TreeY, Double>> beam3 = 
//				MinMaxPriorityQueue.orderedBy(pairComparator).
//				maximumSize(200).create();
//		int depth = 5;
//		for(int i=0; i<depth; ++i) {
//			Pair<TreeY, Double> pair = beam1.poll();
//			Double score = nonComposeModel.wv.dotProduct(
//					nonComposeModel.featureGenerator.getFeatureVector(
//							prob, pair.getFirst()))*1.0;
//			beam3.add(new Pair<TreeY, Double>(pair.getFirst(), lambda*pair.getSecond()+score));
//		}
//		for(int i=0; i<depth; ++i) {
//			Pair<TreeY, Double> pair = beam2.poll();
//			Double score = composeModel.wv.dotProduct(
//					composeModel.featureGenerator.getFeatureVector(
//							prob, pair.getFirst()))*1.0;
//			beam3.add(new Pair<TreeY, Double>(pair.getFirst(), lambda*pair.getSecond()+score));
//		}
//		return beam3.element().getFirst();
//	}
//	
//}