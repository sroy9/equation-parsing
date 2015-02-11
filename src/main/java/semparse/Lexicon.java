package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.NumberUp;

import org.apache.commons.lang.math.NumberUtils;

import parser.DocReader;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;

public class Lexicon implements Serializable {

	private static final long serialVersionUID = 6497021707338812080L;
	Map<String, List<Pair<List<String>, Integer>>> ccgGroups;
	
	public Lexicon() {
		ccgGroups = new HashMap<String, List<Pair<List<String>, Integer>>>();
	}
	
	@Override
	public String toString() {
		String str = "";
		for(String key : ccgGroups.keySet()) {
			str += key + " : " + Arrays.asList(ccgGroups.get(key)) + "\n\n\n";
		}
		return str;
	}
	
	public List<Pair<List<String>, Integer>> get(String key) {
		if(ccgGroups.containsKey(key))
			return ccgGroups.get(key);
		else 
			return new ArrayList<>();
	}
	
	public static List<IntPair> getDivisions(
			List<Pair<String, IntPair>> nodes, IntPair ip) {
		List<IntPair> divisions = new ArrayList<>();
		for(Pair<String, IntPair> pair : nodes) {
			if(Tools.doesContainNotEqual(ip, pair.getSecond())) {
				boolean allow = true;
				for(Pair<String, IntPair> pair1 : nodes) {
					if(Tools.doesContainNotEqual(ip, pair1.getSecond()) &&
							Tools.doesContainNotEqual(pair1.getSecond(), pair.getSecond())) {
						allow = false;
						break;
					}
				}
				if(allow) {
					divisions.add(pair.getSecond());
				}
			}
		}
		return divisions;
	}

	public static List<Pair<String, IntPair>> getNodeString(
			SemX x, List<Pair<String, IntPair>> nodes, IntPair ip) {
		List<Pair<String, IntPair>> tokens = new ArrayList<>();
		List<IntPair> divisions = Lexicon.getDivisions(nodes, ip);
		for(int i=ip.getFirst(); i<ip.getSecond(); ++i) {
			boolean found = false;
			for(IntPair div : divisions) {
				if(div.getFirst() == i) {
					tokens.add(new Pair<String, IntPair>("EXPR", div));
					i = div.getSecond()-1;
					found = true;
					break;
				}
			}
			if(!found) tokens.add(new Pair<String, IntPair>(
					x.ta.getToken(i).toLowerCase(), new IntPair(i, i+1)));
		}
		return tokens;
	}

	public static Lexicon extractLexicon(SLProblem prob) {
		Lexicon lexicon = new Lexicon();
 		for(int i=0; i<prob.goldStructureList.size(); ++i) {
 			SemX x = (SemX) prob.instanceList.get(i);
 			SemY y = (SemY) prob.goldStructureList.get(i);
 			for(Pair<String, IntPair> pair : y.nodes) {
// 				System.out.println("Problem index : "+x.problemIndex);
 				List<Pair<String, IntPair>> pattern = 
 						Lexicon.getNodeString(x, y.nodes, pair.getSecond());
// 				System.out.println("NodeString : "+pattern);
 				List<Pair<String, IntPair>> ccgPattern = 
 						getCCGPattern(pattern, pair.getFirst());
// 				System.out.println("CCG Pattern : "+ccgPattern+"\n");
 				List<String> ccgOnlyStrings = new ArrayList<>();
 				for(Pair<String, IntPair> pat : ccgPattern) {
 					ccgOnlyStrings.add(pat.getFirst());
 				}
 				
 				for(Pair<String, IntPair> pat : ccgPattern) {
 					if(!lexicon.ccgGroups.containsKey(pat.getFirst())) {
 						lexicon.ccgGroups.put(pat.getFirst(), 
 								new ArrayList<Pair<List<String>, Integer>>());
 					}
 					if(!pat.getFirst().equals("EXPR")) {
 						List<String> terms = new ArrayList<>();
 						for(int j=pat.getSecond().getFirst(); j<pat.getSecond().getSecond(); ++j) {
 							if(NumberUtils.isNumber(x.ta.getToken(j))) {
 								terms.add("NUMBER");
 							} else {
 								terms.add(x.ta.getToken(j).toLowerCase());
 							}
 						}
 						int index = findIndex(lexicon.ccgGroups.get(pat.getFirst()), terms);
 						if(index == -1) {
 							lexicon.ccgGroups.get(pat.getFirst()).add(
 									new Pair<List<String>, Integer>(terms, 1));
 						} else {
 							int num = lexicon.ccgGroups.get(pat.getFirst()).get(index).getSecond();
 							lexicon.ccgGroups.get(pat.getFirst()).get(index).setSecond(num+1);
 						}
 					}
 				}
 			}
 		}
 		return lexicon;
	}
	
