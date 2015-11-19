package lasttwo;

import java.util.ArrayList;
import java.util.List;

import joint.JointX;
import structure.Node;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class LasttwoX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public List<IntPair> candidateVars;
	public List<Node> nodes;
	
	public LasttwoX(SimulProb simulProb) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		nodes = new ArrayList<Node>();
		for(int i=0; i<simulProb.quantities.size(); ++i) {
			for(Node leaf : simulProb.equation.root.getLeaves()) {
				if(leaf.label.equals("NUM") && Tools.safeEquals(Tools.getValue(
						simulProb.quantities.get(i)), leaf.value)) {
					Node node = new Node(leaf);
					node.index = i;
					nodes.add(node);
				}
			}
		}
	}
	
	public LasttwoX(JointX simulProb, List<Node> leavesFromNum) {
		quantities = simulProb.quantities;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		nodes = new ArrayList<Node>();
		nodes.addAll(leavesFromNum);
	}
	
}
