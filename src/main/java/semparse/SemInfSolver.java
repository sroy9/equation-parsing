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

public class SemInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private SemFeatGen featGen;
	public Lexicon lexicon;

	public SemInfSolver(SemFeatGen featGen, SLProblem train) {
		this.featGen = featGen;
		this.lexicon = Lexicon.extractLexicon(train);
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
		while(true) {
			
			// Stopping condition : Checks if span is left
			boolean cont = false;
			for(Pair<SemY, Double> pair1 : beam1) {
				if(pair1.getFirst().spans.size() > 0) {
					cont = true;
				}
			}
			if(!cont) break;	
			
			// State transition in beam search
			for(Pair<SemY, Double> pair1 : beam1) {
				if(pair1.getFirst().spans.size() == 0) {
					beam2.add(pair1);
				} else {
					for(String key : lexicon.lex.keySet()) {
						for(List<String> ccgPattern : lexicon.get(key)) {
							Pair<Double, List<IntPair>> alignment = getBestAlignment(
									blob, pair1.getFirst().spans.get(0), 
									ccgPattern, wv, featGen);
							SemY y = new SemY(pair1.getFirst());
							y.spans.remove(0);
							y.spans.addAll(0, alignment.getSecond());
							y.nodes.add(new Pair<String, IntPair>(
									key, pair1.getFirst().spans.get(0)));
							beam2.add(new Pair<SemY, Double>(
									y, pair1.getSecond() + alignment.getFirst()));
						}
					}
				}
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		pred = beam1.element().getFirst();
		return pred;
	}
	
	public static Pair<Double, List<IntPair>> getBestAlignment(
			SemX x, IntPair span, List<String> ccgPattern, 
			WeightVector wv, SemFeatGen featGen) {
//		System.out.println("getBestAlignment called with");
//		System.out.println("Span : "+span);
//		System.out.println("CCG Pattern : "+ccgPattern);
		int m = ccgPattern.size();
		int n = span.getSecond()-span.getFirst();
		List<IntPair> exprs = new ArrayList<>();
		String[][] dpPointers = new String[m+1][n+1];
		double[][] dpScores = new double[m+1][n+1];
		for(int i=0; i<m+1; ++i) {
			dpScores[i][0] = 0.0;
		}
		for(int i=0; i<n+1; ++i) {
			dpScores[0][i] = 0.0;
		}
		for(int i=1; i<=m; ++i) {
			for(int j=1; j<=n; ++j) {
				if(dpScores[i][j-1] > dpScores[i-1][j-1]) {
					dpScores[i][j] = dpScores[i][j-1] + 
							wv.dotProduct(featGen.getCCGFeatureVector(
									x, j+span.getFirst()-1, ccgPattern.get(i-1)));
					dpPointers[i][j] = "SAME";
				} else {
					dpScores[i][j] = dpScores[i-1][j-1] + 
							wv.dotProduct(featGen.getCCGFeatureVector(
									x, j+span.getFirst()-1, ccgPattern.get(i-1)));
					dpPointers[i][j] = "DIFF";
				}
			}
		}
		int i=m, j=n;
		int lastLoc = n+1;
		dpPointers[1][1] = "DIFF";
		while(i!=0 || j!=0) {
			if(dpPointers[i][j] == "DIFF") {
				if(ccgPattern.get(i-1).equals("EXPR")) {
					exprs.add(0, new IntPair(
							j+span.getFirst()-1, lastLoc+span.getFirst()-1));
				}
				lastLoc = j;
				i--;
			}
			j--;
		}
		if(exprs.size() == 1 && exprs.get(0).getFirst() == span.getFirst() &&
				exprs.get(0).getSecond() == span.getSecond()) {
			exprs.clear();
		}
		return new Pair<Double, List<IntPair>>(dpScores[m][n], exprs);
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
