package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
		for(SemY y : enumerateSpans(blob)) {
			beam1.add(new Pair<SemY, Double>(y, 0.0+
					wv.dotProduct(featGen.getSpanFeatureVector(blob, y))));
		}
		for(Pair<SemY, Double> pair1 : beam1) {
			Pair<SemY, Double> pair2 = 
					getBottomUpBestParse(blob, pair1.getFirst(), wv);
			beam2.add(new Pair<SemY, Double>(
					pair2.getFirst(), pair1.getSecond() + pair2.getSecond()));
		}
		pred = beam2.element().getFirst();
		return pred;
	}
	
	public Pair<SemY, Double> getBottomUpBestParse(SemX x, SemY y, WeightVector wv) {
		Double totScore = 0.0;
		List<String> labels = Arrays.asList("EXPR", "ADD", "SUB", "MUL", "DIV", "EQ");
		for(IntPair ip : y.spans) {
			int start = ip.getFirst(), end = ip.getSecond();
//			double score[][] = new double[end-start+1][end-start+1];
//			String label[][] = new String[end-start+1][end-start+1];
			// Have to keep the division used along with the label
			
			for(int j=start+1; j<=end; ++j) {
				for(int i=start; i<j; ++i) {
					// Find argmax across all labels and all divisions
					// label[i][j] <- label if span (i, j) is an expression
					double bestScore = -Double.MAX_VALUE;
					List<IntPair> bestDivision = null;
					String bestLabel = null;
					double score = 0.0;
					for(String label : labels) {
						for(List<IntPair> division : enumerateDivisions(start, end)) {
							score = 1.0*wv.dotProduct(featGen.getExpressionFeatureVector(
									x, i, j, division, label));
							if(score > bestScore) {
								bestScore = score;
								bestLabel = label;
								bestDivision = division;
							}
						}
					}
					
				}
			}
			
			
			
		}
		return new Pair<SemY, Double>(y, totScore);
	}
	
	public static boolean isCandidateEqualChunk(SemX x, int i, int j) {
		boolean mathyToken = false, quantityPresent = false, 
				sameSentence = false;
		if(x.ta.getSentenceFromToken(i).getSentenceId() == 
				x.ta.getSentenceFromToken(j).getSentenceId()) {
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
		yList.add(new SemY());
		// One span
		for(int i=0; i<x.ta.size()-1; ++i) {
			for(int j=i+3; j<x.ta.size(); ++j) {
				if(isCandidateEqualChunk(x, i, j)) {
					SemY y = new SemY();
					y.spans.add(new IntPair(i, j));
					y.nodes.add(new Pair<String, IntPair>(
							"EQ", new IntPair(i, j)));
					yList.add(y);
				}
			}
		}
		// Two span
		for(int i=0; i<x.ta.size()-4; ++i) {
			for(int j=i+3; j<x.ta.size()-3; ++j) {
				for(int k=j+2; k<x.ta.size()-2; ++k) {
					for(int l=k+3; l<x.ta.size(); ++l) {
						if(isCandidateEqualChunk(x, i, j) && 
								isCandidateEqualChunk(x, k, l)) {
							SemY y = new SemY();
							y.spans.add(new IntPair(i, j));
							y.spans.add(new IntPair(k, l));
							y.nodes.add(new Pair<String, IntPair>(
									"EQ", new IntPair(i, j)));
							y.nodes.add(new Pair<String, IntPair>(
									"EQ", new IntPair(k, l)));
							yList.add(y);
						}		
					}
				}
			}
		}
		return yList;
	}
	
	public List<List<IntPair>> enumerateDivisions(int start, int end) {
		List<List<IntPair>> divisions = new ArrayList<>();
		for(int i=start; i<end-1; ++i) {
			for(int j=i+1; j<end; ++j) {
				List<IntPair> ipList = new ArrayList<>();
				ipList.add(new IntPair(i, j));
				divisions.add(ipList);
			}
		}
		for(int i=start; i<end-3; ++i) {
			for(int j=i+1; j<end-2; ++j) {
				for(int k=j+2; k<end-1; ++k) {
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
