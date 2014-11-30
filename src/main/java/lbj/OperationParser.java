package lbj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lbjgen.CompleteClassifier;
import parser.DocReader;
import curator.NewCachingCurator;
import structure.QuantState;
import structure.Expression;
import structure.Operation;
import structure.SimulProb;
import utils.Params;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;

public class OperationParser extends ExpressionParser {

	CompleteClassifier completeClassifier;
	CompleteParser completeParser;
	
	public OperationParser() {
		super();
	}
	
	public OperationParser(double start, double end) {
		DocReader dr = new DocReader();
		List<SimulProb> simulProbList = null;
		System.out.println("Reading data");
		try {
			simulProbList = dr.readSimulProbFromBratDir(
					Params.annotationDir, start, end);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fm = new ExpressionFeatureManager();
		System.out.println("Done");
		exprList = new ArrayList<Expression>();
		index = 0;
		System.out.println("Get Problems from simul probs");
		for (SimulProb simulProb : simulProbList) {
			List<Expression> tmpExprList = null;
			try {
				tmpExprList = extractOperationExprFromSimulProb(simulProb);
				for (Expression expr : tmpExprList) {
					fm.getAllOperationFeatures(expr, simulProb);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Error in node parser");
			}
			exprList.addAll(tmpExprList);
		}
		System.out.println("Done");
	}
	
	public List<Expression> extractOperationExprFromSimulProb(SimulProb simulProb) 
			throws Exception {
		List<Expression> withinEntityPairList = new ArrayList<Expression>();
		for(Expression ex :	extractAllWithinEntityPairExpr(simulProb)) {
			// Populate complete from gold
			populateComplete(ex, simulProb);
			if(ex.complete == Operation.COMPLETE) {
				withinEntityPairList.add(ex);
			} else {
				boolean allow = false;
				for(Expression candidateParent : 
					extractAllWithinEntityPairExpr(simulProb)) {
					if(candidateParent.complete == Operation.COMPLETE) {
						for(Expression subEx : candidateParent
								.getAllSubExpressions()) {
							if(subEx.equalsBasedOnLeaves(ex)) {
								allow = true;
								break;
							}
						}
					}
					if(allow) break;
				}
				if(allow) withinEntityPairList.add(ex);
			}
		}
		for(Expression expr : withinEntityPairList) {
			expr.operation = simulProb.getOperation(expr);
		}
		return withinEntityPairList;
	}
	
	public void populateComplete(Expression ex, SimulProb simulProb) {
		if(simulProb.equations.size() > 0) { 
			ex.complete = simulProb.getComplete(ex);
		} else {
			completeParser.fm.getAllCompleteFeatures(ex, simulProb);
			if(completeClassifier.discreteValue(ex).equals("COMPLETE")) {
				ex.complete = Operation.COMPLETE;
			} else {
				ex.complete = Operation.NONE;
			}
		}
	}
}
