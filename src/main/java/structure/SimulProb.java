package structure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> chunks;
	public List<IntPair> candidateVars;
  	
	public SimulProb(int index) {
		this.index = index;
		equation = new Equation();
		quantities = new ArrayList<QuantSpan>();
		varTokens = new HashMap<>();
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
	}
	
	public void createCandidateVars() {
		candidateVars = new ArrayList<>();
		for(Constituent cons : chunks) {
			if(cons.getLabel().equals("NP")) {
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
			boolean foundNP = false;
			for(int i=0; i<candidateVars.size(); ++i) {
				IntPair ip = candidateVars.get(i);
				if(Tools.doesIntersect(ip, new IntPair(start, end))) {
					if(!varTokens.containsKey(label)) {
						varTokens.put(label, new ArrayList<Integer>());
					}
					varTokens.get(label).add(i);
					foundNP = true;
				}
			}
			if(!foundNP) {
				System.out.println("Problem : "+text);
				System.out.println("Shallow parse : "+Arrays.asList(chunks));
				System.out.println("NP not found for "+line.split("\t")[2]);
			}
		}
	}
	
	
	public static float getVarTokenLoss(Map<String, List<Integer>> gold,
			Map<String, List<Integer>> pred, boolean varNameSwap) {
//		System.out.println("VarTokenLoss called with : "+Arrays.asList(gold)+" and "+
//			Arrays.asList(pred));
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
		return loss;
	}
}
