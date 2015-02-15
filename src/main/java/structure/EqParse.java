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

	public List<Pair<Integer, String>> triggers;
	public List<Pair<String, IntPair>> nodes;
	
	public EqParse() {
		triggers = new ArrayList<Pair<Integer,String>>();
		nodes = new ArrayList<>();
	}
	
	public EqParse(EqParse other) {
		triggers = other.triggers;
		nodes = new ArrayList<>();
		for(Pair<String, IntPair> pair : other.nodes) {
			nodes.add(pair);
		}
	}

	public EqParse(TextAnnotation ta, String annFile) throws IOException {
		this();
		for(String token : ta.getTokens()) {
			
		}
		
		List<String> lines = FileUtils.readLines(new File(annFile));
		for(String line : lines) {
			String strArr[] = line.split("\t")[1].split(" ");
			String label = strArr[0];
			int start = ta.getTokenIdFromCharacterOffset(Integer.parseInt(strArr[1]));
			int end = ta.getTokenIdFromCharacterOffset(Integer.parseInt(strArr[2])-1)+1;
			nodes.add(new Pair<String, IntPair>(label, new IntPair(start, end)));
		}
	}
		
	@Override
	public String toString() {
		return ""+Arrays.asList(nodes);
	}

}
