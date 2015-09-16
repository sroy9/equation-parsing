package struct.numoccur;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import utils.FeatGen;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class NumoccurFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public NumoccurFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		NumoccurX x = (NumoccurX) arg0;
		NumoccurY y = (NumoccurY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(NumoccurX x, NumoccurY y) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<x.quantities.size(); ++i) {
			numoccur.NumoccurX numX = new numoccur.NumoccurX(x, i);
			numoccur.NumoccurY numY = new numoccur.NumoccurY(y.numOccurList.get(i));
			features.addAll(numoccur.NumoccurFeatGen.getFeatures(numX, numY));
		}
		return features;
	}
	
//	public IFeatureVector getGlobalFeatureVector(NumoccurX x, NumoccurY y) {
//		List<String> features = getGlobalFeatures(x, y);
//		return FeatGen.getFeatureVectorFromList(features, lm);
//	}
	
	public IFeatureVector getIndividualFeatureVector(numoccur.NumoccurX x, 
			numoccur.NumoccurY y) {
		List<String> features = numoccur.NumoccurFeatGen.getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
//	public static List<String> getGlobalFeatures(NumoccurX x, NumoccurY y) {
//		List<String> features = new ArrayList<>();
//		for(int i=0; i<x.ta.size(); ++i) {
//			features.add(y+"_Unigram_"+x.ta.getToken(i).toLowerCase());
//		}
//		return features;
//	}
}