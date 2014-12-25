package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class RelationY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<String> relations;
	public int index;
	
	public RelationY() {
		this.relations = new ArrayList<String>();
		index = -1;
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
		this.index = other.index;
	}
	
	public RelationY(SimulProb simulProb, int index) {
		relations = simulProb.relations;
		this.index = index;
		boolean needsSwap = false;
		for(String relation : relations) {
			if(relation.equals("R2")) needsSwap = true;
			if(relation.equals("R1")) break; 
		}
		// This ensures R1 always appears before R2
		if(needsSwap) {
			for(int i=0; i<relations.size(); ++i) {
				if(relations.get(i).equals("R1")) {
					relations.set(i, "R2");
				} else if(relations.get(i).equals("R2")) {
					relations.set(i, "R1");
				}
			}
		}
	}
	
	public static float getLoss(RelationY r1, RelationY r2) {
		float loss = 0.0f;
		assert r1.relations.size() == r2.relations.size();
		if(!r1.relations.get(r1.index).equals(r2.relations.get(r2.index))) {
			loss+=1.0;
		}
		return loss;
	}
}