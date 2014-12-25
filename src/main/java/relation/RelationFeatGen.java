package relation;

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

public class RelationFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;

	public RelationFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		RelationX blob = (RelationX) arg0;
		RelationY relationY= (RelationY) arg1;
		List<String> features = new ArrayList<>();
		features.addAll(getFeatures(blob, relationY));
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	// Cluster Features
	public IFeatureVector getFeatureVector(
			RelationX blob, RelationY labelSet) {
		List<String> feats = getFeatures(blob, labelSet);
		return FeatGen.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getFeatures(
			RelationX blob, RelationY labelSet) {
		List<String> features = new ArrayList<>();
		// Neighborhood tokens
		QuantSpan qs = blob.quantities.get(blob.index);
		int tokenId = blob.ta.getTokenIdFromCharacterOffset(qs.start);
		for(String feature : FeatGen.neighboringTokens(blob.lemmas, tokenId, 2)) {
			features.add(feature);
		}
		// If its the first token of R nature
		boolean Rbefore = false;
		for(int i=0; i<blob.index; i++) {
			if(blob.relations.get(i).startsWith("R")) {
				Rbefore = true;
			}
		}
		features.add("Rbefore_"+Rbefore);
		Sentence sent = blob.ta.getSentenceFromToken(tokenId);
		for(int i=0; i<blob.index; i++) {
			QuantSpan quant = blob.quantities.get(i);
			if(Tools.doesIntersect(new IntPair(quant.start, quant.end), 
					new IntPair(sent.getStartSpan(), sent.getEndSpan()))) {
				features.add("SameSentence_"+blob.relations.get(i));
			}
		}
		return features;
	}
}