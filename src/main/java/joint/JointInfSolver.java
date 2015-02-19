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
		
		List<Template> grafts = extractGraftedTemplates(prob, templates, prob.eqStrings);
 		
		int maxNumSlots = 0;
		for(Template template : grafts) {
			beam1.add(new Pair<Template, Double>(template, 0.0));
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

	
}
