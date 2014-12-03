package structure;

import java.util.List;
import java.util.Map;

public class Equation {
	
	public List<Double> terms;
	public List<Operation> operations;
	
	public Equation(List<Double> terms, List<Operation> operations) {
		this.terms = terms;
		this.operations = operations;
		assert terms.size() == (operations.size() + 1);
	}
	
	public Equation(String eqString) {
		
	}
	
	public String toString() {
		String str = "";
		for(int i=0; i<terms.size(); i++) {
			if(i>0) {
				str += operations.get(i-1);
			}
			str += terms.get(i);
		}
		return str;
	}

}
