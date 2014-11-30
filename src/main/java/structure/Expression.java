package structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;

// Supposed to store : a*x + b*y = c as

//              = 
//     +             c
//  ax     by


public class Expression {
	
	// General equation stuff
	public String varName; // varname can also store string of numbers, null for non-leaves
	public Operation operation; // Denotes the operation (+, -, *, /), null for leaves
	public double operationScore;
	public double equationScore;
	public double noneScore;
	public Set<Expression> operands; // Either two or zero elements
	public Expression parent;
	
	// These are to be filled only when associated with a simul problem
	public String entityName; // E1, E2, E3 or ACROSS
	public Operation complete; // Denotes if the expression is complete for an entity
	public double completeScore;
	public List<String> features;
	
	public Expression(
			String varName, String entityName, Operation op, Set<Expression> exprs) {
		this.varName = varName;
		this.entityName = entityName;
		operation = op;
		operands = exprs;
	}
	
	public Expression(String expr, Expression parent, SimulProb simulProb) {
		this(expr, null);
		populateEntityName(simulProb);
		populateCompleteFromEntity();
	}
	
	public Expression(String expr, Expression parent) {
		// Split at equal sign, assume equal not within brackets
		if(expr == null || expr.trim().equals("")) {
			return;
		}
		String left = null, right = null;
		this.parent = parent;
		if(expr.contains("=")) {
			operation = Operation.EQ;
			complete = Operation.NONE;
			String strArr[] = expr.split("=");
			left = stripExteriorBrackets(strArr[0].trim());
			right = stripExteriorBrackets(strArr[1].trim());
		} else {
			int div = getIndexDiscountingBrackets(expr, "/")<=0?
					expr.length():getIndexDiscountingBrackets(expr, "/");
			int mul = getIndexDiscountingBrackets(expr, "*")<=0?
					expr.length():getIndexDiscountingBrackets(expr, "*");
			int sub = getIndexDiscountingBrackets(expr, "-")<=0?
					expr.length():getIndexDiscountingBrackets(expr, "-");
			int add = getIndexDiscountingBrackets(expr, "+")<=0?
					expr.length():getIndexDiscountingBrackets(expr, "+"); 
			if(Math.min(add, sub) < expr.length()) {
				left = stripExteriorBrackets(expr.substring(
						0, Math.min(add, sub)).trim()).trim();
				right = stripExteriorBrackets(expr.substring(
						Math.min(add, sub)+1).trim()).trim();
				if(add < expr.length()) {
					operation = Operation.ADD;
				} else {
					operation = Operation.SUB;
				}
			} else if(Math.min(mul, div) < expr.length()) {
				left = stripExteriorBrackets(expr.substring(
						0, Math.min(mul, div)).trim()).trim();
				right = stripExteriorBrackets(expr.substring(
						Math.min(mul, div)+1).trim()).trim();
				if(mul < expr.length()) {
					operation = Operation.MUL;
				} else {
					operation = Operation.DIV;
				}
			} else {
				if(expr.equals("V1") || expr.equals("V2")){
					varName = expr;
				} else if(NumberUtils.isNumber(expr)) {
					varName = (Double.parseDouble(expr) + "");
				}
				operation = Operation.NONE;
			}
		}
		if(left!=null && right != null) {
			operands = new HashSet<Expression>();
			operands.add(new Expression(left, this));
			operands.add(new Expression(right, this));
		}
	}
	
