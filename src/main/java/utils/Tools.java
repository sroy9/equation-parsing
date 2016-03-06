package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import structure.Equation;
import structure.Node;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisChunkerHandler;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisPOSHandler;
import edu.illinois.cs.cogcomp.annotation.handler.StanfordParseHandler;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
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
	
	public static boolean isEqual(List<Integer> list1, List<Integer> list2) {
		if(list1 == null && list2 == null) return true;
		if(list1 == null || list2 == null) return false;
		if(list1.size() != list2.size()) return false;
		for(Integer i : list1) {
			if(!list2.contains(i)) {
				return false;
			}
		}
		return true;
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
	
	public static boolean contains(List<Double> arr, Double key) {
		for(Double d : arr) {
			if(Tools.safeEquals(d, key)) {
				return true;
			}
		}
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
	
	
	public static double getJaccardScore(IntPair ip1, IntPair ip2) {
		Double penalty = 0.0;
		int intersect = Math.min(ip1.getSecond(), ip2.getSecond()) - Math.max(ip1.getFirst(), ip2.getFirst());
		int union = Math.max(ip1.getSecond(), ip2.getSecond()) - Math.min(ip1.getFirst(), ip2.getFirst());
		return penalty + 1.0/(union-intersect+1);
	}
	
	public static void populateAndSortByCharIndex(List<Node> nodes, TextAnnotation ta, 
			List<QuantSpan> quantities, List<IntPair> candidateVars) {
		for(Node node : nodes) {
			if(node.label.equals("NUM")) node.charIndex = 
					(quantities.get(node.index).start+quantities.get(node.index).end)/2;
			if(node.label.equals("VAR")) {
				int start = ta.getTokenCharacterOffset(candidateVars.get(node.index).getFirst()).getFirst();
				int end = ta.getTokenCharacterOffset(candidateVars.get(node.index).getSecond()-1).getSecond()-1;
				node.charIndex = (start+end)/2;
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
	}
	
	public static List<Map<String, List<Integer>>> enumerateVarTokens(
			Map<String, List<Integer>> seed) {
		List<Map<String, List<Integer>>> mapList = new ArrayList<>();
		List<Integer> v1 = seed.get("V1");
		List<Integer> v2 = seed.get("V2");
		if(v1 != null && v1.size() > 0 && (v2 == null || v2.size() == 0)) {
			for(Integer i : v1) {
				Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
				map.put("V1", Arrays.asList(i));
				mapList.add(map);
			}
		}
		if(v1 != null && v1.size() > 0 && v2 != null && v2.size() > 0) {
			for(Integer i : v1) {
				for(Integer j : v2) {
					Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
					map.put("V1", Arrays.asList(i));
					map.put("V2", Arrays.asList(j));
					mapList.add(map);
				}
			}
		}
		return mapList;
	}
	
	public static List<Node> populateNodesWithVarTokens(List<Node> leaves,
			Map<String, List<Integer>> varTokens, List<QuantSpan> quantities) {
		List<Node> nodes = new ArrayList<>();
		for(int i=0; i<quantities.size(); ++i) {
			for(Node leaf : leaves) {
				if(leaf.label.equals("NUM") && Tools.safeEquals(Tools.getValue(
						quantities.get(i)), leaf.value)) {
					Node node = new Node(leaf);
					node.index = i;
					nodes.add(node);
				}
			}
		}
		for(Node leaf : leaves) {
			if(leaf.label.equals("VAR") && varTokens.containsKey(leaf.varId) &&
					varTokens.get(leaf.varId).size()>0) {
				Node node = new Node(leaf);
				node.index = varTokens.get(leaf.varId).get(0);
				nodes.add(node);
			}
		}
		return nodes;
	}
	
	public static void populateNodesWithVarTokensInPlace(List<Node> leaves,
			Map<String, List<Integer>> varTokens, List<QuantSpan> quantities) {
		for(int i=0; i<quantities.size(); ++i) {
			for(Node leaf : leaves) {
				if(leaf.label.equals("NUM") && Tools.safeEquals(Tools.getValue(
						quantities.get(i)), leaf.value)) {
					leaf.index = i;
				}
			}
		}
		for(Node leaf : leaves) {
			if(leaf.label.equals("VAR") && varTokens.containsKey(leaf.varId) &&
					varTokens.get(leaf.varId).size()>0) {
				leaf.index = varTokens.get(leaf.varId).get(0);
				
			}
		}
	}
	
	public static List<Map<String, List<Integer>>> enumerateProjectiveVarTokens(
			Map<String, List<Integer>> seed, Equation seedEq, TextAnnotation ta,
			List<QuantSpan> quantities, List<IntPair> candidateVars) {
		List<Map<String, List<Integer>>> projective = new ArrayList<>();
		for(Map<String, List<Integer>> varTokens : Tools.enumerateVarTokens(seed)) {
			boolean proj = true;
			Equation eq = new Equation(seedEq);
			Tools.populateNodesWithVarTokensInPlace(eq.root.getLeaves(), varTokens, quantities);
			Tools.populateAndSortByCharIndex(eq.root.getLeaves(), ta, quantities, candidateVars);
			for(Node node : eq.root.getAllSubNodes()) {
				if(node.children.size() == 2) {
					IntPair ip1 = node.children.get(0).getNodeListSpan();
					IntPair ip2 = node.children.get(1).getNodeListSpan();
					if(!(ip2.getSecond()==(ip1.getFirst()-1) || 
							ip1.getSecond()==(ip2.getFirst()-1))) {
						proj = false;
						break;
					}
				}
			}
			if(proj) {
				projective.add(varTokens);
			} 
		}
		return projective;
	}
	
	public static void visualizeNodeLocWithSynParse(TextAnnotation ta, 
			List<Constituent> parse, List<Node> leaves) {
		String str = "";
		for(Constituent cons : parse) {
			System.out.println(cons.getSurfaceForm() + " : " + cons.getSpan());
		}
		for(int i=0; i<ta.size(); ++i) {
			for(Constituent cons : parse) {
				if(cons.getEndSpan() - cons.getStartSpan() <= 1) continue;
				if(cons.getEndSpan() == i) {
					str += ") ";
				}
			}
			for(Constituent cons : parse) {
				if(cons.getEndSpan() - cons.getStartSpan() <= 1) continue;
				if(cons.getStartSpan() == i) {
					str += "( ";
				}
			}
			for(Node leaf : leaves) {
				if(ta.getTokenIdFromCharacterOffset(leaf.charIndex) == i) {
					str += "^^ ";
				}
			}
			str += ta.getToken(i) + " ";
		}
		System.out.println(str);
	}
}
