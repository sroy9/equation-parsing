//package experiment;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import lbj.CompleteParser;
//import lbj.EquationParser;
//import lbj.ExpressionFeatureManager;
//import lbj.MentionParser;
//import lbj.OperationParser;
//import lbjgen.CompleteClassifier;
//import lbjgen.EquationClassifier;
//import lbjgen.OperationClassifier;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import parser.DocReader;
//import structure.Equation;
//import structure.Expression;
//import structure.KnowledgeBase;
//import structure.Mention;
//import structure.Operation;
//import structure.SimulProb;
//import structure.Span;
//import utils.Params;
//import curator.NewCachingCurator;
//import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
//import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
//import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
//import edu.illinois.cs.cogcomp.sl.applications.tutorial.POSTag;
//import edu.illinois.cs.cogcomp.sl.core.SLModel;
//import edu.illinois.cs.cogcomp.sl.core.SLProblem;
///**
// * 
// * @author subhroroy
// *
// */
//public class Experiment {
//	
//	public static void main(String[] args) throws Exception {
//		InteractiveShell<Experiment> tester = new InteractiveShell<Experiment>(
//				Experiment.class);
//		if (args.length == 0)
//			tester.showDocumentation();
//		else 
//			tester.runCommand(args);
//	}
//
//	@CommandDescription(description="Check data")
//	public static void testDataQuality() throws Exception {
//		DocReader dr = new DocReader();
//		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(
//				Params.annotationDir);
//		for(SimulProb simulProb : simulProbList) {
//			System.out.println(simulProb.index+" : "+simulProb.question);
//			System.out.println("Quantities :");
//			for(QuantSpan qs : simulProb.quantities) {
//				System.out.println(simulProb.question.substring(
//						qs.start, qs.end)+" : "+qs);
//			}
//			System.out.println("Spans :");
//			for(Span span : simulProb.spans) {
//				System.out.println(span.label+" : "+simulProb.question.substring(
//						span.ip.getFirst(), span.ip.getSecond())+ " : "+span.ip);
//			}
//			System.out.println("Cluster Map :");
//			for(String entity : simulProb.clusterMap.keySet()) {
//				System.out.println(entity + " : " + Arrays.asList(
//						simulProb.clusterMap.get(entity).mentionLocMap.keySet()));
//			}
//			System.out.println("Equations :");
//			for(Expression eq : simulProb.equations) {
//				System.out.println(eq.toString());
//			}
//			System.out.println("Node Linking :");
//			simulProb.nodeLinkingForEquations();
//		}
//	}
//	
//	@CommandDescription(description="Tests mention detection")
//	public static void testMentionDetection(String startString, String endString) 
//			throws Exception {
//		double start = Double.parseDouble(startString);
//		double end = Double.parseDouble(endString);
//		MentionParser mentionParser = new MentionParser();
//		DocReader dr = new DocReader();
//		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(
//				Params.annotationDir, start, end);
//		FullSystem fs = new FullSystem("");
//		for(SimulProb simulProb : simulProbList) {
//			SimulProb predSimulProb = new SimulProb(-1);
//			System.out.println("*************************************");
//			System.out.println(simulProb.index+" : "+simulProb.question);
//			predSimulProb.question = simulProb.question;
//			predSimulProb.quantities = simulProb.quantities;
//			
//			System.out.println("\nGold");
//			List<Mention> mentionList = mentionParser.extractMentionExamples(simulProb);
//			for(Mention mention : mentionList) {
//				if(mention.label.equals("O")) {
//					System.out.print(mention.ta.getToken(mention.index)+" ");
//				} else {
//					System.out.print("["+mention.label + " : " + mention.ta.getToken(mention.index)+"] ");
//				}
//			}
//			System.out.println();
//			
//			fs.detectMentionAppendSpansCreateClusters(predSimulProb);
//			
//			System.out.println("\nPredict");
//			mentionList = mentionParser.extractMentionExamples(predSimulProb);
//			for(Mention mention : mentionList) {
//				if(mention.label.equals("O")) {
//					System.out.print(mention.ta.getToken(mention.index)+" ");
//				} else {
//					System.out.print("["+mention.label + " : " + mention.ta.getToken(mention.index)+"]");
//				}
//			}
//			System.out.println();
//			
//			System.out.println("\nGold Cluster Map :");
//			for(String entity : simulProb.clusterMap.keySet()) {
//				System.out.println(entity + " : " + Arrays.asList(
//						simulProb.clusterMap.get(entity).mentionLocMap.keySet()));
//			}
//			System.out.println("\nPredicted Cluster Map :");
//			for(String entity : predSimulProb.clusterMap.keySet()) {
//				System.out.println(entity + " : " + Arrays.asList(
//						predSimulProb.clusterMap.get(entity).mentionLocMap.keySet()));
//			}
//		}
//	}
//	
//	@CommandDescription(description="Tests equation extraction")
//	public static void testEquationFormation(String startString, String endString) 
//			throws Exception {
//		double start = Double.parseDouble(startString);
//		double end = Double.parseDouble(endString);
//		DocReader dr = new DocReader();
//		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(
//				Params.annotationDir, start, end);
//		FullSystem fs = new FullSystem("");
//		int total = 0, correct = 0;
//		for(SimulProb simulProb : simulProbList) {
//			total += simulProb.equations.size();
//			SimulProb predSimulProb = new SimulProb(-1);
//			predSimulProb.question = simulProb.question;
//			predSimulProb.spans = simulProb.spans;
//			predSimulProb.clusterMap = simulProb.clusterMap;
//			List<Expression> exprList = fs.extractEquations(predSimulProb);
//			System.out.println(predSimulProb.question);
//			for(int i = 0; i < Math.min(2, exprList.size()); i++) {
//				Expression expr = exprList.get(i);
//				System.out.println(expr);
//				for(Expression gold : simulProb.equations) { 
//					if(gold.equalsEquation(expr)) {
//						correct++;
//						break;
//					}
//				}
//			}
//		}
//		System.out.println("Correct / Total : "+correct+" / "+total+" = "+ 
//		(correct*1.0/total));
//	}
//	
//	@CommandDescription(description="Runs entire pipeline")
//	public static void testFullSystem(String startString, String endString) 
//			throws Exception {
//		double start = Double.parseDouble(startString);
//		double end = Double.parseDouble(endString);
//		DocReader dr = new DocReader();
//		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(
//				Params.annotationDir, start, end);
//		FullSystem fullSystem = new FullSystem(Params.spModelFile);
//		fullSystem.test(simulProbList);
//	}
//	
//	@CommandDescription(description="Runs entire pipeline")
//	public static void getCompleteErrors(String startString, String endString) 
//			throws Exception {
//		double start = Double.parseDouble(startString);
//		double end = Double.parseDouble(endString);
//		DocReader dr = new DocReader();
//		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(
//				Params.annotationDir, start, end);
//		CompleteClassifier completeClassifier = new CompleteClassifier();
//		CompleteParser completeParser = new CompleteParser();
//		for(SimulProb simulProb : simulProbList) {
//			System.out.println(simulProb.index+" : "+simulProb.question);
//			System.out.println("Quantities :");
//			for(QuantSpan qs : simulProb.quantities) {
//				System.out.println(simulProb.question.substring(
//						qs.start, qs.end)+" : "+qs);
//			}
//			System.out.println("Spans :");
//			for(Span span : simulProb.spans) {
//				System.out.println(span.label+" : "+simulProb.question.substring(
//						span.ip.getFirst(), span.ip.getSecond())+ " : "+span.ip);
//			}
//			System.out.println("Cluster Map :");
//			for(String entity : simulProb.clusterMap.keySet()) {
//				System.out.println(entity + " : " + Arrays.asList(
//						simulProb.clusterMap.get(entity).mentionLocMap.keySet()));
//			}
//			System.out.println("Equations :");
//			for(Expression eq : simulProb.equations) {
//				System.out.println(eq.toString());
//			}
//			List<Expression> exprList = completeParser
//					.extractCompleteExprFromSimulProb(simulProb);
//			Operation prediction;
//			for(Expression expr : exprList) {
//				completeParser.fm.getAllCompleteFeatures(expr, simulProb);
//				if(completeClassifier.discreteValue(expr).equals("COMPLETE")) {
//					prediction = Operation.COMPLETE;
//				} else {
//					prediction = Operation.NONE;
//				}
//				if(prediction != expr.complete) {
//					System.out.println("Error : " + prediction + " : " + expr);
//				}
//			}
//			
//		}
//	}
//	
//	@CommandDescription(description="Runs entire pipeline")
//	public static void getOperationErrors(String startString, String endString) 
//			throws Exception {
//		double start = Double.parseDouble(startString);
//		double end = Double.parseDouble(endString);
//		DocReader dr = new DocReader();
//		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(
//				Params.annotationDir, start, end);
//		OperationClassifier operationClassifier = new OperationClassifier();
//		OperationParser operationParser = new OperationParser();
//		for(SimulProb simulProb : simulProbList) {
//			System.out.println(simulProb.index+" : "+simulProb.question);
//			System.out.println("Quantities :");
//			for(QuantSpan qs : simulProb.quantities) {
//				System.out.println(simulProb.question.substring(
//						qs.start, qs.end)+" : "+qs);
//			}
//			System.out.println("Spans :");
//			for(Span span : simulProb.spans) {
//				System.out.println(span.label+" : "+simulProb.question.substring(
//						span.ip.getFirst(), span.ip.getSecond())+ " : "+span.ip);
//			}
//			System.out.println("Cluster Map :");
//			for(String entity : simulProb.clusterMap.keySet()) {
//				System.out.println(entity + " : " + Arrays.asList(
//						simulProb.clusterMap.get(entity).mentionLocMap.keySet()));
//			}
//			System.out.println("Equations :");
//			for(Expression eq : simulProb.equations) {
//				System.out.println(eq.toString());
//			}
//			List<Expression> exprList = operationParser
//					.extractOperationExprFromSimulProb(simulProb);
//			for(Expression expr : exprList) {
//				operationParser.fm.getAllCompleteFeatures(expr, simulProb);
//				String label = operationClassifier.discreteValue(expr);
//				Operation op;
//				if(label.equals("ADD")) {
//					op = Operation.ADD;
//				} else if(label.equals("SUB")) {
//					op = Operation.SUB;
//				} else if(label.equals("MUL")) {
//					op = Operation.MUL;
//				} else if(label.equals("DIV")) {
//					op = Operation.DIV;
//				} else if(label.equals("EQ")) {
//					op = Operation.EQ;
//				} else {
//					// Should never happen
//					op = Operation.NONE;
//				}
//				if(op != expr.operation) {
//					System.out.println("Error : " + op + " : " + expr);
//				}
//			}
//			
//		}
//	}
//	
//	@CommandDescription(description="Runs entire pipeline")
//	public static void getEquationErrors(String startString, String endString) 
//			throws Exception {
//		double start = Double.parseDouble(startString);
//		double end = Double.parseDouble(endString);
//		DocReader dr = new DocReader();
//		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(
//				Params.annotationDir, start, end);
//		EquationClassifier equationClassifier = new EquationClassifier();
//		EquationParser equationParser = new EquationParser();
//		for(SimulProb simulProb : simulProbList) {
//			System.out.println(simulProb.index+" : "+simulProb.question);
//			System.out.println("Quantities :");
//			for(QuantSpan qs : simulProb.quantities) {
//				System.out.println(simulProb.question.substring(
//						qs.start, qs.end)+" : "+qs);
//			}
//			System.out.println("Spans :");
//			for(Span span : simulProb.spans) {
//				System.out.println(span.label+" : "+simulProb.question.substring(
//						span.ip.getFirst(), span.ip.getSecond())+ " : "+span.ip);
//			}
//			System.out.println("Cluster Map :");
//			for(String entity : simulProb.clusterMap.keySet()) {
//				System.out.println(entity + " : " + Arrays.asList(
//						simulProb.clusterMap.get(entity).mentionLocMap.keySet()));
//			}
//			System.out.println("Equations :");
//			for(Expression eq : simulProb.equations) {
//				System.out.println(eq.toString());
//			}
//			List<Equation> exprList = equationParser.extractEquations(simulProb);
//			for(Equation expr : exprList) {
//				equationParser.fm.getAllFeatures(expr, simulProb);
//				String label = equationClassifier.discreteValue(expr);
//				if(!label.equals(expr.label)) {
//					System.out.println("Error : " + label + " : " + expr);
//				}
//			}
//		}
//	}
//	
//	
//}