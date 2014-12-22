package latentsvm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import structure.Equation;
import structure.Operation;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class Lattice implements IStructure, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6115613027583328438L;
	public List<Equation> equations;
	public List<List<QuantSpan>> clusters;
	public LabelSet labelSet;
	
	public Lattice() {
		equations = new ArrayList<Equation>();
		equations.add(new Equation());
		equations.add(new Equation());
		labelSet = new LabelSet();
		clusters = new ArrayList<>();
		for(int i=0; i<3; ++i) {
			clusters.add(new ArrayList<QuantSpan>());
		}
	}
	
	public Lattice(Lattice lattice) {
		equations = new ArrayList<Equation>();
		equations.add(new Equation(lattice.equations.get(0)));
		equations.add(new Equation(lattice.equations.get(1)));
		labelSet = new LabelSet();
		for(String label : lattice.labelSet.labels) {
			labelSet.addLabel(label);
		}
		clusters = lattice.clusters;
	}

	public Lattice(List<Equation> equations, Blob blob) {
		this.equations = equations;
		if(this.equations.size() == 1) {
			this.equations.add(new Equation());
		}
		assert this.equations.size() == 2;
		labelSet = new LabelSet();
		Set<Integer> candidates;
		for(int quantNo = 0; quantNo < blob.quantities.size(); quantNo++) {
			if(isSpecialCase(blob.simulProb.index, quantNo)) continue;
			// Back to machines
			QuantSpan qs = blob.quantities.get(quantNo);
			candidates = new HashSet<>();
			for(Equation eq : equations) {
				for(int i=0; i<5; ++i) {
					List<Pair<Operation, Double>> list = eq.terms.get(i);
					for(Pair<Operation, Double> pair : list) {
						if(Tools.safeEquals(Tools.getValue(qs), pair.getSecond())) {
							candidates.add(i);
							break;
						}
					}
				}
			}
			if(candidates.size() == 1) {
				for(Integer i : candidates) {
					if(i<2) labelSet.addLabel("E1");
					else if(i<4) labelSet.addLabel("E2");
					else labelSet.addLabel("E3");
				}
			} else if(candidates.size() == 0) {
				labelSet.addLabel("NONE");
			} else {
				System.out.println("PROBLEM");
				System.out.println(quantNo + " : " + qs);
				System.out.println(blob.simulProb.index + " : " +blob.simulProb.question);
			}
		}
		clusters = extractClustersFromLabelSet(blob.quantities, labelSet);
	}

	public static List<List<QuantSpan>> extractClustersFromLabelSet(
			List<QuantSpan> quantities, LabelSet labelSet) {
		// Create a cluster map
		List<List<QuantSpan>> clusters = new ArrayList<>();
		for(int i=0; i<3; ++i) {
			clusters.add(new ArrayList<QuantSpan>());
		}
		for(int i=0; i<quantities.size(); ++i) {
			String label = labelSet.labels.get(i);
			if(label.equals("E1")) clusters.get(0).add(quantities.get(i));
			if(label.equals("E2")) clusters.get(1).add(quantities.get(i));
			if(label.equals("E3")) clusters.get(2).add(quantities.get(i));
		}
		return clusters;
	}

	@Override
	public String toString() {
		String str = "LabelSet : \n";
		str+=Arrays.asList(labelSet.labels)+"\n";
		str+="Clusters : \n";
		for(List<QuantSpan> list : clusters) {
			str+=Arrays.asList(list)+"\n";
		}
		str += "Equations\n";
		for(Equation eq : equations) {
			str += "Equation\n" + eq + "\n";
		}
		return str;
	}
	
	boolean isSpecialCase(int index, int quantNo) {
		// Human annotation
		if(index ==  1292 && quantNo == 0) {
			labelSet.addLabel("E1");
			return true;
		}
		if(index ==  1292 && quantNo == 4) {
			labelSet.addLabel("E2");
			return true;
		}
		if(index ==  1997 && quantNo == 1) {
			labelSet.addLabel("E2");
			return true;
		}
		if(index ==  1997 && quantNo == 3) {
			labelSet.addLabel("E1");
			return true;
		}
		if(index ==  2518 && quantNo == 0) {
			labelSet.addLabel("E1");
			return true;
		}
		if(index ==  2518 && quantNo == 4) {
			labelSet.addLabel("E2");
			return true;
		}
		if(index ==  3623 && quantNo == 0) {
			labelSet.addLabel("E1");
			return true;
		}
		if(index ==  3623 && quantNo == 4) {
			labelSet.addLabel("E2");
			return true;
		}
		if(index ==  5356 && quantNo == 0) {
			labelSet.addLabel("E1");
			return true;
		}
		if(index ==  5356 && quantNo == 4) {
			labelSet.addLabel("E2");
			return true;
		}
		if(index ==  5652 && quantNo == 1) {
			labelSet.addLabel("E2");
			return true;
		}
		if(index ==  5652 && quantNo == 2) {
			labelSet.addLabel("E1");
			return true;		
		}
		if(index ==  6254 && quantNo == 1) {
			labelSet.addLabel("E2");
			return true;
		}
		if(index ==  6254 && quantNo == 3) {
			labelSet.addLabel("E1");
			return true;
		}
		if(index ==  6448 && quantNo == 1) {
			labelSet.addLabel("E2");
			return true;
		}
		if(index ==  6448 && quantNo == 3) {
			labelSet.addLabel("E1");
			return true;
		}
		return false;
	}
	
}
