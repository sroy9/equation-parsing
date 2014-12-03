package mentiondetect;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

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
		assert varSet.ta.size() == labelSet.labels.size();
		List<String> features; 
		features = new ArrayList<String>();
		for(int i = 0; i < labelSet.labels.size(); i++) {
			try {
				features.addAll(getFeatures(varSet, labelSet, i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getFeatureVector(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		return FeatureExtraction.getFeatureVectorFromList(
				getFeatures(varSet, labelSet, index), lm);
	}
		
	public List<String> getFeatures(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features; 
		features =  new ArrayList<String>();
		features.addAll(addPrevLabels(varSet, labelSet, index));
      	features.addAll(addSurroundingTokens(varSet, labelSet, index));
		features.addAll(addHistoryFeatures(varSet, labelSet, index));
		return features;
	}
	
	public List<String> addPrevLabels(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = labelSet.labels.get(index);
		if(index > 0) features.add(prefix + "_PrevLabel_"+labelSet.labels.get(index-1));
		if(index > 1) features.add(prefix + "_PrevLabel_"+labelSet.labels.get(index-2)
				+"_"+labelSet.labels.get(index-1));
		return features;
	}
	
	public List<String> addSurroundingTokens(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = labelSet.labels.get(index);
		// Words
		features.add(prefix+"_Word_"+varSet.ta.getToken(index));
		if(index > 0) features.add(prefix+"_Word_"+varSet.ta.getToken(index-1) 
				+ "_" + varSet.ta.getToken(index));
		// IsNumeric
		if(NumberUtils.isNumber(varSet.ta.getToken(index))) {
			features.add(prefix+"_Number");
		}
		if(index > 0 && NumberUtils.isNumber(varSet.ta.getToken(index-1))) {
			features.add(prefix+"_Prev_Number");
		}
		// POS
		features.add(prefix+"_POS_"+varSet.posTags.get(index).getLabel());
		if(index > 0) features.add(prefix+"_POS_"+varSet.posTags.get(index-1).getLabel() 
				+ "_" + varSet.posTags.get(index).getLabel());
		return features;
	}
	
	public List<String> addHistoryFeatures(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features = new ArrayList<String>();
		String prefix = labelSet.labels.get(index);
		for(int i = 0; i < index; i++) {
			if(varSet.ta.getToken(i).equals(varSet.ta.getToken(index)) && 
					!labelSet.labels.get(i).equals("O")) {
				features.add(prefix+"_Prev_Label_For_Same_Token_"
					+labelSet.labels.get(i).substring(2));
			}
		}
		for(int i = 0; i < index; i++) {
			if(!labelSet.labels.get(i).equals("O")) {
				features.add(prefix+"_Prev_Present_"+labelSet.labels.get(i).substring(2));
			}
		}
		return features;
	}
	
	
}