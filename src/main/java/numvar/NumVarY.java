package numvar;

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

public class NumVarY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public boolean isOneVar;
	
	public NumVarY(boolean isOneVar) {
		this.isOneVar = isOneVar;
	}
	
	public NumVarY(NumVarY other) {
		isOneVar = other.isOneVar;
	}
	
	public static float getLoss(NumVarY y1, NumVarY y2) {
		if(y1.isOneVar == y2.isOneVar) return 0.0f;
		return 1.0f;
	}
}