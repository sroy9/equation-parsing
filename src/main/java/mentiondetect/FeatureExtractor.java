package mentiondetect;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import structure.Clustering;
import structure.LabelSet;
import structure.Mention;
import structure.SimulProb;
import structure.VarSet;
import utils.FeatureExtraction;
import weka.core.SystemInfo;
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
		FeatureVectorBuffer fvb = new FeatureVectorBuffer();
		System.out.println(labelSet.labels.size());
		for (int i = 0; i < labelSet.labels.size(); i++) {
			try {
				long start_time = System.currentTimeMillis();
				fvb.addFeature(getFeatureVector(varSet, labelSet, i));
				long end_time = System.currentTimeMillis();
				long difference = (end_time - start_time);
				System.out.println("time: "+difference);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fvb.toFeatureVector();
	}
	
	public IFeatureVector getFeatureVector(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		
		FeatureVectorBuffer fvb;
//		long start_time = System.currentTimeMillis();
		fvb = new FeatureVectorBuffer();
		List<String> features = new ArrayList<>();
		features.addAll(addSurroundingTokens(varSet, labelSet, index));
		features.addAll(getNgramFeatures(varSet, labelSet, index));
		for (String feature : features) {
			if (!lm.containFeature(feature) && lm.isAllowNewFeatures()) {
				lm.addFeature(feature);
			}
			if (lm.containFeature(feature)) {
				fvb.addFeature(lm.getFeatureId(feature), 1.0);
			}
		}
//		long end_time = System.currentTimeMillis();
//		long difference = (end_time - start_time);
//		System.out.println("time2: "+difference);
		return fvb.toFeatureVector();
	}
	
	public List<String> addSurroundingTokens(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features = new ArrayList<String>();
		for(String feature : FeatureExtraction.getFormPP(
				varSet.simulProb.question, 
				varSet.ta.getTokenCharacterOffset(index).getFirst(), 
				2)) {
			features.add("Window_"+feature);
		}
		for(String feature : FeatureExtraction.getMixed(
				varSet.simulProb.question, 
				varSet.ta.getTokenCharacterOffset(index).getFirst(), 
				2)) {
			features.add("Window_"+feature);
		}
		for(String feature : FeatureExtraction.getPOSWindowPP(
				varSet.simulProb.question, 
				varSet.ta.getTokenCharacterOffset(index).getFirst(), 
				2)) {
			features.add("Window_"+feature);
		}
		return features;
	}
	
	public List<String> getNgramFeatures(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		List<String> features = new ArrayList<String>();
		for(String feature : FeatureExtraction.getUnigrams(varSet.ta.getText())) {
			features.add("Unigram_"+feature);
		}
		for(String feature : FeatureExtraction.getBigrams(varSet.ta.getText())) {
			features.add("Unigram_"+feature);
		}
		return features;
	}
		
		
		
		
		
		
		
		
		
		
		
		
	
}