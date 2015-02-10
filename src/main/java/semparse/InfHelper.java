package semparse;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;

public class InfHelper {

	public static boolean isCandidateEqualChunk(SemX x, int i, int j) {
		if(j-i<5) return false;
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
		List<List<IntPair>> divisions = enumerateDivisions(0, x.ta.size());
		for(List<IntPair> division : divisions) {
			boolean allow = true;
			for(IntPair ip : division) {
				if(!InfHelper.isCandidateEqualChunk(x, ip.getFirst(), ip.getSecond())) {
					allow = false;
					break;
				}
			}
			if(allow) {
				SemY y = new SemY();
				for(IntPair ip : division) {
					y.spans.add(ip);
				}
				yList.add(y);
			}
		}
		return yList;
	}
	
	public static List<List<IntPair>> enumerateDivisions(
			int start, int end) {
		List<List<IntPair>> divisions = new ArrayList<>();
		divisions.add(new ArrayList<IntPair>());
		for(int i=start; i<end; ++i) {
			for(int j=i+1; j<=end; ++j) {
				if(i==start && j==end) continue;
				List<IntPair> ipList = new ArrayList<>();
				ipList.add(new IntPair(i,j));
				divisions.add(ipList);
			}
		}
		for(int i=start; i<end-2; ++i) {
			for(int j=i+1; j<end-1; ++j) {
				for(int k=j+2; k<end; ++k) {
					for(int l=k+1; l<=end; ++l) {
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
	
	public static List<IntPair> extractPartitions(SemY y, IntPair span) {
		List<IntPair> partitions = new ArrayList<>();
		int lastLoc = 0;
		for(int i=span.getFirst()+1; i<span.getSecond(); ++i) {
			if(y.partitions.get(i).equals("B-PART")) {
				partitions.add(new IntPair(lastLoc, i));
				lastLoc = i;
			}
		}
		partitions.add(new IntPair(lastLoc, span.getSecond()));
		return partitions;
	}
	
	public static List<Expr> extractPartitions(List<Expr> tokenLabels) {
		List<Expr> partitions = new ArrayList<>();
		int lastLoc = 0;
		double score = tokenLabels.get(0).score;
		for(int i=1; i<tokenLabels.size(); ++i) {
			if(tokenLabels.get(i).label.equals("B-PART")) {
				Expr expr = new Expr();
				expr.score = score;
				int start = tokenLabels.get(lastLoc).span.getFirst();
				int end = tokenLabels.get(i-1).span.getSecond();
				expr.span = new IntPair(start, end);
				partitions.add(expr);
				lastLoc = i;
				score = 0.0;
			}
			score += tokenLabels.get(i).score;
		}
		Expr expr = new Expr();
		expr.score = score;
		int start = tokenLabels.get(lastLoc).span.getFirst();
		int end = tokenLabels.get(tokenLabels.size()-1).span.getSecond();
		expr.span = new IntPair(start, end);
		expr.divisions = new ArrayList<IntPair>();
		partitions.add(expr);
		return partitions;
	}
	
	public static List<IntPair> extractTokenDivisionFromPartitionDivision(
			List<IntPair> partitions, List<IntPair> partDivision) {
		List<IntPair> tokenDivision = new ArrayList<>();
		for(IntPair div : partDivision) {
			int i = div.getFirst();
			int j = div.getSecond();
			tokenDivision.add(new IntPair(
					partitions.get(i).getFirst(), 
					partitions.get(j-1).getSecond()));
		}
		return tokenDivision;
	}

}
