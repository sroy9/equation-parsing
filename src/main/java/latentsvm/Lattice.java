package latentsvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import structure.Equation;
import structure.Operation;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class Lattice implements IStructure {
	public List<Equation> equations;
	public Map<String, List<QuantSpan>> clusterMap;
	public LabelSet labelSet;
	
	public Lattice() {
		equations = new ArrayList<Equation>();
		equations.add(new Equation());
		equations.add(new Equation());
		labelSet = new LabelSet();
		clusterMap = new HashMap<>();
		clusterMap.put("E1", new ArrayList<QuantSpan>());
		clusterMap.put("E2", new ArrayList<QuantSpan>());
		clusterMap.put("E3", new ArrayList<QuantSpan>());
	}
	
	public Lattice(Lattice lattice) {
		equations = new ArrayList<Equation>();
		equations.add(new Equation(lattice.equations.get(0)));
		equations.add(new Equation(lattice.equations.get(1)));
		labelSet = new LabelSet();
		for(String label : lattice.labelSet.labels) {
			labelSet.addLabel(label);
		}
		clusterMap = lattice.clusterMap;
	}

	public Lattice(List<Equation> equations, Blob blob) {
		this.equations = equations;
		if(this.equations.size() == 1) {
			this.equations.add(new Equation());
		}
		assert this.equations.size() == 2;
		labelSet = new LabelSet();
		clusterMap = new HashMap<>();
		clusterMap.put("E1", new ArrayList<QuantSpan>());
		clusterMap.put("E2", new ArrayList<QuantSpan>());
		clusterMap.put("E3", new ArrayList<QuantSpan>());
		for(QuantSpan qs : blob.quantities) {
			for(Equation eq : equations) {
				for(Pair<Operation, Double> pair : eq.A1) {
					if(Tools.safeEquals(Tools.getValue(qs), pair.getSecond())) {
						clusterMap.get("E1").add(qs);
						break;
					}
				}
				for(Pair<Operation, Double> pair : eq.A2) {
					if(Tools.safeEquals(Tools.getValue(qs), pair.getSecond())) {
						clusterMap.get("E1").add(qs);
						break;
					}
				}
				for(Pair<Operation, Double> pair : eq.B1) {
					if(Tools.safeEquals(Tools.getValue(qs), pair.getSecond())) {
						clusterMap.get("E2").add(qs);
						break;
					}
				}
				for(Pair<Operation, Double> pair : eq.B2) {
					if(Tools.safeEquals(Tools.getValue(qs), pair.getSecond())) {
						clusterMap.get("E2").add(qs);
						break;
					}
				}
				for(Pair<Operation, Double> pair : eq.C) {
					if(Tools.safeEquals(Tools.getValue(qs), pair.getSecond())) {
						clusterMap.get("E3").add(qs);
						break;
					}
				}
			}
		}
	}
	
	// We assume a canonical ordering of equations under lattice
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Lattice)) {
			return false;
		}
		Lattice lattice = (Lattice) obj;
		if(lattice.equations.get(0).equals(equations.get(0)) && 
				lattice.equations.get(1).equals(equations.get(1))) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String str = "";
		for(Equation eq : equations) {
			str += eq + "\n";
		}
		return str;
	}
	
}
