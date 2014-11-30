package lbj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import parser.DocReader;
import structure.Clustering;
import structure.Equation;
import structure.Expression;
import structure.Operation;
import structure.SimulProb;
import utils.Params;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class EquationParser implements Parser{

	public List<Equation> equationList;
	public int index;
	public EquationFeatureManager fm;
	
	public EquationParser() {
		fm = new EquationFeatureManager();
	}
	
	public EquationParser(double start, double end) {
		DocReader dr = new DocReader();
		List<SimulProb> simulProbList = null;
		equationList = new ArrayList<Equation>();
		try {
			simulProbList = dr.readSimulProbFromBratDir(
					Params.annotationDir, start, end);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fm = new EquationFeatureManager();
		int count = 0;
		try {
			for(SimulProb simulProb : simulProbList) {
				System.out.println(++count);
				List<Equation> tmpEquationList =
						extractEquations(simulProb);
				for(Equation equation : tmpEquationList) {
					fm.getAllFeatures(equation, simulProb);
				}
				equationList.addAll(tmpEquationList);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		index = 0;	
	}
	
	public List<Equation> extractEquations(SimulProb simulProb) {
		System.out.println(simulProb.index + " : "+simulProb.question);
		Map<String, List<Expression>> termMap = extractTermMap(simulProb);
		System.out.println("Cluster Map :");
		for(String entity : simulProb.clusterMap.keySet()) {
			System.out.println(entity + " : " + Arrays.asList(
					simulProb.clusterMap.get(entity).mentionLocMap.keySet()));
		}
		System.out.println("Term Map");
		for(String term : termMap.keySet()) {
			System.out.println(term + ":" + Arrays.asList(termMap.get(term)));
		}
		List<Equation> candidates = extractCandidateEquationsFromTermMap(
				extractTermMap(simulProb));		
		System.out.println("Positive Candidates");
		for(Equation candidate : candidates) {
			if(candidate.terms.size() == 2) {
				Expression ex = new Expression(null, "ACROSS", Operation.EQ, 
						new HashSet<Expression>(Arrays.asList(
								candidate.terms.get(0), 
								candidate.terms.get(1))));
				if(simulProb.getOperation(ex) == candidate.operations.get(0)) {
					candidate.label = "EQUATION";
					System.out.println("Equation : " + candidate);
				} else {
//					System.out.println("None : " + candidate);
					candidate.label = "NONE";
				}
			}
			if(candidate.terms.size() == 3) {
				Expression ex1 = new Expression(null, "ACROSS", Operation.EQ, 
						new HashSet<Expression>(Arrays.asList(
								candidate.terms.get(0), 
								candidate.terms.get(1))));
				Expression ex2 = new Expression(null, "ACROSS", Operation.EQ, 
						new HashSet<Expression>(Arrays.asList(
								ex1,
								candidate.terms.get(2))));
				if((simulProb.getOperation(ex1) == candidate.operations.get(0)) && 
						(simulProb.getOperation(ex2) == candidate.operations.get(1))) {
					candidate.label = "EQUATION";
					System.out.println("Equation : " + candidate);
				} else {
					candidate.label = "NONE";
//					System.out.println("None : " + simulProb.getOperation(ex1)+ ":"+simulProb.getOperation(ex2)+":"+candidate);
				}
			}
		}
		return candidates;
	}
	
	public List<Equation> extractCandidateEquationsFromTermMap(
			Map<String, List<Expression>> termMap) {
		List<Equation> eqList = new ArrayList<Equation>();
		List<Operation> operations = new ArrayList<Operation>(Arrays.asList(
				Operation.ADD, Operation.SUB, Operation.MUL, Operation.DIV));
		if(termMap.keySet().size() == 2 && termMap.containsKey("E1") 
				&& termMap.containsKey("E2")) {
			for(Expression ex1 : termMap.get("E1")) {
				for(Expression ex2 : termMap.get("E2")) {
					Equation eq = new Equation(
							new ArrayList<Expression>(Arrays.asList(ex1, ex2)), 
							new ArrayList<Operation>(Arrays.asList(Operation.EQ)),
							termMap);
					eqList.add(eq);
				}
			}
		} else if(termMap.keySet().size() == 2) {
			System.out.println("ERROR");
		}
		if(termMap.keySet().size() == 3 && termMap.containsKey("E1") 
				&& termMap.containsKey("E2") && termMap.containsKey("E3")) {
			for(Expression ex1 : termMap.get("E1")) {
				for(Expression ex2 : termMap.get("E2")) {
					Equation eq = new Equation(
							new ArrayList<Expression>(Arrays.asList(ex1, ex2)), 
							new ArrayList<Operation>(Arrays.asList(Operation.EQ)),
							termMap);
					eqList.add(eq);
				}
			}
			for(Expression ex1 : termMap.get("E1")) {
				for(Expression ex2 : termMap.get("E3")) {
					Equation eq = new Equation(
							new ArrayList<Expression>(Arrays.asList(ex1, ex2)), 
							new ArrayList<Operation>(Arrays.asList(Operation.EQ)),
							termMap);
					eqList.add(eq);
				}
			}
			for(Expression ex1 : termMap.get("E2")) {
				for(Expression ex2 : termMap.get("E3")) {
					Equation eq = new Equation(
							new ArrayList<Expression>(Arrays.asList(ex1, ex2)), 
							new ArrayList<Operation>(Arrays.asList(Operation.EQ)),
							termMap);
					eqList.add(eq);
				}
			}
			for(Expression ex1 : termMap.get("E1")) {
				for(Expression ex2 : termMap.get("E2")) {
					for(Expression ex3 : termMap.get("E3")) {
						for(Operation op : operations) {
							Equation eq = new Equation(
									new ArrayList<Expression>(Arrays.asList(
											ex1, ex2, ex3)),
									new ArrayList<Operation>(Arrays.asList(
											op, Operation.EQ)),
									termMap);
							eqList.add(eq);
						}
					}
				}
			}
		}else if(termMap.keySet().size() == 3) {
			System.out.println("ERROR");
		}
		return eqList;
	}
	
	
	public Map<String, List<Expression>> extractTermMap(SimulProb simulProb) {
		Map<String, List<Expression>> termMap = 
				new HashMap<String, List<Expression>>();
		for(Expression expr : simulProb.equations) {
			for(Expression subEx : expr.getAllSubExpressions()) {
				if(subEx.complete == Operation.COMPLETE) {
					if(!termMap.containsKey(subEx.entityName)) {
						termMap.put(subEx.entityName, new ArrayList<Expression>());
					}
					boolean allow = true;
					for(Expression term : termMap.get(subEx.entityName)) {
						if(term.equalsBasedOnLeaves(subEx)) {
							allow = false;
						}
					}
					if(allow) termMap.get(subEx.entityName).add(subEx);
				}
			}
		}
		return termMap;
	}


	public Object next() {
		if (index < equationList.size()) {
			return equationList.get(index++);
		} else {
			return null;
		}
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}
}
