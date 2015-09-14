package struct.lca;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structure.Node;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class LcaFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public LcaFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
//	@Override
//	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
//		LcaX x = (LcaX) arg0;
//		LcaY y = (LcaY) arg1;
//		List<String> features = new ArrayList<String>();
//		features.addAll(getGlobalFeatures(x, y));
//		for(Node node : y.equation.root.getAllSubNodes()) {
//			if(node.children.size() == 2) {
//				features.addAll(getNodeFeatures(x, node));
//			}
//		}
//		return FeatGen.getFeatureVectorFromList(features, lm);
//	}
	
	public IFeatureVector getGlobalFeatureVector(LcaX x, LcaY y) {
		List<String> features = getGlobalFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public static List<String> getGlobalFeatures(LcaX x, LcaY y) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<x.ta.size(); ++i) {
			features.add(y.equation.root.getSignature()+"_Unigram_"+
					x.ta.getToken(i).toLowerCase());
			if(i<x.ta.size()-1) {
				features.add(y.equation.root.getSignature()+"_Bigram_"+
						x.ta.getToken(i).toLowerCase()+"_"+
						x.ta.getToken(i+1).toLowerCase());
			}
		}
		return features;
	}
	
//	public static List<String> getNodeFeatures(LcaX x, Node node) {
//		List<String> features = new ArrayList<String>();
//		Node child1 = node.children.get(0);
//		Node child2 = node.children.get(1);
//		IntPair ip1 = getSpan(x, node.children.get(0));
//		IntPair ip2 = getSpan(x, node.children.get(1));
//		String prefix = "";
//		int start, end;
//		if(!Tools.doesIntersect(ip1, ip2)) {
//			start = Math.min(ip1.getSecond(), ip2.getSecond());
//			end = Math.max(ip1.getFirst(), ip2.getFirst());
//			for(int i=start; i<=end; ++i) {
//				features.add(node.label+"_MidUnigram_"+x.ta.getToken(i).toLowerCase());
//				if(i<end-1) {
//					features.add(node.label+"_MidBigram_"+x.ta.getToken(i).toLowerCase()+
//							"_"+x.ta.getToken(i+1).toLowerCase());
//				}
//			}
//		}
//		if(Tools.doesContainNotEqual(ip1, ip2) || Tools.doesContainNotEqual(ip2, ip1)) {
//			if(Tools.doesContain(ip1, ip2)) {
//				start = ip2.getFirst();
//				end = ip2.getSecond();
//			} else {
//				start = ip1.getFirst();
//				end = ip1.getSecond();
//			}
//			for(int i=start; i<end; ++i) {
//				features.add(node.label+"_ContainedUnigram_"+x.ta.getToken(i).toLowerCase());
//				if(i<end-1) {
//					features.add(node.label+"_ContainedBigram_"+x.ta.getToken(i).toLowerCase()+
//							"_"+x.ta.getToken(i+1).toLowerCase());
//				}
//			}
//		}
//		if(ip1 == ip2) {
//			for(int i=ip1.getFirst(); i<ip1.getSecond(); ++i) {
//				features.add(node.label+"_SameUnigram_"+x.ta.getToken(i).toLowerCase());
//				if(i<ip1.getSecond()-1) {
//					features.add(node.label+"_SameBigram_"+x.ta.getToken(i).toLowerCase()+
//							"_"+x.ta.getToken(i+1).toLowerCase());
//				}
//			}
//		}
//		return features;
//	}

//	public IFeatureVector getNodeFeatureVector(LcaX x, Node node) {
//		List<String> features = getNodeFeatures(x, node);
//		return FeatGen.getFeatureVectorFromList(features, lm);
//	}
	
