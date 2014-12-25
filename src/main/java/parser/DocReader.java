package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.reflect.*;
import com.google.gson.*;

import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import structure.Equation;
import structure.KnowledgeBase;
import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class DocReader {
	
	// Reads list of files from brat folder
	public List<SimulProb> readSimulProbFromBratDir(String bratDir) throws Exception {
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
//				System.out.println(simulProb.index+" : "+simulProb.question);
//				System.out.println("Quantities :");
//				for(QuantSpan qs : simulProb.quantities) {
//					System.out.println(simulProb.question.substring(
//							qs.start, qs.end)+" : "+qs + " : "+Tools.getValue(qs));
//				}
				simulProb.extractEquations();
				simulProb.extractRelations();
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
	
	public static void main(String args[]) throws Exception {
		DocReader.readSimulProbFromBratDir(Params.annotationDir, 0, 1.0);
	}
}
