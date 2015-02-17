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
import structure.PairComparator;
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
	
	public Expr() {
		divisions = new ArrayList<IntPair>();
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
		JointY pred = null;
		PairComparator<JointY> relationPairComparator = 
				new PairComparator<JointY>() {};
		MinMaxPriorityQueue<Pair<JointY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(relationPairComparator)
				.maximumSize(200).create();
		MinMaxPriorityQueue<Pair<JointY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(relationPairComparator)
				.maximumSize(200).create();
		// Get best equation trees
		
		// Extract grafted templates
		
		// Fill up remaining slots
//		for(int i=0; i<maxNumSlots; ++i) {
//			for(Pair<JointY, Double> pair : beam1) {
//				JointY y = pair.getFirst();
//				if(pair.getFirst().slots.size() >= i) {
//					beam2.add(pair);
//				} else {
//					for(int j=0; j<prob.quantities.size(); ++j) {
//						JointY yNew = new JointY(pair.getFirst());
//						yNew.equations.get(y.slots.get(i).i).
//						terms.get(y.slots.get(i).j).get(y.slots.get(i).k).setSecond(
//								Tools.getValue(prob.quantities.get(j)));
//						yNew.quantityIndex.add(j);
//						beam2.add(new Pair<JointY, Double>(yNew, pair.getSecond() + 
//								wv.dotProduct(featGen.getAlignmentFeatureVector(
//										prob, yNew, i))));
//					}
//				}
//			}
//			beam1.clear();
//			beam1.addAll(beam2);
//			beam2.clear();
//		}
		if(beam2.size() > 0) pred = beam2.element().getFirst();
		return pred;
	}
	
	public static List<List<Equation>> extractTemplates(SLProblem slProb) {
		List<List<Equation>> templates = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			JointY gold = (JointY) struct;
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
	
	public static List<Equation> extractEquationFromParse(
			List<Pair<String, IntPair>> nodes) {
		List<Equation> expressions = new ArrayList<>();
		return expressions;
	}
	
	public static List<List<Equation>> extractGraftedTemplates(
			JointX x, JointY y, List<List<Equation>> templates) {
		List<List<Equation>> relevantTemplates = new ArrayList<>();
		return relevantTemplates;
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
	
	public List<Pair<String, IntPair>> getBottomUpBestParse(
			JointX x, IntPair span, WeightVector wv) {
		
		List<Pair<String, IntPair>> nodes = new ArrayList<>();
		List<String> labels = null;
		int n = span.getSecond() - span.getFirst();
		Expr dpMat[][] = new Expr[n+1][n+1];
		
		for(int j=1; j<=n; ++j) {
			for(int i=j-1; i>=0; --i) {
				if(i+1 == j) {
					labels = Arrays.asList("EXPR", "OP", "ADD", "SUB", "MUL", "DIV", "NULL");
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
			}
		}
		List<Expr> queue = new ArrayList<Expr>();
		queue.add(dpMat[0][n]);
		while(queue.size() > 0) {
			Expr expr = queue.get(0);
			int i = expr.span.getFirst();
			int j = expr.span.getSecond();
			nodes.add(new Pair<String, IntPair>(expr.label, new IntPair(i, j)));
			queue.remove(0);
			for(IntPair division : expr.divisions) {
				queue.add(dpMat[division.getFirst()][division.getSecond()]);
			}
		}
		return nodes;
	}

	public static List<List<IntPair>> enumerateDivisions(JointX x, int start, int end) {
		List<List<IntPair>> divisions = new ArrayList<>();
		if(start+1 == end) {
			divisions.add(new ArrayList<IntPair>());
			return divisions;
		}
		for(int i=start+1; i<end; ++i) {
			List<IntPair> div = Arrays.asList(new IntPair(start, i), new IntPair(i, end));
			divisions.add(div);
		}
		for(int i=start+1; i<end-1; ++i) {
			for(int j=i+1; j<end; ++j) {
				List<IntPair> div = Arrays.asList(new IntPair(start, i), 
						new IntPair(i, j), new IntPair(j, end));
				if((i-start == 1 && x.triggers.get(start).getSecond().equals("OP")) ||
						(j-i == 1 && x.triggers.get(i).getSecond().equals("OP")) ||
						(end-j == 1 && x.triggers.get(j).getSecond().equals("OP"))) {
					divisions.add(div);
				}
			}
		}
		return divisions;
	}	
	
}
