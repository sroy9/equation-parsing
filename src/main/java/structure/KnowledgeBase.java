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
		int index1, index2;
		Knowledge knowledge = new Knowledge(
				"A cow has 4 legs.");
		index1 = knowledge.knowledge.indexOf("cow");
		index2 = knowledge.knowledge.indexOf("4");
		knowledge.addMentions(new ArrayList<Span>(Arrays.asList(
				new Span("V1", new IntPair(index1, index1 + "cow".length())),
				new Span("E1", new IntPair(index1, index1 + "cow".length())),
				new Span("E1", new IntPair(index2, index2 + "4".length()))
				)));
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"cow", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A chicken has 2 legs.");
		index1 = knowledge.knowledge.indexOf("chicken");
		index2 = knowledge.knowledge.indexOf("2");
		knowledge.addMentions(new ArrayList<Span>(Arrays.asList(
				new Span("V1", new IntPair(index1, index1 + "chicken".length())),
				new Span("E1", new IntPair(index1, index1 + "chicken".length())),
				new Span("E1", new IntPair(index2, index2 + "2".length()))
				)));
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"chicken", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A goat has 4 legs.");
		index1 = knowledge.knowledge.indexOf("goat");
		index2 = knowledge.knowledge.indexOf("4");
		knowledge.addMentions(new ArrayList<Span>(Arrays.asList(
				new Span("V1", new IntPair(index1, index1 + "goat".length())),
				new Span("E1", new IntPair(index1, index1 + "goat".length())),
				new Span("E1", new IntPair(index2, index2 + "4".length()))
				)));
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"goat", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A pig has 4 legs.");
		index1 = knowledge.knowledge.indexOf("pig");
		index2 = knowledge.knowledge.indexOf("pig");
		knowledge.addMentions(new ArrayList<Span>(Arrays.asList(
				new Span("V1", new IntPair(index1, index1 + "pig".length())),
				new Span("E1", new IntPair(index1, index1 + "pig".length())),
				new Span("E1", new IntPair(index2, index2 + "4".length()))
				)));
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"pig", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A duck has 2 legs.");
		index1 = knowledge.knowledge.indexOf("duck");
		index2 = knowledge.knowledge.indexOf("2");
		knowledge.addMentions(new ArrayList<Span>(Arrays.asList(
				new Span("V1", new IntPair(index1, index1 + "duck".length())),
				new Span("E1", new IntPair(index1, index1 + "duck".length())),
				new Span("E1", new IntPair(index2, index2 + "2".length()))
				)));
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"duck", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A sheep has 4 legs.");
		index1 = knowledge.knowledge.indexOf("sheep");
		index2 = knowledge.knowledge.indexOf("4");
		knowledge.addMentions(new ArrayList<Span>(Arrays.asList(
				new Span("V1", new IntPair(index1, index1 + "sheep".length())),
				new Span("E1", new IntPair(index1, index1 + "sheep".length())),
				new Span("E1", new IntPair(index2, index2 + "4".length()))
				)));
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"sheep", "leg")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A nickel is 0.05 dollars.");
		index1 = knowledge.knowledge.indexOf("nickel");
		index2 = knowledge.knowledge.indexOf("0.05");
		knowledge.addMentions(new ArrayList<Span>(Arrays.asList(
				new Span("V1", new IntPair(index1, index1 + "nickel".length())),
				new Span("E1", new IntPair(index1, index1 + "nickel".length())),
				new Span("E1", new IntPair(index2, index2 + "0.05".length()))
				)));
		knowledge.addTargets(new ArrayList<String>(Arrays.asList(
				"nickel")));
		knowledgeList.add(knowledge);
		knowledge = new Knowledge(
				"A dime is 0.1 dollars.");
		index1 = knowledge.knowledge.indexOf("dime");
		index2 = knowledge.knowledge.indexOf("0.1");
		knowledge.addMentions(new ArrayList<Span>(Arrays.asList(
				new Span("V1", new IntPair(index1, index1 + "dime".length())),
				new Span("E1", new IntPair(index1, index1 + "dime".length())),
				new Span("E1", new IntPair(index2, index2 + "0.1".length()))
				)));
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
		boolean foundOne = false;
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
				// Check if span labels need to be changed
				boolean change = false;
				if(foundOne) {
					change = true;
					foundOne = true;
				}
				// Add the spans to simulProb
				for(Span span : knowledge.mentions) {
					if(change && span.label.equals("V1")) {
						simulProb.spans.add(new Span("V2", new IntPair(
										span.ip.getFirst()+simulProb.question.length()+1,
										span.ip.getSecond()+simulProb.question.length()+1)));
					} else if(change && span.label.equals("E1")) {
						simulProb.spans.add(new Span("E2", new IntPair(
										span.ip.getFirst()+simulProb.question.length()+1,
										span.ip.getSecond()+simulProb.question.length()+1)));
					} else {
						simulProb.spans.add(new Span(span.label, new IntPair(
								span.ip.getFirst()+simulProb.question.length()+1,
								span.ip.getSecond()+simulProb.question.length()+1)));
					}
				}
				simulProb.question = simulProb.question + " " + knowledge.knowledge;
			}
		}
	}
	
	public void searchForWorldKnowledgeRequirements() throws Exception {
		DocReader dr = new DocReader();
		List<SimulProb> simulProbList = null;
		System.out.println("Reading data");
		simulProbList = dr.readSimulProbFromBratDir(Params.annotationDir);
		for(SimulProb simulProb : simulProbList) {
			if(simulProb.question.contains("nickel")) {
				System.out.println("************************");
				System.out.println(simulProb.index);
				System.out.println(simulProb.question);
			}
		}
	}
	
	public static void main(String args[]) throws Exception {
		KnowledgeBase kb = new KnowledgeBase();
		kb.searchForWorldKnowledgeRequirements();
	}
}
