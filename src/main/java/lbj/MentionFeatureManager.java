package lbj;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import structure.Expression;
import structure.SimulProb;
import structure.Mention;
import utils.FeatureExtraction;
import utils.Tools;
import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class MentionFeatureManager {
	
	public void getAllFeatures(List<Mention> mentionList, int index, SimulProb simulProb) {
		// Add feature functions here
		Mention mention = mentionList.get(index);
		mention.features = new ArrayList<String>();
		mention.features.addAll(getPreviousTags(mentionList, index, simulProb));
		try {
			mention.features.addAll(similarityWithPastMentions(mentionList, index, simulProb));
			mention.features.addAll(getGlobalFeatures(mentionList, index, simulProb));
			mention.features.addAll(getHistoryFeatures(mentionList, index, simulProb));
			mention.features.addAll(getSOPrevious(mentionList, index, simulProb));
			mention.features.addAll(FeatureExtraction.getFormPP(
					simulProb.question, 
					mention.ta.getTokenCharacterOffset(index).getFirst(), 
					2));
			mention.features.addAll(FeatureExtraction.getPOSWindowPP(
					simulProb.question, 
					mention.ta.getTokenCharacterOffset(index).getFirst(), 
					2));
			mention.features.addAll(FeatureExtraction.getMixed(
					simulProb.question, 
					mention.ta.getTokenCharacterOffset(index).getFirst(), 
					2));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error in FeatureManager");
		}
	}
	
	// Feature functions from chunker
	public List<String> getPreviousTags(List<Mention> mentionList, int index, SimulProb simulProb) {
		List<String> features = new ArrayList<String>();
		Mention mention = mentionList.get(index);
		if(index-1>=0) {
			features.add("-1_"+mentionList.get(index-1).label);
		}
		if(index-2>=0) {
			features.add("-2_"+mentionList.get(index-2).label);
		}
		for(int i = 0; i < index; i++) {
			if(mentionList.get(i).label.contains("E1")) {
				features.add("E1_present_before");
				break;
			}
		}
		for(int i = 0; i < index; i++) {
			if(mentionList.get(i).label.contains("E2")) {
				features.add("E2_present_before");
				break;
			}
		}
		return features;
	}
	
	public List<String> getSOPrevious(List<Mention> mentionList, int index, SimulProb simulProb) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
				simulProb.question, ViewNames.POS, false);
		TokenLabelView posView = (TokenLabelView)ta.getView(ViewNames.POS);
		String[] tags = new String[3];
		int i;
		for(i = index; i > Math.max(0, index-2); --i) {
			tags[index-i] = posView.getLabel(i);
		}
		tags[index-i] = posView.getLabel(i);
		if(index-1 >= 0) {
			features.add("lt1_"+mentionList.get(index-1).label+"_"+tags[1]);
		}
		if(index-2 >= 0) {
			features.add("lt2_"+mentionList.get(index-2).label+"_"+tags[2]);
			features.add("ll_"+mentionList.get(index-1).label+"_"+
			mentionList.get(index-2).label);
		}
		return features;
	}
	
	public List<String> getHistoryFeatures(
			List<Mention> mentionList, int index, SimulProb simulProb) 
			throws Exception {
		List<String> features = new ArrayList<String>();
		Mention mention = mentionList.get(index);
		String lastLabel = null; 
		int lastLoc =  -1;
		for(int i = index-1; i >= 0; i--) {
			if(!mentionList.get(i).label.equals("O")) {
				lastLabel = mentionList.get(i).label;
				lastLoc = i;
				break;
			}
		}
		if(lastLoc>=0) {
			lastLabel = lastLabel.substring(2);
			for(int i=lastLoc+1; i<=index; i++) {
				features.add(lastLabel+"_"+mention.ta.getToken(index));
			}
		}
		lastLabel = null; 
		lastLoc =  -1;
		for(int i = index-1; i >= 0; i--) {
			if(!mention.ta.getToken(i).equalsIgnoreCase(mention.ta.getToken(index))) {
				lastLabel = mentionList.get(i).label;
				lastLoc = i;
				break;
			}
		}
		if(lastLoc>=0) {
			if(lastLabel.length() > 2) lastLabel = lastLabel.substring(2);
			features.add("LastLabel_"+lastLabel+"_"+mention.ta.getToken(index));
			features.add("LastLabel_"+lastLabel);
		}
		return features;
	}
	
	public List<String> getGlobalFeatures(
			List<Mention> mentionList, int index, SimulProb simulProb) 
					throws Exception {
		List<String> features = new ArrayList<String>();
		for(String feature : FeatureExtraction.getUnigrams(simulProb.question)) {
			features.add("UNIGRAM_"+feature);
		}
		for(String feature : FeatureExtraction.getBigrams(simulProb.question)) {
			features.add("BIGRAM_"+feature);
		}
		return features;
	}
	
	public List<String> similarityWithPastMentions(
			List<Mention> mentionList, int index, SimulProb simulProb) 
					throws Exception {
		List<String> features = new ArrayList<String>();
		TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
				simulProb.question, ViewNames.LEMMA, false);
		List<Constituent> questionLemmas = ta.getView(ViewNames.LEMMA).
				getConstituents();
		for(int i = 0; i < index; i++) {
			if(questionLemmas.get(i).getLabel().equals(
					questionLemmas.get(index).getLabel())) {
				features.add("PRESENT_BEFORE_WITH_"+mentionList.get(i).label);
			}
		}
		return features;
	}
}
