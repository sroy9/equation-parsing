package lbj;

import java.util.ArrayList;
import java.util.List;

import parser.DocReader;
import structure.Expression;
import structure.SimulProb;
import structure.Span;
import structure.Mention;
import utils.Params;
import utils.Tools;
import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.IR.VariableInstance;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;

public class MentionParser implements Parser {
	public List<Mention> variableMentionList;
	public int index;
	public MentionFeatureManager fm;
	
	public MentionParser() {
		fm = new MentionFeatureManager();
	}
	
	public MentionParser(double start, double end) {
		DocReader dr = new DocReader();
		List<SimulProb> simulProbList = null;
		variableMentionList = new ArrayList<Mention>();
		try {
			simulProbList = dr.readSimulProbFromBratDir(
					Params.annotationDir, start, end);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fm = new MentionFeatureManager();
		try {
			for(SimulProb simulProb : simulProbList) {
				List<Mention> tmpVariableMentionList =
						extractMentionExamples(simulProb);
//				System.out.println("********");
				for(int i = 0; i < tmpVariableMentionList.size(); ++i) {
//					System.out.println(tmpVariableMentionList.get(i).ta.getToken(i)+":"+tmpVariableMentionList.get(i).label);
					fm.getAllFeatures(tmpVariableMentionList, i, simulProb);
				}
				variableMentionList.addAll(tmpVariableMentionList);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		index = 0;	
	}
	
	public List<Mention> extractMentionExamples(SimulProb simulProb) {
		List<Mention> mentions = new ArrayList<Mention>();
		TextAnnotation ta = new TextAnnotation("", "", simulProb.question);
		for(Span span : simulProb.spans) {
			if(span.label.startsWith("V")) {
				continue;
			}
			boolean isVariable = false;
			for(Span span1 : simulProb.spans) {
				if((span1.ip.equals(span.ip)) && span1.label.startsWith("V")) {
					isVariable = true;
					break;
				}
			}
			if(!isVariable) {
				continue;
			}
			int start = ta.getTokenIdFromCharacterOffset(span.ip.getFirst());
			int end = ta.getTokenIdFromCharacterOffset(span.ip.getSecond());
			String label = span.label + "-VAR";
			mentions.add(new Mention(ta, start,"B-"+label));
			for(int i = start+1; i < end; ++i) {
				mentions.add(new Mention(ta, i,"I-"+label));
			}
		}
		// Now get the mentions in order
		List<Mention> mentionsInOrder = new ArrayList<Mention>();
		for(int i = 0; i < ta.size(); ++i) {
			boolean alreadyLabeled = false;
			for(Mention mention : mentions) {
				if(mention.index == i) {
					alreadyLabeled = true;
					mentionsInOrder.add(mention);
					break;
				}
			}
			if(!alreadyLabeled) {
				mentionsInOrder.add(new Mention(ta, i, "O"));
			}
		}
		// Check if E1 and E2 need to be interchanged
		String firstVar = "";
		for(Mention mention : mentionsInOrder) {
			if(mention.label.contains("E2")) {
				firstVar = mention.label;
				break;
			}
			if(mention.label.contains("E1")) {
				firstVar = mention.label;
				break;
			}
		}
		if(firstVar.contains("E2")) {
			for(Mention mention : mentionsInOrder) {
				if(mention.label.contains("E1")) {
					mention.label = mention.label.replace("E1", "E2");
				} else if(mention.label.contains("E2")) {
					mention.label = mention.label.replace("E2", "E1");				
				}
			}
		}
		return mentionsInOrder;
	}
	
	public Object next() {
		if (index < variableMentionList.size()) {
			return variableMentionList.get(index++);
		} else {
			return null;
		}
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

}
