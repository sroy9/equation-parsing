package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import structure.Operation;
import structure.Span;
import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;

public class Tools {
	
	public static NewCachingCurator curator;
	public static Quantifier quantifier;
	
	static {
		try {
			curator = new NewCachingCurator(
					"trollope.cs.illinois.edu", 
					9010, 
					Params.cacheLoc, 
					null);
			quantifier = new Quantifier();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static double sigmoid(double x) {
		return 1.0/(1+Math.pow(Math.E, -x))*2.0-1.0;
	}
	
	public static boolean doesIntersect(IntPair ip1, IntPair ip2) {
		if(ip1.getFirst() <= ip2.getFirst() && ip2.getFirst() < ip1.getSecond()) {
			return true;
		}
		if(ip2.getFirst() <= ip1.getFirst() && ip1.getFirst() < ip2.getSecond()) {
			return true;
		}
		return false;
	}
	
	public static Operation getOperationFromString(String op) {
		if(op.equals("ADD") || op.equals("+")) return Operation.ADD;
		if(op.equals("SUB") || op.equals("-")) return Operation.SUB;
		if(op.equals("MUL") || op.equals("*")) return Operation.MUL;
		if(op.equals("DIV") || op.equals("/")) return Operation.DIV;
		if(op.equals("EQ") || op.equals("=")) return Operation.EQ;
		return null;
	}
	
	public static boolean safeEquals(Double d1, Double d2) {
		if(d1 == null || d2 == null) {
			return false;
		}
		if(d1 > d2 - 0.0001 && d1 < d2 + 0.0001) {
			return true;
		}
		return false;
	}
}
