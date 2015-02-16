package partition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.text.DefaultEditorKit.BeepAction;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class PartitionInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private PartitionFeatGen featGen;

	public PartitionInfSolver(PartitionFeatGen featGen) {
		this.featGen = featGen;
	}
	
	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		PartitionY y1 = (PartitionY) arg1;
		PartitionY y2 = (PartitionY) arg2;
		return PartitionY.getLoss(y1, y2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		PartitionX blob = (PartitionX) x;
		PartitionY gold = (PartitionY) goldStructure;
		PartitionY trueY = new PartitionY(true);
		PartitionY falseY = new PartitionY(false);
		float trueScore = wv.dotProduct(featGen.getFeatureVector(blob, trueY)) + 
				(goldStructure == null ? 0.0f : PartitionY.getLoss(trueY, gold)); 
		float falseScore = wv.dotProduct(featGen.getFeatureVector(blob, falseY)) + 
				(goldStructure == null ? 0.0f : PartitionY.getLoss(falseY, gold));  
		return trueScore > falseScore ? trueY : falseY;
	}
}
