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
import structure.SimpleProb;
import structure.SimulProb;
import structure.Span;

public class DocReader {
	
	// Reads list of files from brat folder
	public List<SimulProb> readSimulProbFromBratDir(String bratDir) throws Exception {
		return readSimulProbFromBratDir(bratDir, 0.0, 1.0);
	}
	
	// Reads list of files from brat folder
	public List<SimulProb> readSimulProbFromBratDir(
			String bratDir, double start, double end) throws Exception {
		List<SimulProb> simulProbList = new ArrayList<SimulProb>();
		Quantifier quantifier = new Quantifier();
		File dir = new File(bratDir);
		for(File file : dir.listFiles()) {
			if(file.getName().endsWith(".txt")) {
				int index = Integer.parseInt(file.getName().substring(
								0, 
								file.getName().length()-4));
				if(index == 2121 || index== 2157 || index == 1658 || 
						index == 3583 || index == 5102) {
					continue;
				}
				SimulProb simulProb = new SimulProb(index);
				simulProb.extractVariableSpans();
				simulProb.extractQuestionsAndSolutions();
				KnowledgeBase.appendWorldKnowledge(simulProb);
				simulProb.extractQuantities(quantifier);
				System.out.println(simulProb.index+" : "+simulProb.question);
				System.out.println("Quantities :");
				for(QuantSpan qs : simulProb.quantities) {
					System.out.println(simulProb.question.substring(
							qs.start, qs.end)+" : "+qs);
				}
				simulProb.extractNpSpans();
				System.out.println("Spans :");
				for(Span span : simulProb.npSpans) {
					System.out.println(span.label+" : "+simulProb.question.substring(
							span.ip.getFirst(), span.ip.getSecond())+ " : "+span.ip);
				}
				simulProb.extractClusters();
				System.out.println("Cluster Map :");
				for(String entity : simulProb.clusterMap.keySet()) {
					System.out.println(entity + " : " + Arrays.asList(
							simulProb.clusterMap.get(entity).mentionLocMap.keySet()));
				}
				simulProb.extractEquations();
				System.out.println("Equations :");
				for(Equation eq : simulProb.equations) {
					System.out.println(eq.toString());
				}
				simulProb.spans = null; // To prevent it from being used
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
}
