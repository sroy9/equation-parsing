package lbj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import parser.DocReader;
import structure.Expression;
import structure.Operation;
import structure.SimulProb;
import utils.Params;

public class CompleteParser extends ExpressionParser {
	
	public CompleteParser() {
		super();
	}
	
	public CompleteParser(double start, double end) {
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
			List<Expression> tmpExprList = extractCompleteExprFromSimulProb(simulProb);
			for (Expression expr : tmpExprList) {
				try {
					fm.getAllCompleteFeatures(expr, simulProb);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Error in node parser");
				}
			}
			exprList.addAll(tmpExprList);
		}
		System.out.println("Done");
	}
	
	public List<Expression> extractCompleteExprFromSimulProb (SimulProb simulProb) {
		List<Expression> exprList = extractAllWithinEntityExpr(simulProb);
		for(Expression expr : exprList) {
			expr.complete = simulProb.getComplete(expr);
		}
		return exprList;
	}

}
