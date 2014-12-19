package structure;

import java.util.ArrayList;
import java.util.List;

import latentsvm.Lattice;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;

public class EquationSolver {

	public static List<Double> solve (Lattice lattice) {
		boolean isOneVar = true;
		for(Operation op : lattice.equations.get(1).operations) {
			if(op != Operation.NONE) {
				isOneVar = false;
				break;
			}
		}
		List<Double> A1 = new ArrayList<>();
		List<Double> A2 = new ArrayList<>();
		List<Double> B1 = new ArrayList<>();
		List<Double> B2 = new ArrayList<>();
		List<Double> C = new ArrayList<>();
		for(int i=0; i<lattice.equations.size(); ++i) {
			Equation eq = lattice.equations.get(i);
			if(eq.operations.get(0) == Operation.NONE) {
				A1.add(0.0);
			} else {
				double a = 1.0;
				for(Pair<Operation, Double> pair : eq.A1) {
					if(pair.getFirst() == Operation.MUL) {
						a*=pair.getSecond();
					} else {
						a /= pair.getSecond();
					}
				}
				A1.add(a);
			}
			if(eq.operations.get(1) == Operation.NONE) {
				A2.add(0.0);
			} else {
				double a = 1.0;
				for(Pair<Operation, Double> pair : eq.A2) {
					if(pair.getFirst() == Operation.MUL) {
						a*=pair.getSecond();
					} else {
						a /= pair.getSecond();
					}
				}
				A2.add(a);
			}
			if(eq.operations.get(2) == Operation.NONE) {
				B1.add(0.0);
			} else {
				double a = 1.0;
				for(Pair<Operation, Double> pair : eq.B1) {
					if(pair.getFirst() == Operation.MUL) {
						a*=pair.getSecond();
					} else {
						a /= pair.getSecond();
					}
				}
				B1.add(a);
			}
			if(eq.operations.get(3) == Operation.NONE) {
				B2.add(0.0);
			} else {
				double a = 1.0;
				for(Pair<Operation, Double> pair : eq.B2) {
					if(pair.getFirst() == Operation.MUL) {
						a*=pair.getSecond();
					} else {
						a /= pair.getSecond();
					}
				}
				B2.add(a);
			}
			if(eq.operations.get(4) == Operation.NONE) {
				C.add(0.0);
			} else {
				double a = 1.0;
				for(Pair<Operation, Double> pair : eq.C) {
					if(pair.getFirst() == Operation.MUL) {
						a *= pair.getSecond();
					} else {
						a /= pair.getSecond();
					}
				}
				C.add(a);
			}

			if(eq.operations.get(2) == Operation.DIV) {
				B1.set(i, B1.get(i)*C.get(i));
				B2.set(i, B2.get(i)*C.get(i));
			}

			if(eq.operations.get(0) == Operation.DIV) {
				A1.set(i, A1.get(i)*C.get(i));
				A2.set(i, A2.get(i)*C.get(i));
			}
			
			if(eq.operations.get(0) == Operation.SUB) A1.set(i, A1.get(i)*-1);
			if(eq.operations.get(1) == Operation.SUB 
					&& eq.operations.get(0)==Operation.ADD) {
				A2.set(i, A2.get(i)*-1);
			}
			if(eq.operations.get(0) == Operation.SUB 
					&& eq.operations.get(1)==Operation.ADD) {
				A2.set(i, A2.get(i)*-1);
			}
			if(eq.operations.get(2) == Operation.SUB) B1.set(i, B1.get(i)*-1);
			if(eq.operations.get(3) == Operation.SUB 
					&& eq.operations.get(2)!=Operation.SUB) {
				B2.set(i, B2.get(i)*-1);
			}
			if(eq.operations.get(2) == Operation.SUB 
					&& eq.operations.get(3)!=Operation.SUB) {
				B2.set(i, B2.get(i)*-1);
			}
			if(eq.operations.get(4) == Operation.SUB) C.set(i, C.get(i)*-1);
				
		}
		List<Double> solutions = new ArrayList<>();
		if(isOneVar) {
			if(Tools.safeEquals(A1.get(0)+B1.get(0), 0.0)) return null;
			solutions.add(-(A2.get(0)+B2.get(0)+C.get(0))/(A1.get(0)+B1.get(0)));
			return solutions;
		} else {
			Double a1 = A1.get(0), a2 = A1.get(1), b1 = B1.get(0), b2 = B1.get(1),
					c1 = A2.get(0)+B2.get(0)+C.get(0),
					c2 = A2.get(1)+B2.get(1)+C.get(1);
			if(Tools.safeEquals(a2*b1-a1*b2, 0.0)) return null;
			if(Tools.safeEquals(b2*a1-b1*a2, 0.0)) return null;
			solutions.add(-(a2*c1-a1*c2)/(a2*b1-a1*b2));
			solutions.add(-(b2*c1-b1*c2)/(b2*a1-b1*a2));
			return solutions;
		}		
	}
}
