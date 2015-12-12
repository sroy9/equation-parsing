package reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class Baselines {

	public static void executeCommand(String command[]) throws Exception {
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectInput(new File("in"));
		pb.redirectOutput(new File("out"));
		pb.command(command);
		pb.start().waitFor();
	}

	public static String getCCGParse(String tokenizedSentence) throws Exception {
		FileUtils.write(new File("in"), tokenizedSentence);
		String command[] = {"java", "-jar", "easyccg-0.2/easyccg.jar", "--model", "easyccg-model"};
		executeCommand(command);
		return FileUtils.readFileToString(new File("out"));
	}
	
	public static String getCCGcategory(String ccgParse, int tokenId) throws Exception {
		int startIndex=-1;
		int index = -1;
		while(true) {
			startIndex = ccgParse.indexOf("<L", startIndex+1);
			index++;
			if(index == tokenId) break;
		}
		int endIndex = ccgParse.indexOf(">", startIndex);
		String category = ccgParse.substring(startIndex, endIndex).split(" ")[1];
		return category.replaceAll("\\[.+?\\]", "").trim();
	}
	
	public static void createLambdaExpForSPF() throws Exception {
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir, 0, 1.0);
		BufferedWriter npList = new BufferedWriter(new FileWriter(
				new File("nplist.ont")));
		List<List<Integer>> folds = DocReader.extractFolds();
		for(int i=0; i<folds.size(); ++i) {
			List<Integer> fold = folds.get(i);
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					new File("fold"+i+".ccg")));
			for(SimulProb prob : simulProbList) {
				if(fold.contains(prob.index)) {
					String tokenizedSentence = "";
					for(String token : prob.ta.getTokens()) {
						tokenizedSentence+=token.toLowerCase()+" ";
					}
					tokenizedSentence+="\n";
					bw.write(tokenizedSentence);
					bw.write(prob.equation.getLambdaExpression()+"\n\n");
//					String ccgParse = getCCGParse(tokenizedSentence.trim());
					for(int j=0; j<prob.quantities.size(); ++j) {
						int tokenId = prob.ta.getTokenIdFromCharacterOffset(
								prob.quantities.get(j).start);
//						npList.write(prob.ta.getToken(tokenId).toLowerCase()+
//								" :- "+getCCGcategory(ccgParse, j)+" : "+
//								Tools.getValue(prob.quantities.get(j))+":n\n");
//						npList.write(prob.ta.getToken(tokenId).toLowerCase()+
//								" :- "+"N/N"+" : "+
//								Tools.getValue(prob.quantities.get(j))+":n\n");	
						npList.write(prob.ta.getToken(tokenId).toLowerCase()+
								" :- "+"NP"+" : "+
								Tools.getValue(prob.quantities.get(j))+":n\n");
					}
					for(String varId : prob.varTokens.keySet()) {
						String phrase = "";
						IntPair ip = prob.candidateVars.get(prob.varTokens.get(varId).get(0));
						for(int j=ip.getFirst(); j<ip.getSecond(); ++j) {
							phrase+= prob.ta.getToken(j).toLowerCase()+" ";
						}
						npList.write(phrase+":- "+"NP"+" : "+varId+":n\n");
					}
					System.out.println("Done");
				}
			}
			bw.close();
		}
		npList.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				new File("geo.consts.ont")));
		bw.write("(\n");
		for(String cons : DocReader.preds) {
			bw.write(cons.trim()+"\n");
		}
		bw.write(")\n");
		bw.close();
	}

	public static void createGizaProbTable() throws Exception {
		for(int i=0; i<1; ++i) {
			Map<Integer, String> srcVcb = new HashMap<>();
			Map<Integer, String> targetVcb = new HashMap<>();
			String str;
			BufferedReader br = new BufferedReader(new FileReader(
					"parallel/A"+i+".vcb"));
			while((str=br.readLine())!=null) {
				String strArr[] = str.split(" ");
				srcVcb.put(Integer.parseInt(strArr[0].trim()), 
						strArr[1].replace("(", "").replace(")", "").trim());
			}
			br.close();
			br = new BufferedReader(new FileReader(
					"parallel/B"+i+".vcb"));
			while((str=br.readLine())!=null) {
				String strArr[] = str.split(" ");
				targetVcb.put(Integer.parseInt(strArr[0].trim()), 
						strArr[1].replace("(", "").replace(")", "").trim());
			}
			br.close();
			br = new BufferedReader(new FileReader(
					"parallel/A"+i+"_B"+i+"_prob"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"parallel/A"+i+"_B"+i+"_wordprob"));
			while((str=br.readLine())!=null) {
				String strArr[] = str.split(" ");
				bw.write(targetVcb.get(Integer.parseInt(strArr[1].trim()))+"  ::  "+
						srcVcb.get(Integer.parseInt(strArr[0].trim()))+"  ::  "+
						strArr[2].trim()+"\n");
				
			}
			br.close();
			bw.close();
		}
	}
	
	public static void main(String args[]) throws Exception {
		createLambdaExpForSPF();
//		createGizaProbTable();
		Tools.pipeline.closeCache();
		System.exit(0);
	}
}