	public static List<Pair<String, IntPair>> getCCGPattern(
			List<Pair<String, IntPair>> pattern, String label) {
		List<Pair<String, IntPair>> ccgPattern = new ArrayList<>();
		for(int i=0; i<pattern.size(); ++i) {
			if(!pattern.get(i).getFirst().equals("EXPR")) {
				int loc = pattern.size();
				for(int j=i+1; j<pattern.size(); ++j) {
					if(pattern.get(j).getFirst().equals("EXPR")) {
						loc = j;
						break;
					}
				}
				int start = pattern.get(i).getSecond().getFirst();
				int end = pattern.get(loc-1).getSecond().getSecond();
				ccgPattern.add(new Pair<String, IntPair>(
						"SOMETHING", new IntPair(start, end)));
				i = loc-1;
			} else {
				ccgPattern.add(pattern.get(i));
			}
		}
//		System.out.println("Intermediate : "+ccgPattern);
		for(int i=0; i<ccgPattern.size(); i++) {
			if(ccgPattern.get(i).getFirst().equals("EXPR")) continue;
			int prev = 0, next = 0;
			for(int j=0; j<i; ++j) {
				if(ccgPattern.get(j).getFirst().equals("EXPR")) {
					prev++;
				}
			}
			for(int j=i+1; j<ccgPattern.size(); ++j) {
				if(ccgPattern.get(j).getFirst().equals("EXPR")) {
					next++;
				}
			}
			String ccgLabel = label + "_" + prev + "_" + next;
			ccgPattern.get(i).setFirst(ccgLabel);
		}
		return ccgPattern;
	}
	
	private static int findIndex(List<Pair<List<String>, Integer>> list, 
			List<String> pattern) {
		for(int i=0; i<list.size(); ++i) {
			Pair<List<String>, Integer> l = list.get(i);
			if((""+Arrays.asList(l.getFirst())).equals(""+Arrays.asList(pattern))) {
				return i;
			}
		}
		return -1;
	}
	
	public static SemY extractLexiconBasedPartition(SemX x, Lexicon lex, int occThreshold) {
		SemY y = new SemY();
		List<String> tokens = new ArrayList<String>();
		for(String token : x.ta.getTokens()) {
			if(NumberUtils.isNumber(token)) {
				tokens.add("NUMBER");
			} else {
				tokens.add(token.toLowerCase());
			}
		}
		List<Pair<String, IntPair>> matches = lexMatches(x, lex, occThreshold);
		matches = maximalMatches(matches);
		
		return y;
	}

	private static List<Pair<String, IntPair>> maximalMatches(
			List<Pair<String, IntPair>> matches) {
		// TODO Auto-generated method stub
		return null;
	}

	private static List<Pair<String, IntPair>> lexMatches(SemX x, Lexicon lex,
			int occThreshold) {
		List<Pair<String, IntPair>> matches = new ArrayList<Pair<String,IntPair>>();
		for(int i=0; i<x.ta.size(); ++i) {
			for(int j=i+1; j<x.ta.size(); ++j) {
				boolean found = false;
				
				
				
				
				
				
			}
		}
		return matches;
	}

	public static void main(String args[]) throws Exception {
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir);
		Lexicon lexicon = extractLexicon(SemDriver.getSP(simulProbList));
		System.out.println(lexicon);
	}
}
