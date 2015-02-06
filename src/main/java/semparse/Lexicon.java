package semparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;

public class Lexicon {

	Map<String, List<List<String>>> lex;
	
	public Lexicon() {
		lex = new HashMap<String, List<List<String>>>();
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
 				lexicon.lex.get(pair.getFirst()).add(
 						SemFeatGen.getNodeString(x, y.nodes, pair.getSecond()));
 			}
 		}
 		System.out.println("Knowledge Base Size : "+lexicon.lex.size());
 		for(String key : lexicon.lex.keySet()) {
 			System.out.println(key + " : " + Arrays.asList(lexicon.lex.get(key)));
 		}
 		return lexicon;
	}
}
