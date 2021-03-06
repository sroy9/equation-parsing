package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import numoccur.NumoccurFeatGen;
import numoccur.NumoccurX;
import numoccur.NumoccurY;
import structure.Node;
import tree.CompFeatGen;
import tree.TreeX;
import tree.TreeY;
import utils.FeatGen;
import var.VarFeatGen;
import var.VarX;
import var.VarY;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class JointFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public JointFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	public IFeatureVector getFeatureVector(JointX x, JointY y, SLModel model) {
		List<String> features = new ArrayList<>();
		NumoccurX numX = new NumoccurX(x);
		NumoccurY numY = new NumoccurY(x, y);
		for(int i=0; i<x.quantities.size(); ++i) {
			features.addAll(NumoccurFeatGen.getIndividualFeatures(
					numX, i, numY.numOccurList.get(i)));
		}
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		features.addAll(VarFeatGen.getFeatures(varX, varY));
		TreeX treeX = new TreeX(x, y.varTokens, y.nodes);
		TreeY treeY = new TreeY(y);
		for(Node node : treeY.equation.root.getAllSubNodes()) {
			if(node.children.size()==2) {
				features.addAll(CompFeatGen.getNodeFeatures(treeX, node));
			}
		}
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		JointX x = (JointX) arg0;
		JointY y = (JointY) arg1;
		List<String> features = new ArrayList<>();
		NumoccurX numX = new NumoccurX(x);
		NumoccurY numY = new NumoccurY(x, y);
		for(int i=0; i<x.quantities.size(); ++i) {
			features.addAll(NumoccurFeatGen.getIndividualFeatures(
					numX, i, numY.numOccurList.get(i)));
		}
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		features.addAll(VarFeatGen.getFeatures(varX, varY));
		TreeX treeX = new TreeX(x, y.varTokens, y.nodes);
		TreeY treeY = new TreeY(y);
		for(Node node : treeY.equation.root.getAllSubNodes()) {
			if(node.children.size()==2) {
				features.addAll(CompFeatGen.getNodeFeatures(treeX, node));
			}
		}
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getIndividualFeatureVector(
			NumoccurX x, int quantIndex, int numOccur) {
		List<String> features = NumoccurFeatGen.getIndividualFeatures(x, quantIndex, numOccur);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getVarTokenFeatureVector(JointX x, JointY y) {
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		List<String> features = VarFeatGen.getFeatures(varX, varY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getNodeFeatureVector(TreeX x, Node node) {
		List<String> features = CompFeatGen.getNodeFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
}