package tree;

import java.io.Serializable;
import java.util.List;

import struct.lca.LcaX;
import struct.lca.LcaY;
import struct.numoccur.NumoccurX;
import struct.numoccur.NumoccurY;
import structure.Node;
import utils.FeatGen;
import var.VarFeatGen;
import var.VarX;
import var.VarY;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class TreeFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public TreeFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		TreeX x = (TreeX) arg0;
		TreeY y = (TreeY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getLcaFeatureVector(TreeX x, TreeY y) {
		struct.lca.LcaX lcaX = new struct.lca.LcaX(x, y.varTokens, y.nodes);
		struct.lca.LcaY lcaY = new struct.lca.LcaY(y);
		List<String> features = struct.lca.LcaFeatGen.getFeatures(lcaX, lcaY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getVarTokenFeatureVector(TreeX x, TreeY y) {
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		List<String> features = VarFeatGen.getFeatures(varX, varY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getNumoccurFeatureVector(TreeX x, TreeY y) {
		struct.numoccur.NumoccurX numX = new struct.numoccur.NumoccurX(x);
		struct.numoccur.NumoccurY numY = new struct.numoccur.NumoccurY(x, y);
		List<String> features = struct.numoccur.NumoccurFeatGen.getFeatures(numX, numY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public static List<String> getFeatures(TreeX x, TreeY y) {
		struct.numoccur.NumoccurX numX = new struct.numoccur.NumoccurX(x);
		struct.numoccur.NumoccurY numY = new struct.numoccur.NumoccurY(x, y);
		List<String> features = struct.numoccur.NumoccurFeatGen.getFeatures(numX, numY);
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		features.addAll(VarFeatGen.getFeatures(varX, varY));
		struct.lca.LcaX lcaX = new struct.lca.LcaX(x, y.varTokens, y.nodes);
		struct.lca.LcaY lcaY = new struct.lca.LcaY(y);
		features.addAll(struct.lca.LcaFeatGen.getFeatures(lcaX, lcaY));
		return features;
	}
	
	public IFeatureVector getGlobalFeatureVector(NumoccurX x, NumoccurY y) {
		List<String> features = struct.numoccur.NumoccurFeatGen.getGlobalFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getIndividualFeatureVector(numoccur.NumoccurX x, 
			numoccur.NumoccurY y) {
		List<String> features = struct.numoccur.NumoccurFeatGen.getIndividualFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getPairFeatureVector(LcaX x, Node node) {
		List<String> features = struct.lca.LcaFeatGen.getPairFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public IFeatureVector getGlobalFeatureVector(LcaX x, LcaY y) {
		List<String> features = struct.lca.LcaFeatGen.getGlobalFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	
}