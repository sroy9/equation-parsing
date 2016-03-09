package reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import structure.Node;
import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class DocReader {
	
	public static Set<String> preds = new HashSet<>();
	
	// Reads list of files from brat folder
	public static List<SimulProb> readSimulProbFromBratDir(String bratDir) 
			throws Exception {
		return getProjectiveProblems(readSimulProbFromBratDir(bratDir, 0.0, 1.0));
	}
	
	// Reads list of files from brat folder
	public static List<SimulProb> readSimulProbFromBratDir(
			String bratDir, double start, double end) throws Exception {
		List<SimulProb> simulProbList = new ArrayList<SimulProb>();
		File dir = new File(bratDir);
		for(File file : dir.listFiles()) {
			if(file.getName().endsWith(".txt")) {
				int index = Integer.parseInt(file.getName().substring(
								0, 
								file.getName().length()-4));
				SimulProb simulProb = new SimulProb(index);
				simulProb.extractTextAndEquation();
				simulProb.extractQuantities();
				simulProb.extractAnnotations();
				simulProb.createCandidateVars();
				simulProb.extractVarTokens(bratDir);
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
		System.out.println("Coref : "+simulProb.coref);
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
	
	public static List<SimulProb> getProjectiveProblems(List<SimulProb> probs) {
		List<SimulProb> projective = new ArrayList<>();
		for(SimulProb prob : probs) {
			List<Map<String, List<Integer>>> varTokens = Tools.enumerateProjectiveVarTokens(
					prob.varTokens, prob.equation, prob.ta, prob.quantities, prob.candidateVars);
			if(varTokens.size() > 0) {
				projective.add(prob);
			}
		}
		return projective;
	}
	
	public static double getAgreementScore(
			List<SimulProb> probs1, List<SimulProb> probs2) {
		double total = 0.0, correct = 0.0;
		for(int i=0; i<probs1.size(); ++i) {
			SimulProb prob1 = probs1.get(i);
			SimulProb prob2 = probs2.get(i);
			if(prob2.varTokens.keySet().size() == 0 || 
					prob1.varTokens.keySet().size() == 0) {
				continue;
			}
//			total += 1;
//			if(Tools.isEqual(prob1.varTokens.get("V1"), prob2.varTokens.get("V1")) &&
//					Tools.isEqual(prob1.varTokens.get("V2"), prob2.varTokens.get("V2"))) {
//				correct += 1;
//			}
			for(String var : prob1.varTokens.keySet()) {
				if(prob2.varTokens.keySet().contains(var)) {
					total += 1;
					if(Tools.isEqual(prob1.varTokens.get(var), prob2.varTokens.get(var), prob1.candidateVars)) {
						correct += 1;
					}
				}
			}
		}
		return correct / total;
	}
	
	
	
	public static void main(String args[]) throws Exception {
		List<SimulProb> simulProbList1 = DocReader.readSimulProbFromBratDir(
						Params.annotationDir, 0, 1.0);
		List<SimulProb> simulProbList2 = DocReader.readSimulProbFromBratDir(
						"/shared/bronte/upadhya3/other/brat/data/equationparsebrat/", 0, 1.0);
//		System.out.println(simulProbList.size());
//		DocReader.createLambdaExpForSPF();
//		DocReader.createGizaProbTable();
		System.out.println(getAgreementScore(simulProbList1, simulProbList2));
//		for(int i=0; i<simulProbList1.size(); ++i) {
//			print(simulProbList1.get(i));
//			print(simulProbList2.get(i));
//		}
		Tools.pipeline.closeCache();
		System.exit(0);
		
	}
}
