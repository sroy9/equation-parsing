package lasttwo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class LasttwoFeatGen extends AbstractFeatureGenerator implements Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public LasttwoFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		LasttwoX x = (LasttwoX) arg0;
		LasttwoY y = (LasttwoY) arg1;
		List<String> features = new ArrayList<>();
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
		
	public IFeatureVector getVarTokenFeatureVector(LasttwoX x, LasttwoY y) {
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		List<String> features = VarFeatGen.getFeatures(varX, varY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getNodeFeatureVector(LasttwoX x, Map<String, List<Integer>> varTokens, 
			List<Node> leaves, Node node) {
		TreeX treeX = new TreeX(x, varTokens, leaves);
		List<String> features = CompFeatGen.getNodeFeatures(treeX, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
}