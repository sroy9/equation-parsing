package reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import structure.Node;
import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class DocReader {
	
	public static Set<String> preds = new HashSet<>();
	
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
					new File(Params.annotationDir+"/"+(i+999000)+".txt"), 
					str);
//			FileUtils.writeStringToFile(
//					new File(Params.annotationDir+"/"+(i+999000)+".ann"), 
//					"");
			
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
//				System.out.println("Reading "+file.getName());
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
		System.out.println("Leaves : "+simulProb.equation.root.getLeaves());
		System.out.println("VarTokens : "+simulProb.varTokens);
		sanityCheck(simulProb);
		System.out.println();
		
	}
	
	public static void sanityCheck(SimulProb prob) {
		for(Node node : prob.equation.root.getLeaves()) {
			if(node.label.equals("VAR")) continue;
			boolean allow = false;
			for(int i=0; i<prob.quantities.size(); ++i) {
				if(Tools.safeEquals(Tools.getValue(
						prob.quantities.get(i)), node.value)) {
					allow = true;
					break;
				}
			}
			if(!allow) {
				System.out.println("Not found : "+node.value);
			}
		}
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
		Collections.shuffle(allIndices,new Random(0));
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
	
	public static void createLambdaExpForSPF() throws Exception {
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir, 0, 1.0);
		List<List<Integer>> folds = extractFolds();
		int count = 0;
		for(List<Integer> fold : folds) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					new File("fold"+(count)+".ccg")));
			BufferedWriter npList = new BufferedWriter(new FileWriter(
					new File("nplist"+(count++)+".ont")));
			for(SimulProb prob : simulProbList) {
				// All numbers should always be available
				for(int i=0; i<prob.quantities.size(); ++i) {
					int tokenId = prob.ta.getTokenIdFromCharacterOffset(
							prob.quantities.get(i).start);
					npList.write(prob.ta.getToken(tokenId).toLowerCase()+
							" :- NP : NUM_"+Tools.getValue(prob.quantities.get(i))+":n\n");		
				}
				if(fold.contains(prob.index)) {
					// Add the questions of the fold
					for(String token : prob.ta.getTokens()) {
						bw.write(token.toLowerCase()+" ");
					}
					bw.write("\n");
					bw.write(prob.equation.getLambdaExpression()+"\n\n");
				} else {
					// Variable phrases for all other folds added
					for(String key : prob.varTokens.keySet()) {
						for(Integer loc : prob.varTokens.get(key)) {
							npList.write(prob.ta.getToken(loc).toLowerCase()+
									" :- NP : "+key+":n\n");
						}
					}
				}
			}
			bw.close();
			npList.close();
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				new File("geo.consts.ont")));
		for(String cons : DocReader.preds) {
			bw.write(cons.trim()+"\n");
		}
		bw.close();
	}
	
	public static void main(String args[]) throws Exception {
//		List<SimulProb> simulProbList = 
//				DocReader.readSimulProbFromBratDir(Params.annotationDir, 0, 1.0);
//		for(SimulProb prob : simulProbList) {
//			print(prob);
//		}
//		DocReader.createBratFiles("data/equationparse.txt");
		DocReader.createLambdaExpForSPF();
		
	}
}
