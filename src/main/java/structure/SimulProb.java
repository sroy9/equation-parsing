package structure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;

import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
 
public class SimulProb {
	
	public int index;
	public String question;
	public List<Equation> equations;
	public List<Double> solutions;
	public List<QuantSpan> quantities; 	
	public List<Span> eqSpans;
	public List<Span> spans;
	public Map<String, List<QuantSpan>> clusterMap;
	
	public SimulProb(int index) {
		this.index = index;
		equations = new ArrayList<Equation>();
		solutions = new ArrayList<Double>();
		quantities = new ArrayList<QuantSpan>();
		spans = new ArrayList<Span>();
		eqSpans = new ArrayList<>();
		clusterMap = new HashMap<String, List<QuantSpan>>();
		clusterMap.put("E1", new ArrayList<QuantSpan>());
		clusterMap.put("E2", new ArrayList<QuantSpan>());
		clusterMap.put("E3", new ArrayList<QuantSpan>());
	}
	
	public void extractQuantities(Quantifier quantifier) throws IOException {
		List<QuantSpan> spanArray = quantifier.getSpans(question, true);
		quantities = new ArrayList<QuantSpan>();
		for(QuantSpan span:spanArray){
			quantities.add(span);
		}
	}
	
	// Equations will be changed to replace variable names by V1, V2
	// Needs to be called after calling extractVariableSpans()
	public void extractQuestionsAndSolutions() throws IOException {
		String fileName = Params.annotationDir + index + ".txt";
		List<String> lines = FileUtils.readLines(new File(fileName));
		question = lines.get(0);
		solutions = new ArrayList<Double>();
		String ans[] = lines.get(lines.size()-1).split(" ");
		for(String str : ans) {
			solutions.add(Double.parseDouble(str.trim()));
		}
	}
	
	// To be called first, reads brat annotation files
	public void extractAllSpans() throws IOException {
		String fileName = Params.annotationDir + index + ".ann";
		List<String> lines = FileUtils.readLines(new File(fileName));
		String label; 
		int start, end;
		// Extract annotations from file
		for (String line : lines) {
			String strArr[] = line.split("\t")[1].split(" ");
			label = strArr[0];
			start = Integer.parseInt(strArr[1]);
			end = Integer.parseInt(strArr[2]);
			spans.add(new Span(label, new IntPair(start, end)));
		}
		// Fill up incomplete annotations, first create map of mentions to labels
		Map<IntPair, Set<String>> mentionLabelsMap = 
				new HashMap<IntPair, Set<String>>();
		for(Span vs : spans) {
			if(mentionLabelsMap.containsKey(vs.ip)) {
				mentionLabelsMap.get(vs.ip).add(vs.label);
			} else {
				Set<String> labelsForASpan = new HashSet<String>();
				labelsForASpan.add(vs.label);
				mentionLabelsMap.put(vs.ip, labelsForASpan);
			}
		}
		// Fill up missing entries
		for(Entry<IntPair, Set<String>> mention : mentionLabelsMap.entrySet()) {
			if(mention.getValue().size() == 1) {
				String varName = null, entityName = null;
				for(String str : mention.getValue()) {
					if(str.startsWith("V")) {
						varName = str;
					}
					if(str.startsWith("E")) {
						entityName = str;
					}
				}
				if(varName != null) {
					spans.add(new Span("E"+varName.substring(1), mention.getKey()));
				}
			}
		}
		// Fill up eqSpans, and remove them from spans
		for(Span span : spans) {
			if(span.ip.getFirst() >= question.length()) {
				eqSpans.add(span);
			}
		}
		for(Span span : eqSpans) {
			spans.remove(span);
		}
	}
	
	public void extractClusters() {
		for(Span span : spans) {
			if(!span.label.startsWith("E")) continue;
			QuantSpan qs = getRelevantQuantSpans(span.ip);
			if(qs != null && !clusterMap.get(span.label).contains(qs)) {
				clusterMap.get(span.label).add(qs);
			}
		}
		// Remove duplicates if already present in E3
		for(QuantSpan qs : clusterMap.get("E3")) {
			if(clusterMap.get("E1").contains(qs)) {
				clusterMap.get("E1").remove(qs);
			}
			if(clusterMap.get("E2").contains(qs)) {
				clusterMap.get("E2").remove(qs);
			}
		}
	}
	
	public void extractEquations() throws IOException {
		String fileName = Params.annotationDir + index + ".txt";
		Map<String, String> variableNames = new HashMap<String, String>();
		List<String> variableNamesSorted = new ArrayList<String>();
		String txt = FileUtils.readFileToString(new File(fileName));
		List<String> lines = FileUtils.readLines(new File(fileName));
		// Find variable name maps
		for(Span vs : eqSpans) {
			if(vs.label.startsWith("V")) {
				variableNames.put(txt.substring(
						vs.ip.getFirst(), vs.ip.getSecond()), vs.label);
			}
		}
		// This is to ensure that longer name comes first, to prevent substring 
		// matching problem
		for(String var : variableNames.keySet()) {
			variableNamesSorted.add(var);
		}
		if(variableNamesSorted.size() == 2) {
			if(variableNamesSorted.get(0).length() < 
					variableNamesSorted.get(1).length()) {
				String tmp = variableNamesSorted.get(0);
				variableNamesSorted.set(0, variableNamesSorted.get(1));
				variableNamesSorted.set(1, tmp); 
			}
		}
		equations = new ArrayList<Equation>();
		for(int i = 2; i < lines.size()-1; ++i) {
			if(i % 2 == 0) {
				for(String varName : variableNamesSorted) {
//					System.out.println("Replacing "+varName+" with "+variableNames.get(varName));
					lines.set(i, lines.get(i).replaceAll(
							varName, 
							variableNames.get(varName)));
//					System.out.println("Resulting in "+lines.get(i));
				}
				equations.add(new Equation(index, lines.get(i), clusterMap));
			}
		}
	}

	public QuantSpan getRelevantQuantSpans(IntPair ip) {
		for(QuantSpan qs : quantities) {
			if((qs.start <= ip.getFirst() && ip.getFirst() < qs.end) 
					|| (ip.getFirst() <= qs.start  && qs.start < ip.getSecond())) {
				return qs;
			}
		}
		return null;
	}
	
}
