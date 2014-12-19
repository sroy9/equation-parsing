package structure;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import parser.DocReader;
import utils.Params;
import utils.Tools;

public class KnowledgeBase {
	
	public static List<Knowledge> knowledgeList;
	
	static {
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
	}
	
	public static void appendWorldKnowledge(SimulProb simulProb) 
			throws Exception {
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
