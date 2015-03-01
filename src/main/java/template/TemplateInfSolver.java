package template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Equation;
import structure.KnowledgeBase;
import structure.Node;
import structure.PairComparator;
import tree.TreeX;
import tree.TreeY;
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
			int quantCount = 0;
			for(Node node : template.equation.root.getLeaves()) {
				if(node.label.equals("NUM")) {
					quantCount++;
				}
			}
			if(quantCount > prob.quantities.size()) {
				continue;
			}
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
						boolean allow = true;
						for(int k=0; k<i; ++k) {
							Node node = y.equation.root.getLeaves().get(k);
							if(Tools.safeEquals(Tools.getValue(prob.quantities.get(j)), node.value)) {
								allow = false;
								break;
							}
						}
						if(allow) {
							TemplateY yNew = new TemplateY(y);
							leaves = yNew.equation.root.getLeaves();
							leaves.get(i).value = Tools.getValue(prob.quantities.get(j));
							leaves.get(i).tokenIndex = prob.ta.getTokenIdFromCharacterOffset(
									prob.quantities.get(j).start);
							beam2.add(new Pair<TemplateY, Double>(yNew, pair.getSecond() + 
									wv.dotProduct(featGen.getAlignmentFeatureVector(
											prob, yNew, leaves, i))));
						}
					}
				} else {
					for(int j=0; j<prob.ta.size(); ++j) {
						if(prob.posTags.get(j).getLabel().startsWith("N") || 
								prob.posTags.get(j).getLabel().startsWith("V") ||
								prob.posTags.get(j).getLabel().startsWith("J") ||
								KnowledgeBase.specialVarTokens.contains(
										prob.ta.getToken(j).toLowerCase())) {
							TemplateY yNew = new TemplateY(y);
							leaves = yNew.equation.root.getLeaves();
							leaves.get(i).tokenIndex = j;
							yNew.varTokens.put(leaves.get(i).varId, Arrays.asList(j));
							beam2.add(new Pair<TemplateY, Double>(yNew, pair.getSecond() + 
									wv.dotProduct(featGen.getAlignmentFeatureVector(
											prob, yNew, leaves, i))));
						}
					}
				}
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		return beam1.element().getFirst();
	}
	
	public TemplateY getLatentBestStructure(
			TemplateX x, TemplateY gold, WeightVector wv) {
		TemplateY best = null;
		double bestScore = -Double.MAX_VALUE;
		if(gold.varTokens.keySet().size() == 1) {
			for(Integer tokenIndex : gold.varTokens.get("V1")) {
				TemplateY yNew = new TemplateY(gold);
				yNew.varTokens.get("V1").clear();
				yNew.varTokens.get("V1").add(tokenIndex);
				for(Node node : yNew.equation.root.getLeaves()) {
					if(node.label.equals("VAR")) {
						node.tokenIndex = tokenIndex;
					}
					if(node.label.equals("NUM")) {
						for(int i=0; i<x.quantities.size(); ++i) {
							if(Tools.safeEquals(node.value, 
									Tools.getValue(x.quantities.get(i)))) {
								node.tokenIndex = x.ta.getTokenIdFromCharacterOffset(
												x.quantities.get(i).start);
								break;
							}
						}
					}
				}
				double score = wv.dotProduct(
						featGen.getFeatureVector(x, yNew));
				if(score > bestScore) {
					best = yNew;
				}
			}
		}
		if(gold.varTokens.keySet().size() == 2) {
			for(Integer tokenIndex1 : gold.varTokens.get("V1")) {
				for(Integer tokenIndex2 : gold.varTokens.get("V2")) {
					TemplateY yNew = new TemplateY(gold);
					yNew.varTokens.get("V1").clear();
					yNew.varTokens.get("V1").add(tokenIndex1);
					yNew.varTokens.get("V2").clear();
					yNew.varTokens.get("V2").add(tokenIndex2);
					for(Node node : yNew.equation.root.getLeaves()) {
						if(node.label.equals("VAR") && node.varId.equals("V1")) {
							node.tokenIndex = tokenIndex1;
						}
						if(node.label.equals("VAR") && node.varId.equals("V2")) {
							node.tokenIndex = tokenIndex2;
						}
						if(node.label.equals("NUM")) {
							for(int i=0; i<x.quantities.size(); ++i) {
								if(Tools.safeEquals(node.value, 
										Tools.getValue(x.quantities.get(i)))) {
									node.tokenIndex = x.ta.getTokenIdFromCharacterOffset(
													x.quantities.get(i).start);
									break;
								}
							}
						}
						
					}
					double score = wv.dotProduct(featGen.getFeatureVector(x, yNew));
					if(score > bestScore) {
						best = yNew;
					}
				}
			}
		}
		return best;
	}
	
	public List<TemplateY> enumerateInstantiations(TemplateX x, TemplateY y) {
		List<TemplateY> instantiations = new ArrayList<TemplateY>();
		instantiations.add(y);
		List<TemplateY> tmpList = new ArrayList<TemplateY>();
		List<Node> leaves = y.equation.root.getLeaves();
		int numLeaves = leaves.size();
		
		for(int i=0; i<numLeaves; ++i) {
			for(TemplateY template : instantiations) {
				if(leaves.get(i).label.equals("VAR")) { 
					for(int j=0; j<x.ta.size(); ++j) {
						if(x.posTags.get(j).getLabel().startsWith("N") || 
								x.posTags.get(j).getLabel().startsWith("V") ||
								x.posTags.get(j).getLabel().startsWith("J") ||
								KnowledgeBase.specialVarTokens.contains(
										x.ta.getToken(j).toLowerCase())) {
							TemplateY yNew = new TemplateY(template);
							yNew.equation.root.getLeaves().get(i).tokenIndex = j;
							tmpList.add(yNew);
						}
					}
				}
				if(leaves.get(i).label.equals("NUM")) {
					for(int j=0; j<x.quantities.size(); ++j) {
						TemplateY yNew = new TemplateY(template);
						boolean allow = true;
						for(int k=0; k<i; ++k) {
							Node node = yNew.equation.root.getLeaves().get(k);
							if(Tools.safeEquals(Tools.getValue(x.quantities.get(j)), node.value)) {
								allow = false;
								break;
							}
						}
						if(allow) {
							yNew.equation.root.getLeaves().get(i).tokenIndex = 
									x.ta.getTokenIdFromCharacterOffset(x.quantities.get(j).start);
							yNew.equation.root.getLeaves().get(i).value = 
									Tools.getValue(x.quantities.get(j));
							tmpList.add(yNew);
						}
					}
				}
			}
			instantiations.clear();
			instantiations.addAll(tmpList);
			tmpList.clear();
		}
		return instantiations;
	}
	
}