	// Finds matching parenthesis in a string, assumption is
	// expression has valid parenthesization, returns -1 if
	// input was not bracket, or matching bracket not found
	public int findMatchingBracket(String str, int pos) {
		if(str==null) {
			return -1;
		}
		if(pos<0  || pos>=str.length()) {
			return -1;
		}
		if(str.charAt(pos)!='(' && str.charAt(pos)!=')') {
			return -1;
		}
		if(str.charAt(pos)=='(') {
			int excess = 1;
			for(int i=pos+1; i<str.length(); i++) {
				if(str.charAt(i) == ')') {
					excess--;
					if(excess==0) {
						return i;
					}
				}
				if(str.charAt(i) == '(') {
					excess++;
				}
			}
		}
		if(str.charAt(pos)==')') {
			int excess = 1;
			for(int i=pos-1; i>=0; i--) {
				if(str.charAt(i) == ')') {
					excess++;
				}
				if(str.charAt(i) == '(') {
					excess--;
					if(excess==0) {
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	public String stripExteriorBrackets(String str) {
		if(str==null) {
			return null;
		}
		int matchingBracketForFirstBracket = findMatchingBracket(str, 0);
		if(matchingBracketForFirstBracket == -1) {
			return str;
		}
		if(matchingBracketForFirstBracket == str.length()-1) {
			return str.substring(1,str.length()-1);
		}
		return str;
	}
	
	
	// returns highest level list of brackets, no composition detected
	// Assumes str has correct parenthesis
	public List<IntPair> getBracketedSpans(String str) {
		List<IntPair> spans = new ArrayList<IntPair>();
		int index1, index2 = -1;
		while(true) {
			index1 = str.indexOf('(',index2+1);
			if(index1 == -1) {
				break;
			}
			index2 = findMatchingBracket(str, index1);
			spans.add(new IntPair(index1, index2));
		}
		return spans;
	}
	
	// Returns index of key, not within any bracket
	public int getIndexDiscountingBrackets(String str, String key) {
		int index  = -1; 
		boolean flag;
		List<IntPair> brackets = getBracketedSpans(str);
		while(true) {
			index = str.indexOf(key, index+1);
			if(index == -1) {
				break;
			}
			flag = true;
			for(IntPair bracket : brackets) {
				if(bracket.getFirst()<index && bracket.getSecond()>index) {
					flag = false;
				}
			}
			if(flag) {
				return index;
			}
		}
		return -1;
	}
	
	@Override
	public String toString() {
		if(operands == null || operands.size() == 0) {
			return  " [" + varName + " "+complete+" "+ entityName+"] ";
		} else {
			String str = "(";
			int i = 0;
			assert operands.size() == 2;
			for(Expression expr : operands) {
				str += expr;
				if(i == 0) {
					str += " [" + operation + " "+complete+" "+ entityName+"] ";
				}
				i++;
			}
			return str+")";
		}
	}
	
	public List<Expression> getYield() {
		List<Expression> list = new ArrayList<Expression>();
		if(operands == null || operands.size() == 0) {
			list.add(this);
		} else {
			assert operands.size() == 2;
			for(Expression expr : operands) {
				list.addAll(expr.getYield());
			}
		}
		return list;
	}
	
	public List<String> getYieldInString() {
		List<Expression> yield = getYield();
		List<String> list = new ArrayList<String>();
		for(Expression expr : yield) {
			list.add(expr.varName);
		}
		return list;
	}
	
	public List<Expression> getAllSubExpressions() {
		List<Expression> subExprs = new ArrayList<Expression>();
		subExprs.add(this);
		if(operands != null) {
			for(Expression expr : operands) {
				subExprs.addAll(expr.getAllSubExpressions());
			}
		}
		return subExprs;
	}
	
	public void populateEntityName(SimulProb simulProb) {
		if(simulProb == null) {
			return;
		}
//		System.out.println(simulProb.index+":"+this);
		// Populate leaves first, only when you are sure
		for(Expression leaf : getYield()) {
			List<String> candidates = new ArrayList<String>();
			for(String entity : simulProb.clusterMap.keySet()) {
				if(simulProb.clusterMap.get(entity).mentionLocMap.
						keySet().contains(leaf.varName)) {
					candidates.add(entity);
				}		
			}
//			System.out.println(leaf.varName+" : "+Arrays.asList(candidates));
			if(candidates.size() == 1) {
				leaf.entityName = candidates.get(0);
			}
		}
//		System.out.println(simulProb.index+":"+this);
		// Populate entity for non-leaves
		for(Expression expr : getAllSubExpressions()) {
			if(expr.entityName != null) {
				continue;
			}
			if(expr.operands == null || expr.operands.size() == 0) {
				continue;
			}
			List<String> leaves = expr.getYieldInString();
			for(String entity : simulProb.clusterMap.keySet()) {
				boolean foundEntity = true;
				for(String leaf : leaves) {
					if(!simulProb.clusterMap.get(entity).mentionLocMap.
							keySet().contains(leaf)) {
						foundEntity = false;
						break;
					}
				}
				if(foundEntity) {
					expr.entityName = entity;
					break;
				}
			}
			if(expr.entityName == null) {
				expr.entityName = "ACROSS";
			}
		}
//		System.out.println(simulProb.index+":"+this);
		// Populate leaves for which there was confusion
		for(Expression leaf : getYield()) {
			if(leaf.entityName != null || leaf.parent == null) {
				continue;
			}
			leaf.entityName = leaf.parent.entityName;
		}
//		System.out.println(simulProb.index+":"+this);
	}
	
	// Assumes entity populated
	public void populateCompleteFromEntity() {
		// Populate complete
		for(Expression expr : getAllSubExpressions()) {
			if(expr.operands != null) {
				for(Expression ex : expr.operands) {
					if(!ex.entityName.equals(expr.entityName)) {
						ex.complete = Operation.COMPLETE;
					}
				}
				for(Expression ex : expr.operands) {
					if(ex.complete == null) {
						ex.complete = Operation.NONE;
					}
				}
			}
		}
	}
		
	public static void main(String args[]) {
		Expression eq = new Expression("(5-z)*x+6*y = 7", null);
		System.out.println(eq.stripExteriorBrackets("(5-z)"));
	}

	// Assumes entityName has been populated
	public Set<IntPair> getAllMentionLocs(SimulProb simulProb) {
		List<Expression> leaves = getYield();
		Set<IntPair> ipSet = new HashSet<IntPair>();
		for(Expression leaf : leaves) {
			if(simulProb.clusterMap.keySet().contains(leaf.entityName) && 
					simulProb.clusterMap.get(leaf.entityName).mentionLocMap.
					keySet().contains(leaf.varName)) {
				ipSet.addAll(simulProb.clusterMap.get(leaf.entityName).
						mentionLocMap.get(leaf.varName));
			}
		}
		return ipSet;
	}

	// Equality check
	// Consider operation, but not complete
	// Does not consider entityName
	// To be used for final equation checking
	public boolean equalsEquation(Object obj) {
		if(!(obj instanceof Expression)) {
			return false;
		}
		Expression expr = (Expression)obj;
		if(varName == null && expr.varName != null) {
			return false;
		}
		if(varName != null && !varName.equals(expr.varName)) {
			return false;
		}
		if(varName == null) {
			if(operands.size() != expr.operands.size()) {
				return false;
			}
			List<Expression> exprList = new ArrayList<Expression>();
			for(Expression ex : operands) {
				exprList.add(ex); 
			}
			for(Expression ex : expr.operands) {
				exprList.add(ex); 
			}
			return (exprList.get(0).equalsEquation(exprList.get(2)) && 
					exprList.get(1).equalsEquation(exprList.get(3)) ) || 
					(exprList.get(0).equalsEquation(exprList.get(3)) && 
							exprList.get(1).equalsEquation(exprList.get(2)));
		}
		return true;
	}
	
	// Equality check
	// Does not consider operation, complete
	// Considers entityName, varName
	public boolean equalsWithoutOperation(Object obj) {
		if(!(obj instanceof Expression)) {
			return false;
		}
		Expression expr = (Expression)obj;
		if(varName == null && expr.varName != null) {
			return false;
		}
		if(entityName == null && expr.entityName != null) {
			return false;
		}
		if(varName != null && !varName.equals(expr.varName)) {
			return false;
		}
		if(entityName != null && !entityName .equals(expr.entityName )) {
			return false;
		}
		if(varName == null) {
			if(operands.size() != expr.operands.size()) {
				return false;
			}
			List<Expression> exprList = new ArrayList<Expression>();
			for(Expression ex : operands) {
				exprList.add(ex); 
			}
			for(Expression ex : expr.operands) {
				exprList.add(ex); 
			}
			return (exprList.get(0).equalsWithoutOperation(exprList.get(2)) && 
					exprList.get(1).equalsWithoutOperation(exprList.get(3)) ) || 
					(exprList.get(0).equalsWithoutOperation(exprList.get(3)) && 
							exprList.get(1).equalsWithoutOperation(exprList.get(2)));
		}
		return true;
	}
	
	// Equality check
	// Does not consider operation, complete
	// Considers entityName, varName
	public boolean equalsBasedOnLeaves(Object obj) {
		if(!(obj instanceof Expression)) {
			return false;
		}
		Expression expr = (Expression)obj;
		List<Expression> exprList1 = getYield();
		List<Expression> exprList2 = expr.getYield();
		if(exprList1.size() != exprList2.size()) {
			return false;
		}
		for(Expression expr1 : exprList1) {
			boolean found = false;
//			System.out.println("Expr1: "+expr1.varName + " " + expr1.entityName);
			for(Expression expr2 : exprList2) {
//				System.out.println("Expr2: " + expr2.varName + " " + expr2.entityName);
				if(expr1.varName.equals(expr2.varName) && 
						expr1.entityName.equals(expr2.entityName)) {
					found = true;
				}
			}
			if(!found) {
				return false;
			}
		}
		return true;
	}
	
	public Operation getComplete(Expression expr) {
		List<Expression> exprList = getAllSubExpressions();
		for(Expression ex : exprList) {
			if(ex.equalsBasedOnLeaves(expr)) {
				return ex.complete;
			}
		}
		return Operation.NONE;
	}
	
	public Operation getOperation(Expression expr) {
		if(expr.operands == null || expr.operands.size() == 0) {
			return Operation.NONE;
		}
		List<Expression> operands1 = new ArrayList<Expression>();
		for(Expression ex : expr.operands) {
			operands1.add(ex);
		}
		for(Expression ex : getAllSubExpressions()) {
			if(ex.operands == null || ex.operands.size() == 0) {
				continue;
			}
			List<Expression> operands2 = new ArrayList<Expression>();
			for(Expression exex : ex.operands) {
				operands2.add(exex);
			}
			if(operands1.get(0).equalsBasedOnLeaves(operands2.get(0))
					&& operands1.get(1).equalsBasedOnLeaves(operands2.get(1))) {
				return ex.operation;
			}
			if(operands1.get(0).equalsBasedOnLeaves(operands2.get(1))
					&& operands1.get(1).equalsBasedOnLeaves(operands2.get(0))) {
				return ex.operation;
			}
		}
		return Operation.NONE;
	}
	
	public Set<String> getEntitySet() {
		Set<String> entitySet = new HashSet<String>();
		for(Expression expr : getYield()) {
			entitySet.add(expr.entityName);
		}
		return entitySet;
	}
	
	public Set<Expression> getSiblings() {
		if(parent != null) {
			return parent.operands;
		}
		return null;
	}
	
	public void setEqationScore() {
		double total = 0.0;
		double score = 0.0;
		for(Expression ex : getAllSubExpressions()) {
			if(ex.operands == null || ex.operands.size() == 0) {
				continue;
			}
			score += Tools.sigmoid(ex.operationScore);
			total = total + 1;
		}
		if(total > 0) {
			score = score / total;
		}
		equationScore = score;
	}
}
