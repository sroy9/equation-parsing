package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lca.LcaFeatGen;
import lca.LcaX;
import lca.LcaY;
import numoccur.NumoccurFeatGen;
import numoccur.NumoccurX;
import numoccur.NumoccurY;
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
	
	public IFeatureVector getLcaFeatureVector(TreeX x, Node leaf1, Node leaf2, String op) {
		LcaX lcaX = new LcaX(x, leaf1, leaf2);
		LcaY lcaY = new LcaY(op);
		List<String> features = LcaFeatGen.getFeatures(lcaX, lcaY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getLcaFeatureVector(TreeX x, Node node) {
		List<String> features = new ArrayList<String>();
		if(node.children.size() == 2) {
			for(Node leaf1 : node.children.get(0).getLeaves()) {
				for(Node leaf2 : node.children.get(1).getLeaves()) {
					LcaX lcaX = new LcaX(x, leaf1, leaf2);
					LcaY lcaY = new LcaY(node.label);
					features.addAll(LcaFeatGen.getFeatures(lcaX, lcaY));
					String label = node.label;
					if(label.equals("SUB") || label.equals("DIV")) label += "_REV";
					lcaX = new LcaX(x, leaf2, leaf1);
					lcaY = new LcaY(label);
					features.addAll(LcaFeatGen.getFeatures(lcaX, lcaY));
				}
			}
		}
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public IFeatureVector getVarTokenFeatureVector(TreeX x, TreeY y) {
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		List<String> features = VarFeatGen.getFeatures(varX, varY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getNumoccurFeatureVector(TreeX prob, int quantIndex, int numOccur) {
		NumoccurX numX = new NumoccurX(prob, quantIndex);
		NumoccurY numY = new NumoccurY(numOccur);
		List<String> features = NumoccurFeatGen.getFeatures(numX, numY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public static List<String> getFeatures(TreeX x, TreeY y) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<x.quantities.size(); ++i) {
			NumoccurX numX = new NumoccurX(x, i);
			NumoccurY numY = new NumoccurY(x, y, i);
			features.addAll(NumoccurFeatGen.getFeatures(numX, numY));
		}
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		features.addAll(VarFeatGen.getFeatures(varX, varY));
		for(Node leaf1 : y.equation.root.getLeaves()) {
			for(Node leaf2 : y.equation.root.getLeaves()) {
				if(leaf1 == leaf2) continue;
				LcaX lcaX = new LcaX(x, leaf1, leaf2);
				LcaY lcaY = new LcaY(y.equation.root.findLabelofLCA(leaf1, leaf2));
				features.addAll(LcaFeatGen.getFeatures(lcaX, lcaY));
			}
		}
		return features;
	}	
}