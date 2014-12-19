package structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import utils.Tools;

// Holds canonical equations
// Ax +/- By +/- C = 0
// where A = \product A_i, B = \product B_i and C = \product C_i
// and A_i's, B_i's and C_i's are present in text

public class Equation {
	
	public List<Pair<Operation, Double>> A1, A2, B1, B2, C;
	// Should be of length 5, of which one of third or fifth should be EQ
	public List<Operation> operations; 
	
	public Equation() {
		this.A1 = new ArrayList<>();
		this.A2 = new ArrayList<>();
		this.B1 = new ArrayList<>();
		this.B2 = new ArrayList<>();
		this.C = new ArrayList<>();
		this.operations = new ArrayList<>();
		for(int i=0; i<5; i++) {
			operations.add(Operation.NONE);
		}
	}
	
	public Equation(Equation eq) {
		this.A1 = new ArrayList<>(eq.A1);
		this.A2 = new ArrayList<>(eq.A2);
		this.B1 = new ArrayList<>(eq.B1);
		this.B2 = new ArrayList<>(eq.B2);
		this.C = new ArrayList<>(eq.C);
		this.operations = new ArrayList<>();
		for(int i=0; i<5; i++) {
			operations.add(eq.operations.get(i));
		}
	}
	
	// Takes into consideration that A's and B's can be interchanged
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Equation)) return false;
		Equation eq = (Equation) obj;
		if(Arrays.asList(A1).equals(Arrays.asList(eq.A1)) && 
				Arrays.asList(A2).equals(Arrays.asList(eq.A2)) &&
				Arrays.asList(B1).equals(Arrays.asList(eq.B1)) &&
				Arrays.asList(B2).equals(Arrays.asList(eq.B2)) &&
				Arrays.asList(C).equals(Arrays.asList(eq.C))) {
			if(Arrays.asList(operations).equals(Arrays.asList(eq.operations))) {
				return true;
			}
		}
		if(Arrays.asList(A1).equals(Arrays.asList(eq.B1)) && 
				Arrays.asList(A2).equals(Arrays.asList(eq.B2)) &&
				Arrays.asList(B1).equals(Arrays.asList(eq.A1)) &&
				Arrays.asList(B2).equals(Arrays.asList(eq.A2)) &&
				Arrays.asList(C).equals(Arrays.asList(eq.C))) {
			if(operations.get(0) == eq.operations.get(2) && 
					operations.get(1) == eq.operations.get(3) && 
					operations.get(2) == eq.operations.get(0) && 
					operations.get(3) == eq.operations.get(1) && 
					operations.get(4) == eq.operations.get(4)) {
				return true;
			}
		}
		return false;
	}
	
	public Equation(int index, String eqString) {
		this();
		// For negative problems
		if(eqString.equals("(2.0*V1)-(-8.0)=(-12.0)")) {
			operations.set(0, Operation.ADD);
			operations.set(1, Operation.SUB);
			operations.set(3, Operation.SUB);
			A1.add(new Pair<Operation, Double>(Operation.MUL, 2.0));
			A2.add(new Pair<Operation, Double>(Operation.MUL, -8.0));
			C.add(new Pair<Operation, Double>(Operation.MUL, -12.0));
			return;
		}
		if(eqString.equals("0.833*V1=(-60.0)")) {
			operations.set(0, Operation.ADD);
			operations.set(3, Operation.SUB);
			A1.add(new Pair<Operation, Double>(Operation.MUL, 0.833));
			C.add(new Pair<Operation, Double>(Operation.MUL, -60.0));
			return;
		}
		if(index == 6666 && eqString.equals("V1+V2=(-64.0)")) {
			operations.set(0, Operation.ADD);
			operations.set(2, Operation.ADD);
			operations.set(4, Operation.SUB);
			C.add(new Pair<Operation, Double>(Operation.MUL, -64.0));
			return;
		}

		// For ambiguous matching
		if(index == 6208 && eqString.equals("V1=(2.0*V2)+1.0")) {
			operations.set(0, Operation.ADD);
			operations.set(2, Operation.SUB);
			operations.set(3, Operation.ADD);
			B1.add(new Pair<Operation, Double>(Operation.MUL, 2.0));
			B2.add(new Pair<Operation, Double>(Operation.MUL, 1.0));
			return;
		}
		
		// Replace brackets
		eqString = eqString.replace("(", "");
		eqString = eqString.replace(")", "");
		String strArr[] = eqString.split("(\\+|\\-|=)");
	}
	
	public Operation getOperation(String str, String eqString) {
		int index = eqString.indexOf(str);
		if(index == 0) {
			return Operation.ADD;	
		} else {
			Operation op = Tools.getOperationFromString(""+eqString.charAt(index-1));
			if(op == Operation.EQ) return Operation.SUB;
			return op;
		}
	}
	
	public String toString() {
		String str = "";
		str+="A1 : ";
		for(Pair<Operation, Double> pair : A1) {
			str+="["+pair.getFirst()+" "+pair.getSecond()+"] ";
		}
		str+="\n";
		str+="A2 : ";
		for(Pair<Operation, Double> pair : A2) {
			str+="["+pair.getFirst()+" "+pair.getSecond()+"] ";
		}
		str+="\n";
		str+="B1 : ";
		for(Pair<Operation, Double> pair : B1) {
			str+="["+pair.getFirst()+" "+pair.getSecond()+"] ";
		}
		str+="\n";
		str+="B2 : ";
		for(Pair<Operation, Double> pair : B2) {
			str+="["+pair.getFirst()+" "+pair.getSecond()+"] ";
		}
		str+="\n";
		str+="C : ";
		for(Pair<Operation, Double> pair : C) {
			str+="["+pair.getFirst()+" "+pair.getSecond()+"] ";
		}
		str+="\n";
		str+="Operations : " + Arrays.asList(operations) + "\n";
		return str;
	}
	

}
