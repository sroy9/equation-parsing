package structure;

import java.util.List;

public class Knowledge {
	
	public String knowledge;
	public String label;
	public List<String> targets; // to be used only by knowledge base
	
	public Knowledge(String knowledge) {
		this.knowledge = knowledge;
	}
	
	public void addTargets(List<String> targets) {
		this.targets = targets;
	}

}
