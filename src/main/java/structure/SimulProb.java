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
	public String text;
	public boolean isOneVar;
	public Equation equation;
	public List<QuantSpan> quantities;
	public Map<String, List<Integer>> varTokens;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> chunks;
	public List<Constituent> parse;
	public List<Pair<String, IntPair>> skeleton;
  	
	public SimulProb(int index) {
		this.index = index;
		equation = new Equation();
		quantities = new ArrayList<QuantSpan>();
		varTokens = new HashMap<>();
	}
	
	public void extractQuantities(Quantifier quantifier) throws IOException {
		List<QuantSpan> spanArray = quantifier.getSpans(text, true);
		quantities = new ArrayList<QuantSpan>();
		for(QuantSpan span : spanArray){
			if(span.object instanceof Quantity || span.object instanceof Ratio) {
				quantities.add(span);
			}
		}
	}
	
	public void extractTextAndEquation() throws Exception {
		String fileName = Params.annotationDir + index + ".txt";
		List<String> lines = FileUtils.readLines(new File(fileName));
		text = lines.get(0);
		equation = new Equation(0, lines.get(2));
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
		ta = new TextAnnotation("", "", text);
		posTags = Tools.curator.getTextAnnotationWithSingleView(
				text, ViewNames.POS, false)
				.getView(ViewNames.POS).getConstituents();
		lemmas = Tools.curator.getTextAnnotationWithSingleView(
				text, ViewNames.LEMMA, false)
				.getView(ViewNames.LEMMA).getConstituents();
		chunks = Tools.curator.getTextAnnotationWithSingleView(
				text, ViewNames.SHALLOW_PARSE, false)
				.getView(ViewNames.SHALLOW_PARSE).getConstituents();
		parse = Tools.curator.getTextAnnotationWithSingleView(
				text, ViewNames.PARSE_STANFORD, false)
				.getView(ViewNames.PARSE_STANFORD).getConstituents();
		skeleton = Tools.getSkeleton(ta, lemmas, parse, quantities);
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
			if(!varTokens.containsKey(label)) {
				varTokens.put(label, new ArrayList<Integer>());
			}
			for(int i=start; i<end; ++i) {
				if(posTags.get(i).getLabel().startsWith("N") || 
						posTags.get(i).getLabel().startsWith("V")) {
					varTokens.get(label).add(i);
				}
			}
		}
	}
	
	public static float getVarTokenLossOrderFixed(
			Map<String, List<Integer>> varToken1,
			Map<String, List<Integer>> varToken2) {
		float loss1 = 0.0f, loss2 = 0.0f;
		for(String key : varToken1.keySet()) {
			if(!varToken1.get(key).contains(varToken2.get(key).get(0))) {
				loss1 += 1.0;
			}
			if(!varToken2.get(key).contains(varToken1.get(key).get(0))) {
				loss2 += 1.0;
			}
		}
		return Math.min(loss1, loss2);
	}
}
