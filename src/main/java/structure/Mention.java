package structure;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

public class Mention {
	public TextAnnotation ta;
	public int index;
	public String label; // B-MENTION, I-MENTION or O
	public List<String> features;
	
	public Mention(TextAnnotation ta, int index, String label) {
		this.ta = ta;
		this.index = index;
		this.label = label;
	}
	
}
