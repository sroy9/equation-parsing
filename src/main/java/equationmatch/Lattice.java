package equationmatch;

import java.util.ArrayList;
import java.util.List;

import structure.Equation;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class Lattice implements IStructure {
	public List<Equation> equations;
	
	public Lattice() {
		equations = new ArrayList<Equation>();
		equations.add(new Equation());
		equations.add(new Equation());
	}
	
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Lattice)) {
			return false;
		}
		Lattice lattice = (Lattice) obj;
		if(lattice.equations.get(0).equals(equations.get(0)) && 
				lattice.equations.get(1).equals(equations.get(1))) {
			return true;
		}
		if(lattice.equations.get(0).equals(equations.get(1)) && 
				lattice.equations.get(1).equals(equations.get(0))) {
			return true;
		}
		return false;
	}

	public Lattice(List<Equation> equations) {
		this.equations = equations;
		if(this.equations.size() == 1) {
			this.equations.add(new Equation());
		}
		assert this.equations.size() == 2;
	}
}
