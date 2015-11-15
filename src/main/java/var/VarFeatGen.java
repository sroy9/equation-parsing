package var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class VarFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public VarFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		VarX x = (VarX) arg0;
		VarY y = (VarY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(VarX x, VarY y) {
		List<String> features = new ArrayList<>();
		List<IntPair> candidates = new ArrayList<IntPair>();
		for(String key : y.varTokens.keySet()) {
			assert y.varTokens.get(key).size() < 2;
			if(y.varTokens.get(key).size() == 0) continue;
			candidates.add(x.candidateVars.get(y.varTokens.get(key).get(0)));
		}
		for(IntPair candidate : candidates) {
			for(int i=candidate.getFirst(); i<candidate.getSecond(); ++i) {
				features.add("VarUnigram_"+x.ta.getToken(i).toLowerCase());
			}
			for(int i=candidate.getFirst(); i<candidate.getSecond()-1; ++i) {
				features.add("VarBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
						x.ta.getToken(i+1).toLowerCase());
			}
			if(candidate.getSecond() - candidate.getFirst() > 5) {
				features.add("Span>5");
			}
		}
		if(candidates.size() == 2) {
			if(candidates.get(0) == candidates.get(1)) features.add("SameSpan");
			features.add("Coref_"+y.coref);
		}
		return features;
	}
}