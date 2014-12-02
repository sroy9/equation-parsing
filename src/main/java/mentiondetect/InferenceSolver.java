package mentiondetect;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import structure.LabelSet;
import structure.VarSet;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class InferenceSolver extends AbstractInferenceSolver implements Serializable{

	private static final long serialVersionUID = -5897765443533109252L;
	private FeatureExtractor featGen;

	public InferenceSolver(FeatureExtractor featGen) {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector weight, IInstance ins)
			throws Exception {
		// TODO Auto-generated method stub
		return getLossAugmentedBestStructure(weight, ins, null);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception {
		LabelSet gold = (LabelSet) goldStructure;
		VarSet varSet = (VarSet) ins;
		assert varSet.sent.size() == gold.labels.size();
		List<String> labels = Arrays.asList(
				"B-E1", "I-E1", "B-E2", "I-E2", "B-E3", "I-E3", "O");
		
		int numOflabels = labels.size();
		int numOfTokens = varSet.sent.size();
				
		LabelSet[][] dpTable = new LabelSet[numOfTokens][numOflabels];	
		double[][] dpScores = new double[numOfTokens][numOflabels];	
		for(int i = 0; i < numOflabels; i++) {
			dpTable[0][i] = new LabelSet();
			dpTable[0][i].addLabel(labels.get(i));
			dpScores[0][i] = weight.dotProduct(
					featGen.getFeatureVector(varSet, dpTable[0][i], 0))
					+((gold !=null && !labels.get(i).equals(gold.labels.get(0)))?1:0);
		}
		for (int i = 1; i < numOfTokens; i++) {
			for (int j = 0; j < numOflabels; j++) {
				double bestScore = Float.NEGATIVE_INFINITY;
				int bestIndex = -1;
				for (int k = 0; k < numOflabels; k++) {
//					System.out.println((i-1)+" "+k+" : "+dpTable[i-1][k].labels.size());
					assert dpTable[i-1][k].labels.size() == i;
					dpTable[i-1][k].addLabel(labels.get(j));
					double candidateScore = dpScores[i-1][k] + weight.dotProduct(
							featGen.getFeatureVector(varSet, dpTable[i-1][k], i))
							+((gold !=null && !labels.get(j).equals(gold.labels.get(i)))
									?1:0);
					if (candidateScore > bestScore) {
						bestScore = candidateScore;
						bestIndex = k;
					}
					dpTable[i-1][k].removeLast();
				}
				dpTable[i][j] = new LabelSet(dpTable[i-1][bestIndex]);
				dpTable[i][j].addLabel(labels.get(j));
				dpScores[i][j] = bestScore;
			}
		}
		double bestScore = Float.NEGATIVE_INFINITY;
		int bestIndex = -1;
		for (int i = 0; i < numOflabels; i++) {
			if (dpScores[numOfTokens - 1][i] > bestScore) {
				bestIndex = i;
			}
		}
		return dpTable[numOfTokens-1][bestIndex];
	}

	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {
		LabelSet goldLabelSet = (LabelSet) gold;
		LabelSet predLabelSet = (LabelSet) pred;
		assert goldLabelSet.labels.size() == predLabelSet.labels.size();
		float hammingLoss = 0;
		for(int i = 0; i < goldLabelSet.labels.size(); i++) {
			if(!goldLabelSet.labels.get(i).equals(predLabelSet.labels.get(i))) {
				hammingLoss += 1.0;
			}
		}
		return hammingLoss;
	}
	

}