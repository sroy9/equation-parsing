package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Equation;
import structure.KnowledgeBase;
import structure.Node;
import structure.PairComparator;
import structure.Trigger;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

class Expr {
	public double score;
	public String label;
	public IntPair span;
	public List<IntPair> divisions;
	public String eqString;
	
	public Expr() {
		divisions = new ArrayList<IntPair>();
		eqString = "";
	}
	
	@Override
	public String toString() {
		return label +" "+span+" "+divisions;
	}
	
	public Expr(Expr other) {
		this.score = other.score;
		this.label = other.label;
		this.span = other.span;
		this.divisions = new ArrayList<IntPair>();
		this.divisions.addAll(other.divisions);
	}
}

public class JointInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private JointFeatGen featGen;
	public List<List<Equation>> templates;

	public JointInfSolver(JointFeatGen featGen, List<List<Equation>> templates) 
			throws Exception {
		this.featGen = featGen;
		this.templates = templates;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		JointY r1 = (JointY) arg1;
		JointY r2 = (JointY) arg2;
		return JointY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		JointX prob = (JointX) x;
		JointY gold = (JointY) goldStructure;
		JointY pred = new JointY();
		PairComparator<Template> jointPairComparator = 
				new PairComparator<Template>() {};
		MinMaxPriorityQueue<Pair<Template, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(jointPairComparator)
				.maximumSize(200).create();
		MinMaxPriorityQueue<Pair<Template, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(jointPairComparator)
				.maximumSize(200).create();
		
		// Get best equation trees
		List<String> eqStrings = new ArrayList<>();
		for(IntPair eqSpan : prob.eqSpans) {
			Pair<String, List<Node>> pair = getBottomUpBestParse(prob, eqSpan, wv);
			eqStrings.add(pair.getFirst());
			pred.nodes.addAll(pair.getSecond());
		}
		
		// Extract grafted templates
		List<Template> relevantTemplates = extractGraftedTemplates(
				prob, templates, eqStrings); 
		int maxNumSlots = 0;
		for(Template template : relevantTemplates) {
			if(template.slots.size() > maxNumSlots) {
				maxNumSlots = template.slots.size();
			}
		}
		
		// Fill up remaining slots
		for(int i=0; i<maxNumSlots; ++i) {
			for(Pair<Template, Double> pair : beam1) {
				Template y = pair.getFirst();
				if(pair.getFirst().slots.size() >= i) {
					beam2.add(pair);
				} else {
					for(int j=0; j<prob.quantities.size(); ++j) {
						Template yNew = new Template(y);
						yNew.equations.get(y.slots.get(i).i).
						terms.get(y.slots.get(i).j).get(y.slots.get(i).k).setSecond(
								Tools.getValue(prob.quantities.get(j)));
						beam2.add(new Pair<Template, Double>(yNew, pair.getSecond() + 
								wv.dotProduct(featGen.getAlignmentFeatureVector(
										prob, yNew, i))));
					}
				}
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		pred.equations = beam1.element().getFirst().equations;
		return pred;
	}
	
	public static List<List<Equation>> extractTemplates(SLProblem slProb) {
		List<List<Equation>> templates = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			JointY gold = new JointY((JointY) struct);
			for(Equation eq1 : gold.equations) {
				for(int j=0; j<5; ++j) {
					for(int k=0; k<eq1.terms.get(j).size(); ++k) {
						eq1.terms.get(j).get(k).setSecond(null);
					}
				}
			}
			boolean alreadyPresent = false;
			for(int i=0; i< templates.size(); ++i) { 
				JointY y = new JointY();
				y.equations = templates.get(i); 
				if(JointY.getEquationLoss(gold, y) < 0.0001) {
					alreadyPresent = true;
					break;
				}
			}
			if(!alreadyPresent) {
				templates.add(gold.equations);
			}
		}
		System.out.println("Number of templates : "+templates.size());
		return templates;
	}
	
	public static List<Template> extractGraftedTemplates(
			JointX x, List<List<Equation>> templates, List<String> eqStrings) {
		List<Template> relevantTemplates = new ArrayList<>();
		List<Equation> mathEquations = new ArrayList<>();
		for(String eqString : eqStrings) {
			int count = 0;
			String newStr = "";
			for(int i=0; i<eqString.length(); ++i) {
				if(eqString.charAt(i) == 'V') {
					count++;
					newStr += "V"+count;
				} else {
					newStr += eqString.charAt(i);
				}
			}
			if(!newStr.contains("=")) {
				count++;
				newStr = newStr+"=V"+count;
			}
			mathEquations.add(new Equation(0, eqString));
 		}
		for(List<Equation> template : templates) {
			List<Equation> graft = extractedGraftedTemplate(
					template, mathEquations);
			if(graft != null) {
				relevantTemplates.add(new Template(graft));
			}
		}
		return relevantTemplates;
	}
	
	// Greedy matching should work
	public static List<Equation> extractedGraftedTemplate(
			List<Equation> template, List<Equation> mathEquations) {
		List<Equation> graft = new ArrayList<>();
		for(Equation eq : template) {
			graft.add(new Equation(eq));
		}
		List<IntPair> match = new ArrayList<>();
		boolean allFound = true;
		for(Equation eq : mathEquations) {
			boolean found = false;
			Equation eq1 = new Equation(eq);
			for(int j=0; j<5; ++j) {
				for(int k=0; k<eq1.terms.get(j).size(); ++k) {
					eq1.terms.get(j).get(k).setSecond(null);
				}
			}
			for(int i=0; i<template.size(); ++i) {
				if(Equation.getLoss(template.get(i), eq1) < 0.01 && 
						!match.contains(new IntPair(i, 2))) {
					graft.set(i, eq);
					match.add(new IntPair(i, 2));
					found = true;
					break;
				}
			}
			if(found) continue;
			for(int i=0; i<template.size(); ++i) {
				if(partialEquationMatch(template.get(i), eq1, 0) && 
						!match.contains(new IntPair(i, 0))) {
					graft.get(i).terms.set(0, eq1.terms.get(0));
					graft.get(i).terms.set(1, eq1.terms.get(1));
					graft.get(i).operations.set(0, eq1.operations.get(0));
					graft.get(i).operations.set(1, eq1.operations.get(1));
					match.add(new IntPair(i, 0));
					found = true;
					break;
				}
				if(partialEquationMatch(template.get(i), eq1, 1) && 
						!match.contains(new IntPair(i, 1))) {
					graft.get(i).terms.set(2, eq1.terms.get(0));
					graft.get(i).terms.set(3, eq1.terms.get(1));
					graft.get(i).operations.set(2, eq1.operations.get(0));
					graft.get(i).operations.set(3, eq1.operations.get(1));
					match.add(new IntPair(i, 1));
					found = true;
					break;
				}
			}
			if(!found) {
				allFound = false;
				break;
			}
		}
		if(allFound) {
			return graft;
		}
		return null;
	}
	
	public static boolean partialEquationMatch(
			Equation template, Equation eq, int index) {
		if(template.terms.get(4).size() > 0) return false;
		if(Equation.getLossPairLists(template.terms.get(2*index), eq.terms.get(0)) < 0.01) {
			if(Equation.getLossPairLists(template.terms.get(2*index+1), eq.terms.get(1)) < 0.01) {
				if(template.operations.get(2*index) == eq.operations.get(0) &&
						template.operations.get(2*index+1) == eq.operations.get(1)) {
					return true;		
				}
			}
		}
		return false;
	}
	
	public List<Equation> enumerateEquations(
			Set<Double> availableNumbers, Equation seed) {
		List<Equation> list1 = new ArrayList<>();
		list1.add(seed);
		List<Equation> list2 = new ArrayList<>();
		for(IntPair slot : seed.slots) {
			for(Equation y1 : list1) {
				for(Double d : availableNumbers) {
					Equation y = new Equation(y1);
					y.terms.get(slot.getFirst()).get(
							slot.getSecond()).setSecond(d);
					list2.add(y);
				}
			}
			list1.clear();
			list1.addAll(list2);
			list2.clear();
		}
		// Ensure that same number not used twice
		for(Equation y : list1) {
			boolean allow = true;
			for(int i=0; i<seed.slots.size(); ++i) {
				IntPair slot1 = seed.slots.get(i);
				for(int j=i+1; j<seed.slots.size(); ++j) {
					IntPair slot2 = seed.slots.get(j);
					if(Tools.safeEquals(y.terms.get(slot1.getFirst())
							.get(slot1.getSecond()).getSecond(), 
							y.terms.get(slot2.getFirst())
							.get(slot2.getSecond()).getSecond())) {
						allow = false;
						break;
					}
				}
				if(!allow) break;
			}
			if(allow) list2.add(y);
		}
		return list2;
	}
	
	public Pair<String, List<Node>> getBottomUpBestParse(
			JointX x, IntPair span, WeightVector wv) {
		
		List<Node> nodes = new ArrayList<>();
		List<String> labels = null;
		int n = span.getSecond() - span.getFirst();
		Expr dpMat[][] = new Expr[n+1][n+1];
		List<Trigger> triggers = new ArrayList<>();
		for(int i=span.getFirst(); i<span.getSecond(); ++i) {
			triggers.add(x.triggers.get(i));
		}
		
		for(int j=1; j<=n; ++j) {
			for(int i=j-1; i>=0; --i) {
				if(i+1 == j && triggers.get(i).label.equals("NUMBER")) {
					labels = Arrays.asList("EXPR", "ADD", "SUB", "MUL", "DIV");
				} else if(i+1 == j && triggers.get(i).label.equals("OP")) {
					labels = Arrays.asList("OP", "ADD", "SUB", "DIV");
				} else {
					labels = Arrays.asList("EQ", "ADD", "SUB", "MUL", "DIV");
				}
				double bestScore = -Double.MAX_VALUE;
				List<IntPair> bestDivision = null;
				String bestLabel = null;
				double score;
				for(String label : labels) {
					for(List<IntPair> division : enumerateDivisions(x, i, j)) { 
						score = 1.0*wv.dotProduct(featGen.getExpressionFeatureVector(
								x, i, j, division, label));
						for(IntPair ip : division) {
							score += dpMat[ip.getFirst()][ip.getSecond()].score;
						}
						if(score > bestScore) {
							bestScore = score;
							bestLabel = label;
							bestDivision = division;
						}
					}
				}
				dpMat[i][j] = new Expr();
				dpMat[i][j].score = bestScore;
				dpMat[i][j].label = bestLabel;
				dpMat[i][j].span = new IntPair(i, j);
				dpMat[i][j].divisions = bestDivision;
				if(i+1 == j) {
					if(bestLabel.equals("EXPR")) dpMat[i][j].eqString = ""+triggers.get(i).num;
					if(bestLabel.equals("ADD")) dpMat[i][j].eqString = "V+V";
					if(bestLabel.equals("SUB")) dpMat[i][j].eqString = "V-V";
					if(bestLabel.equals("MUL")) dpMat[i][j].eqString = triggers.get(i).num+"*V";
					if(bestLabel.equals("DIV") && triggers.get(i).num == null) {
						dpMat[i][j].eqString = "V/V";
					}
					if(bestLabel.equals("DIV") && triggers.get(i).num != null) {
						dpMat[i][j].eqString = "V/"+triggers.get(i).num;
					}
				} else {
					List<Integer> locs = new ArrayList<>();
					int count = 0;
					for(int k=0; k<bestDivision.size(); ++k) {
						IntPair ip = bestDivision.get(k);
						if(!dpMat[ip.getFirst()][ip.getSecond()].eqString.equals("")) {
							locs.add(k);
							count++;
						}
					}
					if(count == 1) {
						IntPair ip = bestDivision.get(locs.get(0));
						if(bestLabel.equals("ADD")) dpMat[i][j].eqString = 
								"V+"+dpMat[ip.getFirst()][ip.getSecond()];
						if(bestLabel.equals("SUB")) dpMat[i][j].eqString = 
								"V-"+dpMat[ip.getFirst()][ip.getSecond()];
						if(bestLabel.equals("MUL")) dpMat[i][j].eqString = 
								"V*"+dpMat[ip.getFirst()][ip.getSecond()];
						if(bestLabel.equals("DIV")) dpMat[i][j].eqString = 
								"V/"+dpMat[ip.getFirst()][ip.getSecond()];
					}
					if(count == 2) {
						IntPair ip1 = bestDivision.get(locs.get(0));
						IntPair ip2 = bestDivision.get(locs.get(1));
						if(bestLabel.equals("EQ")) dpMat[i][j].eqString = 
								dpMat[ip1.getFirst()][ip1.getSecond()]+
								"="+dpMat[ip2.getFirst()][ip2.getSecond()];
						if(bestLabel.equals("ADD")) dpMat[i][j].eqString = 
								dpMat[ip1.getFirst()][ip1.getSecond()]+
								"+"+dpMat[ip2.getFirst()][ip2.getSecond()];
						if(bestLabel.equals("SUB")) dpMat[i][j].eqString = 
								dpMat[ip1.getFirst()][ip1.getSecond()]+
								"-"+dpMat[ip2.getFirst()][ip2.getSecond()];
						if(bestLabel.equals("MUL")) dpMat[i][j].eqString = 
								dpMat[ip1.getFirst()][ip1.getSecond()]+
								"*"+dpMat[ip2.getFirst()][ip2.getSecond()];
						if(bestLabel.equals("DIV")) dpMat[i][j].eqString = 
								dpMat[ip1.getFirst()][ip1.getSecond()]+
								"/"+dpMat[ip2.getFirst()][ip2.getSecond()];
					}
					
				}
				
			}
		}
		List<Expr> queue = new ArrayList<Expr>();
		queue.add(dpMat[0][n]);
		while(queue.size() > 0) {
			Expr expr = queue.get(0);
			int i = expr.span.getFirst();
			int j = expr.span.getSecond();
			nodes.add(new Node(expr.label, new IntPair(i, j)));
			queue.remove(0);
			for(IntPair division : expr.divisions) {
				queue.add(dpMat[division.getFirst()][division.getSecond()]);
			}
		}
		return new Pair<String, List<Node>>(dpMat[0][n].eqString, nodes);
	}

	public static List<List<IntPair>> enumerateDivisions(
			JointX x, int start, int end) {
		List<List<IntPair>> divisions = new ArrayList<>();
		if(start+1 == end) {
			divisions.add(new ArrayList<IntPair>());
			return divisions;
		}
		for(int i=start+1; i<end; ++i) {
			List<IntPair> div = Arrays.asList(
					new IntPair(start, i), new IntPair(i, end));
			divisions.add(div);
		}
		for(int i=start+1; i<end-1; ++i) {
			for(int j=i+1; j<end; ++j) {
				List<IntPair> div = Arrays.asList(new IntPair(start, i), 
						new IntPair(i, j), new IntPair(j, end));
				if((i-start == 1 && x.triggers.get(start).label.equals("OP")) ||
						(j-i == 1 && x.triggers.get(i).label.equals("OP")) ||
						(end-j == 1 && x.triggers.get(j).label.equals("OP"))) {
					divisions.add(div);
				}
			}
		}
		return divisions;
	}	
	
}
