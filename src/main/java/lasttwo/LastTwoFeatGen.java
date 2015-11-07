package lasttwo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import struct.lca.LcaX;
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

public class LastTwoFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public LastTwoFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		LastTwoX x = (LastTwoX) arg0;
		LastTwoY y = (LastTwoY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getVarTokenFeatureVector(LastTwoX x, LastTwoY y) {
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		List<String> features = VarFeatGen.getFeatures(varX, varY);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getFeatures(LastTwoX x, LastTwoY y) {
		List<String> features = new ArrayList<>();
		VarX varX = new VarX(x);
		VarY varY = new VarY(y);
		features.addAll(VarFeatGen.getFeatures(varX, varY));
		struct.lca.LcaX lcaX = new struct.lca.LcaX(x, y.varTokens, y.nodes);
		struct.lca.LcaY lcaY = new struct.lca.LcaY(y);
		features.addAll(struct.lca.LcaFeatGen.getFeatures(lcaX, lcaY));
		return features;
	}
	
	public IFeatureVector getPairFeatureVector(LcaX x, Node node) {
		List<String> features = struct.lca.LcaFeatGen.getPairFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	
}