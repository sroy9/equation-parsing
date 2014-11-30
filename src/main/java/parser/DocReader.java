package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.reflect.*;
import com.google.gson.*;

import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;
import structure.Expression;
import structure.KnowledgeBase;
import structure.SimpleProb;
import structure.SimulProb;
import structure.Span;
import utils.Params;

public class DocReader {
	
	// Writes simpleProb to xml file
	public void writeSimpleProbToFile(List<SimpleProb> problemList,String xmlFile) 
			throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(xmlFile)));
		for(SimpleProb prob:problemList){
			bw.write("<problem>\n<question>\n"+prob.question+"\n</question>\n");
			bw.write("<answer>\n"+prob.answer+"\n</answer>\n");
			bw.write("<op>\n"+prob.operation+"\n</op>\n");
			bw.write("<val>\n"+prob.ans.value+"\n</val>\n");
			bw.write("<unit>\n"+prob.ans.units+"\n</unit>\n</problem>\n");		
		}
		bw.close();
	}
	
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
				if(index == 2121 || index== 2157 || index == 1658 || index == 3583 || index == 5102) {
					continue;
				}
				SimulProb simulProb = new SimulProb(index);
				
				simulProb.extractVariableSpans();
				simulProb.extractQuestionsAndEquations();
				KnowledgeBase.appendWorldKnowledge(simulProb);
				simulProb.extractQuantities(quantifier);
				simulProb.extractClusters();
				for(Expression eq : simulProb.equations) {
					eq.populateEntityName(simulProb);
					eq.populateCompleteFromEntity();
				}
				simulProb.nodeLinkingForEquations();
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
	
	// Cache parse tree and SRL annotations
	public void cacheAnnotations(List<SimulProb> simulProbList) throws Exception {
		NewCachingCurator curator = new NewCachingCurator("trollope.cs.illinois.edu", 9010,
				"/Users/subhroroy/cache/", null);	
		int count = 0;
		for(SimulProb simulProb : simulProbList) {
			TextAnnotation newTa = curator.getTextAnnotationWithSingleView(
					simulProb.question, ViewNames.POS, false);
			newTa = curator.getTextAnnotationWithSingleView(
					simulProb.question, ViewNames.SHALLOW_PARSE, false);
			newTa = curator.getTextAnnotationWithSingleView(
					simulProb.question, ViewNames.PARSE_BERKELEY, false);
			newTa = curator.getTextAnnotationWithSingleView(
					simulProb.question, ViewNames.DEPENDENCY_STANFORD, false);
			newTa = curator.getTextAnnotationWithSingleView(
					simulProb.question, ViewNames.SRL_VERB, false);
			System.out.println(count++);
		}
	}
	
	// Stats
	public void getStats() throws Exception{
		List<SimpleProb> problemList = readSimpleProbFromFile(Params.simpleProbFileXml);
//		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
//							new File(Experiment.simpleProbFileSer)));
//		List<SimpleProb> problemList = (List<SimpleProb>)in.readObject();
//		in.close();
		int count[][] = new int[10][4];
		for(int i=0;i<10;i++){
			for(int j=0;j<4;j++){
				count[i][j]=0;
			}
		}
		for(SimpleProb prob:problemList){
			prob.extractQuantities();
			System.out.println("Sentence : "+prob.question);
			System.out.println("Quantities detected : ");
			for(QuantSpan span : prob.quantities) {
				System.out.println(((Quantity)span.object) + " " + 
						span.start + " " + span.end);
			}
			System.out.println("Operation : " + prob.operation);
			System.out.println("Answer : "+prob.ans);
			if(prob.operation.equals("add"))
				count[prob.quantities.size()][0]++;
			if(prob.operation.equals("sub"))
				count[prob.quantities.size()][1]++;
			if(prob.operation.equals("mul"))
				count[prob.quantities.size()][2]++;
			if(prob.operation.equals("div"))
				count[prob.quantities.size()][3]++;
			System.out.println("Valid : " + prob.isValid() + 
					prob.quantities.size()) ;
		}
		
		System.out.println("Total: " + problemList.size());
		for(int i=0;i<10;i++){
			for(int j=0;j<4;j++){
				System.out.print(count[i][j]+" ");
			}
			System.out.println();
		}
	} 
	
	// Generate .txt and .ann files for brat annotation
//	public void generateFilesForBratAnnotation() throws Exception {
//		List<SimulProb> simulProbList = readSimulProbFromFile(
//				Params.simulProbFileJson);
//		String str;
//		for(SimulProb simulProb : simulProbList) {
//			str = simulProb.question;
//			for(Expression eq : simulProb.equations) {
//				str += "\n\n" + eq;
//			}
//			str += "\n\n";
//			for(Double soln : simulProb.solutions) {
//				str += soln + " ";
//			}
//			str = str.trim();
//			FileUtils.write(new File(
//					"" + simulProb.index+".txt"), 
//					str);
//		}
//	}
	
	public static void main(String args[]) throws Exception{
		
		DocReader dr = new DocReader();
		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(Params.annotationDir);
		for(SimulProb simulProb : simulProbList) {
			System.out.println("Index : "+simulProb.index);
			System.out.println("Question : "+simulProb.question);
			System.out.println("Equations : "+Arrays.asList(simulProb.equations));
			System.out.println("Solutions : "+Arrays.asList(simulProb.solutions));
		}
	}
}
