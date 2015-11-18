//package tree;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//
//import structure.Node;
//import utils.FeatGen;
//import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
//import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
//import edu.illinois.cs.cogcomp.sl.core.IInstance;
//import edu.illinois.cs.cogcomp.sl.core.IStructure;
//import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
//import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
//
//public class NoncompFeatGen extends AbstractFeatureGenerator implements Serializable {
//	
//	private static final long serialVersionUID = 1810851154558168679L;
//	public Lexiconer lm = null;
//	
//	public NoncompFeatGen(Lexiconer lm) {
//		this.lm = lm;
//	}
//	
//	@Override
//	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
//		TreeX x = (TreeX) arg0;
//		TreeY y = (TreeY) arg1;
//		List<String> features = getFeatures(x, y);
//		return FeatGen.getFeatureVectorFromList(features, lm);
//	}
//
//	public IFeatureVector getNodeFeatureVector(TreeX x, Node node) {
//		List<String> features = getNodeFeatures(x, node);
//		return FeatGen.getFeatureVectorFromList(features, lm);
//	}
//	
//	public static List<String> getFeatures(TreeX x, TreeY y) {
//		List<String> features = new ArrayList<String>();
//		for(Node node : y.equation.root.getAllSubNodes()) {
//			if(node.children.size() == 2) {
//				features.addAll(getNodeFeatures(x, node));
//			}
//		}
//		return features;
//	}
//	
//	public static List<String> getNodeFeatures(TreeX x, Node node) {
//		List<String> features = new ArrayList<String>();
//		if(node.children.size() == 0) {
//			System.err.println("Pair Features called with a leaf : expected non-leaf");
//		}
//		Node node1 = node.children.get(0);
//		Node node2 = node.children.get(1);
//		IntPair ip1 = node1.getNodeListSpan();
//		IntPair ip2 = node2.getNodeListSpan();
//		if(node1.children.size()==0 && node2.children.size()==0 && 
//				(!node1.projection || !node2.projection)) {
//			features.addAll(TreeFeatGen.getNonProjectiveFeatures(x, node));
//		} else if(ip1.getFirst()!=-1 && ip2.getFirst()!=-1 && 
//				(((ip1.getSecond()+1)==ip2.getFirst()) || ((ip2.getSecond()+1)==ip1.getFirst()))) {
//			features.addAll(TreeFeatGen.getNonProjectiveFeatures(x, node));
//		} else {
//			features.add("NOT_ALLOWED_STUFF");
//		}
//		return features;
//	}
//	
//	
//}