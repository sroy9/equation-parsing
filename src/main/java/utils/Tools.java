package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import semparse.SemX;
import structure.Operation;
import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
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
	
	public static List<Pair<String, IntPair>> getSkeleton(
			TextAnnotation ta, List<Constituent> lemmas, List<Constituent> parse, 
			List<QuantSpan> quantities) {
		return getSkeleton(ta, lemmas, null, parse, quantities);
	}
	
	public static List<Pair<String, IntPair>> getSkeleton(
			TextAnnotation ta, List<Constituent> lemmas, List<Constituent> posTags,
			List<Constituent> parse, 
			List<QuantSpan> quantities) {
		List<Pair<String, IntPair>> skeleton = new ArrayList<>();
		List<String> unigrams = FeatGen.getLemmatizedUnigrams(lemmas, 0, ta.size()-1);
		int i=0;
		while(i<ta.size()) {
			Constituent npChunk = null;
			int chunkWidth = 0; 
			for(Constituent cons : parse) {
				if(cons.getStartSpan() == i && cons.getLabel().equals("NP")) {
					boolean allow = true;
					for(QuantSpan qs : quantities) {
						int tokenId = ta.getTokenIdFromCharacterOffset(qs.start);
						if(tokenId >= cons.getStartSpan() && tokenId < cons.getEndSpan()) {
							allow = false;
							break;
						}
					}
					if(allow && cons.getEndSpan() - cons.getStartSpan() > chunkWidth) {
						npChunk = cons;
						chunkWidth = cons.getEndSpan() - cons.getStartSpan();
					}
				}
			}
			if(npChunk != null) {
				skeleton.add(new Pair<String, IntPair>("NP", new IntPair(
						npChunk.getStartSpan(), npChunk.getEndSpan())));
				i = npChunk.getEndSpan();
				continue;
			}
			if(posTags == null || (!posTags.get(i).getLabel().startsWith("NN") && 
					!posTags.get(i).getLabel().startsWith("VB"))) {
				skeleton.add(new Pair<String, IntPair>(unigrams.get(i), new IntPair(i, i+1)));
			} else {
				skeleton.add(new Pair<String, IntPair>(posTags.get(i).getLabel().substring(0, 2), 
						new IntPair(i, i+1)));
			}
			i++;
		}
		return skeleton;
	}
	
	public static String skeletonString(List<Pair<String, IntPair>> skeleton) {
		String str = "";
		for(Pair<String, IntPair> pair : skeleton) {
			str += pair.getFirst()+" ";
		}
		return str.trim();
	}
	
	public static boolean isOneVar(List<String> relations) {
		boolean r1 = false, r2 = false;
		for(String relation : relations) {
			if(relation.equals("R1") || relation.equals("BOTH")) r1 = true;
			if(relation.equals("R2") || relation.equals("BOTH")) r2 = true;
		}
		if(r1 && r2) return false;
		return true;
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
}
