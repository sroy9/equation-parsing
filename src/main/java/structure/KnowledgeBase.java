package structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import utils.Tools;

public class KnowledgeBase {
	
	public static List<Knowledge> knowledgeList;
	public static Map<String, List<String>> mathNodeMap;
	public static Set<String> mathNodeSet;
	public static Set<String> mathIndicatorSet;
	
	static {
		// Animal knowledge
		knowledgeList = new ArrayList<Knowledge>();
		Knowledge knowledge = new Knowledge(
				"A cow has 4 legs.");
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"cow", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A chicken has 2 legs.");
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"chicken", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A goat has 4 legs.");
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"goat", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A pig has 4 legs.");
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"pig", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A duck has 2 legs.");
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"duck", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A sheep has 4 legs.");
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"sheep", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A nickel is 0.05 dollars.");
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"nickel")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A dime is 0.1 dollars.");
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"dime")));
		knowledgeList.add(knowledge);
		
		// Math knowledge
		mathNodeMap = new HashMap<String, List<String>>();
		mathNodeMap.put("ADD", Arrays.asList("plus", "more", "sum", "exceeds", "added"));
		mathNodeMap.put("SUB", Arrays.asList("subtracted", "minus", "less", "difference"));
		mathNodeMap.put("MUL", Arrays.asList("product"));
		mathNodeMap.put("DIV", Arrays.asList("ratio"));
		mathNodeSet = new HashSet<>();
		mathNodeSet.addAll(mathNodeMap.get("ADD"));
		mathNodeSet.addAll(mathNodeMap.get("SUB"));
		mathNodeSet.addAll(mathNodeMap.get("MUL"));
		mathNodeSet.addAll(mathNodeMap.get("DIV"));
		mathIndicatorSet = new HashSet<>();
		mathIndicatorSet.addAll(mathNodeSet);
		mathIndicatorSet.addAll(Arrays.asList(
				"times", "thrice", "triple", "twice", "double", "half"));
	}
	
	public static void appendWorldKnowledge(SimulProb simulProb) throws Exception {
		// Rule based knowledge acquisition
		TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
				simulProb.question, ViewNames.LEMMA, false);
		List<Constituent> questionLemmas = 
				ta.getView(ViewNames.LEMMA).getConstituents();
		for(Knowledge knowledge : KnowledgeBase.knowledgeList) {
			int count = 0; 
			for(String target : knowledge.targets) {
				for(Constituent lemma : questionLemmas) {
					if(lemma.getLabel().equals(target)) {
						count++;
						break;
					}
				}
			}
			if(count == knowledge.targets.size()) {
				simulProb.question = simulProb.question + " " + knowledge.knowledge;
			}
		}
	}
}
