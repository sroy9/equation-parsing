package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class SemY implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2399969922362221136L;
	public List<String> labels;
	
	public SemY() {
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
	
	public SemY(SemY other) {
		this.labels = new ArrayList<String>();
		for(String label : other.labels) {
			this.labels.add(label);
		}
	}
}