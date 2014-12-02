package mentiondetect;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import structure.Clustering;
import structure.LabelSet;
import structure.Mention;
import structure.SimulProb;
import structure.VarSet;
import utils.FeatureExtraction;
import utils.FeatureVectorCacher;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.POSTag;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.Sentence;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class FeatureExtractor extends AbstractFeatureGenerator implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4648678253190655148L;
	/**
	 * This function returns a feature vector \Phi(x,y) based on an 
	 * instance-structure pair.
	 * 
	 * @return Feature Vector \Phi(x,y), where x is the input instance 
	 * and y is the
	 *         output structure
	 */
	protected Lexiconer lm = null;	
   
	public FeatureExtractor(Lexiconer lm) {		
		this.lm = lm;
	}

	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		VarSet varSet = (VarSet) arg0;
		LabelSet labelSet = (LabelSet) arg1;
		assert varSet.sent.size() == labelSet.labels.size();
		List<String> features = FeatureVectorCacher.getMentionDetectionFeatures(
				varSet, labelSet);
		if(features != null) {
			return FeatureExtraction.getFeatureVectorFromList(features, lm);
		}
		features = new ArrayList<String>();
		for(int i = 0; i < labelSet.labels.size(); i++) {
			try {
				features.addAll(getFeatures(varSet, labelSet, i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FeatureVectorCacher.cache(varSet, labelSet, features);
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getFeatureVector(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		return FeatureExtraction.getFeatureVectorFromList(
				getFeatures(varSet, labelSet, index), lm);
	}
		
	public List<String> getFeatures(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features = FeatureVectorCacher.getMentionDetectionFeatures(
				varSet, labelSet, index);
		if(features != null) return features;
		features =  new ArrayList<String>();
		features.addAll(addPrevLabels(varSet, labelSet, index));
		features.addAll(addSurroundingTokens(varSet, labelSet, index));
		features.addAll(getNgramFeatures(varSet, labelSet, index));
		FeatureVectorCacher.cache(varSet, labelSet, index, features);
		return features;
	}
	
	public List<String> addPrevLabels(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features = new ArrayList<String>();
		if(index > 0) features.add("PrevLabel_"+labelSet.labels.get(index-1));
		if(index > 1) features.add("PrevLabel_"+labelSet.labels.get(index-2)+"_"
				+labelSet.labels.get(index-1));
		return features;
	}
	
	public List<String> addSurroundingTokens(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "";
		if(index > 0) prefix = labelSet.labels.get(index-1);
		for(String feature : FeatureExtraction.getFormPP(
				varSet.simulProb.question, 
				varSet.ta.getTokenCharacterOffset(
						varSet.sent.getStartSpan()+index).getFirst(), 
				2)) {
			features.add(prefix+"_Window_"+feature);
		}
		for(String feature : FeatureExtraction.getMixed(
				varSet.simulProb.question, 
				varSet.ta.getTokenCharacterOffset(
						varSet.sent.getStartSpan()+index).getFirst(), 
				2)) {
			features.add(prefix+"_Window_"+feature);
		}
		for(String feature : FeatureExtraction.getPOSWindowPP(
				varSet.simulProb.question, 
				varSet.ta.getTokenCharacterOffset(
						varSet.sent.getStartSpan()+index).getFirst(), 
				2)) {
			features.add(prefix+"_Window_"+feature);
		}
		return features;
	}
	
	public List<String> getNgramFeatures(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = "";
		if(index > 0) prefix = labelSet.labels.get(index-1);
		for(String feature : FeatureExtraction.getUnigrams(varSet.sent.getText())) {
			features.add(prefix+"_Unigram_"+feature);
		}
		for(String feature : FeatureExtraction.getBigrams(varSet.sent.getText())) {
			features.add(prefix+"_Bigram_"+feature);
		}
		return features;
	}
		
		
		
		
		
		
		
		
		
		
		
		
	
}