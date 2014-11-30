package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class FeatureExtraction {
	
	public static List<String> getLemmatizedUnigrams(
			TextAnnotation ta, int start, int end) throws Exception {
		List<Constituent> questionLemmas = ta.getView(ViewNames.LEMMA).
				getConstituents();
		List<String> unigrams = new ArrayList<String>();
		for(int i = start; i <= end; ++i) {
			unigrams.add(questionLemmas.get(i).getLabel());
		}
		return unigrams;
	}

	public static List<String> getLemmatizedBigrams(
			TextAnnotation ta, int start, int end) throws Exception {
		List<Constituent> questionLemmas = ta.getView(ViewNames.LEMMA).
				getConstituents();
		List<String> bigrams = new ArrayList<String>();
		for(int i = start; i < end; ++i) {
			bigrams.add(questionLemmas.get(i).getLabel()+"_"+
					questionLemmas.get(i+1).getLabel());
		}
		return bigrams;
	}
	
	public static List<String> getUnigrams(String text) throws Exception {
		TextAnnotation ta = new TextAnnotation("", "", text);
		List<String> unigrams = new ArrayList<String>();
		for(int i = 0; i < ta.size(); ++i) {
			unigrams.add(ta.getToken(i).toLowerCase());
		}
		return unigrams;
	}

	public static List<String> getBigrams(String text) throws Exception {
		TextAnnotation ta = new TextAnnotation("", "", text);
		List<String> bigrams = new ArrayList<String>();
		for(int i = 0; i < ta.size()-1; ++i) {
			bigrams.add(ta.getToken(i).toLowerCase()+"_"
					+ta.getToken(i+1).toLowerCase());
		}
		return bigrams;
	}
	public static List<String> getBigramsPOS(String text) throws Exception {
		List<String> bigrams = new ArrayList<String>();
		return bigrams;
	}
	public static List<String> getConjunctions(List<String> features) {
		List<String> conjunctions = new ArrayList<String>();
		for(String feature1 : features) {
			for(String feature2 : features) {
				conjunctions.add(feature1+"_"+feature2);
			}
		}
		return conjunctions;
	}
	
	public static List<String> getConjunctions(
			List<String> features1, List<String> features2) {
		List<String> conjunctions = new ArrayList<String>();
		for(String feature1 : features1) {
			for(String feature2 : features2) {
				conjunctions.add(feature1+"_"+feature2);
			}
		}
		return conjunctions;
	}
	
	
	public static List<String> getUnigramsInWindow(
			String text, int charOffset, int window) {
		TextAnnotation ta = new TextAnnotation("", "", text);
		int pos = ta.getTokenIdFromCharacterOffset(charOffset);
		List<String> unigrams = new ArrayList<String>();
		for(int i = Math.max(0, pos-window); 
				i <= Math.min(pos+window, ta.size()-1); 
				++i) {
			unigrams.add(ta.getToken(i).toLowerCase());
		}
		return unigrams;
	}
	
	
	public static List<String> getUnigramsInWindowWithRelativePosition(
			String text, int charOffset, int window) {
		TextAnnotation ta = new TextAnnotation("", "", text);
		int pos = ta.getTokenIdFromCharacterOffset(charOffset);
		List<String> unigrams = new ArrayList<String>();
		for(int i = Math.max(0, pos-window); 
				i <= Math.min(pos+window, ta.size()-1); 
				++i) {
			unigrams.add((i-pos)+"_"+ta.getToken(i).toLowerCase());
		}
		return unigrams;
	}
	
	public static List<String> getPOSInWindowWithRelativePosition(
			String text, int charOffset, int window) throws Exception {
		TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
				text, ViewNames.POS, false);
		TokenLabelView posView = (TokenLabelView)ta.getView(ViewNames.POS);
		int pos = ta.getTokenIdFromCharacterOffset(charOffset);
		List<String> unigrams = new ArrayList<String>();
		for(int i = Math.max(0, pos-window); 
				i <= Math.min(pos+window, ta.size()-1); 
				++i) {
			unigrams.add((i-pos)+"_"+posView.getLabel(i));
		}
		return unigrams;
	}
	
	// From Chunker
	public static List<String> getFormPP(String text, int charOffset, int window) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
				text, ViewNames.POS, false);
		int pos = ta.getTokenIdFromCharacterOffset(charOffset);
		int before = 2;
		int after = 2;
		int k = 2;
		String[] forms = new String[before+after+1];
		for (int i = Math.max(0, pos-before); 
				i <= Math.min(ta.size()-1, pos+after); 
				++i) {
			forms[i-Math.max(0, pos-before)] = ta.getToken(i);
		}  
		for (int j = 0; j < k; j++) {
			for (int i = 0; i < forms.length; i++) {
				StringBuffer f = new StringBuffer();
				for(int context=0; context <= j && i + context < forms.length; context++) {
					if (context != 0) f.append("_");
					f.append(forms[i+context]);
				}
				features.add(i + "_" + j + "_" + f.toString());
			}
		}
		return features;
	}
	
	// From Chunker
	public static List<String> getPOSWindowPP(String text, int charOffset, int window) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
				text, ViewNames.POS, false);
		TokenLabelView posView = (TokenLabelView)ta.getView(ViewNames.POS);
		int pos = ta.getTokenIdFromCharacterOffset(charOffset);
		int before = 3;
		int after = 3;
		int k = 3;
		String[] tags = new String[before+after+1];
		for (int i = Math.max(0, pos-before); 
				i <= Math.min(ta.size()-1, pos+after); 
				++i) {
			tags[i-Math.max(0, pos-before)] = posView.getLabel(i);
		}  
		for (int j = 0; j < k; j++) {
			for (int i = 0; i < tags.length; i++) {
				StringBuffer f = new StringBuffer();
				for(int context=0; 
						context <= j && i + context < tags.length; 
						context++) {
					if (context != 0) f.append("_");
					f.append(tags[i+context]);
				}
				features.add(i + "_" + j + "_" + f.toString());
			}
		}
		return features;
	}
	
	// From Chunker
	public static List<String> getMixed(String text, int charOffset, int window) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
				text, ViewNames.POS, false);
		TokenLabelView posView = (TokenLabelView)ta.getView(ViewNames.POS);
		int pos = ta.getTokenIdFromCharacterOffset(charOffset);
		int before = 2;
		int after = 2;
		int k = 2;
		String[] tags = new String[before+after+1];
		String[] forms = new String[before+after+1];
		for (int i = Math.max(0, pos-before); 
				i <= Math.min(ta.size()-1, pos+after); 
				++i) {
			tags[i-Math.max(0, pos-before)] = posView.getLabel(i);
			forms[i-Math.max(0, pos-before)] = ta.getToken(i);
		}
		for (int j = 1; j < k; j++) {
			for (int x = 0; x < 2; x++) {
				boolean t = true;
				for (int i = 0; i < tags.length; i++) {
					StringBuffer f = new StringBuffer();
					for(int context=0; 
							context <= j && i + context < tags.length; 
							context++) {
						if (context != 0) f.append("_");
						if (t && x ==0) {
							f.append(tags[i+context]);
						} else {
							f.append(forms[i+context]);
						}
						t = !t;
					}
					features.add(i + "_" + j + "_" + f.toString());
				}
			}
		}
		return features;
	}
	
	public static List<String> getMixed(
			String text, int startOffset, int endOffset, int window) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = new TextAnnotation("", "", text);
		int startPos = ta.getTokenIdFromCharacterOffset(startOffset);
		int endPos = ta.getTokenIdFromCharacterOffset(endOffset);
		for(int i = startPos; i <= endPos; i++) {
			features.addAll(getMixed(
					text, ta.getTokenCharacterOffset(i).getFirst(), window));
		}
		return features;
	}
	
	public static List<String> getPOSWindowPP(
			String text, int startOffset, int endOffset, int window) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = new TextAnnotation("", "", text);
		int startPos = ta.getTokenIdFromCharacterOffset(startOffset);
		int endPos = ta.getTokenIdFromCharacterOffset(endOffset);
		for(int i = startPos; i <= endPos; i++) {
			features.addAll(getPOSWindowPP(
					text, ta.getTokenCharacterOffset(i).getFirst(), window));
		}
		return features;
	}
	
	public static List<String> getFormPP(
			String text, int startOffset, int endOffset, int window) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = new TextAnnotation("", "", text);
		int startPos = ta.getTokenIdFromCharacterOffset(startOffset);
		int endPos = ta.getTokenIdFromCharacterOffset(endOffset);
		for(int i = startPos; i <= endPos; i++) {
			features.addAll(getFormPP(
					text, ta.getTokenCharacterOffset(i).getFirst(), window));
		}
		return features;
	}
	
	public static IntPair getClosestMention(Set<IntPair> ipSet, IntPair key) {
		int minDist = 1000; 
		IntPair closest = null;
		for(IntPair ip : ipSet) {
			if(Math.abs(ip.getFirst()-key.getFirst()) < minDist) {
				minDist = Math.abs(ip.getFirst()-key.getFirst());
				closest = ip;
			}
		}
		return closest;
	}
	
	public static IntPair getSpanBetweenClosestMention(Set<IntPair> ipSet1, Set<IntPair> ipSet2) {
		int minDist = 1000; 
		IntPair closest1 = null;
		IntPair closest2 = null;
		for(IntPair ip1 : ipSet1) {
			for(IntPair ip2 : ipSet2) {
				if(Math.abs(ip1.getFirst()-ip2.getFirst()) < minDist) {
					minDist = Math.abs(ip1.getFirst()-ip2.getFirst());
					closest1 = ip1;
					closest2 = ip2;
				}
			}
		}
		return new IntPair(Math.min(closest1.getSecond(), closest2.getSecond()),
				Math.max(closest1.getFirst(), closest2.getFirst()));
	}
}
