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
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;
import edu.illinois.cs.cogcomp.quant.standardize.Ratio;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
 
public class SimulProb {
	
	public int index;
	public String question;
	public List<Equation> equations;
	public List<Double> solutions;
	public List<QuantSpan> quantities; 	
	public List<Span> npSpans;
	public List<Span> eqSpans;
	public List<Span> spans;
	public Map<String, QuantState> clusterMap;
	
	public SimulProb(int index) {
		this.index = index;
		equations = new ArrayList<Equation>();
		solutions = new ArrayList<Double>();
		quantities = new ArrayList<QuantSpan>();
		spans = new ArrayList<Span>();
		npSpans = new ArrayList<>();
		eqSpans = new ArrayList<>();
		clusterMap = new HashMap<String, QuantState>();
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
		
		for(Entry<IntPair, Set<String>> mention : mentionLabelsMap.entrySet()) {
			if(mention.getValue().size() == 1) {
				continue;
			}
			// Find variable and entity associated with a mention
			String varName = null, entityName = null;
			for(String str : mention.getValue()) {
				if(str.startsWith("V")) {
					varName = str;
				}
				if(str.startsWith("E")) {
					entityName = str;
				}
			}
			// Now check id varName has occurred with other entity
			boolean withTwoEntities = false;
			for(Entry<IntPair, Set<String>> mention1 : mentionLabelsMap.entrySet()) {
				if(mention1.getValue().contains(varName)) {
					for(String str : mention1.getValue()) {
						if(str.startsWith("E") && !str.equals(entityName)) {
							withTwoEntities = true;
						}
					}
				}
			}
			// If unique, go ahead and fill up missing entries
			if(!withTwoEntities) {
				for(Entry<IntPair, Set<String>> mention1 : mentionLabelsMap.entrySet()) {
					if(mention1.getValue().contains(varName) && 
							mention1.getValue().size() == 1) {
						spans.add(new Span(entityName, mention1.getKey()));
						mention1.getValue().add(entityName);
					}
				}
			}
		}
	}
	
	public void extractEqSpans() {
		for(Span span : spans) {
			if(span.ip.getFirst() >= question.length()) {
				eqSpans.add(span);
			}
		}
	}
	
	// Assumed to be called after extractVariableSpan() and quantities, and problem 
	// extraction
	// Extracts candidate NPs which can belong to some entity
	public void extractNpSpans() throws Exception {
		List<Span> candidateNpSpans = Tools.getCandidateNPs(question, quantities);
		System.out.println("NP Spans : ");
		for(Span span : candidateNpSpans) {
			System.out.println(question.substring(
					span.ip.getFirst(), span.ip.getSecond()));
		}
		for(Span span : candidateNpSpans) {
			for(Span span1 : spans) {
				if(Tools.doesIntersect(span.ip, span1.ip) && 
						span1.label.startsWith("E")) {
					span.label = span1.label;
					npSpans.add(span);
					break;
				}
			}
		}
	}
	
	public void extractClusters() {
		clusterMap = new HashMap<String, QuantState>();
		for(Span span : npSpans) {
			if(!clusterMap.keySet().contains(span.label)) {
				clusterMap.put(span.label, new QuantState(span.label));
			}
			QuantSpan qs = getRelevantQuantSpans(span.ip);
			if(qs != null) {
				if(getValue(qs) != null) {
					clusterMap.get(span.label).addToCluster(getValue(qs)+"", span.ip);
				}
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
		System.out.println(Arrays.asList(variableNames));
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
					System.out.println("Replacing "+varName+" with "+variableNames.get(varName));
					lines.set(i, lines.get(i).replaceAll(
							varName, 
							variableNames.get(varName)));
					System.out.println("Resulting in "+lines.get(i));
				}
				equations.add(new Equation(lines.get(i), clusterMap));
			}
		}
	}
	
	// Returns V1 or V2, if ip corresponds to such a label, otherwise returns null
	public String getVariable(IntPair ip) {
		for(Span vs : spans) {
			if(vs.ip.getFirst() == ip.getFirst() 
					&& vs.ip.getSecond() == ip.getSecond() 
					&& vs.label.startsWith("V")) {
				return vs.label;
			}
		}
		return null;
	}
	
	public Double getValue(QuantSpan qs) {
		if (qs.object instanceof Quantity) {
			return ((Quantity)qs.object).value;
		} else if (qs.object instanceof Ratio) {
			return ((Ratio)qs.object).numerator.value / 
					((Ratio)qs.object).denominator.value;
		}
		return null;
	}
	
	public String getUnit(QuantSpan qs) {
		if (qs.object instanceof Quantity) {
			return ((Quantity)qs.object).units;
		} else if (qs.object instanceof Ratio) {
			return ((Ratio)qs.object).denominator.units;
		}
		return null;
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
