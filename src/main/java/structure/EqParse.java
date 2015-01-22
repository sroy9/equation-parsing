package structure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

public class EqParse {
	
	public List<Pair<String, IntPair>> nodes;
	public Map<Integer, List<Integer>> edges;
	
	public EqParse() {
		nodes = new ArrayList<>();
		edges = new HashMap<>();
	}
	
	public EqParse(TextAnnotation ta, String annFile) throws IOException {
		this();
		List<String> lines = FileUtils.readLines(new File(annFile));
		// Nodes
		for(String line : lines) {
			String strArr[] = line.split("\t")[1].split(" ");
			String label = strArr[0];
			int start = ta.getTokenIdFromCharacterOffset(Integer.parseInt(strArr[1]));
			int end = ta.getTokenIdFromCharacterOffset(Integer.parseInt(strArr[2])-1)+1;
			nodes.add(new Pair<String, IntPair>(label, new IntPair(start, end)));
		}
		// Edges
		for(int i=0; i<nodes.size(); ++i) {
			edges.put(i, new ArrayList<Integer>());
		}
		for(int i=0; i<nodes.size(); ++i) {
			for(int j=0; j<nodes.size(); ++j) {
				if(i!=j && Tools.doesContain(
						nodes.get(i).getSecond(), nodes.get(j).getSecond())) {
					edges.get(i).add(j);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(nodes);
	}

}
