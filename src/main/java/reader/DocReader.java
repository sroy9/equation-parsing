package reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import structure.Equation;
import structure.KnowledgeBase;
import structure.SimulProb;
import utils.FeatGen;
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
				simulProb.extractEquations();
				simulProb.extractRelations();
				simulProb.extractEqParse();
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
	
	public static void print(SimulProb simulProb) {
		System.out.println(simulProb.index+" : "+simulProb.question);
		System.out.println("Skeleton : ");
		for(Pair<String, IntPair> pair : simulProb.skeleton) {
			System.out.print(pair.getFirst()+" ");
		}
		System.out.println();
		System.out.println("Quantities :");
		for(QuantSpan qs : simulProb.quantities) {
			System.out.println(simulProb.question.substring(
					qs.start, qs.end)+" : "+qs + " : "+Tools.getValue(qs));
		}
		System.out.println("Relation : "+Arrays.asList(simulProb.relations));
		System.out.println("Triggers : "+simulProb.triggers);
		System.out.println("Nodes : "+simulProb.nodes);
		for(Equation eq : simulProb.equations) {
			System.out.println("Equation :\n"+eq);
		}
	}
	
	public static void main(String args[]) throws Exception {
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir, 0, 1.0);
		for(SimulProb prob : simulProbList) {
			print(prob);
		}
		
	}
}
