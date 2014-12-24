package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class RelationY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<String> relations;
	
	public RelationY() {
		this.relations = new ArrayList<String>();
	}
	
	public void addRelation(String label) {
		relations.add(label);
	}
	
	public void removeLast() {
		if(relations.size()>0) {
			relations.remove(relations.size()-1);
		}
	}
	
	public RelationY(RelationY other) {
		this.relations = new ArrayList<String>();
		for(String label : other.relations) {
			this.relations.add(label);
		}
	}
	
	public RelationY(SimulProb simulProb) {
		relations = simulProb.relations;
	}
	
	public static float getLoss(RelationY r1, RelationY r2) {
		float loss = 0.0f;
		assert r1.relations.size() == r2.relations.size(); 
		int len = r1.relations.size();
		for(int i=0; i<len; ++i) {
			if(!r1.relations.get(i).equals(r2.relations.get(i))) {
				loss+=1.0;
			}
		}
		return loss;
	}
}