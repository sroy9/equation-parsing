package lbj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parser.DocReader;
import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import structure.Expression;
import structure.SimulProb;
import utils.FeatureExtraction;
import utils.Tools;

public class ExpressionFeatureManager {
	
	// Complete Classifier : 86.4-87 % F1
	public void getAllCompleteFeatures(Expression expr, SimulProb simulProb) {
		// Add feature functions here
		expr.features = new ArrayList<String>();
		try {
			expr.features.addAll(getCompleteTokenFeatures(expr, simulProb));
			expr.features.addAll(getCompleteClusterFeatures(expr, simulProb));
			expr.features.addAll(getCompleteSentenceFeatures(expr, simulProb));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<String> getCompleteTokenFeatures(Expression expr, SimulProb simulProb) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix;
		if(expr.operands == null || expr.operands.size() == 0) {
			prefix = "SINGLE";
			if(expr.varName.startsWith("V")) {
				prefix+="_VAR";
			}
			for(IntPair ip : expr.getAllMentionLocs(simulProb)) {
				for(String feature : FeatureExtraction.getFormPP(
						simulProb.question, ip.getFirst(), 2)) {
					features.add(prefix+"_"+feature);
				}
				for(String feature : FeatureExtraction.getMixed(
						simulProb.question, ip.getFirst(), 2)) {
					features.add(prefix+"_"+feature);
				}
				for(String feature : FeatureExtraction.getPOSWindowPP(
						simulProb.question, ip.getFirst(), 2)) {
					features.add(prefix+"_"+feature);
				}
			}
			for(String feature : FeatureExtraction.getUnigrams(simulProb.question)) {
				features.add(prefix+"_UNIGRAM_"+feature);
			}
			for(String feature : FeatureExtraction.getBigrams(simulProb.question)) {
				features.add(prefix+"_BIGRAM_"+feature);
			}
		} else {
			prefix = "JOIN_"+expr.getYield().size();
			assert expr.operands.size() == 2;
			for(Expression ex : expr.operands) {
				if(ex.varName != null && ex.varName.startsWith("V")) {
					prefix+="_VAR";
				}
			}
			for(IntPair ip : expr.getAllMentionLocs(simulProb)) {
				for(String feature : FeatureExtraction.getFormPP(
						simulProb.question, ip.getFirst(), 2)) {
					features.add(prefix+"_"+feature);
				}
				for(String feature : FeatureExtraction.getMixed(
						simulProb.question, ip.getFirst(), 2)) {
					features.add(prefix+"_"+feature);
				}
				for(String feature : FeatureExtraction.getPOSWindowPP(
						simulProb.question, ip.getFirst(), 2)) {
					features.add(prefix+"_"+feature);
				}
			}
			for(String feature : FeatureExtraction.getUnigrams(simulProb.question)) {
				features.add(prefix+"_UNIGRAM_"+feature);
			}
			for(String feature : FeatureExtraction.getBigrams(simulProb.question)) {
				features.add(prefix+"_BIGRAM_"+feature);
			}
		}
		return features;
	}
	
	public List<String> getCompleteClusterFeatures(
			Expression expr, SimulProb simulProb) throws Exception {
		List<String> features = new ArrayList<String>();
		Set<IntPair> exprIps = expr.getAllMentionLocs(simulProb);
		Set<IntPair> entityIps = new HashSet<IntPair>();
		for(String varName : simulProb.clusterMap.get(expr.entityName)
				.mentionLocMap.keySet()) {
			entityIps.addAll(simulProb.clusterMap.get(expr.entityName)
				.mentionLocMap.get(varName));
		}
		for(IntPair ip : exprIps) {
			entityIps.remove(ip);
		}
		for(IntPair ip : entityIps) {
			String prefix = "";
			if(expr.operands == null || expr.operands.size() == 0) {
				prefix = "SINGLE";
				if(expr.varName.startsWith("V")) {
					prefix+="_VAR";
				}
			} else {
				prefix = "JOIN";
			}
			if(entityIps.size() == 1) {
				prefix += "_LEFT_SINGLE";
			}
			for(String varName : simulProb.clusterMap.get(expr.entityName)
					.mentionLocMap.keySet()) {
				if(simulProb.clusterMap.get(expr.entityName).mentionLocMap.
						get(varName).contains(ip) && varName.startsWith("V")) {
					prefix += "_LEFT_VAR";
					break;
				}
			}
			for(String feature : FeatureExtraction.getFormPP(
					simulProb.question, ip.getFirst(), 2)) {
				features.add(prefix+"_"+feature);
			}
			for(String feature : FeatureExtraction.getMixed(
					simulProb.question, ip.getFirst(), 2)) {
				features.add(prefix+"_"+feature);
			}
			for(String feature : FeatureExtraction.getPOSWindowPP(
					simulProb.question, ip.getFirst(), 2)) {
				features.add(prefix+"_"+feature);
			}
		}
		features.add(entityIps.size()+" items left");
		return features;
	}
	
	public List<String> getCompleteSentenceFeatures(
			Expression expr, SimulProb simulProb) throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = new TextAnnotation("", "", simulProb.question);
		if(expr.operands == null || expr.operands.size() == 0) {
			String prefix = "Single_Sentence";
			Set<IntPair> ipSet = expr.getAllMentionLocs(simulProb);
			for(IntPair ip : ipSet) {
				Sentence sentence = ta.getSentenceFromToken(
						ta.getTokenIdFromCharacterOffset(ip.getFirst()));
				for(String feature : FeatureExtraction.getBigrams(
						sentence.getText())) {
					features.add(prefix+"_"+feature);	
				}
				for(String feature : FeatureExtraction.getUnigrams(
						sentence.getText())) {
					features.add(prefix+"_"+feature);	
				}
			}
		} 
		return features;
	}
	
