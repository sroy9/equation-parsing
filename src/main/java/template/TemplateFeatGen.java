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
		return features;
	}
	
	public static List<String> alignmentFeatures(TemplateX x, TemplateY y, int slotNo) {
		List<String> features = new ArrayList<>();
		return features;
	}

}