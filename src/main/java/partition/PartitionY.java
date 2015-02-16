package partition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import structure.Equation;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class PartitionY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public boolean isPartition;
	
	public PartitionY(boolean isPartition) {
		this.isPartition = isPartition;
	}
	
	public PartitionY(PartitionY other) {
		isPartition = other.isPartition;
	}
	
	public static float getLoss(PartitionY y1, PartitionY y2) {
		if(y1.isPartition == y2.isPartition) return 0.0f;
		return 1.0f;
	}
}