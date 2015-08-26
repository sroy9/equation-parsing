package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structure.Operation;
import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;
import edu.illinois.cs.cogcomp.quant.standardize.Ratio;

public class Tools {
	
	public static NewCachingCurator curator;
	public static Quantifier quantifier;
	
	static {
		try {
			curator = new NewCachingCurator(
					"trollope.cs.illinois.edu", 
					9010, 
					Params.cacheLoc, 
					null);
			quantifier = new Quantifier();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static double sigmoid(double x) {
		return 1.0/(1+Math.pow(Math.E, -x))*2.0-1.0;
	}
	
	public static boolean doesIntersect(IntPair ip1, IntPair ip2) {
		if(ip1.getFirst() <= ip2.getFirst() && ip2.getFirst() < ip1.getSecond()) {
			return true;
		}
		if(ip2.getFirst() <= ip1.getFirst() && ip1.getFirst() < ip2.getSecond()) {
			return true;
		}
		return false;
	}
	
	// is ip2 subset of ip1
	public static boolean doesContain(IntPair big, IntPair small) {
		if(big.getFirst() <= small.getFirst() && small.getSecond() <= big.getSecond()) {
			return true;
		}
		return false;
	}
	

	public static boolean doesContainNotEqual(IntPair big, IntPair small) {
		if(big.getFirst() == small.getFirst() && big.getSecond() == small.getSecond()) {
			return false;
		}
		if(big.getFirst() <= small.getFirst() && small.getSecond() <= big.getSecond()) {
			return true;
		}
		return false;
	}
	
	public static Operation getOperationFromString(String op) {
		if(op.equals("ADD") || op.equals("+")) return Operation.ADD;
		if(op.equals("SUB") || op.equals("-")) return Operation.SUB;
		if(op.equals("MUL") || op.equals("*")) return Operation.MUL;
		if(op.equals("DIV") || op.equals("/")) return Operation.DIV;
		if(op.equals("EQ") || op.equals("=")) return Operation.EQ;
		return null;
	}
	
	public static boolean safeEquals(Double d1, Double d2) {
		if(d1 == null && d2 == null) return true;
		if(d1 == null || d2 == null) {
			return false;
		}
		if(d1 > d2 - 0.0001 && d1 < d2 + 0.0001) {
			return true;
		}
		return false;
	}

	public static Double getValue(QuantSpan qs) {
		if (qs.object instanceof Quantity) {
			return ((Quantity)qs.object).value;
		} else if (qs.object instanceof Ratio) {
			return ((Ratio)qs.object).numerator.value / 
					((Ratio)qs.object).denominator.value;
		}
		return null;
	}

	public static String getUnit(QuantSpan qs) {
		if (qs.object instanceof Quantity) {
			return ((Quantity)qs.object).units;
		} else if (qs.object instanceof Ratio) {
			return ((Ratio)qs.object).denominator.units;
		}
		return null;
	}
	
	public static String getBound(QuantSpan qs) {
		if (qs.object instanceof Quantity) {
			return ((Quantity)qs.object).bound;
		} else if (qs.object instanceof Ratio) {
			return ((Ratio)qs.object).denominator.bound;
		}
		return null;
	}

	public static int getTokenIndex(QuantSpan qs, TextAnnotation ta) {
		return ta.getTokenIdFromCharacterOffset(qs.start);
	}
	
	public static List<Double> uniqueNumbers(List<QuantSpan> quantSpans) {
		List<Double> uniqueNos = new ArrayList<>();
		for(int i=0; i<quantSpans.size(); i++) {
			QuantSpan qs = quantSpans.get(i);
			boolean allow = true;
			for(int j=0; j<i; j++) {
				if(Tools.safeEquals(Tools.getValue(qs), Tools.getValue(quantSpans.get(j)))) {
					allow = false;
					break;
				}
			}
			if(allow) uniqueNos.add(Tools.getValue(qs));
		}
		return uniqueNos;
	}
	
	public static List<QuantSpan> getRelevantQuantSpans(
			Double d, List<QuantSpan> quantSpans) {
		List<QuantSpan> relevantSpans = new ArrayList<QuantSpan>();
		for(QuantSpan qs : quantSpans) {
			if(Tools.safeEquals(d, Tools.getValue(qs))) {
				relevantSpans.add(qs);
			}
		}
		return relevantSpans;
	}
	
//	public List<Constituent> getAllConsInPath(
//			List<Constituent> dependencyCons, int leaf1, int leaf2) {
//		List<Constituent> cons1 = new ArrayList<Constituent>();
//		List<Constituent> cons2 = new ArrayList<Constituent>();
//		Constituent cons = dependencyCons.get(leaf1);
//		while(cons.getIncomingRelations().size()>0 && )
//		
//	}
	
	public static String skeletonString(List<Pair<String, IntPair>> skeleton) {
		String str = "";
		for(Pair<String, IntPair> pair : skeleton) {
			str += pair.getFirst()+" ";
		}
		return str.trim();
	}
	
	public static int getNONEcount(List<String> relations) {
		int count = 0;
		for(String relation : relations) {
			if(relation.equals("NONE")) {
				count++;
			}
		}
		return count;
	}
	
	public static boolean contains(List<Double> arr, Double key) {
		for(Double d : arr) {
			if(Tools.safeEquals(d, key)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean equals(List<Double> arr1, List<Double> arr2) {
		if(arr1 == null || arr2 == null) return false;
		if(arr1.size() != arr2.size()) return false;
		for(Double d1 : arr1) {
			boolean found = false;
			for(Double d2 : arr2) {
				if(Tools.safeEquals(d1, d2)) {
					found = true;
				}
			}
			if(!found) return false;
		}
		return true;
	}
	
	public static boolean areAllTokensInSameSentence(
			TextAnnotation ta, List<Integer> tokenIds) {
		Set<Integer> sentenceIds = new HashSet<>();
		for(Integer tokenId : tokenIds) {
			sentenceIds.add(ta.getSentenceFromToken(tokenId).getSentenceId());
		}
		if(sentenceIds.size() == 1) return true;
		return false;
	}
	
	public static Integer max(List<Integer> intList) {
		Integer max = Integer.MIN_VALUE;
		for(Integer i : intList) {
			if(max < i) {
				max = i;
			}
		}
		return max;
	}
	
	public static Integer min(List<Integer> intList) {
		Integer min = Integer.MAX_VALUE;
		for(Integer i : intList) {
			if(min > i) {
				min = i;
			}
		}
		return min;
	}
	
	public static boolean isSkeletonIndex(
			List<Pair<String, IntPair>> skeleton, int index) {
		for(Pair<String, IntPair> pair : skeleton) {
			if(pair.getSecond().getFirst() == index || 
					pair.getSecond().getSecond() == index) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isConstituentIndex(
			List<Constituent> cons, int index) {
		for(Constituent con : cons) {
			if(con.getStartSpan() == index || 
					con.getEndSpan() == index) {
				return true;
			}
		}
		return false;
	}
	
	
}
