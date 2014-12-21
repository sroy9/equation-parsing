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
		clusters = new ArrayList<>();
		for(int i=0; i<3; ++i) {
			clusters.add(new ArrayList<QuantSpan>());
		}
		for(QuantSpan qs : blob.quantities) {
			for(Equation eq : equations) {
				for(int i=0; i<5; ++i) {
					List<Pair<Operation, Double>> list = eq.terms.get(i);
					for(Pair<Operation, Double> pair : list) {
						if(Tools.safeEquals(Tools.getValue(qs), pair.getSecond())) {
							clusters.get(i/2).add(qs);
							break;
						}
					}
				}
			}
		}
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
