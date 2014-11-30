//package experiment;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.print.attribute.standard.PresentationDirection;
//
//import curator.NewCachingCurator;
//import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
//import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
//import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
//import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
//import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
//import edu.illinois.cs.cogcomp.lbjava.classify.Score;
//import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
//import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
//import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
//import edu.illinois.cs.cogcomp.sl.core.SLModel;
//import gurobi.GRB;
//import gurobi.GRBEnv;
//import gurobi.GRBException;
//import gurobi.GRBLinExpr;
//import gurobi.GRBModel;
//import gurobi.GRBVar;
//import lbj.CompleteParser;
//import lbj.EquationParser;
//import lbj.ExpressionParser;
//import lbj.MentionParser;
//import lbj.OperationParser;
//import lbjgen.CompleteClassifier;
//import lbjgen.EquationClassifier;
//import lbjgen.OperationClassifier;
//import lbjgen.MentionClassifier;
//import structure.Clustering;
//import structure.Equation;
//import structure.Expression;
//import structure.Knowledge;
//import structure.KnowledgeBase;
//import structure.Mention;
//import structure.Operation;
//import structure.QuantState;
//import structure.SimulProb;
//import structure.Span;
//import utils.Tools;
//
//public class FullSystem {
//	
//	MentionClassifier mentionClassifier;
//	MentionParser mentionParser;
//	CompleteClassifier completeClassifier;
//	CompleteParser completeParser;
//	OperationClassifier operationClassifier;
//	OperationParser operationParser;
//	EquationClassifier equationClassifier;
//	EquationParser equationParser;
//	
//	
//	public FullSystem(String modelSP) throws Exception {
//		mentionClassifier = new MentionClassifier();
//		mentionParser = new MentionParser();
//		completeClassifier = new CompleteClassifier();
//		completeParser = new CompleteParser();
//		operationClassifier = new OperationClassifier();
//		operationParser = new OperationParser();
//		equationClassifier = new EquationClassifier();
//		equationParser = new EquationParser();
//	}
//	
//	public void detectMentionAppendSpansCreateClusters(SimulProb simulProb) 
//			throws Exception {
//		List<Mention> mentions = mentionParser.extractMentionExamples(simulProb);
//		int start = -1, end = -1;	
//		String previous = "";
//	    boolean inChunk = false;
//	    String prediction="";
//	    String nextPrediction = "";
//	    for(int i = 0; i < mentions.size(); i++) {
//	    	mentionParser.fm.getAllFeatures(mentions, i, simulProb);
//	    	mentions.get(i).label = mentionClassifier.discreteValue(mentions.get(i));
//	    }
//	    for(int i=0; i<mentions.size(); i++) {
//	    	Mention mention = mentions.get(i);
//	    	prediction = mentions.get(i).label;
//	    	if(i-1>=0) {
//	    		previous = mentions.get(i-1).label;
//	    	}
//	    	if(i+1 < mentions.size()) {
//	    		nextPrediction = mentions.get(i+1).label;
//	    	}
//	    	if (prediction.startsWith("B-") || prediction.startsWith("I-") 
//	    			&& !previous.endsWith(prediction.substring(2))){
//	    		if( !inChunk ){
//	    			inChunk = true;
//	    			start = mention.ta.getTokenCharacterOffset(i).getFirst();
//	    		}		
//	    	}
//    		if (!prediction.equals("O")
//    					&& (i+1 == mentions.size()
//    					|| nextPrediction.equals("O")
//    					|| nextPrediction.startsWith("B-")
//    					|| !nextPrediction.endsWith(prediction.substring(2)))){
//    			inChunk = false;
//    			end = mention.ta.getTokenCharacterOffset(i).getSecond();
//    			String label = prediction.substring(2);
//    			if(label.startsWith("E1")) {
//        			simulProb.spans.add(new Span("E1", new IntPair(start, end)));
//    				if(label.contains("VAR")) {
//            			simulProb.spans.add(new Span("V1", new IntPair(start, end)));
//    				}
//    			}
//    			if(label.startsWith("E2")) {
//        			simulProb.spans.add(new Span("E2", new IntPair(start, end)));
//    				if(label.contains("VAR")) {
//            			simulProb.spans.add(new Span("V2", new IntPair(start, end)));
//    				}
//    			}
//    			if(label.startsWith("E3")) {
//        			simulProb.spans.add(new Span("E3", new IntPair(start, end)));
//    			}
//    		}
//	    }
//	    System.out.println();
//	    createClusters(simulProb);
//	}
//	
//	// create clusters at time of inference, assumes spans added using 
//	// detectMentionAppendSpansCreateClusters()
//	public void createClusters(SimulProb simulProb) {
//		for(Span span : simulProb.spans) {
//			if(!simulProb.clusterMap.containsKey(span.label)) {
//				simulProb.clusterMap.put(
//						span.label, new QuantState(span.label));
//			}
//			boolean foundNumber = false;
//			for(QuantSpan qs : simulProb.quantities) {
//				if(Tools.doesIntersect(span.ip, new IntPair(qs.start, qs.end))) {
//					simulProb.clusterMap.get(span.label).addToCluster(
//							""+simulProb.getValue(qs), new IntPair(qs.start, qs.end));
//					foundNumber = true;
//					break;
//				}
//			}
//			if(!foundNumber) {
//				if(span.label.contains("E1")) {
//					simulProb.clusterMap.get(span.label).addToCluster("V1", span.ip);
//				}
//				if(span.label.contains("E2")) {
//					simulProb.clusterMap.get(span.label).addToCluster("V2", span.ip);
//				}
//			}
//		}
//	}
//	
//	public List<Expression> getAnswers(String question) throws Exception {
//		SimulProb predSimulProb = new SimulProb(-1);
//		predSimulProb.question = question;
//		// Extract world knowledge
//		KnowledgeBase.appendWorldKnowledge(predSimulProb);
//		// Detecting mentions, create clusters
//		detectMentionAppendSpansCreateClusters(predSimulProb);
//		// Extracting equations
//		return extractEquations(predSimulProb);
//	}
//	
//	public void test(List<SimulProb> simulProbList) throws Exception {
//		int total = 0, incorrect = 0;
//		for(SimulProb simulProb : simulProbList) {
//			total++;
//			List<Expression> answers = getAnswers(simulProb.question);
//			for(Expression expr : answers) {
//				System.out.println(expr);
//			}
//			for(Expression gold : simulProb.equations) {
//				boolean found = false;
//				for(Expression pred : answers) {
//					if(gold.equalsEquation(pred)) {
//						found = true;
//						break;
//					}
//				}
//				if(!found) {
//					incorrect++;
//					break;
//				}
//			}
//		}
//		System.out.println("Accuracy : " + (1 - incorrect*1.0/total));
//	}
//	
//	public List<Expression> extractEquations(SimulProb simulProb) throws Exception {
//		System.out.println(simulProb.index + " : " + simulProb.question);
//		System.out.println("Cluster Map :");
//		for(String entity : simulProb.clusterMap.keySet()) {
//			System.out.println(entity + " : " + Arrays.asList(
//					simulProb.clusterMap.get(entity).mentionLocMap.keySet()));
//		}
//		List<Expression> completeExprList = 
//				completeParser.extractCompleteExprFromSimulProb(simulProb);
//		Map<String, List<Expression>> terms = new HashMap<String, List<Expression>>();
//		for(Expression ex : completeExprList) {
//			if(!terms.containsKey(ex.entityName)) {
//				terms.put(ex.entityName, new ArrayList<Expression>());
//			}
//			terms.get(ex.entityName).add(ex);
//		}
//		System.out.println("Term Map: ");
//		for(String entity : terms.keySet()) {
//			System.out.println(entity + ":" + terms.get(entity));
//		}
//		for(String entity : terms.keySet()) {
//			terms.put(entity, getTopComplete(terms.get(entity), 2, simulProb));
//			List<Expression> list = new ArrayList<Expression>();
//			for(Expression ex : terms.get(entity)) {
//				list.add(extractBestExpressionFromTermLeaves(ex, simulProb));
//			}
//			terms.put(entity, list);
//		}
//		System.out.println("Term Map: ");
//		for(String entity : terms.keySet()) {
//			System.out.println(entity + ":" + terms.get(entity));
//		}
//		List<Equation> equationList = 
//				equationParser.extractCandidateEquationsFromTermMap(terms);
//		List<Equation> answerList = getTopEquations(equationList, 2, simulProb);
//		List<Expression> answers = new ArrayList<Expression>();
//		for(Equation eq : answerList) {
//			if(eq.terms.size() == 2) {
//				answers.add(new Expression(null, null, eq.operations.get(0), 
//						new HashSet<Expression>(eq.terms)));
//			} else {
//				Expression firstJoin = new Expression(null, null, eq.operations.get(0), 
//						new HashSet<Expression>(
//								Arrays.asList(eq.terms.get(0), eq.terms.get(1))));
//				answers.add(new Expression(null, null, eq.operations.get(1), 
//						new HashSet<Expression>(
//								Arrays.asList(firstJoin, eq.terms.get(2)))));
//			}
//		}
//		return answers;
//	}
//	
//	public List<Expression> getTopComplete(
//			List<Expression> exprList, int k, SimulProb simulProb) {
//		for(Expression expr : exprList) {
//			completeParser.fm.getAllCompleteFeatures(expr, simulProb);
//			expr.complete = (completeClassifier.discreteValue(expr).equals("COMPLETE"))? 
//					Operation.COMPLETE : Operation.NONE;
//			ScoreSet scoreSet = completeClassifier.scores(expr);
//			for(Score score : scoreSet.toArray()) {
//				if(score.value.equals("COMPLETE")) {
//					expr.completeScore = score.score;
//				}
//			}
//		}
//		Expression temp;
//		for(int i = 0; i < exprList.size()-1; ++i) {
//			for(int j = 0; j < exprList.size()-1 -i; j++) {
//				if(exprList.get(j).completeScore < exprList.get(j+1).completeScore) {
//					temp = exprList.get(j);
//					exprList.set(j, exprList.get(j+1));
//					exprList.set(j+1, temp);
//				}
//			}
//		}
//		List<Expression> newList = new ArrayList<Expression>();
//		for(int i = 0; i < Math.min(k, exprList.size()); i++) {
//			if(exprList.get(i).complete != Operation.COMPLETE) {
//				break;
//			}
//			newList.add(exprList.get(i));
//		}
//		return newList;
//	}
//	
//	public List<Equation> getTopEquations(
//			List<Equation> equationList, int k, SimulProb simulProb) {
//		for(Equation equation : equationList) {
//			equationParser.fm.getAllFeatures(equation, simulProb);
//			equation.label = equationClassifier.discreteValue(equation);
//			ScoreSet scoreSet = equationClassifier.scores(equation);
//			for(Score score : scoreSet.toArray()) {
//				if(score.value.equals(equation.label)) {
//					equation.equationScore = score.score;
//				}
//			}
//		}
//		Equation temp;
//		for(int i = 0; i < equationList.size()-1; ++i) {
//			for(int j = 0; j < equationList.size()-1 -i; j++) {
//				if(equationList.get(j).equationScore < 
//						equationList.get(j+1).equationScore) {
//					temp = equationList.get(j);
//					equationList.set(j, equationList.get(j+1));
//					equationList.set(j+1, temp);
//				}
//			}
//		}
//		List<Equation> newList = new ArrayList<Equation>();
//		for(int i = 0; i < Math.min(k, equationList.size()); i++) {
//			newList.add(equationList.get(i));
//		}
//		return newList;
//	}
//	
//	public Expression extractBestExpressionFromTermLeaves(
//			Expression term, SimulProb simulProb) {
//		List<Expression> exprList = term.getYield();
//		int len = exprList.size();
//		while(len>1) {
//			double best = -10000.0;
//			Operation bestLabel = Operation.NONE;
//			int bestI = -1, bestJ = -1;
//			for(int i = 0; i < len; i++) {
//				for(int j = i + 1;  j < len; j++) {
//					Set<Expression> ops = new HashSet<Expression>();
//					ops.add(exprList.get(i));
//					ops.add(exprList.get(j));
//					Expression ex = new Expression(
//							null, exprList.get(i).entityName, null, ops);
//					operationParser.fm.getAllOperationFeatures(ex, simulProb);
//					for(Score score : operationClassifier.scores(ex).toArray()) {
//						if(score.score > best && !score.value.equals("NONE")) {
//							best = score.score;
//							if(score.value.equals("ADD")) bestLabel = Operation.ADD;
//							if(score.value.equals("SUB")) bestLabel = Operation.SUB;
//							if(score.value.equals("MUL")) bestLabel = Operation.MUL;
//							if(score.value.equals("DIV")) bestLabel = Operation.DIV;
//							bestI = i;
//							bestJ = j;
//						}
//					}
//				}
//			}
//			Set<Expression> ops = new HashSet<Expression>();
//			ops.add(exprList.get(bestI));
//			ops.add(exprList.get(bestJ));
//			
//			Expression ex = new Expression(null, exprList.get(bestI).entityName, bestLabel, ops);
//			exprList.add(ex);
//			exprList.remove(bestI);
//			exprList.remove(bestJ-1);
//			len = exprList.size();
//		}
//		return exprList.get(0);
//	}
//	
//}
