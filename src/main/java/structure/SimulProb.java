package structure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;
import edu.illinois.cs.cogcomp.quant.standardize.Ratio;
 
public class SimulProb {
	
	public int index;
	public String question;
	public boolean isOneVar;
	public List<Equation> equations;
	public List<Double> solutions;
	public List<QuantSpan> quantities;
	public List<String> relations;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> chunks;
	public List<Constituent> parse;
	public List<Pair<String, IntPair>> skeleton;
	public List<Node> nodes;
  	
	public SimulProb(int index) {
		this.index = index;
		equations = new ArrayList<Equation>();
		solutions = new ArrayList<Double>();
		quantities = new ArrayList<QuantSpan>();
		relations = new ArrayList<String>();
		nodes = new ArrayList<>();
	}
	
	public void extractQuantities(Quantifier quantifier) throws IOException {
		List<QuantSpan> spanArray = quantifier.getSpans(question, true);
		quantities = new ArrayList<QuantSpan>();
		for(QuantSpan span : spanArray){
			if(span.object instanceof Quantity || span.object instanceof Ratio) {
				quantities.add(span);
			}
		}
	}
	
	public void extractQuestionsAndSolutions() throws Exception {
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
		if(equations.size() == 1) {
			equations.get(0).isOneVar = true;
		} else {
			boolean oneVar = true;
			Equation eq = equations.get(1);
			for(int i=0; i<5; ++i) {
				if(eq.terms.get(i).size() > 0) {
					oneVar = false;
					break;
				}
			}
			for(int i=0; i<4; ++i) {
				if(eq.operations.get(i) != Operation.ADD && i%2 == 0) {
					oneVar = false;
					break;
				}
				if(eq.operations.get(i) != Operation.NONE && i%2 == 1) {
					oneVar = false;
					break;
				}
			}
			if(oneVar) {
				equations.get(0).isOneVar = true;
				equations.remove(1);
			}
		}
		isOneVar = equations.size() == 1 ? true : false;
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
	
	public void extractAnnotations() throws Exception {
		ta = new TextAnnotation("", "", question);
		posTags = Tools.curator.getTextAnnotationWithSingleView(
				question, ViewNames.POS, false)
				.getView(ViewNames.POS).getConstituents();
		lemmas = Tools.curator.getTextAnnotationWithSingleView(
				question, ViewNames.LEMMA, false)
				.getView(ViewNames.LEMMA).getConstituents();
		chunks = Tools.curator.getTextAnnotationWithSingleView(
				question, ViewNames.SHALLOW_PARSE, false)
				.getView(ViewNames.SHALLOW_PARSE).getConstituents();
		parse = Tools.curator.getTextAnnotationWithSingleView(
				question, ViewNames.PARSE_STANFORD, false)
				.getView(ViewNames.PARSE_STANFORD).getConstituents();
		skeleton = Tools.getSkeleton(ta, lemmas, parse, quantities);
	}

	public void extractEqParse() throws IOException {
		String annFile = Params.annotationDir+"/"+index+".ann";
		for(int i=0; i<ta.size(); ++i) {
			if(KnowledgeBase.mathNodeSet.contains(
					ta.getToken(i).toLowerCase())) {
				triggers.add(new Trigger(i, "OP", null));
			} else {
				for(int j=0; j<quantities.size(); ++j) {
					int start = ta.getTokenIdFromCharacterOffset(
							quantities.get(j).start);
					if(i == start) {
						triggers.add(new Trigger(i, "NUMBER", 
								Tools.getValue(quantities.get(j))));
						break;
					}
				}
			}
		}
		List<String> lines = FileUtils.readLines(new File(annFile));
		for(String line : lines) {
			String strArr[] = line.split("\t")[1].split(" ");
			String label = strArr[0];
			int start = ta.getTokenIdFromCharacterOffset(
					Integer.parseInt(strArr[1]));
			int end = ta.getTokenIdFromCharacterOffset(
					Integer.parseInt(strArr[2])-1)+1;
			List<Integer> relevantIndex = new ArrayList<>();
			for(int i=0; i<triggers.size(); ++i) {
				if(triggers.get(i).index>=start && 
						triggers.get(i).index<end) {
					relevantIndex.add(i);
				}
			}
			if(relevantIndex.size() == 0) continue;
			nodes.add(new Node(label, new IntPair(
					relevantIndex.get(0), 
					relevantIndex.get(relevantIndex.size()-1)+1), 
					new ArrayList<Node>()));
		}
		
		
	}
}
