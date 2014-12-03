package structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import parser.DocReader;
import utils.Params;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
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
import equationmatch.BruteForceInfSolver;
import equationmatch.LatticeFeatureExtractor;

public class Blob implements IInstance {

	public Map<String, List<Expression>> termMap;
	List<String> features;
	public SimulProb simulProb; // which problem
	public Lattice goldLattice;

	public Blob(Map<String, List<Expression>> termmap, SimulProb simulProb) {
		termMap = termmap;
		this.simulProb = simulProb;
	}

	public static void main(String[] args) throws Exception {
		

		InteractiveShell<Blob> tester = new InteractiveShell<Blob>(Blob.class);
		if (args.length == 0)
			tester.showDocumentation();
		else {
			tester.runCommand(args);
		}
		
	}
	public static void crossVal() throws Exception
	{
		SLProblem problem = getSP();
		List<Pair<SLProblem, SLProblem>> folds = problem.splitDataToNFolds(5, new Random());
		for(int i=0;i<folds.size();i++)
		{
			Pair<SLProblem, SLProblem> fold = folds.get(i);
			SLProblem train = fold.getFirst();
			SLProblem test = fold.getSecond();
			trainModel("cvFold"+i+".model",train);
			testModel("cvFold"+i+".model", test);
		}
		
	}
	public static void doTrainTest() throws Exception
	{
		SLProblem problem=getSP();
		double trainFrac = 0.8;
		Pair<SLProblem, SLProblem> trainTest = problem
				.splitTrainTest((int) (trainFrac * problem.size()));
		SLProblem train = trainTest.getFirst();
		SLProblem test = trainTest.getSecond();
		trainModel("model.save", train);
		testModel("model.save", test);
	}

	private static SLProblem getSP() throws Exception {
		// TODO Auto-generated method stub
		DocReader dr = new DocReader();
		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(Params.annotationDir);
		SLProblem problem = new SLProblem();
		for (SimulProb simulProb : simulProbList) {
			Map<String, List<Expression>> termMap = Blob
					.extractTermMap(simulProb);
			Blob blob = new Blob(termMap, simulProb);
			List<String> paths = getGold(simulProb, blob);
			Lattice goldStr = new Lattice(paths);
			// Sanity check
			boolean allow = false;
			for (Lattice l : BruteForceInfSolver.getPossibleLegalStructures(blob)) {
				if (l.equals(goldStr)) {
					allow = true;
					break;
				}
			}
			if (!allow)
				continue;
			blob.goldLattice = goldStr;
			problem.addExample(blob, goldStr);
		}
		return problem;
	}

	private static void testModel(String modelPath, SLProblem sp)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		// for(String f:model.lm.allCurrentFeatures())
		// System.out.println(f);
		printStatsWV(model.wv);
		double acc = 0.0;
		double total = sp.instanceList.size();
		for (int i = 0; i < sp.instanceList.size(); i++) {
			Lattice gold = (Lattice) sp.goldStructureList.get(i);
			Lattice prediction = (Lattice) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));
			if (gold.equals(prediction)) {
				acc += 1.0;
			}
		}
		System.out.println("Accuracy : " + acc + " / " + total + " = "
				+ (acc / total));
	}

	private static void trainModel(String modelPath, SLProblem train)
			throws Exception {
		// TODO Auto-generated method stub
		SLModel model = new SLModel();
		Lexiconer lm = new Lexiconer();
		lm.setAllowNewFeatures(true);
		model.lm = lm;
		AbstractFeatureGenerator fg = (AbstractFeatureGenerator) new LatticeFeatureExtractor(
				lm);
		model.featureGenerator = fg;
		model.infSolver = new BruteForceInfSolver(fg);
		SLParameters para = new SLParameters();
		para.loadConfigFile(Params.spConfigFile);
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		model.wv = learner.train(train);
		printStatsWV(model.wv);
		lm.setAllowNewFeatures(false);
		model.saveModel(modelPath);
	}

	private static void printStatsWV(WeightVector wv) {
		int nzeroes = 0;
		System.out.println("SIZE: " + wv.getLength());
		for (float f : wv.getInternalArray()) {
			if (f != 0.0)
				nzeroes++;
		}
		System.out.println("NZ values: " + nzeroes);
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
		for (Expression ex : simulProb.equations) {
			Integer index1 = null, index2 = null, index3 = null;
			if (blob.termMap.containsKey("E1")) {
				for (Expression subEx : ex.getAllSubExpressions()) {
					for (int i = 0; i < blob.termMap.get("E1").size(); i++) {
						if (subEx.equalsBasedOnLeaves(blob.termMap.get("E1")
								.get(i))) {
							index1 = i;
							break;
						}
					}
					if (index1 != null)
						break;
				}
			}
			if (blob.termMap.containsKey("E2")) {
				for (Expression subEx : ex.getAllSubExpressions()) {
					for (int i = 0; i < blob.termMap.get("E2").size(); i++) {
						if (subEx.equalsBasedOnLeaves(blob.termMap.get("E2")
								.get(i))) {
							index2 = i;
							break;
						}
					}
					if (index2 != null)
						break;
				}
			}
			if (blob.termMap.containsKey("E3")) {
				for (Expression subEx : ex.getAllSubExpressions()) {
					for (int i = 0; i < blob.termMap.get("E3").size(); i++) {
						if (subEx.equalsBasedOnLeaves(blob.termMap.get("E3")
								.get(i))) {
							index3 = i;
							break;
						}
					}
					if (index3 != null)
						break;
				}
			}
			if (index1 != null && index2 != null && index3 != null) {
				Operation op = simulProb.getShyamOperation(
						blob.termMap.get("E1").get(index1),
						blob.termMap.get("E2").get(index2),
						blob.termMap.get("E3").get(index3));
				eqs.add(index1 + "_" + op + "_" + index2 + "_" + index3);
			}
			if (index1 != null && index2 != null && index3 == null) {
				eqs.add(index1 + "_EQ_" + index2);
			}
		}
		return eqs;
	}
}
