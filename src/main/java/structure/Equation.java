package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import utils.Tools;

// Holds canonical equations
// Ax +/- By +/- C = 0
// where A = \product A_i, B = \product B_i and C = \product C_i
// and A_i's, B_i's and C_i's are present in text

public class Equation implements Serializable {
	
	private static final long serialVersionUID = -1593105262537880720L;
	public List<List<Pair<Operation, Double>>> terms;
	public List<Operation> operations; 
	
	public Equation() {
		terms = new ArrayList<>();
		for(int i=0; i<5; ++i) {
			terms.add(new ArrayList<Pair<Operation, Double>>());
		}
		this.operations = new ArrayList<>();
		for(int i=0; i<4; i++) {
			operations.add(Operation.NONE);
		}
	}
	
	public Equation(Equation eq) {
		terms = new ArrayList<>();
		for(int i=0; i<5; ++i) {
			terms.add(new ArrayList<Pair<Operation, Double>>());
			for(Pair<Operation, Double> pair : eq.terms.get(i)) {
				terms.get(i).add(new Pair<Operation, Double>(
						pair.getFirst(), pair.getSecond()));
			}
		}
		this.operations = new ArrayList<>();
		for(int i=0; i<4; i++) {
			operations.add(eq.operations.get(i));
		}
	}
	
	public Equation(int index, String eqString) {
		this();
		eqString = eqString.trim()+" ";
		int lastLoc = 0;
		for(int i=0; i<eqString.length(); ++i) {
			char ch = eqString.charAt(i);
			char prevCh = ' ';
			if(i>0) prevCh = eqString.charAt(i-1);
			if(ch == '=' || ch =='+' || (ch == '-' && !isSymbol(prevCh)) ||
					i == eqString.length()-1) {
				String term = eqString.substring(lastLoc, i);
				Operation op = Operation.NONE;
				if(lastLoc == 0 || eqString.charAt(lastLoc-1) == '+' 
						|| eqString.charAt(lastLoc-1) == '=') {
					op = Operation.ADD;
				} else {
					op = Operation.SUB;
				}
				if(term.contains("V1")) { 
					addTerm(0, term); 
					operations.set(0, op); 
				} else if(term.contains("V2")) {
					addTerm(2, term);
					operations.set(2, op);
				} else if(sameSideOfEquation(eqString, "V2", "V1") && 
						!sameSideOfEquation(eqString, term, "V1")) {
					addTerm(4, term);
				} else if(sameSideOfEquation(eqString, term, "V1") || 
							diffSideOfEquation(eqString, term, "V2")) {
					addTerm(1, term);
					operations.set(1, op);
				} else if(sameSideOfEquation(eqString, term, "V2") || 
						diffSideOfEquation(eqString, term, "V1")) {
					addTerm(3, term);
					operations.set(3, op);
				} else {
					System.out.println("ISSUE HERE : "+index);
				}
				lastLoc = i+1;
			}
		}
	}
	
	private void addTerm(int index, String term) {
		int lastLoc = 0;
		term = term + " ";
		for(int i=0; i<term.length(); ++i) {
			char ch = term.charAt(i);
			if(ch == '*' || ch == '/' || i == term.length()-1) {
				String number = term.substring(lastLoc, i);
				try {
					Double d = Double.parseDouble(number);
					if(lastLoc == 0 || term.charAt(lastLoc-1) == '*') {
						terms.get(index).add(
								new Pair<Operation, Double>(Operation.MUL, d));
					} else {
						terms.get(index).add(
								new Pair<Operation, Double>(Operation.DIV, d));
					}
				} catch (NumberFormatException e) {
					
				}
				lastLoc = i+1;
			}
		}
	}

	public boolean isSymbol(char ch) {
		if(ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '=') {
			return true;
		}
		return false;
	}
	
	// Assumes key1 and key2 both substrings of eqString
	public boolean sameSideOfEquation(String eqString, String key1, String key2) {
		String strArr[] = eqString.split("=");
		if(strArr[0].contains(key1) && strArr[0].contains(key2)) {
			return true;
		}
		if(strArr[1].contains(key1) && strArr[1].contains(key2)) {
			return true;
		}
		return false;
	}
	
	// Assumes key1 and key2 both substrings of eqString
	public boolean diffSideOfEquation(String eqString, String key1, String key2) {
		String strArr[] = eqString.split("=");
		if(strArr[0].contains(key1) && strArr[1].contains(key2)) {
			return true;
		}
		if(strArr[1].contains(key1) && strArr[0].contains(key2)) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		String str = "";
		for(int i=0; i<5; ++i) {
			List<Pair<Operation, Double>> list = terms.get(i);
			for(Pair<Operation, Double> pair : list) {
				str+="["+pair.getFirst()+" "+pair.getSecond()+"] ";
			}
			str+="\n";
		}
		return str;
	}
	

}
