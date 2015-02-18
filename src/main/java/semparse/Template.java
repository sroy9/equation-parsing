package semparse;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import structure.Equation;

public class Template {

	public List<Equation> equations;
	public List<Slot> slots;
	
	public Template() {
		equations = new ArrayList<>();
		slots = new ArrayList<>();
	}
	
	public Template(List<Equation> eqList) {
		this.equations = eqList;
		slots = new ArrayList<>();
		for(int i=0; i<eqList.size(); ++i) {
			for(IntPair slot : eqList.get(i).slots) {
				if(eqList.get(i).terms.get(slot.getFirst()).
						get(slot.getSecond()).getSecond() == null) {
					slots.add(new Slot(i, slot.getFirst(), slot.getSecond()));
				}
			}
		}
	}
	
	public Template(Template other) {
		this();
		for(Equation eq : other.equations) {
			equations.add(new Equation(eq));
		}
		slots.addAll(other.slots);
	}
	
}
