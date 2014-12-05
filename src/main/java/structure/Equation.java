package structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import utils.Tools;

// Holds canonical equations
// Ax +/- By +/- C = 0
// where A = \product A_i, B = \product B_i and C = \product C_i
// and A_i's, B_i's and C_i's are present in text

public class Equation {
	
	public List<Pair<Operation, String>> A1, A2, B1, B2, C;
	// Should be of length 5, of which one of third or fifth should be EQ
	List<Operation> operations; 
	
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
	
	public Equation(String eqString, Map<String, QuantState> clusterMap) {
		this();
		eqString = eqString.replace("(", "");
		eqString = eqString.replace(")", "");
		String strArr[] = eqString.split("(\\+|\\-|=)");
		for(String str : strArr) {
			int lastLoc = 0;
			Operation lastOp = Operation.MUL;
			String correctTerm = getTerm(str, clusterMap);
			if(correctTerm == null) System.out.println("EqString : "+eqString);

			if(correctTerm.equals("A1")) operations.set(0, getOperation(str, eqString));
			if(correctTerm.equals("A2")) operations.set(1, getOperation(str, eqString));
			if(correctTerm.equals("B1")) operations.set(2, getOperation(str, eqString));
			if(correctTerm.equals("B2")) operations.set(3, getOperation(str, eqString));
			if(correctTerm.equals("C")) operations.set(4, getOperation(str, eqString));
			
			for(int i = 0; i < str.length(); i++) {
				if(Tools.getOperationFromString(""+str.charAt(i)) != null) {
					String term = (eqString.substring(lastLoc, i));
					if(term.equals("V1") || term.equals("V2")) continue;
					if(correctTerm.equals("A1")) A1.add(
							new Pair<Operation, String>(lastOp, term));
					if(correctTerm.equals("A2")) A2.add(
							new Pair<Operation, String>(lastOp, term));
					if(correctTerm.equals("B1")) B1.add(
							new Pair<Operation, String>(lastOp, term));
					if(correctTerm.equals("B2")) B2.add(
							new Pair<Operation, String>(lastOp, term));
					if(correctTerm.equals("C")) C.add(
							new Pair<Operation, String>(lastOp, term));
					lastLoc = i+1;
					lastOp = Tools.getOperationFromString(""+str.charAt(i));
				}
			}
			String term = (eqString.substring(lastLoc));
			if(term.equals("V1") || term.equals("V2")) continue;
			if(correctTerm.equals("A1")) A1.add(
					new Pair<Operation, String>(lastOp, term));
			if(correctTerm.equals("A2")) A2.add(
					new Pair<Operation, String>(lastOp, term));
			if(correctTerm.equals("B1")) B1.add(
					new Pair<Operation, String>(lastOp, term));
			if(correctTerm.equals("B2")) B2.add(
					new Pair<Operation, String>(lastOp, term));
			if(correctTerm.equals("C")) C.add(
					new Pair<Operation, String>(lastOp, term));		
		}
	}
	
	public String getTerm(String str, Map<String, QuantState> clusterMap) {
		if(str.contains("V1")) return "A1";
		if(str.contains("V2")) return "B1";
		int lastLoc = 0;
		for(int i = 0; i < str.length(); i++) {
			if(Tools.getOperationFromString(""+str.charAt(i)) != null) {
				String term = (str.substring(lastLoc, i));
				int numCandidates = 0;
				String candidate = null;
				for(String key : clusterMap.keySet()) {
					if(clusterMap.get(key).mentionLocMap.containsKey(term)) {
						if(key.equals("E1")) candidate = "A2";
						if(key.equals("E2")) candidate = "B2";
						if(key.equals("E3")) candidate = "C";
						numCandidates++;
					}
				}
				if(numCandidates == 1) {
					return candidate;
				}
				lastLoc = i;
			}
		}
		String term = (str.substring(lastLoc));
		int numCandidates = 0;
		String candidate = null;
		for(String key : clusterMap.keySet()) {
			if(clusterMap.get(key).mentionLocMap.containsKey(term)) {
				if(key.equals("E1")) candidate = "A2";
				if(key.equals("E2")) candidate = "B2";
				if(key.equals("E3")) candidate = "C";
				numCandidates++;
			}
		}
		if(numCandidates == 1) {
			return candidate;
		}
		return null;
	}
	
	public Operation getOperation(String str, String eqString) {
		int index = eqString.indexOf(str);
		if(index == 0) {
			return Operation.ADD;	
		} else {
			return Tools.getOperationFromString(""+eqString.charAt(index-1));
		}
	}
	
	public String toString() {
		String str = "";
		System.out.print("A1 : ");
		for(Pair<Operation, String> pair : A1) {
			System.out.print("["+pair.getFirst()+" "+pair.getSecond()+"] ");
		}
		System.out.println();
		System.out.print("A2 : ");
		for(Pair<Operation, String> pair : A2) {
			System.out.print("["+pair.getFirst()+" "+pair.getSecond()+"] ");
		}
		System.out.println();
		System.out.print("B1 : ");
		for(Pair<Operation, String> pair : B1) {
			System.out.print("["+pair.getFirst()+" "+pair.getSecond()+"] ");
		}
		System.out.println();
		System.out.print("B2 : ");
		for(Pair<Operation, String> pair : B2) {
			System.out.print("["+pair.getFirst()+" "+pair.getSecond()+"] ");
		}
		System.out.println();
		System.out.print("C : ");
		for(Pair<Operation, String> pair : C) {
			System.out.print("["+pair.getFirst()+" "+pair.getSecond()+"] ");
		}
		System.out.println();
		System.out.println("Operations : " + Arrays.asList(operations));
		return str;
	}

}
