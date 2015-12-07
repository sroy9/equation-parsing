package reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import structure.SimulProb;
import utils.Params;
import utils.Tools;

public class Baselines {

	public static void executeCommand(String command) {
		Process p;
		try {
			System.out.println("Executing : "+command);
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getCCGcategory(String tokenizedSentence, int tokenId) throws IOException {
		FileUtils.write(new File("in"), tokenizedSentence);
		executeCommand("java -jar easyccg-0.2/easyccg.jar --model easyccg-model < in > out");
		return FileUtils.readFileToString(new File("out"));
	}
	
	public static void createLambdaExpForSPF() throws Exception {
		List<SimulProb> simulProbList = 
				DocReader.readSimulProbFromBratDir(Params.annotationDir, 0, 1.0);
		List<List<Integer>> folds = DocReader.extractFolds();
		for(int i=0; i<folds.size(); ++i) {
			List<Integer> fold = folds.get(i);
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					new File("fold"+i+".ccg")));
			for(SimulProb prob : simulProbList) {
				if(fold.contains(prob.index)) {
					// Add the questions of the fold
					for(String token : prob.ta.getTokens()) {
						bw.write(token.toLowerCase()+" ");
					}
					bw.write("\n");
					bw.write(prob.equation.getLambdaExpression()+"\n\n");
				}
			}
			bw.close();
		}
		BufferedWriter npList = new BufferedWriter(new FileWriter(
				new File("nplist.ont")));
		for(SimulProb prob : simulProbList) {
			for(int i=0; i<prob.quantities.size(); ++i) {
				int tokenId = prob.ta.getTokenIdFromCharacterOffset(
						prob.quantities.get(i).start);
				npList.write(prob.ta.getToken(tokenId).toLowerCase()+
						" :- NP : "+Tools.getValue(prob.quantities.get(i))+":n\n");		
			}
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
		System.out.println(getCCGcategory("I have to go to school .", 9));
	}
}
