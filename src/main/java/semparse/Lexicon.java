package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.DocReader;
import structure.SimulProb;
import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;

public class Lexicon implements Serializable {

	private static final long serialVersionUID = 6497021707338812080L;
	Map<String, List<List<String>>> lex;
	
	public Lexicon() {
		lex = new HashMap<String, List<List<String>>>();
	}
	
	@Override
	public String toString() {
		String str = "";
		for(String key : lex.keySet()) {
			str += key + " : " + Arrays.asList(lex.get(key)) + "\n\n\n";
		}
		return str;
	}
	
	public List<List<String>> get(String key) {
		if(lex.containsKey(key))
			return lex.get(key);
		else 
			return new ArrayList<>();
	}
	
	public List<List<String>> getAll() {
		List<List<String>> allPats = new ArrayList<>();
		for(String key : lex.keySet()) {
			allPats.addAll(lex.get(key));
		}
		return allPats;
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
 				if(!lexicon.lex.containsKey(pair.getFirst())) {
 					lexicon.lex.put(pair.getFirst(), new ArrayList<List<String>>());
 				}
 				List<Pair<String, IntPair>> pattern = 
 						Lexicon.getNodeString(x, y.nodes, pair.getSecond());
 				List<Pair<String, IntPair>> ccgPattern = 
 						getCCGPattern(pattern, pair.getFirst());
 				List<String> ccgOnlyStrings = new ArrayList<>();
 				for(Pair<String, IntPair> pat : ccgPattern) {
 					ccgOnlyStrings.add(pat.getFirst());
 				}
 				if(!contains(lexicon.lex.get(pair.getFirst()), ccgOnlyStrings)) {
 					lexicon.lex.get(pair.getFirst()).add(ccgOnlyStrings);
 				}
 			}
 		}
 		return lexicon;
	}
	
	public static List<Pair<String, IntPair>> getCCGPattern(
			List<Pair<String, IntPair>> pattern, String label) {
		List<Pair<String, IntPair>> ccgPattern = new ArrayList<>();
		int lastLoc = 0;
		for(int i=0; i<pattern.size(); ++i) {
			if(pattern.get(i).equals("EXPR")) {
				if(lastLoc < i) ccgPattern.add(new Pair<String, IntPair>(
						"SOMETHING", new IntPair(lastLoc, i)));
				ccgPattern.add(pattern.get(i));
				lastLoc = pattern.get(i).getSecond().getSecond();
				i=lastLoc;
			}
		}
		if(!pattern.get(pattern.size()-1).equals("EXPR")) {
			ccgPattern.add(new Pair<String, IntPair>("SOMETHING", new IntPair(
							lastLoc, 
							pattern.get(pattern.size()-1).getSecond().getSecond())));
		}
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
			String ccgLabel = "";
			for(int j=0; j<prev; j++) {
				ccgLabel += "EXPR\\";
			}
			ccgLabel += label;
			for(int j=0; j<next; j++) {
				ccgLabel += "/EXPR";
			}
			ccgPattern.get(i).setFirst(ccgLabel);
		}
		return ccgPattern;
	}
	
	private static boolean contains(List<List<String>> list,
			List<String> pattern) {
		for(List<String> l : list) {
			if((""+Arrays.asList(l)).equals(""+Arrays.asList(pattern))) {
				return true;
			}
		}
		return false;
	}

	public static void main(String args[]) throws Exception {
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir);
		Lexicon lexicon = extractLexicon(SemDriver.getSP(simulProbList));
		System.out.println(lexicon);
	}
}
