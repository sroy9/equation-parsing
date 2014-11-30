package structure;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.IStructure;

/**
 * maintains the two "paths", each path denotes a equation
 * 
 * @author upadhya3
 *
 */
public class Lattice implements IStructure {
	public List<String> edgesWithOps;
	public List<List<Expression>> termList;
	public List<Operation> opList;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((edgesWithOps == null) ? 0 : edgesWithOps.hashCode());
		result = prime * result + ((opList == null) ? 0 : opList.hashCode());
		result = prime * result
				+ ((termList == null) ? 0 : termList.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return edgesWithOps.toString();
	}

	@Override
	// public boolean equals(Object obj) {
	// if (this == obj)
	// return true;
	// if (obj == null)
	// return false;
	// if (getClass() != obj.getClass())
	// return false;
	// Lattice other = (Lattice) obj;
	// if (edgesWithOps == null) {
	// if (other.edgesWithOps != null)
	// return false;
	// } else if (!edgesWithOps.equals(other.edgesWithOps))
	// return false;
	// if (opList == null) {
	// if (other.opList != null)
	// return false;
	// } else if (!opList.equals(other.opList))
	// return false;
	// if (termList == null) {
	// if (other.termList != null)
	// return false;
	// } else if (!termList.equals(other.termList))
	// return false;
	// return true;
	// }
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Lattice other = (Lattice) obj;
		if (edgesWithOps == null) {
			if (other.edgesWithOps != null)
				return false;
		} else {
			if (edgesWithOps.size() != other.edgesWithOps.size()) {
				return false;
			}
			if (edgesWithOps.size() == 1) {
				if (edgesWithOps.get(0).equals(other.edgesWithOps.get(0))) {
					return true;
				}
			}
			if (edgesWithOps.size() == 2) {
				if (edgesWithOps.get(0).equals(other.edgesWithOps.get(0))
						&& edgesWithOps.get(1)
								.equals(other.edgesWithOps.get(1))) {
					return true;
				}
				if (edgesWithOps.get(0).equals(other.edgesWithOps.get(1))
						&& edgesWithOps.get(1)
								.equals(other.edgesWithOps.get(0))) {
					return true;
				}
			}
		}
		return false;
	}

	public Lattice(List<String> edgesWithOps) {
		this.edgesWithOps = edgesWithOps;
		this.termList = new ArrayList<>();
		this.opList = new ArrayList<>();
	}
}
