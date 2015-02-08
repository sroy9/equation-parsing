package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
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
				maximumSize(200).create();
		MinMaxPriorityQueue<Pair<SemY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).
				maximumSize(200).create();
		MinMaxPriorityQueue<Pair<SemY, Double>> shortBeam1 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).
				maximumSize(10).create();
		MinMaxPriorityQueue<Pair<SemY, Double>> shortBeam2 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).
				maximumSize(10).create();
		for(SemY y : enumerateSpans(blob)) {
			beam1.add(new Pair<SemY, Double>(y, 0.0+
					wv.dotProduct(featGen.getSpanFeatureVector(blob, y))));
		}
		for(Pair<SemY, Double> pair : beam1) {
			shortBeam1.add(pair);
			for(IntPair span : pair.getFirst().spans) {
				for(int i=span.getFirst(); i<span.getSecond(); ++i) {
					for(Pair<SemY, Double> p : shortBeam1) {
						for(String label : Arrays.asList("B-PART", "I-PART")) {
							SemY y = new SemY(p.getFirst());
							y.partitions.put(i, label);
							shortBeam2.add(new Pair<SemY, Double>(y, 
									1.0*wv.dotProduct(featGen.getPartitionFeatureVector(blob, i))));
						}
					}
					shortBeam1.clear();
					shortBeam1.addAll(shortBeam2);
					shortBeam2.clear();
				}
			}
			beam2.addAll(shortBeam1);
		}
		beam1.clear();
		for(Pair<SemY, Double> pair : beam2) {
			beam1.add(getBottomUpBestParse(blob, pair.getFirst(), wv));
		}
		pred = beam1.element().getFirst();
		return pred;
	}
	
	public Pair<SemY, Double> getBottomUpBestParse(SemX x, SemY y, WeightVector wv) {
		List<String> labels = null;
		Double totScore = 0.0;
		for(IntPair ip : y.spans) {
			int start = ip.getFirst(), end = ip.getSecond(); 
			Expr dpMat[][] = new Expr[end-start+1][end-start+1];
			for(int j=start+1; j<=end; ++j) {
				for(int i=j-1; i>=start; --i) {
					if(i == start && j == end) {
						labels = Arrays.asList("EQ");
					} else {
						labels = Arrays.asList("EXPR", "ADD", "SUB", "MUL", "DIV");
					}
					double bestScore = -Double.MAX_VALUE;
					List<IntPair> bestDivision = null;
					String bestLabel = null;
					double score = 0.0;
					for(String label : labels) {
						for(List<IntPair> division : enumerateDivisions(x, i, j)) {
							score = 1.0*wv.dotProduct(featGen.getExpressionFeatureVector(
									x, i, j, division, label));
							for(IntPair span : division) {
								score += dpMat[span.getFirst()-start][span.getSecond()-start].score;
							}
							if(score > bestScore) {
								bestScore = score;
								bestLabel = label;
								bestDivision = division;
							}
						}
					}
					dpMat[i-start][j-start] = new Expr();
					dpMat[i-start][j-start].score = bestScore;
					dpMat[i-start][j-start].label = bestLabel;
					dpMat[i-start][j-start].divisions = bestDivision;
					dpMat[i-start][j-start].span = new IntPair(i, j);
				}
			}
			List<Expr> queue = new ArrayList<Expr>();
			queue.add(dpMat[0][end-start]);
			Expr expr = queue.get(0);
			queue.remove(0);
			for(IntPair division : expr.divisions) {
				queue.add(dpMat[division.getFirst()-start][division.getSecond()-start]);
			}
			while(queue.size() > 0) {
				expr = queue.get(0);
				y.nodes.add(new Pair<String, IntPair>(expr.label, expr.span));
				queue.remove(0);
				for(IntPair division : expr.divisions) {
					queue.add(dpMat[division.getFirst()-start][division.getSecond()-start]);
				}
			}
			totScore += dpMat[0][end-start].score;
		}
		return new Pair<SemY, Double>(y, totScore);
	}

	
	public static boolean isCandidateEqualChunk(SemX x, int i, int j) {
		boolean mathyToken = false, quantityPresent = false, 
				sameSentence = false;
		if(x.ta.getSentenceFromToken(i).getSentenceId() == 
				x.ta.getSentenceFromToken(j-1).getSentenceId()) {
			sameSentence = true;
		}
		for(Integer tokenId : x.mathyTokenIndices) {
			if(i <= tokenId && tokenId < j) {
				mathyToken = true;
				break;
			}
		}
		for(QuantSpan qs : x.quantities) {
			int loc = x.ta.getTokenIdFromCharacterOffset(qs.start);
			if(i <= loc && loc < j) {
				quantityPresent = true;
				break;
			}
		}
		if(sameSentence && mathyToken && quantityPresent) return true;
		return false;
	}
	
	public static List<SemY> enumerateSpans(SemX x) {
		List<SemY> yList = new ArrayList<>();
		List<List<IntPair>> divisions = enumerateDivisions(x, 0, x.ta.size());
		for(List<IntPair> division : divisions) {
			boolean allow = true;
			for(IntPair ip : division) {
				if(!isCandidateEqualChunk(x, ip.getFirst(), ip.getSecond())) {
					allow = false;
					break;
				}
			}
			if(allow) {
				SemY y = new SemY();
				for(IntPair ip : division) {
					y.spans.add(ip);
					y.nodes.add(new Pair<String, IntPair>("EQ", ip));
				}
				yList.add(y);
			}
		}
		return yList;
	}
	
	public static List<List<IntPair>> enumerateDivisions(
			SemX x, int start, int end) {
		List<List<IntPair>> divisions = new ArrayList<>();
		divisions.add(new ArrayList<IntPair>());
		for(int i=start; i<end; ++i) {
			for(int j=i+1; j<end; ++j) {
				if(i==start && j==end) continue;
				List<IntPair> ipList = new ArrayList<>();
				ipList.add(new IntPair(i,j));
				divisions.add(ipList);
			}
		}
		for(int i=start; i<end-3; ++i) {
			for(int j=i+1; j<end-3; ++j) {
				for(int k=j+2; k<end; ++k) {
					for(int l=k+1; l<end; ++l) {
						List<IntPair> ipList = new ArrayList<>();
						ipList.add(new IntPair(i, j));
						ipList.add(new IntPair(k, l));
						divisions.add(ipList);
					}
				}
			}
		}
		return divisions;
	}
}