//	public static IntPair getSpan(LcaX x, Node node) {
//		Set<IntPair> spans = new HashSet<IntPair>();
//		for(Node leaf : node.getLeaves()) {
//			if(leaf.label.equals("VAR")) spans.add(x.candidateVars.get(leaf.index));
//			if(leaf.label.equals("NUM")) {
//				QuantSpan qs = x.quantities.get(leaf.index);
//				spans.add(new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
//						x.ta.getTokenIdFromCharacterOffset(qs.end)));
//			}
//		}
//		int min = 1000, max = 0;
//		for(IntPair span : spans) {
//			if(span.getFirst() < min) {
//				min = span.getFirst();
//			}
//			if(span.getSecond() > max) {
//				max = span.getSecond();
//			}
//		}
//		return new IntPair(min, max);
//	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		LcaX x = (LcaX) arg0;
		LcaY y = (LcaY) arg1;
		List<String> features = new ArrayList<String>();
		features.addAll(getGlobalFeatures(x, y));
		for(Node node : y.equation.root.getAllSubNodes()) {
			if(node.children.size() == 2) {
				features.addAll(getPairFeatures(x, node));
			}
		}
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public List<String> getPairFeatures(LcaX x, Node node) {
		List<String> features = new ArrayList<String>();
		if(node.children.size() == 2) {
			for(Node leaf1 : node.children.get(0).getLeaves()) {
				for(Node leaf2 : node.children.get(1).getLeaves()) {
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
	
	public IFeatureVector getPairFeatureVector(LcaX x, Node node) {
		List<String> features = getPairFeatures(x, node);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
//	public static List<String> getPairFeatures(lca.LcaX x, lca.LcaY y) {
//		List<String> features = new ArrayList<>();
//		IntPair ip1, ip2;
//		String prefix = "";
//		if(x.leaf1.label.equals("VAR")) {
//			prefix+="VAR";
//			ip1 = x.candidateVars.get(x.leaf1.index);
//		} else {
//			prefix+="NUM";
//			QuantSpan qs = x.quantities.get(x.leaf1.index);
//			ip1 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
//					x.ta.getTokenIdFromCharacterOffset(qs.end));
//		}
//		if(x.leaf2.label.equals("VAR")) {
//			prefix+="VAR";
//			ip2 = x.candidateVars.get(x.leaf2.index);
//		} else {
//			prefix+="NUM";
//			QuantSpan qs = x.quantities.get(x.leaf2.index);
//			ip2 = new IntPair(x.ta.getTokenIdFromCharacterOffset(qs.start), 
//					x.ta.getTokenIdFromCharacterOffset(qs.end));
//		}
//		if(ip1.getFirst() > ip2.getFirst()) prefix += "REV";
//		if(ip1.getFirst() == ip2.getFirst() && ip1.getSecond() > ip2.getSecond()) prefix += "REV";
//		
//		int min = Math.min(ip1.getSecond(), ip2.getSecond());
//		int max = Math.max(ip1.getFirst(), ip2.getFirst());
//		int left = Math.min(ip1.getFirst(), ip2.getFirst());
//		int right = Math.max(ip1.getSecond(), ip2.getSecond());
//		for(int i=min; i<max; ++i) {
//			addFeature(prefix+"_MidUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
//		}
//		for(int i=min; i<max-1; ++i) {
//			addFeature(prefix+"_MidBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.ta.getToken(i+1).toLowerCase(), y, features);
//			addFeature(prefix+"_MidLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.posTags.get(i+1).getLabel(), y, features);
//			addFeature(prefix+"_MidPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
//					x.ta.getToken(i+1).toLowerCase(), y, features);
//		}
//		for(int i=ip1.getFirst(); i<ip1.getSecond(); ++i) {
//			addFeature(prefix+"_FirstUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
//		}
//		for(int i=ip1.getFirst(); i<ip1.getSecond()-1; ++i) {
//			addFeature(prefix+"_FirstBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.ta.getToken(i+1).toLowerCase(), y, features);
//			addFeature(prefix+"_FirstLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.posTags.get(i+1).getLabel(), y, features);
//			addFeature(prefix+"_FirstPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
//					x.ta.getToken(i+1).toLowerCase(), y, features);
//		}
//		for(int i=ip2.getFirst(); i<ip2.getSecond(); ++i) {
//			addFeature(prefix+"_SecondUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
//		}
//		for(int i=ip2.getFirst(); i<ip2.getSecond()-1; ++i) {
//			addFeature(prefix+"_SecondBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.ta.getToken(i+1).toLowerCase(), y, features);
//			addFeature(prefix+"_SecondLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.posTags.get(i+1).getLabel(), y, features);
//			addFeature(prefix+"_SecondPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
//					x.ta.getToken(i+1).toLowerCase(), y, features);
//		}
//		for(int i=Math.max(0, left-2); i<left; ++i) {
//			addFeature(prefix+"_LeftUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
//			addFeature(prefix+"_LeftBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.ta.getToken(i+1).toLowerCase(), y, features);
////			addFeature(prefix+"_LeftLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
////					x.posTags.get(i+1).getLabel(), y, features);
////			addFeature(prefix+"_LeftPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
////					x.ta.getToken(i+1).toLowerCase(), y, features);
//		}
//		for(int i=right; i<Math.min(x.ta.size()-1, right+2); ++i) {
//			addFeature(prefix+"_RightUnigram_"+x.ta.getToken(i).toLowerCase(), y, features);
//			addFeature(prefix+"_RightBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
//					x.ta.getToken(i+1).toLowerCase(), y, features);
////			addFeature(prefix+"_RightLexPosBigram_"+x.ta.getToken(i).toLowerCase()+"_"+
////					x.posTags.get(i+1).getLabel(), y, features);
////			addFeature(prefix+"_RightPosLexBigram_"+x.posTags.get(i).getLabel()+"_"+
////					x.ta.getToken(i+1).toLowerCase(), y, features);
//		}
//		if(prefix.contains("NUMNUM")) {
//			if(x.leaf1.value > x.leaf2.value) {
//				addFeature(prefix+"_Desc", y, features);
//			} else {
//				addFeature(prefix+"_Asc", y, features);
//			}
//		}
//		return features;
//	}
//
//	public static void addFeature(String string, lca.LcaY y, List<String> features) {
//		features.add(string+"_"+y);
//	}
}