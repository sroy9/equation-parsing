package lbj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import structure.Equation;
import structure.Expression;
import structure.SimulProb;
import utils.FeatureExtraction;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class EquationFeatureManager {
	
	public void getAllFeatures(Equation equation, SimulProb simulProb) {
		// Add feature functions here
		equation.features = new ArrayList<String>();
		try {
			equation.features.addAll(getTokenFeatures(equation, simulProb));
//			equation.features.addAll(getClusterFeatures(equation, simulProb));
//			System.out.println("Eq : "+equation);
//			System.out.println(Arrays.asList(getTokenFeatures(equation, simulProb)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getTokenFeatures(Equation equation, SimulProb simulProb) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
				simulProb.question, ViewNames.LEMMA, false);
		String prefix1 = "", prefix2 = "";
		if(equation.terms.size() == 2) {
			prefix1 = "Op_" + equation.terms.get(0).getYield().size() + "_" + 
						equation.terms.get(1).getYield().size();
			prefix2 = "Op_" + equation.terms.get(1).getYield().size() + "_" + 
					equation.terms.get(0).getYield().size();
		}
		if(equation.terms.size() == 3) {
			prefix1 = "Op_" + equation.terms.get(0).getYield().size() + "_" + 
					equation.operations.get(0) + "_" + 
					equation.terms.get(1).getYield().size() + "_" + 
					equation.terms.get(2).getYield().size();
			prefix2 = "Op_" + equation.terms.get(1).getYield().size() + "_" + 
					equation.operations.get(0) + "_" + 
					equation.terms.get(0).getYield().size() + "_" + 
					equation.terms.get(2).getYield().size();
		}
		for(Expression term : equation.terms) {
			for(Expression leaf : term.getYield()) {
				if(leaf.varName != null && leaf.varName.startsWith("V")) {
					prefix1 += "_VAR";
					prefix2 += "_VAR";
					break;
				}
			}
		}
		String str = "";
		for(Expression term : equation.terms) {
			if(term.varName != null && term.varName.startsWith("V")) {
				str += "_SINGLEVAR";
			}
		}
		features.add(prefix1 + "_" + str);
		features.add(prefix2 + "_" + str);
		for(String feature : FeatureExtraction.getUnigrams(simulProb.question)) {
			features.add(prefix1 + "_" + feature);
			features.add(prefix2 + "_" + feature);
		}
		for(String feature : FeatureExtraction.getBigrams(simulProb.question)) {
			features.add(prefix1 + "_" + feature);
			features.add(prefix2 + "_" + feature);
		}
		
		features.add(prefix1 + "_" + ta.getNumberOfSentences());
		features.add(prefix2 + "_" + ta.getNumberOfSentences());
		
		if(equation.terms.size() == 3) {
			for(IntPair ip : equation.terms.get(2).getAllMentionLocs(simulProb)) {
				for(String feature : FeatureExtraction.getFormPP(
						simulProb.question, ip.getFirst(), 2)) {
					features.add(prefix1 + "_E3_" + feature);
					features.add(prefix2 + "_E3_" + feature);
				}
				for(String feature : FeatureExtraction.getMixed(
						simulProb.question, ip.getFirst(), 2)) {
					features.add(prefix1 + "_E3_" + feature);
					features.add(prefix2 + "_E3_" + feature);
				}
				for(String feature : FeatureExtraction.getPOSWindowPP(
						simulProb.question, ip.getFirst(), 2)) {
					features.add(prefix1 + "_E3_" + feature);
					features.add(prefix2 + "_E3_" + feature);
				}
			}
		}
		return features;
	}
	
	public List<String> getClusterFeatures(Equation equation, SimulProb simulProb) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix1 = "", prefix2 = "";
		if(equation.terms.size() == 2) {
			prefix1 = "Op_" + equation.terms.get(0).getYield().size() + "_" + 
						equation.terms.get(1).getYield().size();
			prefix2 = "Op_" + equation.terms.get(1).getYield().size() + "_" + 
					equation.terms.get(0).getYield().size();
		}
		if(equation.terms.size() == 3) {
			prefix1 = "Op_" + equation.terms.get(0).getYield().size() + "_" + 
					equation.operations.get(0) + "_" + 
					equation.terms.get(1).getYield().size() + "_" + 
					equation.terms.get(2).getYield().size();
			prefix2 = "Op_" + equation.terms.get(1).getYield().size() + "_" + 
					equation.operations.get(0) + "_" + 
					equation.terms.get(0).getYield().size() + "_" + 
					equation.terms.get(2).getYield().size();
		}
		for(Expression term : equation.terms) {
			for(Expression leaf : term.getYield()) {
				if(leaf.varName != null && leaf.varName.startsWith("V")) {
					prefix1 += "_VAR";
					prefix2 += "_VAR";
					break;
				}
			}
		}
		features.add(prefix1+"_Num_clusters_"+equation.termMap.keySet().size());
		features.add(prefix2+"_Num_clusters_"+equation.termMap.keySet().size());
		for(Expression term : equation.termMap.get("E1")) {
			if(!term.equalsBasedOnLeaves(equation.terms.get(0))) {
				features.add(prefix1+"_LEFT_"+term.getYield().size());
				features.add(prefix2+"_LEFT_"+term.getYield().size());
				if(term.varName != null && term.varName.startsWith("V")) {
					features.add(prefix1+"_LEFT_SINGLEVAR");
					features.add(prefix2+"_LEFT_SINGLEVAR");
					
				}
			}
		}
		for(Expression term : equation.termMap.get("E2")) {
			if(!term.equalsBasedOnLeaves(equation.terms.get(1))) {
				features.add(prefix1+"_LEFT_"+term.getYield().size());
				features.add(prefix2+"_LEFT_"+term.getYield().size());
				if(term.varName != null && term.varName.startsWith("V")) {
					features.add(prefix1+"_LEFT_SINGLEVAR");
					features.add(prefix2+"_LEFT_SINGLEVAR");
				}
			}
			
		}
		if(equation.terms.size() == 3) {
			for(Expression term : equation.termMap.get("E3")) {
				if(!term.equalsBasedOnLeaves(equation.terms.get(2))) {
					features.add(prefix1+"_LEFT_"+term.getYield().size());
					features.add(prefix2+"_LEFT_"+term.getYield().size());
					if(term.varName != null && term.varName.startsWith("V")) {
						features.add(prefix1+"_LEFT_SINGLEVAR");
						features.add(prefix2+"_LEFT_SINGLEVAR");
					}
				}
				
			}
		}
		return features;
	}

}
