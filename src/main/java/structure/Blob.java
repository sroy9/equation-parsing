package structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lbj.EquationParser;
import parser.DocReader;
import sl.BruteForceInfSolver;
import sl.LatticeFeatureExtractor;
import utils.Params;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.POSTag;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class Blob implements IInstance {

	public Map<String, List<Expression>> termMap;
	List<String> features;
	public SimulProb simulProb; // which problem

	public Blob(Map<String, List<Expression>> termmap, SimulProb simulProb) {
		termMap = termmap;
		this.simulProb = simulProb;
	}
	
	public static void main(String[] args) throws Exception {
		DocReader dr = new DocReader();
		List<SimulProb> simulProbList = dr
				.readSimulProbFromBratDir(Params.annotationDir);
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			Map<String, List<Expression>> termMap = Blob
					.extractTermMap(simulProb);
			Blob blob = new Blob(termMap, simulProb);
			List<String> paths = getGold(simulProb, blob);
			Lattice goldStr = new Lattice(paths);
			problem.addExample(blob, goldStr);
		}
		double trainFrac = 0.8;
		Pair<SLProblem, SLProblem> trainTest = problem.splitTrainTest((int) (trainFrac*problem.size()));
		SLProblem train = trainTest.getFirst();
		SLProblem test= trainTest.getFirst();
		trainModel("model.save",train);
		testModel("model.save",test);
	}

	private static void testModel(String modelPath, SLProblem sp) throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
//		for(String f:model.lm.allCurrentFeatures())
//			System.out.println(f);
		printStatsWV(model.wv);
		double acc = 0.0;
		double total = sp.instanceList.size();
		for (int i = 0; i < sp.instanceList.size(); i++) {
			Lattice gold = (Lattice) sp.goldStructureList.get(i);
			Lattice prediction = (Lattice) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			if(gold.equals(prediction)) {
				acc += 1.0;
			}
		}
		System.out.println("Accuracy : "+ acc + " / "+total+" = "+(acc/total));
	}

	private static void trainModel(String modelPath,SLProblem train) throws Exception {
		// TODO Auto-generated method stub
		SLModel model = new SLModel();
		Lexiconer lm = new Lexiconer();
		lm.setAllowNewFeatures(true);
		model.lm=lm;
		AbstractFeatureGenerator fg = (AbstractFeatureGenerator) new LatticeFeatureExtractor(lm);
		model.featureGenerator=fg;
		model.infSolver=new BruteForceInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg,
				para);
		model.wv = learner.train(train);
		printStatsWV(model.wv);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}

	private static void printStatsWV(WeightVector wv) {
		// TODO Auto-generated method stub
		int nzeroes=0;
		System.out.println("SIZE: "+wv.getLength());
		for(float f:wv.getInternalArray())
		{
			if(f!=0.0)
				nzeroes++;
		}
		System.out.println("NZ values: "+nzeroes);
	}

	public static Map<String, List<Expression>> extractTermMap(
			SimulProb simulProb) {
		Map<String, List<Expression>> termMap = new HashMap<String, List<Expression>>();
		for (Expression expr : simulProb.equations) {
			for (Expression subEx : expr.getAllSubExpressions()) {
				if (subEx.complete == Operation.COMPLETE) {
					if (!termMap.containsKey(subEx.entityName)) {
						termMap.put(subEx.entityName,
								new ArrayList<Expression>());
					}
					boolean allow = true;
					for (Expression term : termMap.get(subEx.entityName)) {
						if (term.equalsBasedOnLeaves(subEx)) {
							allow = false;
						}
					}
					if (allow)
						termMap.get(subEx.entityName).add(subEx);
				}
			}
		}
		return termMap;
	}

	public static List<String> getGold(SimulProb simulProb, Blob blob) {
		List<String> eqs = new ArrayList<>();
		Integer index1 = null, index2 = null, index3 = null;
		for(Expression ex : simulProb.equations) {
			if(blob.termMap.containsKey("E1")) {
				for(Expression subEx : ex.getAllSubExpressions()) {
					for(int i = 0; i <  blob.termMap.get("E1").size(); i++) {
						if(subEx.equalsBasedOnLeaves(blob.termMap.get("E1").get(i))) {
							index1 = i;
							break;
						}
					}
					if(index1 != null) break;
				}
			}
			if(blob.termMap.containsKey("E2")) {
				for(Expression subEx : ex.getAllSubExpressions()) {
					for(int i = 0; i <  blob.termMap.get("E2").size(); i++) {
						if(subEx.equalsBasedOnLeaves(blob.termMap.get("E2").get(i))) {
							index2 = i;
							break;
						}
					}
					if(index2 != null) break;
				}
			}
			if(blob.termMap.containsKey("E3")) {
				for(Expression subEx : ex.getAllSubExpressions()) {
					for(int i = 0; i <  blob.termMap.get("E3").size(); i++) {
						if(subEx.equalsBasedOnLeaves(blob.termMap.get("E3").get(i))) {
							index3 = i;
							break;
						}
					}
					if(index3 != null) break;
				}
			}
			if(index1 != null && index2 != null && index3 != null) {
				Operation op = simulProb.getShyamOperation(blob.termMap.get("E1").get(index1), 
						blob.termMap.get("E2").get(index2), blob.termMap.get("E3").get(index3));
				eqs.add(index1+"_"+op+"_"+index2+"_"+index3);
			}
			if(index1 != null && index2 != null && index3 == null) {
				eqs.add(index1+"_EQ_"+index2);
			}
		}
		return eqs;
	}
}
