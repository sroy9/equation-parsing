package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import structure.Node;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisChunkerHandler;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisPOSHandler;
import edu.illinois.cs.cogcomp.annotation.handler.StanfordParseHandler;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.SimpleQuantifier;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;
import edu.illinois.cs.cogcomp.quant.standardize.Ratio;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

public class Tools {
	
	public static SimpleQuantifier quantifier;
	public static AnnotatorService pipeline;
	
	static {
		try {
			ResourceManager rm = new ResourceManager(Params.pipelineConfig);
			
	        IllinoisTokenizer tokenizer = new IllinoisTokenizer();
	        TextAnnotationBuilder taBuilder = new TextAnnotationBuilder( tokenizer );
	        IllinoisPOSHandler pos = new IllinoisPOSHandler();
	        IllinoisChunkerHandler chunk = new IllinoisChunkerHandler();
	        
	        Properties stanfordProps = new Properties();
            stanfordProps.put( "annotators", "pos, parse") ;
            stanfordProps.put("parse.originalDependencies", true);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator( "pos", stanfordProps );
            ParserAnnotator parseAnnotator = new ParserAnnotator( "parse", stanfordProps );
            StanfordParseHandler parser = new StanfordParseHandler( posAnnotator, parseAnnotator );
	        
	        Map< String, Annotator> extraViewGenerators = new HashMap<String, Annotator>();

	        extraViewGenerators.put( ViewNames.POS, pos );
	        extraViewGenerators.put( ViewNames.SHALLOW_PARSE, chunk );
	        extraViewGenerators.put(ViewNames.PARSE_STANFORD, parser);
	        
	        Map< String, Boolean > requestedViews = new HashMap<String, Boolean>();
	        for ( String view : extraViewGenerators.keySet() )
	            requestedViews.put( view, false );

	        pipeline =  new AnnotatorService(taBuilder, extraViewGenerators, rm);
			quantifier = new SimpleQuantifier();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double sigmoid(double x) {
		return 1.0/(1+Math.pow(Math.E, -x))*2.0-1.0;
	}
	
	public static boolean doesIntersect(IntPair ip1, IntPair ip2) {
		if(ip1.getFirst() <= ip2.getFirst() && ip2.getFirst() <= ip1.getSecond()) {
			return true;
		}
		if(ip2.getFirst() <= ip1.getFirst() && ip1.getFirst() <= ip2.getSecond()) {
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
	
	public static double getJaccardScore(IntPair ip1, IntPair ip2) {
		return 1.0*(Math.min(ip1.getSecond(), ip2.getSecond()) - Math.max(ip1.getFirst(), ip2.getFirst()))
				/ (Math.max(ip1.getSecond(), ip2.getSecond()) - Math.min(ip1.getFirst(), ip2.getFirst()));		
	}
	
	public static void populateAndSortByCharIndex(List<Node> nodes, TextAnnotation ta, 
			List<QuantSpan> quantities, List<IntPair> candidateVars, boolean coref) {
		for(Node node : nodes) {
			if(node.label.equals("NUM")) node.charIndex = 
					(quantities.get(node.index).start+quantities.get(node.index).end)/2;
			if(node.label.equals("VAR")) {
				int start = ta.getTokenCharacterOffset(candidateVars.get(node.index).getFirst()).getFirst();
				int end = ta.getTokenCharacterOffset(candidateVars.get(node.index).getSecond()-1).getSecond()-1;
				node.charIndex = (start+end)/2;
			}
			node.projection = true;
		}
		for(int i=0; i<nodes.size(); ++i) {
			for(int j=0; j<nodes.size(); ++j) {
				if(i!=j) {
					Node node1 = nodes.get(i);
					Node node2 = nodes.get(j);
					if(node1.label.equals(node2.label) && node1.index == node2.index) {
						if(node1.label.equals("VAR") && coref) {
							node1.projection = false;
						}
						if(node1.label.equals("NUM")) {
							node1.projection = false;
						}
					}
				}
			}
		}
		Collections.sort(nodes, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return Integer.compare(o1.charIndex, o2.charIndex);
			}
		});
		int index=0;
		for(Node node : nodes) {
			node.nodeListIndex = index;
			++index;
		}
//		System.out.println("Nodelist : ");
//		for(Node node : nodes) {
//			System.out.print(node+" ");
//		}
//		System.out.println();
	}
	
}
