package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.text.DefaultEditorKit.BeepAction;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
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
	
	@Override
	public String toString() {
		return label +" "+span+" "+divisions;
	}
}

public class SemInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private SemFeatGen featGen;

	public SemInfSolver(SemFeatGen featGen) {
		this.featGen = featGen;
	}
	
	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		SemY y1 = (SemY) arg1;
		SemY y2 = (SemY) arg2;
		return SemY.getLoss(y1, y2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		SemX blob = (SemX) x;
		SemY gold = (SemY) goldStructure;
		SemY pred = null;
		PairComparator<SemY> semPairComparator = 
				new PairComparator<SemY>() {};
		MinMaxPriorityQueue<Pair<SemY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).
				maximumSize(50).create();
		MinMaxPriorityQueue<Pair<SemY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).
				maximumSize(50).create();
		MinMaxPriorityQueue<Pair<SemY, Double>> shortBeam1 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).
				maximumSize(10).create();
		MinMaxPriorityQueue<Pair<SemY, Double>> shortBeam2 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).
				maximumSize(10).create();
//		System.out.println(new Date()+" : Enumerating spans");
		for(SemY y : InfHelper.enumerateSpans(blob)) {
			beam1.add(new Pair<SemY, Double>(y, 0.0+
					wv.dotProduct(featGen.getSpanFeatureVector(blob, y))));
		}
//		System.out.println(new Date()+" : Partitioning");
		for(Pair<SemY, Double> pair : beam1) {
			SemY y = pair.getFirst();
			for(IntPair span : pair.getFirst().spans) {
				List<Expr> partitions = getBestPartition(blob, span, wv);
				for(Expr partition : partitions) {
					y.partitions.put(partition.span.getFirst(), "B-PART");
					for(int i=partition.span.getFirst()+1; i<partition.span.getSecond(); ++i) {
						y.partitions.put(i, "I-PART");
					}
				}
				Pair<SemY, Double> parse = getBottomUpBestParse(blob, y, partitions, wv);
				beam2.add(new Pair<SemY, Double>(parse.getFirst(), 
						pair.getSecond() + parse.getSecond()));
			}
		}
		pred = beam2.element().getFirst();
//		System.out.println(new Date()+" : Inference done");
		return pred;
	}
	
	public List<Expr> getBestPartition(
			SemX x, IntPair span, WeightVector wv) {
		List<Expr> exprList = new ArrayList<Expr>();
		List<String> labels = Arrays.asList("B-PART", "I-PART");
		for(int i=span.getFirst(); i<span.getSecond(); ++i) {
			String bestLabel = null;
			double bestScore = - Double.MAX_VALUE;
			String prevLabel = null;
			if(i > span.getFirst()) {
				prevLabel = exprList.get(i-span.getFirst()-1).label;
			}
			for(String label : labels) {
				double score = wv.dotProduct(featGen.getPartitionFeatureVector(
						x, i, prevLabel, label));
				if(score > bestScore) {
					bestScore = score;
					bestLabel = label;
				}
			}
			Expr expr = new Expr();
			expr.label = bestLabel;
			expr.score = bestScore;
			expr.span = new IntPair(i, i+1);
			exprList.add(expr);
		}
		return InfHelper.extractPartitions(exprList);
	}
	
	public Pair<SemY, Double> getBottomUpBestParse(
			SemX x, SemY y, List<Expr> partitions, WeightVector wv) {
		List<String> labels = null;
		int n = partitions.size();
		Expr dpMat[][] = new Expr[n+1][n+1];
		for(int i=0; i<n; ++i) {
			dpMat[i][i+1] = partitions.get(i);
		}
		for(int j=2; j<=n; ++j) {
			for(int i=j-2; i>=0; --i) {
				if(i == 0 && j == n) {
					labels = Arrays.asList("EQ");
				} else {
					labels = Arrays.asList("EXPR", "ADD", "SUB", "MUL", "DIV");
				}
				double bestScore = -Double.MAX_VALUE;
				List<IntPair> bestDivision = null;
				String bestLabel = null;
				double score;
				for(String label : labels) {
					for(List<IntPair> division : InfHelper.enumerateDivisions(i, j)) {
						if(label.equals("EXPR") && division.size() > 0) continue;
						if(!label.equals("EXPR") && division.size() == 0) continue; 
						if(label.equals("EQ") && division.size() != 2) continue; 
						score = 1.0*wv.dotProduct(featGen.getExpressionFeatureVector(
								x, partitions.get(i).span.getFirst(), partitions.get(j-1).span.getSecond(), 
								InfHelper.extractTokenDivisionFromPartitionDivision(partitions, division), 
								label));
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
		Expr expr = queue.get(0);
		queue.remove(0);
		for(IntPair division : expr.divisions) {
			queue.add(dpMat[division.getFirst()][division.getSecond()]);
		}
		while(queue.size() > 0) {
			expr = queue.get(0);
			int i = expr.span.getFirst();
			int j = expr.span.getSecond();
			y.nodes.add(new Pair<String, IntPair>(expr.label, 
					new IntPair(partitions.get(i).span.getFirst(), 
							partitions.get(j-1).span.getSecond())));
			queue.remove(0);
			for(IntPair division : expr.divisions) {
				queue.add(dpMat[division.getFirst()][division.getSecond()]);
			}
		}
		return new Pair<SemY, Double>(y, dpMat[0][n].score);
	}	
	
}
