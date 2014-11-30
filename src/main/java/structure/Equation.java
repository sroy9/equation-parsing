package structure;

import java.util.List;
import java.util.Map;

public class Equation {
	
	public List<Expression> terms;
	public List<Operation> operations;
	public List<String> features;
	public Map<String, List<Expression>> termMap;
	public String label;
	public double equationScore;
	
	public Equation(List<Expression> equation, List<Operation> operations, 
			Map<String, List<Expression>> termMap) {
		this.terms = equation;
		this.operations = operations;
		this.termMap = termMap;
		assert equation.size() == (operations.size() + 1);
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
