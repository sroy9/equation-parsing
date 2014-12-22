package latentsvm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class LabelSet implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2399969922362221136L;
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
	
	public LabelSet(LabelSet other) {
		this.labels = new ArrayList<String>();
		for(String label : other.labels) {
			this.labels.add(label);
		}
	}
}