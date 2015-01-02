package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import semparse.SemFeatGen;
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

public class RelationFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;
	
	public RelationFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		RelationX x = (RelationX) arg0;
		RelationY y = (RelationY) arg1;
		List<String> features = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(features, lm);
	}
	
	public IFeatureVector getRelationFeatureVector(RelationX x, RelationY y) {
		List<String> feats = getRelationFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(feats, lm);
	}
	
	public IFeatureVector getEquationFeatureVector(RelationX x, RelationY y) {
		List<String> feats = getEquationFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
//		System.out.println("GetFeatures : "+y.equations.size());
		features.addAll(getRelationFeatures(x, y));
		features.addAll(getEquationFeatures(x, y));
		return features;
	}
	
	public List<String> getRelationFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
		for(int i=0; i<y.relations.size(); ++i) {
			features.addAll(relationFeatures(x, y, i));
		}
		return features;
	}
	
	public List<String> getEquationFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
		features.addAll(solutionFeatures(x, y));
		return features;
	}
	
	public List<String> relationFeatures(RelationX x, RelationY y, int index) {
		List<String> features = new ArrayList<>();
		String prefix = "";
		if(y.relations.get(index).startsWith("R")) {
			for(int i=0; i<index; ++i) {
				if(y.relations.get(i).startsWith("R")) {
					prefix = (y.relations.get(index).equals(
							y.relations.get(i)) ? "SAME" : "DIFF");
					QuantSpan qs1 = x.quantities.get(i);
					QuantSpan qs2 = x.quantities.get(index);
					int tokenId1 = x.ta.getTokenIdFromCharacterOffset(qs1.start);
					Sentence sent1 = x.ta.getSentenceFromToken(tokenId1);
					int tokenId2 = x.ta.getTokenIdFromCharacterOffset(qs2.start);
					Sentence sent2 = x.ta.getSentenceFromToken(tokenId2);
					if(sent1.getSentenceId() == sent2.getSentenceId()) {
						features.add(prefix+"_SameSentence");
					} else {
						features.add(prefix+"_DiffSentence");
					}
					if(Tools.safeEquals(Tools.getValue(qs1), Tools.getValue(qs2))) {
						features.add(prefix+"_SameNumber");
					}
					if(Tools.getUnit(qs1).contains(Tools.getUnit(qs2)) ||
							Tools.getUnit(qs2).contains(Tools.getUnit(qs1))) {
						features.add(prefix+"_SameUnit");
					}
				}
			}
		} else {
			prefix = y.relations.get(index);
			int tokenId = x.ta.getTokenIdFromCharacterOffset(x.quantities.get(index).start);
			Sentence sent = x.ta.getSentenceFromToken(tokenId);
			List<Constituent> sentLemmas = FeatGen.partialLemmas(
					x.lemmas, sent.getStartSpan(), sent.getEndSpan());
			List<Pair<String, IntPair>> sentSkeleton = FeatGen.getPartialSkeleton(
					x.skeleton, sent.getStartSpan(), sent.getEndSpan());
			for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton, tokenId, 3)) {
				features.add(prefix+"_"+feature);
			}
		}
		return features;
	}
	
	public List<String> solutionFeatures(RelationX x, RelationY y) {
		List<String> features = new ArrayList<>();
		List<Double> solns = EquationSolver.solveSemYs(y.equations); 
		if(solns == null) {
			features.add("Not_Solvable");
			return features;
		}
		for(Double d : solns) {
			if(d-d.intValue() < 0.0001) {
				features.add("Integer_Solution");
			}
			if(d>0) features.add("Positive solution");
		}
		return features;
	}
	
	
	
	
}