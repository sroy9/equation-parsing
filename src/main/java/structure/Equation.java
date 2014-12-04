package structure;

import java.util.List;

import utils.Tools;

public class Equation {
	
	public String label;
	public List<String> terms;
	public List<Operation> operations;
	
	public Equation(String label, List<String> terms, List<Operation> operations) {
		this.label = label;
		this.terms = terms;
		this.operations = operations;
		assert terms.size() == (operations.size() + 1);
	}
	
	public Equation(String eqString) {
		String strArr[] = eqString.split("(\\+|\\-|\\*|\\/|=)");
		assert strArr.length <= 3;
		int lastLoc = 0;
		for(int i = 0; i < eqString.length(); i++) {
			if(Tools.getOperationFromString(""+eqString.charAt(i)) != null) {
				terms.add(eqString.substring(lastLoc, i));
				operations.add(Tools.getOperationFromString(""+eqString.charAt(i)));
				lastLoc = i+1;
			}
		}
		terms.add(eqString.substring(lastLoc));
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
