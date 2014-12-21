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

import latentsvm.Blob;
import latentsvm.Lattice;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;

import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
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
	
	public SimulProb(int index) {
		this.index = index;
		equations = new ArrayList<Equation>();
		solutions = new ArrayList<Double>();
		quantities = new ArrayList<QuantSpan>();
	}
	
	public void extractQuantities(Quantifier quantifier) throws IOException {
		List<QuantSpan> spanArray = quantifier.getSpans(question, true);
		quantities = new ArrayList<QuantSpan>();
		for(QuantSpan span:spanArray){
			if(span.object instanceof Quantity || span.object instanceof Ratio) {
				quantities.add(span);
			}
		}
	}
	
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

	// Equations will be changed to replace variable names by V1, V2
	public void extractEquations() throws IOException {
		String fileName = Params.annotationDir + index + ".txt";
		Map<String, String> variableNames = new HashMap<String, String>();
		List<String> variableNamesSorted = new ArrayList<String>();
		List<String> lines = FileUtils.readLines(new File(fileName));
		List<String> equationStrings = new ArrayList<>();
		equations = new ArrayList<Equation>();
		for(int i = 2; i < lines.size()-1; ++i) {
			if(i % 2 == 0) {
				equationStrings.add(lines.get(i).replaceAll("\\(|\\)", ""));
			}
		}
		for(String eq : equationStrings) {
			for(String str : eq.split("(\\+|\\-|\\*|\\/|=)")) {
				if(str.length() == 0) continue;
				try {
					Double d = Double.parseDouble(str.trim());
				} catch(NumberFormatException e) {
					int size = variableNames.keySet().size();
					if(!variableNames.keySet().contains(str.trim())) {
						variableNames.put(str.trim(), "V"+(size+1));	
					}
				}
			}
		}
		System.out.println("Variable Names : "+Arrays.asList(variableNames));
		if(variableNames.size() > 2) System.out.println("ISSUE HERE : "+index);
		
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
		for(int i=0; i<equationStrings.size(); ++i) {
			for(String varName : variableNamesSorted) {
				equationStrings.set(i, equationStrings.get(i).replaceAll(
						varName, 
						variableNames.get(varName)));
			}
			equations.add(new Equation(index, equationStrings.get(i)));
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
	
	public void checkSolver() throws Exception {
		List<Double> solns = EquationSolver.solve(new Lattice(equations, new Blob(this)));
		System.out.println("Gold solutions : "+Arrays.asList(solutions));
		System.out.println("Predicted solutions : "+Arrays.asList(solns));
		if(solns == null) System.out.println("Error : No solutions : "+index);
		for(Double d1 : solutions) {
			boolean present = false;
			for(Double d2 : solns) {
				if(Tools.safeEquals(d1, d2)) {
					present = true;
					break;
				}
			}
			if(!present) {
				System.out.println("Error : Solutions not matching : "+index);
			}
		}
	}
	
	
}
