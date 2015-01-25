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
import structure.PairComparator;
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
	public List<Equation> equationTemplates;
	public List<List<Integer>> systemTemplates;

	public JointInfSolver(JointFeatGen featGen, 
			Pair<List<Equation>, List<List<Integer>>> templates) throws Exception {
		this.featGen = featGen;
		this.equationTemplates = templates.getFirst();
		this.systemTemplates = templates.getSecond();
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
		JointY pred = null;
		PairComparator<JointY> relationPairComparator = 
				new PairComparator<JointY>() {};
		MinMaxPriorityQueue<Pair<JointY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(relationPairComparator)
				.maximumSize(200).create();
		MinMaxPriorityQueue<Pair<JointY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(relationPairComparator)
				.maximumSize(200).create();
		
		// Number of Variables and Relation labels
		// We assume if numVar > number of equations found, there exists 
		// at least one consistent template pair
		int maxNumSlots = 0;
		for(boolean isOneVar : Arrays.asList(true, false)) {
			Double numVarScore = 0.0 + 
					wv.dotProduct(featGen.getNumVarFeatureVector(prob, isOneVar));
			for(Pair<List<Equation>, List<Slot>> pair : 
				extractRelevantTemplates(prob, isOneVar)) {
				JointY y = new JointY();
				y.isOneVar = isOneVar;
				y.equations = pair.getFirst();
				y.slots = pair.getSecond();
				beam1.add(new Pair<JointY, Double>(y, numVarScore));
				if(y.slots.size() > maxNumSlots) maxNumSlots = y.slots.size();
			}
		}
		
		for(int i=0; i<maxNumSlots; ++i) {
			for(Pair<JointY, Double> pair : beam1) {
				JointY y = pair.getFirst();
				if(pair.getFirst().slots.size() >= i) {
					beam2.add(pair);
				} else {
					for(int j=0; j<prob.quantities.size(); ++j) {
						JointY yNew = new JointY(pair.getFirst());
						yNew.equations.get(y.slots.get(i).i).
						terms.get(y.slots.get(i).j).get(y.slots.get(i).k).setSecond(
								Tools.getValue(prob.quantities.get(j)));
						yNew.quantityIndex.add(j);
						beam2.add(new Pair<JointY, Double>(yNew, pair.getSecond() + 
								wv.dotProduct(featGen.getAlignmentFeatureVector(
										prob, yNew, i))));
					}
				}
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		if(beam2.size() > 0) pred = beam2.element().getFirst();
		return pred;
	}
	
	public static Pair<List<Equation>, List<List<Integer>>> extractTemplates(
			SLProblem slProb) {
		List<Equation> templates = new ArrayList<>();
		List<List<Integer>> templatePairs = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			JointY gold = (JointY) struct;
			List<Integer> locs = new ArrayList<>();
			for(Equation eq1 : gold.equations) {
				for(int j=0; j<5; ++j) {
					for(int k=0; k<eq1.terms.get(j).size(); ++k) {
						eq1.terms.get(j).get(k).setSecond(null);
					}
				}
				boolean alreadyPresent = false;
				for(int i=0; i< templates.size(); ++i) { 
					Equation eq2 = templates.get(i); 
					if(Equation.getLoss(eq1, eq2) < 0.0001) {
						alreadyPresent = true;
						locs.add(i);
						break;
					}
				}
				if(!alreadyPresent) {
					locs.add(templates.size());
					templates.add(eq1);
				}
			}
			templatePairs.add(locs);
		}
		System.out.println("Number of templates : "+templates.size());
		System.out.println("Number of template pairs: "+templatePairs.size());
		return new Pair<List<Equation>, List<List<Integer>>>(templates, templatePairs);
	}
	
	// Assumes return list will be non zero
	public List<Pair<List<Equation>, List<Slot>>> extractRelevantTemplates(
			JointX x, boolean isOneVar) {
		List<Pair<List<Equation>, List<Slot>>> relevantTemplatesWithSlots = new ArrayList<>();
		List<List<Equation>> relevantTemplates = new ArrayList<>();
		if(x.existingEquations.size() == 0) {
			for(List<Integer> list : systemTemplates) {
				if(isOneVar && list.size() == 1) {
					List<Equation> eqList = new ArrayList<>();
					eqList.add(equationTemplates.get(list.get(0)));
					relevantTemplates.add(eqList);
				}
				if(!isOneVar && list.size() == 2) {
					List<Equation> eqList = new ArrayList<>();
					eqList.add(equationTemplates.get(list.get(0)));
					eqList.add(equationTemplates.get(list.get(1)));
					relevantTemplates.add(eqList);
				}
			}
		}
		if(x.existingEquations.size() == 1 && !isOneVar) {
			int index = getTemplateNumber(x.existingEquations.get(0));
			for(List<Integer> list : systemTemplates) {
				if(list.size() == 2) {
					if(list.get(0) == index) {
						List<Equation> eqList = new ArrayList<>();
						eqList.add(x.existingEquations.get(0));
						eqList.add(equationTemplates.get(list.get(1)));
						relevantTemplates.add(eqList);
					}
					if(list.get(1) == index) {
						List<Equation> eqList = new ArrayList<>();
						eqList.add(x.existingEquations.get(0));
						eqList.add(equationTemplates.get(list.get(0)));
						relevantTemplates.add(eqList);
					}
				}
			}
		}
		if(x.existingEquations.size() == 1 && isOneVar) {
			List<Equation> eqList = new ArrayList<>();
			eqList.add(x.existingEquations.get(0));
			relevantTemplates.add(eqList);
		}
		for(List<Equation> eqList : relevantTemplates) {
			List<Slot> slots = new ArrayList<>();
			for(int i=0; i<eqList.size(); ++i) {
				Equation eq = eqList.get(i);
				for(IntPair ip : eq.slots) {
					if(eq.terms.get(ip.getFirst()).get(ip.getSecond()) != null) {
						slots.add(new Slot(i, ip.getFirst(), ip.getSecond()));
					}
				}
			}
			relevantTemplatesWithSlots.add(new Pair<List<Equation>, List<Slot>>(
					eqList, slots));
		}
		return relevantTemplatesWithSlots;
	}
	
	public int getTemplateNumber(Equation eq) {
		int index = -1;
		Equation newEq = new Equation(eq);
		for(int j=0; j<5; ++j) {
			for(int k=0; k<newEq.terms.get(j).size(); ++k) {
				newEq.terms.get(j).get(k).setSecond(null);
			}
		}
		for(int i=0; i<equationTemplates.size(); ++i) {
			Equation template = equationTemplates.get(i);
			if(Equation.getLoss(template, newEq) < 0.001) {
				index = i;
				break;
			}
		}
		return index;
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
