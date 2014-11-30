package lbj;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structure.QuantState;
import structure.Expression;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class ExpressionParser implements Parser {
	public List<Expression> exprList;
	public int index;
	public ExpressionFeatureManager fm;

	public ExpressionParser() {
		fm = new ExpressionFeatureManager();
	}

	public List<Expression> extractAllWithinEntityExpr(SimulProb simulProb) {
		List<Expression> nodeList = new ArrayList<Expression>();
		for (String entity : simulProb.clusterMap.keySet()) {
			nodeList.addAll(extractAllWithinEntityExpr(simulProb, entity));
		}
		return nodeList;
	}
	
	// Create nodes from the simulProb belonging to entity
	public List<Expression> extractAllWithinEntityExpr(
			SimulProb simulProb, String entityName) {
		List<Expression> nodeListForSingleEntity = new ArrayList<Expression>();
		QuantState cluster = simulProb.clusterMap.get(entityName);
		// Add singleton nodes
		for (String mention : cluster.mentionLocMap.keySet()) {
			nodeListForSingleEntity
					.add(new Expression(mention, entityName, null, null)); 
		}
		int prevLen = 0, len = 0, newAdded;
		while (true) {
			newAdded = 0;
			prevLen = len;
			len = nodeListForSingleEntity.size();
			for (int i = 0; i < len; ++i) {
				for (int j = i + 1; j < len; ++j) {
					if (j >= prevLen
							&& isCombinable(
									nodeListForSingleEntity.get(i),
									nodeListForSingleEntity.get(j))) {
						Set<Expression> ops = new HashSet<Expression>();
						ops.add(nodeListForSingleEntity.get(i));
						ops.add(nodeListForSingleEntity.get(j));
						Expression newExpr = new Expression(null, entityName, null, ops);
						// Check that we are not creating same leaf sets
						if(!hasSameLeafSet(nodeListForSingleEntity, newExpr)) {
							nodeListForSingleEntity.add(newExpr);
							newAdded++;
						}
					}
				}
			}
			if (newAdded == 0) {
				break;
			}
		}
		return nodeListForSingleEntity;
	}
	
	// Assumes input to have same entity expressions
	// Returns expressions made from sub expressions from different entities
	public List<Expression> extractAllAcrossEntityPairExpr(
			List<Expression> sameEntityExprList) {
		List<Expression> nodeList = new ArrayList<Expression>();
		nodeList.addAll(sameEntityExprList);
		int newAdded, len;
		while(true) {
			newAdded = 0;
			len = nodeList.size();
			for(int i = 0; i < len; ++i) {
				for(int j = i + 1; j < len; ++j) {
					boolean allowed = true;
					for(String entity : nodeList.get(i).getEntitySet()) {
						if(nodeList.get(j).getEntitySet().contains(entity)) {
							allowed = false;
							break;
						}
					}
					if(!allowed) {
						continue;
					}
					Set<Expression> ops = new HashSet<Expression>();
					ops.add(nodeList.get(i));
					ops.add(nodeList.get(j));
					Expression newExpr = new Expression(null, "ACROSS", null, ops);
					if(hasSameLeafSetAndPartition(nodeList, newExpr)) {
						continue;
					}
					nodeList.add(newExpr);
					newAdded++;
				}
			}
			if(newAdded == 0) {
				break;
			}
		}
		// Now remove the input
		List<Expression> newExprList = new ArrayList<Expression>();
		for(int i = sameEntityExprList.size(); i < nodeList.size(); i++) {
			newExprList.add(nodeList.get(i));
		}
		return newExprList;
	}
	
	
	public List<Expression> extractAllWithinEntityPairExpr(SimulProb simulProb) {
		List<Expression> nodeList = new ArrayList<Expression>();
		for (String entity : simulProb.clusterMap.keySet()) {
			nodeList.addAll(extractAllWithinEntityPairExpr(simulProb, entity));
		}
		return nodeList;
	}
	
	// Create pair nodes from the simulProb belonging to entity
	public List<Expression> extractAllWithinEntityPairExpr(
			SimulProb simulProb, String entityName) {
		List<Expression> nodeListForSingleEntity = extractAllWithinEntityExpr(
				simulProb, entityName);
		List<Expression> pairListForSingleEntity = new ArrayList<Expression>();
		for(int i = 0; i < nodeListForSingleEntity.size(); ++i) {
			for(int j = i+1; j < nodeListForSingleEntity.size(); ++j) {
				if(!isCombinable(nodeListForSingleEntity.get(i), 
						nodeListForSingleEntity.get(j))) {
					continue;
				}
				Set<Expression> ops = new HashSet<Expression>();
				ops.add(nodeListForSingleEntity.get(i));
				ops.add(nodeListForSingleEntity.get(j));
				Expression newExpr = new Expression(null, entityName, null, ops);
				if(hasSameLeafSetAndPartition(pairListForSingleEntity, newExpr)) {
					continue;
				}
				pairListForSingleEntity.add(newExpr);
			}
		}
		return pairListForSingleEntity;
	}
	
	// Returns true if two nodes are combinable, as in they both don't
	// contain the same segments
	public boolean isCombinable(Expression expr1, Expression expr2) {
		// System.out.println("Comparing : "+node1+" "+node2);
		List<Expression> leaves1 = expr1.getYield();
		List<Expression> leaves2 = expr2.getYield();
		for (Expression e1 : leaves1) {
			for (Expression e2 : leaves2) {
				if (e1.equalsBasedOnLeaves(e2)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean hasSameLeafSet(List<Expression> exprSet, Expression key) {
		for(Expression expr : exprSet) {
			if(expr.equalsBasedOnLeaves(key)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasSameLeafSetAndPartition(
			List<Expression> exprSet, Expression key) {
		List<Expression> keyList = new ArrayList<Expression>();
		for(Expression ex : key.operands) {
			keyList.add(ex);
		}
		List<Expression> exprList = new ArrayList<Expression>();
		for(Expression expr : exprSet) {
			exprList.clear();
			if(expr.equalsBasedOnLeaves(key)) {
				for(Expression ex : expr.operands) {
					exprList.add(ex);
				}
				if(keyList.size() == 2 && exprList.size() == 2) {
					if(keyList.get(0).equals(exprList.get(0)) && 
							keyList.get(0).equals(exprList.get(0))) {
						return true;
					}
					if(keyList.get(0).equalsBasedOnLeaves(exprList.get(1)) && 
							keyList.get(1).equalsBasedOnLeaves(exprList.get(0))) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// Extract features and then send it
	// To not explode space requirements, clear features in lbj code
	public Object next() {
		if (index < exprList.size()) {
			return exprList.get(index++);
		} else {
			return null;
		}
	}

	public void reset() {
		// TODO Auto-generated method stub
		index = 0;
	}

	public void close() {
		// TODO Auto-generated method stub

	}

}
