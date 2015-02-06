package semparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.DocReader;
import structure.SimulProb;
import utils.Params;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;

public class Lexicon {

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
	
	public static Lexicon extractLexicon(SLProblem prob) {
		Lexicon lexicon = new Lexicon();
 		for(int i=0; i<prob.goldStructureList.size(); ++i) {
 			SemX x = (SemX) prob.instanceList.get(i);
 			SemY y = (SemY) prob.goldStructureList.get(i);
 			for(Pair<String, IntPair> pair : y.nodes) {
 				if(!lexicon.lex.containsKey(pair.getFirst())) {
 					lexicon.lex.put(pair.getFirst(), new ArrayList<List<String>>());
 				}
 				List<String> pattern = SemFeatGen.getNodeString(x, y.nodes, pair.getSecond());
 				if(!contains(lexicon.lex.get(pair.getFirst()), pattern)) {
 					lexicon.lex.get(pair.getFirst()).add(pattern);
 				}
 			}
 		}
 		return lexicon;
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
