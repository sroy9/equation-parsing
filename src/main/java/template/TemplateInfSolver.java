package template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Equation;
import structure.Node;
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
	public List<TemplateY> templates;

	public TemplateInfSolver(TemplateFeatGen featGen, List<TemplateY> templates) 
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
		PairComparator<TemplateY> pairComparator = 
				new PairComparator<TemplateY>() {};
		MinMaxPriorityQueue<Pair<TemplateY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(pairComparator)
				.maximumSize(200).create();
		MinMaxPriorityQueue<Pair<TemplateY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(pairComparator)
				.maximumSize(200).create();
		
		int maxNumSlots = 0;
		for(TemplateY template : templates) {
			beam1.add(new Pair<TemplateY, Double>(template, 0.0));
			if(template.equation.root.getLeaves().size() > maxNumSlots) {
				maxNumSlots = template.equation.root.getLeaves().size();
			}
		}
		
		// Fill up remaining slots
		for(int i=0; i<maxNumSlots; ++i) {
			for(Pair<TemplateY, Double> pair : beam1) {
				TemplateY y = pair.getFirst();
				List<Node> leaves = y.equation.root.getLeaves();
				if(leaves.size() <= i) {
					beam2.add(pair);
				} else if(leaves.get(i).label.equals("NUM")) {
					for(int j=0; j<prob.quantities.size(); ++j) {
						TemplateY yNew = new TemplateY(y);
						leaves = yNew.equation.root.getLeaves();
						leaves.get(i).value = Tools.getValue(prob.quantities.get(j));
						leaves.get(i).tokenIndex = prob.ta.getTokenIdFromCharacterOffset(
								prob.quantities.get(j).start);
						beam2.add(new Pair<TemplateY, Double>(yNew, pair.getSecond() + 
								wv.dotProduct(featGen.getAlignmentFeatureVector(
										prob, yNew, i))));
					}
				} else {
					for(int j=0; j<prob.ta.size(); ++j) {
						TemplateY yNew = new TemplateY(y);
						leaves = yNew.equation.root.getLeaves();
						leaves.get(i).tokenIndex = j;
						beam2.add(new Pair<TemplateY, Double>(yNew, pair.getSecond() + 
								wv.dotProduct(featGen.getAlignmentFeatureVector(
										prob, yNew, i))));
					}
					
				}
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		pred.equation = beam1.element().getFirst().equation;
		return pred;
	}
	
}
