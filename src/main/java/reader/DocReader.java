package reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import structure.Equation;
import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class DocReader {
	
	public static void createBratFiles(String eqParseFile) throws Exception {
		String lines[] = FileUtils.readFileToString(new File(eqParseFile)).split("\n");
		
		for(int i=0; i<lines.length; ++i) {
			if(lines[i].startsWith("#")) continue;
			TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
					lines[i], ViewNames.POS, false);
			List<Constituent> posTags = ta.getView(ViewNames.POS)
					.getConstituents();
			List<Constituent> chunks = Tools.curator.getTextAnnotationWithSingleView(
					lines[i], ViewNames.SHALLOW_PARSE, false)
					.getView(ViewNames.SHALLOW_PARSE).getConstituents();
			String str = "";
			str += lines[i]+"\n\n"+lines[i+1]+"\n\n";
			for(int j=0; j<=ta.size(); ++j) {
				for(Constituent cons : posTags) {
					if(cons.getLabel().startsWith("N") && 
							cons.getEndSpan() == j) {
						str += ")";
					}
				}
				for(Constituent cons : chunks) {
					if(cons.getEndSpan() == j) {
						str += "]";
					}
				}
				for(Constituent cons : chunks) {
					if(cons.getStartSpan() == j) {
						str += "[";
					}
				}
				for(Constituent cons : posTags) {
					if(cons.getLabel().startsWith("N") && 
							cons.getStartSpan() == j) {
						str += "(";
					}
				}
				if(j==ta.size()) continue;
				str += ta.getToken(j) + " ";
			}
			FileUtils.writeStringToFile(
					new File(Params.annotationDir+"/"+i+".txt"), 
					str);
			
			++i;
		}
	}
	
	// Reads list of files from brat folder
	public static List<SimulProb> readSimulProbFromBratDir(String bratDir) 
			throws Exception {
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
				System.out.println("Reading "+file.getName());
				int index = Integer.parseInt(file.getName().substring(
								0, 
								file.getName().length()-4));
				SimulProb simulProb = new SimulProb(index);
				simulProb.extractTextAndEquation();
				simulProb.extractQuantities(quantifier);
				simulProb.extractAnnotations();
				simulProb.extractVarTokens();
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
	
	public static void print(SimulProb simulProb) {
//		if(!simulProb.equation.toString().contains("V2")) {
		System.out.println(simulProb.index+" : "+simulProb.text);
		System.out.println("Quantities :");
		for(QuantSpan qs : simulProb.quantities) {
			System.out.print("[" + simulProb.text.substring(
					qs.start, qs.end)+" : "+ qs + " : "+Tools.getValue(qs) + "] ");
		}
		System.out.println();
		System.out.println("Equation : "+simulProb.equation);
		System.out.println("VarTokens : "+simulProb.varTokens);
		System.out.println();
		
	}
	
	public static List<List<Integer>> extractFolds() {
		List<List<Integer>> folds = new ArrayList<>();
		List<Integer> allIndices = new ArrayList<>();
		File dir = new File(Params.annotationDir);
		for(File file : dir.listFiles()) {
			if(file.getName().endsWith(".txt")) {
//				System.out.println("Reading "+file.getName());
				int index = Integer.parseInt(file.getName().substring(
								0, 
								file.getName().length()-4));
				allIndices.add(index);
			}
		}
		Collections.shuffle(allIndices);
		for(int i=0; i<5; ++i) {
			List<Integer> fold = new ArrayList<>();
			for(int j = (int) (i*allIndices.size()/5.0); 
					j < (int) ((i+1)*allIndices.size()/5.0); ++j) {
				fold.add(allIndices.get(j));
			}
			folds.add(fold);
		}
		return folds;
	}
	
	public static void main(String args[]) throws Exception {
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir, 0, 1.0);
		for(SimulProb prob : simulProbList) {
			print(prob);
		}
//		DocReader.createBratFiles("data/equationparse.txt");
		
	}
}
