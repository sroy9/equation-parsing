package reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import structure.Equation;
import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class DocReader {
	
	public static void createBratFiles(String eqParseFile) throws IOException {
		String lines[] = FileUtils.readFileToString(new File(eqParseFile)).split("\n");
		for(int i=0; i<lines.length; ++i) {
			if(lines[i].startsWith("#")) continue;
			FileUtils.writeStringToFile(
					new File(Params.annotationDir+"/"+i+".txt"), 
					lines[i]+"\n\n"+lines[i+1]);
			FileUtils.writeStringToFile(
					new File(Params.annotationDir+"/"+i+".ann"), 
					"");
			++i;
		}
	}
	
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
				SimulProb simulProb = new SimulProb(index);
				simulProb.extractQuestionsAndSolutions();
				simulProb.extractQuantities(quantifier);
				simulProb.extractAnnotations();
				simulProb.extractEquations();
				simulProb.extractEqParse();
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
		System.out.println("Nodes : "+simulProb.nodes);
		for(Equation eq : simulProb.equations) {
			System.out.println("Equation :\n"+eq);
		}
	}
	
	
	public static void main(String args[]) throws Exception {
//		List<SimulProb> simulProbList = 
//				DocReader.readSimulProbFromBratDir(Params.annotationDir, 0, 1.0);
//		for(SimulProb prob : simulProbList) {
//			print(prob);
//		}
		DocReader.createBratFiles("data/equationparse.txt");
		
	}
}
