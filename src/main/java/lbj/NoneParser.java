//package lbj;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
//import lbjgen.CompleteClassifier;
//import parser.DocReader;
//import structure.Expression;
//import structure.Operation;
//import structure.SimulProb;
//import utils.Params;
//
//public class NoneParser extends ExpressionParser {
//	
//	CompleteClassifier completeClassifier;
//	CompleteParser completeParser;
//	
//	public NoneParser() {
//		super();
//		completeClassifier = new CompleteClassifier();
//		completeParser = new CompleteParser();
//	}
//	
//	public NoneParser(double start, double end) {
//		this(start, end, 3);
//	}
//	
//	public NoneParser(double start, double end, int topK) {
//		DocReader dr = new DocReader();
//		List<SimulProb> simulProbList = null;
//		System.out.println("Reading data");
//		try {
//			simulProbList = dr.readSimulProbFromBratDir(
//					Params.annotationDir, start, end);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		fm = new ExpressionFeatureManager();
//		completeClassifier = new CompleteClassifier();
//		completeParser = new CompleteParser();
//		System.out.println("Done");
//		exprList = new ArrayList<Expression>();
//		index = 0;
//		System.out.println("Get Problems from simul probs");
//		int count = 0;
//		for (SimulProb simulProb : simulProbList) {
//			List<Expression> tmpExprList = null;
//			try {
//				tmpExprList = extractNoneExprFromSimulProb(
//						simulProb, topK);
//				for (Expression expr : tmpExprList) {
//					fm.getAllNoneFeatures(expr, simulProb);
//				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				System.out.println("Error in node parser");
//			}
//			System.out.println(new Date() + " : " + (++count));
////			for(Expression ex : tmpExprList) {
////				System.out.println(ex.operation + " : " + ex.getYieldInString());
////			}
//			exprList.addAll(tmpExprList);
//		}
//		System.out.println("Done");
//	}
//
//	public List<Expression> extractNoneExprFromSimulProb(
//			SimulProb simulProb, int topK) throws Exception {
//		List<Expression> withinEntityPairList = new ArrayList<Expression>();
//		
////		System.out.println("Within Entity Pair");
////		print(extractAllWithinEntityPairExpr(simulProb));
//		
//		for(Expression ex :	extractAllWithinEntityPairExpr(simulProb)) {
//			// Populate complete from gold
//			populateComplete(ex, simulProb);
//			if(ex.complete == Operation.COMPLETE) {
//				withinEntityPairList.add(ex);
//			} else {
//				boolean allow = false;
//				for(Expression candidateParent : 
//					extractAllWithinEntityPairExpr(simulProb)) {
//					if(candidateParent.complete == Operation.COMPLETE) {
//						for(Expression subEx : candidateParent
//								.getAllSubExpressions()) {
//							if(subEx.equalsBasedOnLeaves(ex)) {
//								allow = true;
//								break;
//							}
//						}
//					}
//					if(allow) break;
//				}
//				if(allow) withinEntityPairList.add(ex);
//			}
//		}
//		
////		System.out.println("Within Entity Pair with complete ");
////		print(withinEntityPairList);
//		
//		List<Expression> topCompleteList = new ArrayList<Expression>();
//		for(Expression ex : extractAllWithinEntityExpr(simulProb)) {
//			populateComplete(ex, simulProb);
//			if(ex.complete == Operation.COMPLETE) {
//				topCompleteList.add(ex);
//			}
//		}
//		
////		System.out.println("Top Complete");
////		print(topCompleteList);
//		
//		List<Expression> acrossEntityList = 
//				extractAllAcrossEntityPairExpr(topCompleteList);
//		List<Expression> exprList = new ArrayList<Expression>();
//		
////		System.out.println("Across");
////		print(acrossEntityList);
//		
//		exprList.addAll(withinEntityPairList);
//		exprList.addAll(acrossEntityList);
//		
//		for(Expression expr : exprList) {
//			Operation op;
//			for(Expression goldExpr : simulProb.equations) {
//				op = goldExpr.getOperation(expr);
//				if(op != Operation.NONE) {
//					expr.operation = op;
//					break;
//				}
//			}
//			if(expr.operation == null) {
//				expr.operation = Operation.NONE;
//			}
//		}
//		return exprList;
//	}
//	
//	
//	public void populateComplete(Expression ex, SimulProb simulProb) {
//		if(simulProb.equations.size() > 0) { 
//			Operation op;
//			for(Expression goldExpr : simulProb.equations) {
//				op = goldExpr.getComplete(ex);
//				if(op != Operation.NONE) {
//					ex.complete = op;
//					break;
//				}
//			}
//			if(ex.complete == null) {
//				ex.complete = Operation.NONE;
//			}
//		} else {
//			completeParser.fm.getAllCompleteFeatures(ex, simulProb);
//			if(completeClassifier.discreteValue(ex).equals("COMPLETE")) {
//				ex.complete = Operation.COMPLETE;
//			} else {
//				ex.complete = Operation.NONE;
//			}
//		}
//	}
//	
//	public void print(List<Expression> exprList) {
//		for(Expression ex : exprList) {
//			System.out.println("Eq : "+ex);
//		}
//	}
//
//}