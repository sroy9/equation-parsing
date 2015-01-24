package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Equation;
import structure.PairComparator;
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
	public List<Pair<JointY, Double>> beam;

	public JointInfSolver(JointFeatGen featGen, 
			List<Equation> equationTemplates,
			List<List<Integer>> systemTemplates,
			int testFold) throws Exception {
		this.featGen = featGen;
		this.equationTemplates = equationTemplates;
		this.systemTemplates = systemTemplates;
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
		for(boolean isOneVar : Arrays.asList(true, false)) {
			Double numVarScore = 0.0 + 
					wv.dotProduct(featGen.getNumVarFeatureVector(prob, isOneVar));
			if(prob.existingEquations.size() == 0) {
				
			}
			if(prob.existingEquations.size() == 1) {
				
			}
			
			
		}
		
		if(beam1.size() > 0) pred = beam1.element().getFirst();
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
	public List<List<Equation>> extractRelevantTemplates(JointX x, boolean isOneVar) {
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
			for(IntPair ip : systemTemplates) {
				
			}
		}
		return relevantTemplates;
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
	
	
}
