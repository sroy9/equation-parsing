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
	public static long spanTime=0, dpTime=0;
	public Map<String, List<List<String>>> knowledgeBase;

	public SemInfSolver(SemFeatGen featGen, SLProblem train) {
		this.featGen = featGen;
		this.knowledgeBase = extractKnowledgeBase(train);
	}
	
 	private Map<String, List<List<String>>> extractKnowledgeBase(SLProblem train) {
 		Map<String, List<List<String>>> knowledgeBase = new HashMap<>();
 		for(int i=0; i<train.goldStructureList.size(); ++i) {
 			SemX x = (SemX) train.instanceList.get(i);
 			SemY y = (SemY) train.goldStructureList.get(i);
//			System.out.println("EqParse : " +y.nodes);
 			for(Pair<String, IntPair> pair : y.nodes) {
 				if(!knowledgeBase.containsKey(pair.getFirst())) {
 					knowledgeBase.put(pair.getFirst(), new ArrayList<List<String>>());
 				}
 				knowledgeBase.get(pair.getFirst()).add(
 						SemFeatGen.getNodeString(x, y.nodes, pair.getSecond()));
// 				System.out.println(pair.getFirst() + " : " +
// 						SemFeatGen.getNodeString(x, y.nodes, pair.getSecond()));
 			}
 		}
 		System.out.println("Knowledge Base Size : "+knowledgeBase.size());
 		for(String key : knowledgeBase.keySet()) {
 			System.out.println(key + " : " + Arrays.asList(knowledgeBase.get(key)));
 		}
 		return knowledgeBase;
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
				maximumSize(5).create();
		MinMaxPriorityQueue<Pair<SemY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(semPairComparator).
				maximumSize(5).create();
		long lStartTime = System.nanoTime();
		for(SemY y : enumerateSpans(blob)) {
			beam1.add(new Pair<SemY, Double>(y, 0.0+
					wv.dotProduct(featGen.getSpanFeatureVector(blob, y))));
		}
		long lEndTime = System.nanoTime();
	 	long difference = lEndTime - lStartTime;
	 	spanTime+=difference/1000000;
//	 	System.out.println("Span Elapsed milliseconds: " + spanTime);
	 	lStartTime = System.nanoTime();
	 	for(Pair<SemY, Double> pair1 : beam1) {
			Pair<SemY, Double> pair2 = 
					getBottomUpBestParse(blob, pair1.getFirst(), wv);
			beam2.add(new Pair<SemY, Double>(
					pair2.getFirst(), pair1.getSecond() + pair2.getSecond()));
		}
	 	lEndTime = System.nanoTime();
	 	difference = lEndTime - lStartTime;
	 	dpTime+= difference/1000000;
//	 	System.out.println("DP Elapsed milliseconds: " + dpTime);
		pred = beam2.element().getFirst();
		return pred;
	}
	
	public Pair<SemY, Double> getBottomUpBestParse(SemX x, SemY y, WeightVector wv) {
		List<String> labels = null;
		Double totScore = 0.0;
		for(IntPair ip : y.spans) {
			int start = ip.getFirst(), end = ip.getSecond();
//			System.out.println("DP matrix of size : "+(end-start+1));
			Expr dpMat[][] = new Expr[end-start+1][end-start+1];
			for(int j=start+1; j<=end; ++j) {
				for(int i=j-1; i>=start; --i) {
					// Find argmax across all labels and all divisions
					// label[i][j] <- label if span (i, j) is an expression
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
//								System.out.println("Trying to access : "+
//										(span.getFirst()-start)+" "+(span.getSecond()-start));
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
					y.spans.add(new IntPair(ip.getFirst(), ip.getSecond()));
					y.nodes.add(new Pair<String, IntPair>(
							"EQ", new IntPair(ip.getFirst(), ip.getSecond())));
				}
				yList.add(y);
			}
		}
		return yList;
	}
	
	public static List<List<IntPair>> enumerateDivisions(SemX x, int start, int end) {
		List<List<IntPair>> divisions = new ArrayList<>();
		divisions.add(new ArrayList<IntPair>());
		for(int i=start; i<end; ++i) {
			for(int j=i; j<end; ++j) {
				if(i==start && j==end-1) continue;
				List<IntPair> ipList = new ArrayList<>();
				ipList.add(new IntPair(x.skeleton.get(i).getSecond().getFirst(), 
						x.skeleton.get(j).getSecond().getSecond()));
				divisions.add(ipList);
			}
		}
		for(int i=start; i<end-3; ++i) {
			for(int j=i; j<end-3; ++j) {
				for(int k=j+2; k<end; ++k) {
					for(int l=k; l<end; ++l) {
						List<IntPair> ipList = new ArrayList<>();
						ipList.add(new IntPair(x.skeleton.get(i).getSecond().getFirst(), 
								x.skeleton.get(j).getSecond().getSecond()));
						ipList.add(new IntPair(x.skeleton.get(k).getSecond().getFirst(),
								x.skeleton.get(l).getSecond().getSecond()));
						divisions.add(ipList);
					}
				}
			}
		}
		return divisions;
	}
}
