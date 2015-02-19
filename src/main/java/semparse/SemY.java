package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import structure.Equation;
import structure.EquationSolver;
import structure.Node;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class SemY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<Node> nodes;
	
	public SemY() {
		nodes = new ArrayList<>();
	}
	
	public SemY(SemY other) {
		nodes = new ArrayList<>();
		nodes.addAll(other.nodes);
	}
	
	public SemY(SimulProb prob,  IntPair span) {
		nodes = new ArrayList<Node>();
		for(Node node : prob.nodes) {
			if(Tools.doesContain(span, node.span)) {
				nodes.add(node);
			}
		}
		for(int i=span.getFirst(); i<span.getSecond(); ++i) {
			boolean found = false;
			for(Node node : nodes) {
				if(node.span.getFirst() == i && node.span.getSecond() == (i+1)) {
					found = true;
					break;
				}
			}
			if(!found) nodes.add(new Node("NULL", new IntPair(i, i+1), null));
		}
		for(Node node : nodes) {
			List<Node> children = new ArrayList<>();
			for(Node node1 : nodes) {
				if(Tools.doesContainNotEqual(node.span, node1.span)) {
					boolean allow = true;
					for(Node node2 : nodes) {
						if(Tools.doesContainNotEqual(node.span, node2.span) 
								&& Tools.doesContainNotEqual(node2.span, node1.span)) {
							allow = false;
							break;
						}
					}
					if(allow) {
						children.add(node1);
					}
				}
			}
			Collections.sort(children, new Comparator<Node>() {
			    @Override
			    public int compare(Node a, Node b) {
			    		return (int)Math.signum(a.span.getFirst() - b.span.getFirst());
			    }
			});
			node.children = children;
		}
	}
	
	public static float getNodeLoss(SemY y1, SemY y2) {
		float loss = 0.0f;
		for(Node pair1 : y1.nodes) {
			boolean found = false;
			for(Node pair2 : y2.nodes) {
				if(pair1.label.equals(pair2.label) && 
						pair1.span.equals(pair2.span)) {
					found = true;
					break;
				}
			}
			if(!found) loss += 1.0;
		}
		for(Node pair1 : y2.nodes) {
			boolean found = false;
			for(Node pair2 : y1.nodes) {
				if(pair1.label.equals(pair2.label) && 
						pair1.span.equals(pair2.span)) {
					found = true;
					break;
				}
			}
			if(!found) loss += 1.0;
		}
		return loss;
	}
	
	public static float getLoss(SemY y1, SemY y2) {
		return getNodeLoss(y1, y2);
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(nodes);
	}
}