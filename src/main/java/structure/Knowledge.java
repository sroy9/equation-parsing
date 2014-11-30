package structure;

import java.util.ArrayList;
import java.util.List;

public class Knowledge {
	
	public String knowledge;
	public String label;
	public List<Span> mentions;
	public List<String> targets; // to be used only by knowledge base
	
	public Knowledge(String knowledge) {
		this.knowledge = knowledge;
		this.mentions = new ArrayList<Span>();
	}
	
	public Knowledge(String knowledge, List<Span> mentionList, String label) {
		this.knowledge = knowledge;
		this.mentions = mentionList;
		this.label = label;
		
	}
	public void addMentions(List<Span> mentionList) {
		mentions = mentionList;
	}
	
	public void addTargets(List<String> targets) {
		this.targets = targets;
	}

}
