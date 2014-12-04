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
	
	// Reads simpleProb from xml file
	public List<SimpleProb> readSimpleProbFromFile(String xmlFile) throws Exception {
		List<SimpleProb> problemList = new ArrayList<SimpleProb>();
		BufferedReader br = new BufferedReader(new FileReader(xmlFile));
		while(true){
			String str = br.readLine();
			if(str==null)
				break;
			br.readLine();
			String question = br.readLine();
			br.readLine();br.readLine();
			String answer = br.readLine();
			br.readLine();br.readLine();
			String op = br.readLine();
			br.readLine();br.readLine();
			String str1 = br.readLine();
			double val = Double.parseDouble(str1);
			br.readLine();br.readLine();
			String unit = br.readLine();
			br.readLine();br.readLine();
			System.out.println(xmlFile+" "+question);
			SimpleProb prob = new SimpleProb(question,answer,op,val,unit);
			problemList.add(prob);
		}
		br.close();
		return problemList;
	}
	
	// Reads list of simulProbs from json file, uses guava
	// Also brat annotations 
	// TODO merge everything into json file, once we are decided about the 
	// annotations
	public List<SimulProb> readSimulProbFromFile(String jsonFile) throws Exception {
	    String sFileContents = null;
	    try{
	      sFileContents = 
	        FileUtils.readFileToString(new File(jsonFile));
	    } catch(IOException ex){
	      throw new RuntimeException(ex);
	    }
	    List<SimulProb> lQuestions = 
	      new Gson().fromJson( 
	    		  sFileContents,
	              new TypeToken<List<SimulProb>>(){}.getType());
	    // Get annotations for variables
	    for(SimulProb simulProb : lQuestions) {
	    	simulProb.extractVariableSpans();
	    }
	    return lQuestions;
	}
	
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
				simulProb.extractQuestionsAndEquations();
				KnowledgeBase.appendWorldKnowledge(simulProb);
				simulProb.extractQuantities(quantifier);
				simulProb.extractClusters();
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
	
	// Writes list of simulProb to json file (uses guava)
	static void writeSimulProbToJson(List<SimulProb> lQuestions, String sFilename) {
	    Gson gson = new GsonBuilder()
	      .setPrettyPrinting()
	      .disableHtmlEscaping()
	      .create();
	    String sJson = gson.toJson(lQuestions);
	    try{
	      FileUtils.writeStringToFile(new File(sFilename), sJson);
	    }catch(IOException ex){
	      throw new RuntimeException(ex);
	    }
	}
}
