package structure;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class LabelSet implements IStructure{
	
	public List<String> labels;
	
	public LabelSet() {
		this.labels = new ArrayList<String>();
	}
	
	public void addLabel(String label) {
		labels.add(label);
	}
	
	public void removeLast() {
		if(labels.size()>0) {
			labels.remove(labels.size()-1);
		}
	}
}
