package relation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class LatentSegmentation {
	
	// argmax_h w^T \phi(x,h,y)
	public static RelationY getBestLatentVariable(
			WeightVector wv, RelationX x, RelationY y) {
		Map<String, List<Double>> eqNumbers = new HashMap<String, List<Double>>();
		
		return y;
	}

}
