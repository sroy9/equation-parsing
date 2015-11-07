package lasttwo;

import java.util.ArrayList;
import java.util.List;

import structure.Node;
import structure.SimulProb;
import tree.TreeX;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class LastTwoX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public List<IntPair> candidateVars;
	public List<Node> nodes;
	
	public LastTwoX(SimulProb simulProb, List<Node> nodes) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		this.nodes = new ArrayList<>();
		for(Node node : nodes) {
			if(node.label.equals("NUM")) {
				this.nodes.add(node);
			}
		}
	}
	
	public LastTwoX(TreeX simulProb, List<Node> nodes) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.problemIndex;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		this.nodes = new ArrayList<>();
		for(Node node : nodes) {
			if(node.label.equals("NUM")) {
				this.nodes.add(node);
			}
		}
	}
	
}
