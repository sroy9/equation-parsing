package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import com.google.gson.reflect.*;
import com.google.gson.*;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import structure.Equation;
import structure.KnowledgeBase;
import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class DocReader {
	
	// Reads list of files from brat folder
	public static List<SimulProb> readSimulProbFromBratDir(String bratDir) throws Exception {
		return readSimulProbFromBratDir(bratDir, 0.0, 1.0);
	}
	
	// Reads list of files from brat folder
	public static List<SimulProb> readSimulProbFromBratDir(
			String bratDir, double start, double end) throws Exception {
		List<SimulProb> simulProbList = new ArrayList<SimulProb>();
		Quantifier quantifier = new Quantifier();
		File dir = new File(bratDir);
		for(File file : dir.listFiles()) {
			if(file.getName().endsWith(".txt")) {
				int index = Integer.parseInt(file.getName().substring(
								0, 
								file.getName().length()-4));
				if(index == 2121 || index == 5894 || index == 1583 || index == 2455
						|| index == 5133 || index == 6027) {
					continue;
				}
				SimulProb simulProb = new SimulProb(index);
				simulProb.extractQuestionsAndSolutions();
				KnowledgeBase.appendWorldKnowledge(simulProb);
				simulProb.extractQuantities(quantifier);
				simulProb.extractAnnotations();
//				System.out.println(simulProb.index+" : "+simulProb.question);
//				System.out.println("Parse : ");
//				for(Constituent cons : simulProb.parse) {
//					System.out.println(cons.getLabel()+" : "+cons.getSurfaceString());
//				}
//				System.out.println("Skeleton : ");
//				for(Pair<String, IntPair> pair : simulProb.skeleton) {
//					System.out.print(pair.getFirst()+" ");
//				}
//				System.out.println();
//				System.out.println("Quantities :");
//				for(QuantSpan qs : simulProb.quantities) {
//					System.out.println(simulProb.question.substring(
//							qs.start, qs.end)+" : "+qs + " : "+Tools.getValue(qs));
//				}
				simulProb.extractEquations();
//				for(Equation eq : simulProb.equations) {
//					System.out.println("Equation :\n"+eq);
//				}
				simulProb.extractRelations();
//				System.out.println("Relation : "+Arrays.asList(simulProb.relations));
				simulProb.checkSolver();
				simulProbList.add(simulProb);
			}
		}
		List<SimulProb> newSimulProbList = new ArrayList<SimulProb>();
		for(int i = (int)(start*simulProbList.size()); 
				i < (int)(end*simulProbList.size()); 
				++i) {
			newSimulProbList.add(simulProbList.get(i));
		}
		return newSimulProbList;
	}
	
	public static List<List<Integer>> extractFolds() throws IOException {
		List<List<Integer>> folds = new ArrayList<>();
		for(int i=0; i<5; ++i) {
			String foldNumbers = FileUtils.readFileToString(
					new File(Params.foldsFile+i+".txt"));
			List<Integer> foldIndices = new ArrayList<>();
			for(String str : foldNumbers.split("\n")) {
				foldIndices.add(Integer.parseInt(str));
			}
			folds.add(foldIndices);
		}
		return folds;
	}
	
	public static List<String> getCompositionSentences(
			List<SimulProb> simulProbList) {
		List<String> sentenceList = new ArrayList<>();
		for(SimulProb prob : simulProbList) {
			List<Integer> tokenIdsR1 = Tools.getTokenIdsForRelation(
					prob.ta, prob.quantities, prob.relations, "R1");
			List<Integer> tokenIdsR2 = Tools.getTokenIdsForRelation(
					prob.ta, prob.quantities, prob.relations, "R2");
			if(tokenIdsR1.size() > 0 && 
					Tools.areAllTokensInSameSentence(prob.ta, tokenIdsR1)) {
				if(tokenIdsR2.size() == 0 || Tools.max(tokenIdsR1) < Tools.min(tokenIdsR2)
						|| Tools.max(tokenIdsR2) < Tools.min(tokenIdsR1)) {
					sentenceList.add(Tools.getSententialForm(
							prob.ta, Tools.min(tokenIdsR1), Tools.max(tokenIdsR1)));
				}
			}
			if(tokenIdsR2.size() > 0 && 
					Tools.areAllTokensInSameSentence(prob.ta, tokenIdsR2)) {
				if(tokenIdsR1.size() == 0 || Tools.max(tokenIdsR2) < Tools.min(tokenIdsR1)
						|| Tools.max(tokenIdsR1) < Tools.min(tokenIdsR2)) {
					sentenceList.add(Tools.getSententialForm(
							prob.ta, Tools.min(tokenIdsR2), Tools.max(tokenIdsR2)));
				}
			}	
		}
		return sentenceList;
	}
	
	public static void main(String args[]) throws Exception {
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir, 0, 1.0);
		List<String> compSentences = getCompositionSentences(simulProbList);
		for(String sent : compSentences) {
			System.out.println(sent);
		}
		
	}
}