	// Operation Classifier : 
	// We assume single value expressions are pruned out before
	public void getAllOperationFeatures(Expression expr, SimulProb simulProb) {
		// Add feature functions here
		expr.features = new ArrayList<String>();
		try {
			expr.features.addAll(getOperationTokenFeatures(expr, simulProb));
//			expr.features.addAll(getOperationClusterFeatures(expr, simulProb));
			expr.features.addAll(getCompleteTokenFeatures(expr, simulProb));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<String> getOperationTokenFeatures(Expression expr, SimulProb simulProb)
			throws Exception {
		List<String> features = new ArrayList<String>();
		assert expr.operands.size() == 2;
		List<Expression> operandsList = new ArrayList<Expression>();
		for(Expression ex : expr.operands) {
			operandsList.add(ex); 
		}
		TextAnnotation ta = new TextAnnotation("", "", simulProb.question);
		String prefix = "";
		prefix += Math.min(operandsList.get(0).getYield().size(),
				operandsList.get(1).getYield().size())+"_"+
		Math.max(operandsList.get(0).getYield().size(),
				operandsList.get(1).getYield().size());
		for(Expression ex : expr.operands) {
			if(ex.varName != null && ex.varName.startsWith("V")) {
				prefix+="_VAR";
				break;
			}
		}
		for(String feature : FeatureExtraction.getUnigrams(simulProb.question)) {
			features.add(prefix+ "_Unigram_" +feature);		
		}
		for(String feature : FeatureExtraction.getBigrams(simulProb.question)) {
			features.add(prefix+ "_Bigram_" +feature);		
		}
		Set<IntPair> ipSet1 = operandsList.get(0).getAllMentionLocs(simulProb);
		Set<IntPair> ipSet2 = operandsList.get(1).getAllMentionLocs(simulProb);
		for(IntPair ip1 : ipSet1) {
			IntPair ip2 = FeatureExtraction.getClosestMention(ipSet2, ip1);
			int start = Math.min(ip1.getFirst(), ip2.getFirst());
			int end = Math.max(ip1.getSecond(), ip2.getSecond());
			int startTokenId = ta.getTokenIdFromCharacterOffset(ip1.getFirst());
			int endTokenId = ta.getTokenIdFromCharacterOffset(ip2.getFirst());
			int startSentenceId = ta.getSentenceFromToken(
					ta.getTokenIdFromCharacterOffset(
							ip1.getFirst())).getSentenceId();
			int endSentenceId = ta.getSentenceFromToken(
					ta.getTokenIdFromCharacterOffset(
							ip2.getFirst())).getSentenceId();
			if(startSentenceId != endSentenceId) {
//				prefix += "_Diff_Sentence";
				for(String feature : FeatureExtraction.getUnigrams(
						ta.getSentence(startSentenceId).getText())) {
					features.add(prefix+ "_" +feature);	
				}
				for(String feature : FeatureExtraction.getBigrams(
						ta.getSentence(startSentenceId).getText())) {
					features.add(prefix+ "_" +feature);	
				}
				for(String feature : FeatureExtraction.getUnigrams(
						ta.getSentence(endSentenceId).getText())) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getBigrams(
						ta.getSentence(endSentenceId).getText())) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getFormPP(
						simulProb.question, start, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getMixed(
						simulProb.question, start, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getPOSWindowPP(
						simulProb.question, start, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getFormPP(
						simulProb.question, end, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getMixed(
						simulProb.question, end, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getPOSWindowPP(
						simulProb.question, end, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				
			} else {
//				prefix += "_Same_Sentence";
				for(String feature : FeatureExtraction.getUnigrams(
						ta.getSentence(startSentenceId).getText())) {
					features.add(prefix+"_"+feature);	
				}
				for(String feature : FeatureExtraction.getBigrams(
						ta.getSentence(startSentenceId).getText())) {
					features.add(prefix+"_"+feature);	
				}
				for(String feature : FeatureExtraction.getFormPP(
						simulProb.question, start, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getMixed(
						simulProb.question, start, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getPOSWindowPP(
						simulProb.question, start, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getFormPP(
						simulProb.question, end, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getMixed(
						simulProb.question, end, 2)) {
					features.add(prefix+ "_" +feature);		
				}
				for(String feature : FeatureExtraction.getPOSWindowPP(
						simulProb.question, end, 2)) {
					features.add(prefix+ "_" +feature);		
				}
			}
		}
		return features;
	}
	
	public List<String> getOperationClusterFeatures(
			Expression expr, SimulProb simulProb) throws Exception {
		List<String> features = new ArrayList<String>();
		Set<IntPair> exprIps = expr.getAllMentionLocs(simulProb);
		Set<IntPair> entityIps = new HashSet<IntPair>();
		for(String varName : simulProb.clusterMap.get(expr.entityName)
				.mentionLocMap.keySet()) {
			entityIps.addAll(simulProb.clusterMap.get(expr.entityName)
				.mentionLocMap.get(varName));
		}
		for(IntPair ip : exprIps) {
			entityIps.remove(ip);
		}
		String prefix = "";
		for(IntPair ip : entityIps) {
			for(String varName : simulProb.clusterMap.get(expr.entityName)
					.mentionLocMap.keySet()) {
				if(simulProb.clusterMap.get(expr.entityName).mentionLocMap.
						get(varName).contains(ip) && varName.startsWith("V")) {
					prefix += "_LEFT_VAR";
					break;
				}
			}
		}
		for(IntPair ip : entityIps) {
			for(String varName : simulProb.clusterMap.get(expr.entityName)
					.mentionLocMap.keySet()) {
				if(simulProb.clusterMap.get(expr.entityName).mentionLocMap.
						get(varName).contains(ip) && varName.startsWith("0.01")) {
					prefix += "_LEFT_0.01";
					break;
				}
			}
		}
		prefix+="_"+expr.getYield().size();
		for(String feature : FeatureExtraction.getUnigrams(simulProb.question)) {
			features.add(prefix+ "_Unigram_" +feature);		
		}
		for(String feature : FeatureExtraction.getBigrams(simulProb.question)) {
			features.add(prefix+ "_Bigram_" +feature);		
		}
		return features;
	}
//	// None Classifier : In order to prune negative examples for Operation classifier
//	// 94.147 % precision on NONE
//	public void getAllNoneFeatures(Expression expr, SimulProb simulProb) {
//		// Add feature functions here
//		expr.features = new ArrayList<String>();
//		try {
//			expr.features.addAll(getNoneTokenFeatures(expr, simulProb));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public List<String> getNoneTokenFeatures(Expression expr, SimulProb simulProb) 
//			throws Exception {
//		List<String> features = new ArrayList<String>();
//		assert expr.operands.size() == 2;
//		String prefix = "";
//		List<Expression> operandsList = new ArrayList<Expression>();
//		for(Expression ex : expr.operands) {
//			operandsList.add(ex);
//			if(ex.varName != null && ex.varName.startsWith("V")) {
//				prefix += "_VAR";
//			}
//		}
//		prefix += "_" + Math.min(operandsList.get(0).getYield().size(), 
//				operandsList.get(1).getYield().size()) + "_" + 
//				Math.max(operandsList.get(0).getYield().size(), 
//						operandsList.get(1).getYield().size());
//		String prefix1 = operandsList.get(0).entityName.substring(0, 1) + "_" +
//				operandsList.get(1).entityName.substring(0, 1) + 
//				(operandsList.get(0).entityName.equals(operandsList.get(1).entityName) ? 
//						"_Same_Entity" : "_Diff_Entity");
//		String prefix2 = operandsList.get(1).entityName.substring(0, 1) + "_" +
//				operandsList.get(0).entityName.substring(0, 1) + 
//				(operandsList.get(0).entityName.equals(operandsList.get(1).entityName) ? 
//						"_Same_Entity" : "_Diff_Entity");
//		for(IntPair ip1 : operandsList.get(0).getAllMentionLocs(simulProb)) {
//			for(IntPair ip2 : operandsList.get(1).getAllMentionLocs(simulProb)) {
//				for(String feature1 : FeatureExtraction.getUnigramsInWindow(
//						simulProb.question, ip1.getFirst(), 2)) {
//					for(String feature2 : FeatureExtraction.getUnigramsInWindow(
//						simulProb.question, ip2.getFirst(), 1)) {
//						features.add(prefix+"_"+feature1+"_"+feature2);
//						features.add(prefix+"_"+feature2+"_"+feature1);
//						features.add(prefix1+"_"+feature1+"_"+feature2);
//						features.add(prefix1+"_"+feature2+"_"+feature1);
//						features.add(prefix2+"_"+feature1+"_"+feature2);
//						features.add(prefix2+"_"+feature2+"_"+feature1);
//					}
//				}
//				for(String feature1 : 
//					FeatureExtraction.getPOSInWindowWithRelativePosition(
//						simulProb.question, ip1.getFirst(), 1)) {
//					for(String feature2 : 
//						FeatureExtraction.getPOSInWindowWithRelativePosition(
//						simulProb.question, ip2.getFirst(), 1)) {
//						features.add(prefix+"_"+feature1+"_"+feature2);
//						features.add(prefix+"_"+feature2+"_"+feature1);
//						features.add(prefix1+"_"+feature1+"_"+feature2);
//						features.add(prefix1+"_"+feature2+"_"+feature1);
//						features.add(prefix2+"_"+feature1+"_"+feature2);
//						features.add(prefix2+"_"+feature2+"_"+feature1);
//					}
//				}
//			}
//		}
//		for(String feature : FeatureExtraction.getUnigrams(simulProb.question)) {
//			features.add(prefix+"_UNIGRAM_"+feature);
//		}
//		for(String feature : FeatureExtraction.getBigrams(simulProb.question)) {
//			features.add(prefix+"_BIGRAM_"+feature);
//		}
//		for(String feature : FeatureExtraction.getUnigrams(simulProb.question)) {
//			features.add(prefix1+"_UNIGRAM_"+feature);
//		}
//		for(String feature : FeatureExtraction.getBigrams(simulProb.question)) {
//			features.add(prefix1+"_BIGRAM_"+feature);
//		}
//		for(String feature : FeatureExtraction.getUnigrams(simulProb.question)) {
//			features.add(prefix2+"_UNIGRAM_"+feature);
//		}
//		for(String feature : FeatureExtraction.getBigrams(simulProb.question)) {
//			features.add(prefix2+"_BIGRAM_"+feature);
//		}
//		return features;
//	}
	
	

}
