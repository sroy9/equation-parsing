package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.reflect.*;
import com.google.gson.*;

import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import structure.KnowledgeBase;
import structure.SimpleProb;
import structure.SimulProb;

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
				simulProb.extractNpSpans();
				simulProb.extractClusters();
				simulProb.extractEquations();
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
