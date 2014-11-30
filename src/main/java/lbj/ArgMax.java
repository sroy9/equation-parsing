package lbj;

import java.util.ArrayList;
import java.util.List;

import lbjgen.CompleteClassifier;
import structure.Expression;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;


public class ArgMax {
	
//	public static List<Expression> getMostProbableNodes(
//			CompleteClassifier classifier, List<Expression> exprList, 
//			SimulProb simulProb, int topK) throws Exception {
//		ExpressionFeatureManager fm = new ExpressionFeatureManager();
//		for(Expression expr : exprList) {
//			fm.getAllCompleteFeatures(expr, simulProb);
//			ScoreSet scoreSet = classifier.scores(expr);
//			for(Score score : scoreSet.toArray()) {
//				if(score.value.equals("COMPLETE")) {
//					expr.completeScore = score.score;
//					break;
//				}
//			}
//		}
//		// Now find the top k
//		List<Expression> rankedNodes = new ArrayList<Expression>();
//		for(int i = 0; i < topK ; ++i) {
//			if(exprList.size() == 0) {
//				break;
//			}
//			Expression best = findBest(exprList);
//			rankedNodes.add(best);
//			exprList.remove(best);
//		}
//		return rankedNodes;
//	}
	

	public static List<Expression> getMostProbableNodes(
			CompleteClassifier classifier, List<Expression> exprList, 
			SimulProb simulProb, int topK) throws Exception {
		List<Expression> selectedNodes = new ArrayList<Expression>();
		ExpressionFeatureManager fm = new ExpressionFeatureManager();
		for(Expression expr : exprList) {
			fm.getAllCompleteFeatures(expr, simulProb);
			if(classifier.discreteValue(expr).equals("COMPLETE")) {
				selectedNodes.add(expr);
			}
		}
		return selectedNodes;
	}
	
	// Assumes score has been computed
	public static Expression findBest(List<Expression> exprList) {
		Expression best = null;
		for(Expression expr : exprList) {
			if(best == null || expr.completeScore > best.completeScore) {
				best = expr;
			}
		}
		return best;
	}

}
