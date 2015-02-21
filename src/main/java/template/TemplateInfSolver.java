package template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class TemplateInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private TemplateFeatGen featGen;
	public List<List<Equation>> templates;

	public TemplateInfSolver(TemplateFeatGen featGen, List<List<Equation>> templates) 
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
		TemplateY r1 = (TemplateY) arg1;
		TemplateY r2 = (TemplateY) arg2;
		return TemplateY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		TemplateX prob = (TemplateX) x;
		TemplateY gold = (TemplateY) goldStructure;
		TemplateY pred = new TemplateY();
		PairComparator<Template> jointPairComparator = 
				new PairComparator<Template>() {};
		MinMaxPriorityQueue<Pair<Template, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(jointPairComparator)
				.maximumSize(200).create();
		MinMaxPriorityQueue<Pair<Template, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(jointPairComparator)
				.maximumSize(200).create();
		
		List<Template> grafts = TemplateDriver.extractGraftedTemplates(
				prob, templates, prob.eqStrings);
 		
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
				if(pair.getFirst().slots.size() <= i) {
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
