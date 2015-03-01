package template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import utils.FeatGen;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class TemplateFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public TemplateFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		TemplateX x = (TemplateX) arg0;
		TemplateY y = (TemplateY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	
	public IFeatureVector getAlignmentFeatureVector(TemplateX x, TemplateY y, int slotNo) {
		List<String> features = alignmentFeatures(x, y, slotNo);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	public static List<String> getFeatures(TemplateX x, TemplateY y) {
		List<String> features = new ArrayList<>();
		int numLeaves = y.equation.root.getLeaves().size();
		for(int i=0; i<numLeaves; ++i) {
			features.addAll(alignmentFeatures(x, y, i));
		}
		return features;
	}
	
	public static List<String> alignmentFeatures(TemplateX x, TemplateY y, int slotNo) {
		List<String> features = new ArrayList<>();
		features.addAll(singleSlotFeatures(x, y, slotNo));
		for(int i=0; i<slotNo; ++i) {
			features.addAll(slotPairFeatures(x, y, i, slotNo));
		}
		if(slotNo == 0) {
			features.addAll(documentFeatures(x, y));
		}
		return features;
	}
	
	public static List<String> singleSlotFeatures(TemplateX x, TemplateY y, int slotNo) {
		List<String> features = new ArrayList<>();
		return features;
	}
	
	public static List<String> slotPairFeatures(TemplateX x, TemplateY y, int slot1, int slot2) {
		List<String> features = new ArrayList<>();
		return features;
	}
	
	public static List<String> documentFeatures(TemplateX x, TemplateY y) {
		List<String> features = new ArrayList<>();
		List<String> unigrams = FeatGen.getUnigrams(x.ta);
		for(int i=0; i<unigrams.size(); ++i) {
			features.add(y.templateId+"_"+unigrams.get(i));
			if(i<unigrams.size()-1) {
				features.add(y.templateId+"_"+unigrams.get(i)+"_"+unigrams.get(i+1));
			}
		}
		return features;
	}
	

}