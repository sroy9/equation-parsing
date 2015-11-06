package struct.lca;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Node;
import utils.FeatGen;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
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

//	public IFeatureVector getGlobalFeatureVector(LcaX x, LcaY y) {
//		List<String> features = getGlobalFeatures(x, y);
//		return FeatGen.getFeatureVectorFromList(features, lm);
//	}
//	
//	public static List<String> getGlobalFeatures(LcaX x, LcaY y) {
//		List<String> features = new ArrayList<>();
//		for(int i=0; i<x.ta.size(); ++i) {
//			features.add(y.equation.root.getSignature()+"_Unigram_"+
//					x.ta.getToken(i).toLowerCase());
//			if(i<x.ta.size()-1) {
//				features.add(y.equation.root.getSignature()+"_Bigram_"+
//						x.ta.getToken(i).toLowerCase()+"_"+
//						x.ta.getToken(i+1).toLowerCase());
//			}
//		}
//		return features;
//	}
	
	
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
			IntPair ip1, ip2, ip;
			for(Node leaf1 : node.children.get(0).getLeaves()) {
//				if(leaf1.label.equals("VAR")) {
//					ip1 = x.candidateVars.get(leaf1.index);
//				} else {
//					QuantSpan qs = x.quantities.get(leaf1.index);
//					ip1 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
//							x.ta.getTokenIdFromCharacterOffset(qs.end));
//				}
				for(Node leaf2 : node.children.get(1).getLeaves()) {
//					if(leaf2.label.equals("VAR")) {
//						ip2 = x.candidateVars.get(leaf2.index);
//					} else {
//						QuantSpan qs = x.quantities.get(leaf2.index);
//						ip2 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
//								x.ta.getTokenIdFromCharacterOffset(qs.end));
//					}
//					boolean allow = true;
//					for(Node leaf : node.getLeaves()) {
//						if(leaf.label.equals("VAR")) {
//							ip = x.candidateVars.get(leaf.index);
//						} else {
//							QuantSpan qs = x.quantities.get(leaf.index);
//							ip = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
//									x.ta.getTokenIdFromCharacterOffset(qs.end));
//						}
//						if((ip1.getSecond()<=ip.getFirst() && ip.getSecond()<ip2.getFirst()) || 
//								(ip2.getSecond()<=ip.getFirst() && ip.getSecond()<ip1.getFirst())) {
//							allow = false;
//						}
//					}
//					if(!allow) continue;
					lca.LcaX lcaX = new lca.LcaX(x, leaf1, leaf2);
					lca.LcaY lcaY = new lca.LcaY(node.label);
					features.addAll(lca.LcaFeatGen.getFeatures(lcaX, lcaY));
					String label = node.label;
					if(label.equals("SUB") || label.equals("DIV")) label += "_REV";
					lcaX = new lca.LcaX(x, leaf2, leaf1);
					lcaY = new lca.LcaY(label);
					features.addAll(lca.LcaFeatGen.getFeatures(lcaX, lcaY));
				}
			}
		}
		List<String> newFeats = new ArrayList<String>();
		for(String feat : features) {
			newFeats.add(feat+"_"+node.getSignature());
		}
		return newFeats;
	}
	
	public static List<String> getPairFeaturesWithoutGlobalPrefix(LcaX x, Node node) {
		List<String> features = new ArrayList<String>();
		if(node.children.size() == 2) {
			IntPair ip1, ip2, ip;
			for(Node leaf1 : node.children.get(0).getLeaves()) {
//				if(leaf1.label.equals("VAR")) {
//					ip1 = x.candidateVars.get(leaf1.index);
//				} else {
//					QuantSpan qs = x.quantities.get(leaf1.index);
//					ip1 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
//							x.ta.getTokenIdFromCharacterOffset(qs.end));
//				}
				for(Node leaf2 : node.children.get(1).getLeaves()) {
//					if(leaf2.label.equals("VAR")) {
//						ip2 = x.candidateVars.get(leaf2.index);
//					} else {
//						QuantSpan qs = x.quantities.get(leaf2.index);
//						ip2 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
//								x.ta.getTokenIdFromCharacterOffset(qs.end));
//					}
//					boolean allow = true;
//					for(Node leaf : node.getLeaves()) {
//						if(leaf.label.equals("VAR")) {
//							ip = x.candidateVars.get(leaf.index);
//						} else {
//							QuantSpan qs = x.quantities.get(leaf.index);
//							ip = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
//									x.ta.getTokenIdFromCharacterOffset(qs.end));
//						}
//						if((ip1.getSecond()<=ip.getFirst() && ip.getSecond()<ip2.getFirst()) || 
//								(ip2.getSecond()<=ip.getFirst() && ip.getSecond()<ip1.getFirst())) {
//							allow = false;
//						}
//					}
//					if(!allow) continue;
					lca.LcaX lcaX = new lca.LcaX(x, leaf1, leaf2);
					lca.LcaY lcaY = new lca.LcaY(node.label);
					features.addAll(lca.LcaFeatGen.getFeatures(lcaX, lcaY));
					String label = node.label;
					if(label.equals("SUB") || label.equals("DIV")) label += "_REV";
					lcaX = new lca.LcaX(x, leaf2, leaf1);
					lcaY = new lca.LcaY(label);
					features.addAll(lca.LcaFeatGen.getFeatures(lcaX, lcaY));
				}
			}
		}
		return features;
	}
	
}