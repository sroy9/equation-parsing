package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class JointFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public JointFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		JointX x = (JointX) arg0;
		JointY y = (JointY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	
	public IFeatureVector getAlignmentFeatureVector(JointX x, JointY y, int slotNo) {
		List<String> features = alignmentFeatures(x, y, slotNo);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
		
	private List<String> alignmentFeatures(JointX x, JointY y, int slotNo) {
		// TODO Auto-generated method stub
		return null;
	}

	public static List<String> getFeatures(JointX x, JointY y) {
		List<String> features = new ArrayList<>();
//		features.addAll(numVarFeatures(x, y.isOneVar));
		return features;
	}
	
	public static List<String> numVarFeatures(JointX x, boolean isOneVar) {
		List<String> features = new ArrayList<>();
		String prefix = ""+isOneVar;
		features.add(prefix+"_NumQuestionSentences_"+FeatGen.getQuestionSentences(x.ta).size());
		for(String feature : FeatGen.getLemmatizedBigrams(x.lemmas, 0, x.lemmas.size()-1)) {
			features.add(prefix+"_"+feature);
		}
		for(int i=0; i<x.skeleton.size()-1; ++i) {
			features.add(prefix+"_"+x.skeleton.get(i)+"_"+x.skeleton.get(i+1));
		}
		for(Sentence sent : FeatGen.getQuestionSentences(x.ta)) {
			for(int i=0; i<sent.size(); ++i) {
				features.add(prefix+"_Q_"+sent.getToken(i));
			}
			for(int i=0; i<sent.size()-1; ++i) {
				features.add(prefix+"_Q_"+sent.getToken(i)+"_"+sent.getToken(i+1));
			}
		}
		int numQuant = x.quantities.size();
		int uniqueQuant = Tools.uniqueNumbers(x.quantities).size();
		int numSent = x.ta.getNumberOfSentences();
		
		if(numQuant < 2) features.add(prefix+"_numQuant<2");
		else if(numQuant < 4) features.add(prefix+"_numQuant<4");
		else features.add(prefix+"_numQuant<6");
		
		if(uniqueQuant < 2) features.add(prefix+"_uniqueQuant<2");
		else if(uniqueQuant < 4) features.add(prefix+"_uniqueQuant<4");
		else features.add(prefix+"_uniqueQuant<6");
		
		if(numSent < 2) features.add(prefix+"_numSent<2");
		else if(numSent < 4) features.add(prefix+"_numSent<4");
		else features.add(prefix+"_numSent<6");
		return features;
	}

	public IFeatureVector getExpressionFeatureVector(JointX x, int i, int j,
			List<IntPair> division, String label) {
		// TODO Auto-generated method stub
		return null;
	}
	
}