package template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Equation;
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
//		MinMaxPriorityQueue<Pair<TemplateY, Double>> shortBeam = 
//				MinMaxPriorityQueue.orderedBy(pairComparator)
//				.maximumSize(10).create();
		
		for(TemplateY template : templates) {
			int quantCount = 0;
			for(Node node : template.equation.root.getLeaves()) {
				if(node.label.equals("NUM")) {
					quantCount++;
				}
			}
			if(quantCount != prob.relevantQuantIndices.size()) {
				continue;
			}
			beam1.add(new Pair<TemplateY, Double>(template, 0.0 
					+ wv.dotProduct(featGen.getGlobalFeatureVector(prob, template))
					));
		}
		
//		System.out.println("After just adding : "+beam1.size());
		for(Pair<TemplateY, Double> pair : beam1) {
			for(TemplateY y : enumerateInstantiationsVars(prob, pair.getFirst())) {
				beam2.add(new Pair<TemplateY, Double>(y, pair.getSecond() 
						+ wv.dotProduct(featGen.getVarTokenFeatureVector(prob, y))
						));
			}
//			beam2.addAll(shortBeam);
//			shortBeam.clear();
		}
//		System.out.println("After vars : "+beam2.size());
		beam1.clear();
		beam1.addAll(beam2);
		beam2.clear();
		for(Pair<TemplateY, Double> pair : beam1) {
			for(TemplateY y : enumerateInstantiationsNums(prob, pair.getFirst())) {
				beam2.add(new Pair<TemplateY, Double>(y, pair.getSecond() + 
						wv.dotProduct(featGen.getNumFeatureVector(prob, y))));
			}
//			beam2.addAll(shortBeam);
//			shortBeam.clear();
		}
//		System.out.println("Inference returns : "+beam2.element().getFirst());
		if(goldStructure == null && beam2.isEmpty()) return new TemplateY();
		return beam2.element().getFirst();
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
						node.index = tokenIndex;
					}
					if(node.label.equals("NUM")) {
						for(int i=0; i<x.quantities.size(); ++i) {
							if(Tools.safeEquals(node.value, 
									Tools.getValue(x.quantities.get(i)))) {
								node.index = x.ta.getTokenIdFromCharacterOffset(
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
							node.index = tokenIndex1;
						}
						if(node.label.equals("VAR") && node.varId.equals("V2")) {
							node.index = tokenIndex2;
						}
						if(node.label.equals("NUM")) {
							for(int i=0; i<x.quantities.size(); ++i) {
								if(Tools.safeEquals(node.value, 
										Tools.getValue(x.quantities.get(i)))) {
									node.index = x.ta.getTokenIdFromCharacterOffset(
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
	
	public List<TemplateY> enumerateInstantiationsVars(TemplateX x, TemplateY seed) {
		List<TemplateY> instantiations = new ArrayList<TemplateY>();
		List<Integer> varIndex = new ArrayList<Integer>();
		for(int i=0; i<seed.equation.root.getLeaves().size(); ++i) {
			Node node = seed.equation.root.getLeaves().get(i);
			if(node.label.equals("VAR")) {
				varIndex.add(i);
			}
		}
		if(varIndex.size()!=1 && varIndex.size()!=2) {
			System.out.println("ISSUE IN EnumerateInstNum");
		}
		
		if(varIndex.size() == 1) {
			for(int i=0; i<x.ta.size(); ++i) {
				if(x.posTags.get(i).getLabel().startsWith("N") || 
						x.posTags.get(i).getLabel().startsWith("V") ||
						x.posTags.get(i).getLabel().startsWith("J") ||
					KnowledgeBase.specialVarTokens.contains(
							x.ta.getToken(i).toLowerCase())) {
					TemplateY yNew = new TemplateY(seed);
					yNew.varTokens.put("V1", new ArrayList<Integer>());
					yNew.varTokens.get("V1").add(i);
					instantiations.add(yNew);
				}
			}
		}
		
		if(varIndex.size() == 2) {
			for(int i=0; i<x.ta.size(); ++i) {
				if(x.posTags.get(i).getLabel().startsWith("N") || 
						x.posTags.get(i).getLabel().startsWith("V") ||
						x.posTags.get(i).getLabel().startsWith("J") ||
						KnowledgeBase.specialVarTokens.contains(
								x.ta.getToken(i).toLowerCase())) {
					for(int j=i; j<x.ta.size(); ++j) {
						if(x.posTags.get(j).getLabel().startsWith("N") || 
								x.posTags.get(j).getLabel().startsWith("V") ||
								x.posTags.get(j).getLabel().startsWith("J") ||
								KnowledgeBase.specialVarTokens.contains(
										x.ta.getToken(j).toLowerCase())) {
							TemplateY yNew = new TemplateY(seed);
							yNew.varTokens.put("V1", new ArrayList<Integer>());
							yNew.varTokens.put("V2", new ArrayList<Integer>());
							yNew.varTokens.get("V1").add(i);
							yNew.varTokens.get("V2").add(j);
							instantiations.add(yNew);
						}
					}
				}
			}
		}
//		System.out.println("EnumerateVar returns "+instantiations.size());
		return instantiations;
	}
 	
	
	
	public List<TemplateY> enumerateInstantiationsNums(TemplateX x, TemplateY y) {
		List<TemplateY> instantiations = new ArrayList<TemplateY>();
		instantiations.add(y);
		List<TemplateY> tmpList = new ArrayList<TemplateY>();
		List<Node> leaves = y.equation.root.getLeaves();
		int numLeaves = leaves.size();
//		System.out.println("Enumerate template : "+y);
		
		for(int i=0; i<numLeaves; ++i) {
			if(leaves.get(i).label.equals("NUM")) {
				for(TemplateY template : instantiations) {
//					for(int j=0; j<x.quantities.size(); ++j) {
					for(Integer j : x.relevantQuantIndices) {
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
							yNew.equation.root.getLeaves().get(i).index = 
									x.ta.getTokenIdFromCharacterOffset(x.quantities.get(j).start);
							yNew.equation.root.getLeaves().get(i).value = 
									Tools.getValue(x.quantities.get(j));
							tmpList.add(yNew);
//							System.out.println("Enumerated : "+yNew);
						}
					}
				}
				instantiations.clear();
				instantiations.addAll(tmpList);
				tmpList.clear();
			}
		}
//		System.out.println("EnumerateNum returns "+instantiations.size());
		return instantiations;
	}
	
}
