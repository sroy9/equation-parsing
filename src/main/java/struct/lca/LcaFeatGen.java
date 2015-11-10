package struct.lca;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Node;
import utils.FeatGen;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class LcaFeatGen extends AbstractFeatureGenerator implements Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public LcaFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		LcaX x = (LcaX) arg0;
		LcaY y = (LcaY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public IFeatureVector getPairFeatureVector(LcaX x, Node node) {
		List<String> features = getPairFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(LcaX x, LcaY y) {
		List<String> features = new ArrayList<String>();
		for(Node node : y.equation.root.getAllSubNodes()) {
			if(node.children.size() == 2) {
				features.addAll(getPairFeatures(x, node));
			}
		}
		return features;
	}
	
	public static List<String> getPairFeatures(LcaX x, Node node) {
		List<String> features = new ArrayList<String>();
		if(node.children.size() == 2) {
			for(Node leaf1 : node.children.get(0).getLeaves()) {
				for(Node leaf2 : node.children.get(1).getLeaves()) {
					boolean allow = true;
					int preIndex = 0;
					for(Node leaf : x.nodes) {
						if(leaf.charIndex < Math.min(leaf1.charIndex, leaf2.charIndex)) {
							preIndex = leaf.charIndex;
						}
						if((leaf1.charIndex<leaf.charIndex && leaf.charIndex<leaf2.charIndex) || 
								(leaf2.charIndex<leaf.charIndex && leaf.charIndex<leaf1.charIndex)) {
							allow = false;
						}
					}
					if(!allow) continue;
					String prePhrase = x.ta.getText().toLowerCase().substring(
							preIndex, Math.min(leaf1.charIndex, leaf2.charIndex));
					String midPhrase = x.ta.getText().toLowerCase().substring(
							Math.min(leaf1.charIndex, leaf2.charIndex), 
							Math.max(leaf1.charIndex, leaf2.charIndex));
					if(leaf1.charIndex <= leaf2.charIndex) {
						lca.LcaX lcaX = new lca.LcaX(x, leaf1, leaf2, prePhrase, midPhrase);
						lca.LcaY lcaY = new lca.LcaY(node.label);
						features.addAll(lca.LcaFeatGen.getFeatures(lcaX, lcaY));
					}
					if(leaf2.charIndex < leaf1.charIndex) {
						String label = node.label;
						if(label.equals("SUB") || label.equals("DIV")) label += "_REV";
						lca.LcaX lcaX = new lca.LcaX(x, leaf2, leaf1, prePhrase, midPhrase);
						lca.LcaY lcaY = new lca.LcaY(label);
						features.addAll(lca.LcaFeatGen.getFeatures(lcaX, lcaY));
					}
				}
			}
		}
		return features;
	}
	
}