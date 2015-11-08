package structure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import utils.Params;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;
import edu.illinois.cs.cogcomp.quant.standardize.Ratio;
 
public class SimulProb {
	
	public int index;
	public String text;
	public boolean isOneVar;
	public Equation equation;
	public List<QuantSpan> quantities;
	public Map<String, List<Integer>> varTokens;
	public boolean coref;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> chunks;
	public List<Constituent> parse;
	public List<IntPair> candidateVars;
  	
	public SimulProb(int index) {
		this.index = index;
		equation = new Equation();
		quantities = new ArrayList<QuantSpan>();
		varTokens = new HashMap<>();
		coref = false;
	}
	
	public void extractQuantities() throws Exception {
		List<QuantSpan> spanArray = Tools.quantifier.getSpans(text);
		quantities = new ArrayList<QuantSpan>();
		for(QuantSpan span : spanArray){
			if(span.object instanceof Quantity || span.object instanceof Ratio) {
				quantities.add(span);
				for(int i=span.end; i<Math.min(span.end+3, text.length()); ++i) {
					if(text.charAt(i) == '%') {
						// This assumes usage of simple quantifier
						((Quantity)span.object).value *= 0.01;
						break;
					}
				}
				if(span.start>0 && text.charAt(span.start-1)=='-') {
					((Quantity)span.object).value *= -1;
				}
				if(span.end+6 <= text.length() && text.substring(span.end, span.end+6).contains("cents")) {
					((Quantity)span.object).value *= 0.01;
				}
			}
		}
	}
	
	public void extractTextAndEquation() throws Exception {
		String fileName = Params.annotationDir + index + ".txt";
		List<String> lines = FileUtils.readLines(new File(fileName));
		text = lines.get(0);
		equation = new Equation(lines.get(2));
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
		ta = Tools.pipeline.createAnnotatedTextAnnotation(text, false);
		posTags = ta.getView(ViewNames.POS).getConstituents();
		chunks = ta.getView(ViewNames.SHALLOW_PARSE).getConstituents();
		parse = ta.getView(ViewNames.PARSE_STANFORD).getConstituents();
	}
	
	public void createCandidateVars() {
		candidateVars = new ArrayList<>();
		for(Constituent cons : parse) {
			if(cons.getLabel().startsWith("NP") || cons.getLabel().startsWith("NN")) {
				candidateVars.add(cons.getSpan());
			}
		}
	}
	
	public void extractVarTokens() throws IOException {
		String annFile = Params.annotationDir+"/"+index+".ann";
		List<String> lines = FileUtils.readLines(new File(annFile));
		for(String line : lines) {
			String strArr[] = line.split("\t")[1].split(" ");
			String label = strArr[0];
			int start = ta.getTokenIdFromCharacterOffset(
					Integer.parseInt(strArr[1]));
			int end = ta.getTokenIdFromCharacterOffset(
					Integer.parseInt(strArr[2])-1)+1;
			int bestIp = -1;
			Double bestJscore = 0.0;
			for(int i=0; i<candidateVars.size(); ++i) {
				IntPair ip = candidateVars.get(i);
				double score = Tools.getJaccardScore(ip, new IntPair(start, end));
				if(score > bestJscore) {
					bestJscore = score;
					bestIp = i;
				}
			}
			if(bestIp < 0) {
				System.out.println("Problem : "+text);
				System.out.println("Parse : ");
				for(Constituent cons : parse) {
					System.out.println(cons.getLabel()+" : "+cons.getSurfaceForm());
				}
				System.out.println("NP not found for "+line.split("\t")[2]);
			} else {
				if(!varTokens.containsKey(label)) {
					varTokens.put(label, new ArrayList<Integer>());
				}
				varTokens.get(label).add(bestIp);
			}
		}
		int numV1 = 0;
		for(Node leaf : equation.root.getLeaves()) {
			if(leaf.label.equals("VAR") && leaf.varId.equals("V1")) {
				numV1++;
			}
		}
		if(numV1>1) {
			coref = true;
			varTokens.put("V2", varTokens.get("V1"));
			for(Node leaf : equation.root.getLeaves()) {
				if(leaf.label.equals("VAR") && leaf.varId.equals("V1")) {
					leaf.varId = "V2";
					break;
				}
			}
		}
	}
	
	public static float getVarTokenLoss(
			Map<String, List<Integer>> gold, boolean goldCoref,
			Map<String, List<Integer>> pred, boolean predCoref,
			boolean varNameSwap) {
		float loss = 0.0f;
		if(gold.keySet().size() != pred.keySet().size()) return 4.0f;
		if(gold.keySet().size() == 1) {
			if(!gold.get("V1").contains(pred.get("V1").get(0))) {
				loss += 1.0;
			}
		} else {
			if(!varNameSwap && !gold.get("V1").contains(pred.get("V1").get(0))) {
				loss += 1.0;
			}
			if(!varNameSwap && !gold.get("V2").contains(pred.get("V2").get(0))) {
				loss += 1.0;
			}
			if(varNameSwap && !gold.get("V1").contains(pred.get("V2").get(0))) {
				loss += 1.0;
			}
			if(varNameSwap && !gold.get("V2").contains(pred.get("V1").get(0))) {
				loss += 1.0;
			}
		}
		return loss + ((goldCoref == predCoref)?0.0f:1.0f);
	}
}
