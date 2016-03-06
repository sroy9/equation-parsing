package tree;

import java.util.List;
import java.util.Map;

import joint.JointX;
import lasttwo.LasttwoX;
import structure.Node;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class TreeX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public List<IntPair> candidateVars;
	public Map<String, List<Integer>> varTokens;
	public List<Node> nodes;
	
	// Constructors assume nodes to be sorted based on charIndex
	
	public TreeX(SimulProb simulProb, Map<String, List<Integer>> varTokens,
			List<Node> nodes) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		parse = simulProb.parse;
		candidateVars = simulProb.candidateVars;
		this.varTokens = varTokens;
		this.nodes = nodes;
		for(int i=0; i<nodes.size()-1; ++i) {
			if(nodes.get(i).charIndex > nodes.get(i+1).charIndex) {
				System.err.println("Problem Here : Nodelist not sorted, sorted expected");
			}
		}
	}
	
	public TreeX(JointX simulProb, Map<String, List<Integer>> varTokens,
			List<Node> nodes) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.problemIndex;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		parse = simulProb.parse;
		candidateVars = simulProb.candidateVars;
		this.varTokens = varTokens;
		this.nodes = nodes;
		for(int i=0; i<nodes.size()-1; ++i) {
			if(nodes.get(i).charIndex > nodes.get(i+1).charIndex) {
				System.err.println("Problem Here : Nodelist not sorted, sorted expected");
			}
		}
	}
	
	public TreeX(LasttwoX simulProb, Map<String, List<Integer>> varTokens,
			List<Node> nodes) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.problemIndex;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		this.varTokens = varTokens;
		this.nodes = nodes;
		for(int i=0; i<nodes.size()-1; ++i) {
			if(nodes.get(i).charIndex > nodes.get(i+1).charIndex) {
				System.err.println("Problem Here : Nodelist not sorted, sorted expected");
			}
		}
	}
	
}
