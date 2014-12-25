package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class RelationY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public String relation;
	
	public RelationY() {
		relation = null;
	}
	
	 public RelationY(String label) {
		relation = label;
	}
	
	public static float getLoss(RelationY r1, RelationY r2) {
		float loss = 0.0f;
		if(!r1.relation.equals(r2.relation)) {
			loss+=1.0;
		}
		return loss;
	}
}