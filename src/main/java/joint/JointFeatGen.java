package joint;

import java.io.Serializable;
import java.util.List;

import numoccur.NumoccurX;
import numoccur.NumoccurY;
import structure.Node;
import tree.TreeX;
import utils.FeatGen;
import var.VarFeatGen;
import var.VarX;
import var.VarY;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class JointFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public JointFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		JointX x = (JointX) arg0;
		JointY y = (JointY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getLcaFeatureVector(JointX x, JointY y) {
		tree.TreeX lcaX = new tree.TreeX(x, y.varTokens, y.nodes);
		tree.TreeY lcaY = new tree.TreeY(y);
		List<String> features = tree.TreeFeatGen.getFeatures(lcaX, lcaY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getVarTokenFeatureVector(JointX x, JointY y) {
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		List<String> features = VarFeatGen.getFeatures(varX, varY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getNumoccurFeatureVector(JointX x, JointY y) {
		numoccur.NumoccurX numX = new numoccur.NumoccurX(x);
		numoccur.NumoccurY numY = new numoccur.NumoccurY(x, y);
		List<String> features = numoccur.NumoccurFeatGen.getFeatures(numX, numY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public static List<String> getFeatures(JointX x, JointY y) {
		numoccur.NumoccurX numX = new numoccur.NumoccurX(x);
		numoccur.NumoccurY numY = new numoccur.NumoccurY(x, y);
		List<String> features = numoccur.NumoccurFeatGen.getFeatures(numX, numY);
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		features.addAll(VarFeatGen.getFeatures(varX, varY));
		tree.TreeX lcaX = new tree.TreeX(x, y.varTokens, y.nodes);
		tree.TreeY lcaY = new tree.TreeY(y);
		features.addAll(tree.TreeFeatGen.getFeatures(lcaX, lcaY));
		return features;
	}
	
	public IFeatureVector getGlobalFeatureVector(NumoccurX x, NumoccurY y) {
		List<String> features = numoccur.NumoccurFeatGen.getGlobalFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getIndividualFeatureVector(numoccur.NumoccurX x, 
			numoccur.NumoccurY y) {
		List<String> features = numoccur.NumoccurFeatGen.getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getPairFeatureVector(TreeX x, Node node) {
		List<String> features = tree.TreeFeatGen.getPairFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	
}