package joint;


import relation.RelationX;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class JointX implements IInstance{
	
	public RelationX relationX;
	public int problemIndex;
	
	public JointX(SimulProb simulProb) {
		relationX = new RelationX(simulProb);
		problemIndex = simulProb.index;
	}
}
