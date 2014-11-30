package lbj;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structure.Operation;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.learn.TestingMetric;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class Evaluate {

//	public static double testTopKFromSameEntity(
//			SameEntityTermClassifier predict, 
//			SameEntityTermLabel gold, 
//			NodeParser parser,
//			int topK) {
//		// TODO Auto-generated method stub
//		Set<String> totalProblems = new HashSet<String>();
//		Set<String> incorrectProblems = new HashSet<String>();	
//		System.out.println("Parser type : " + parser.next().getClass());
//		for(Node node = (Node)parser.next(); node != null; 
//				node = (Node)parser.next()) {
//			if(totalProblems.contains(
//					node.simulProb.index + "_" + node.entityName)) {
//				continue;
//			} else {  
//				totalProblems.add(
//						node.simulProb.index + "_" + node.entityName);
//				List<Node> goldTerms = parser.extractGoldTermsFromSimulProb(
//						node.simulProb, node.entityName);
//				List<Node> predictedTerms = 
//						parser.extractTopKTermsFromSimulProb(
//								node.simulProb, node.entityName, topK);
//				for(Node node1 : goldTerms) {
//					boolean found = false;
//					for(Node node2 : predictedTerms) {
//						if(node1.toString().equals(node2.toString())) {
//							found = true;
//							break;
//						}
//					}
//					if(!found) {
//						incorrectProblems.add(
//								node.simulProb.index + "_" + node.entityName);
//					}
//				}
//			}
//		}
//		return 1.0 - incorrectProblems.size()*1.0/totalProblems.size();	
//	}

}
