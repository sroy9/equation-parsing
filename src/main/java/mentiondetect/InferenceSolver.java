package mentiondetect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import structure.Expression;
import structure.LabelSet;
import structure.Mention;
import structure.SimulProb;
import structure.VarSet;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import gurobi.*;

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
		VarSet varSet = (VarSet) ins;
		LabelSet labelSet = new LabelSet();
		List<String> labels = Arrays.asList(
				"B-E1", "I-E1", "B-E2", "I-E2", "B-E3", "I-E3", "O");
		for(int i = 0; i < varSet.ta.size(); i++) {
			String bestLabel = null;
			double bestScore = -100000;
			for(String label : labels) {
				labelSet.addLabel(label);
				if(weight.dotProduct(featGen.getFeatureVector(varSet, labelSet, i))
						> bestScore) {
					bestLabel = label;
					bestScore = weight.dotProduct(featGen.getFeatureVector(
							varSet, labelSet, i));
				}	
				labelSet.removeLast();	
			}
			labelSet.addLabel(bestLabel);
		}
		return labelSet;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception {
		// TODO Auto-generated method stub
		return getBestStructure(weight, ins);
	}

	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {
		// TODO Auto-generated method stub
		LabelSet goldLabelSet = (LabelSet) gold;
		LabelSet predLabelSet = (LabelSet) pred;
		assert goldLabelSet.labels.size() == predLabelSet.labels.size();
		for(int i = 0; i < goldLabelSet.labels.size(); i++) {
			if(!goldLabelSet.labels.get(i).equals(predLabelSet.labels.get(i))) {
				return 1;
			}
		}
		return 0;
	}
	

}
