package structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class VarSet implements IInstance {
	
	public TextAnnotation ta;
	public int sentId;
	public Sentence sent;
	public SimulProb simulProb;
	
	public VarSet(SimulProb simulProb, int sentId) throws Exception {
		this.simulProb = simulProb; 
		this.sentId = sentId;
		ta = new TextAnnotation("", "", simulProb.question);
		sent = ta.getSentence(sentId);
	}
	
	public LabelSet getGold () {
		LabelSet gold = new LabelSet();
		List<Mention> mentionList = new ArrayList<Mention>();
		for(Span span : simulProb.spans) {
			if(span.label.startsWith("V")) {
				continue;
			}
			int start = ta.getTokenIdFromCharacterOffset(span.ip.getFirst());
			int end = ta.getTokenIdFromCharacterOffset(span.ip.getSecond());
			mentionList.add(new Mention(ta, start,"B-"+span.label));
			for(int i = start+1; i < end; ++i) {
				mentionList.add(new Mention(ta, i,"I-"+span.label));
			}
		}
		// Now get the mentions in order
		List<Mention> mentionsInOrder = new ArrayList<Mention>();
		for(int i = 0; i < ta.size(); ++i) {
			boolean alreadyLabeled = false;
			for(Mention mention : mentionList) {
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
		// Create gold label
		for(Mention mention : mentionsInOrder) {
			if(sentId == ta.getSentenceFromToken(mention.index).getSentenceId()) {
				gold.addLabel(mention.label);
			}
		}
		assert gold.labels.size() == sent.size();
		return gold;
	}
	
}
