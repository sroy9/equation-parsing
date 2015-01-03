package joint;


import relation.RelationX;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class JointX implements IInstance{
	
	RelationX relationX;
	
	public JointX(SimulProb simulProb) {
		relationX = new RelationX(simulProb);
	}
}
