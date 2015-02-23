package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
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
	public List<IntPair> slots;
	public boolean isOneVar;
	public Map<String, List<Integer>> varTokens;
	
	public Equation() {
		isOneVar = false;
		terms = new ArrayList<>();
		for(int i=0; i<5; ++i) {
			terms.add(new ArrayList<Pair<Operation, Double>>());
		}
		this.operations = new ArrayList<>();
		for(int i=0; i<4; i++) {
			operations.add(Operation.NONE);
		}
		slots = new ArrayList<>();
		varTokens = new HashMap<String, List<Integer>>();
	}
	
	public Equation(Equation eq) {
		terms = new ArrayList<>();
		isOneVar = eq.isOneVar;
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
		slots = new ArrayList<>();
		slots.addAll(eq.slots);
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(eq.varTokens);
	}
	
	public Equation(int index, String eqString) {
		this();
//		System.out.println("EqString : "+eqString);
		eqString = eqString.trim()+" ";
		int lastLoc = 0;
		for(int i=0; i<eqString.length(); ++i) {
			char ch = eqString.charAt(i);
			if(ch == '(') {
				while(eqString.charAt(i) != ')') ++i;
				continue;
			}
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
		for(int i=0; i<terms.size(); ++i) {
			for(int j=0; j<terms.get(i).size(); ++j) {
				slots.add(new IntPair(i, j));
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
					Double d = Double.parseDouble(number.replaceAll("\\(|\\)", ""));
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
		String str = "Variables : "+(isOneVar ? "1" : "2")+"\n";
		for(int i=0; i<5; ++i) {
			List<Pair<Operation, Double>> list = terms.get(i);
			for(Pair<Operation, Double> pair : list) {
				str+=i+" : ["+pair.getFirst()+" "+pair.getSecond()+"] ";
			}
		}
		str+="\nOp : "+Arrays.asList(operations);
		return str;
	}
	
	public boolean isOneVar() {
		if(operations.get(0) == Operation.NONE || 
				operations.get(2) == Operation.NONE) {
			return true;
		}
		return false;
	}
	
	public static float getLoss(Equation y1, Equation y2) {
		float loss1 = Equation.getLossOrderFixed(y1, y2);
		// copy made to compute the alternate loss
		Equation y11 = new Equation(y1);
		exchange(y11.terms);
		exchange(y11.operations);
		float loss2 = Equation.getLossOrderFixed(y11, y2);
		return Math.min(loss1, loss2);
	}
	
	public static <A> void exchange(List<A> l) {
		A tmp = l.get(0);
		l.set(0, l.get(2));
		l.set(2, tmp);
		tmp = l.get(1);
		l.set(1, l.get(3));
		l.set(3, tmp);
	}
	
	public static float getLossOrderFixed(Equation y1, Equation y2) {
		float loss = 0.0f;
		if(y1.isOneVar != y2.isOneVar) loss += 10.0;
		for(int i=0; i<5; i++) {
			List<Pair<Operation, Double>> pairList1 = y1.terms.get(i);
			List<Pair<Operation, Double>> pairList2 = y2.terms.get(i);
			loss += getLossPairLists(pairList1, pairList2);
		}
		for(int i=0; i<4; ++i) {
			if(y1.operations.get(i) != y2.operations.get(i)) {
				loss += 1;
			}
		}
		return loss;
	}
	
	public static float getLossPairLists(List<Pair<Operation, Double>> pairList1,
			List<Pair<Operation, Double>> pairList2) {
		if(pairList1.size() != pairList2.size()) return 4.0f;
		if(pairList1.size() == 0) {
			return 0.0f;
		}
		if(pairList1.size() == 1) {
			float loss = 0.0f;
			Pair<Operation, Double> pair1 = pairList1.get(0);
			Pair<Operation, Double> pair2 = pairList2.get(0);
			if(pair1.getFirst() != pair2.getFirst()) loss += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss += 1.0;
			return loss;
		} 
		if(pairList2.size() == 2) {
			float loss1 = 0.0f, loss2 = 0.0f;
			Pair<Operation, Double> pair1 = pairList1.get(0);
			Pair<Operation, Double> pair2 = pairList2.get(0);
			if(pair1.getFirst() != pair2.getFirst()) loss1 += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss1 += 1.0;
			pair1 = pairList1.get(1);
			pair2 = pairList2.get(1);
			if(pair1.getFirst() != pair2.getFirst()) loss1 += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss1 += 1.0;
			pair1 = pairList1.get(0);
			pair2 = pairList2.get(1);
			if(pair1.getFirst() != pair2.getFirst()) loss2 += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss2 += 1.0;
			pair1 = pairList1.get(1);
			pair2 = pairList2.get(0);
			if(pair1.getFirst() != pair2.getFirst()) loss2 += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss2 += 1.0;
			return Math.min(loss1, loss2);
		}
		System.err.println("Issue in getLossPairLists");
		return 0.0f;
	}
	

}
