package semparse;

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
import utils.FeatureExtraction;
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

public class SemFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;

	public SemFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		SemX blob = (SemX) arg0;
		Lattice lattice = (Lattice) arg1;
		List<String> features = new ArrayList<>();
		for(int i=0; i<lattice.labelSet.labels.size(); ++i) {
			features.addAll(getClusterFeatures(blob, lattice.labelSet, i));
		}
		features.addAll(getEquationFeatures(blob, lattice));
		return FeatureExtraction.getFeatureVectorFromList(features, lm);
	}

	// Cluster Features
	public IFeatureVector getClusterFeatureVector(
			SemX blob, SemY labelSet, int index) {
		List<String> feats = getClusterFeatures(blob, labelSet, index);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getClusterFeatures(
			SemX blob, SemY labelSet, int index) {
		List<String> features = new ArrayList<>();
		String prefix = labelSet.labels.get(index);
		QuantSpan qs = blob.quantities.get(index);
//		System.out.println("Cluster Features");
		for(String feature : singleFeatures(new IntPair(qs.start, qs.end), blob)) {
			features.add(prefix+"_"+feature);
//			System.out.println(prefix+"_"+feature);
		}
		return features;
	}
	
	// Equation Features
	public IFeatureVector getEquationFeatureVector(
			SemX blob, Lattice lattice) throws Exception {
		List<String> feats = getEquationFeatures(blob, lattice);
		return FeatureExtraction.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getEquationFeatures(SemX blob, Lattice lattice)  {
		List<String> features = new ArrayList<>();
		for(int i=0; i<2; i++) {
			Equation eq = lattice.equations.get(i);
			for(int j=0; j<5; ++j) {
				for(int k=0; k<eq.terms.get(j).size(); ++k) {
					features.addAll(getEquationFeatures(
						blob, lattice, i, j, k));
				}
			}
		}
		return features;
	}
	 
	public List<String> getEquationFeatures(
			SemX blob, Lattice lattice, int i, int j, int k) {
		List<String> features = new ArrayList<>();
		String prefix = "";
		if(j==0 || j==2) prefix = "AB1";
		if(j==1 || j==3) prefix = "AB2";
		if(j==4) prefix = "C";
		for(String feature : singleFeatures(
				lattice.equations.get(i).terms.get(j).get(k).getSecond(), 
				j, blob, lattice)) {
			features.add(prefix+"_"+feature);
		}
		return features;
	}
	
	// Utility functions
	
	public List<IntPair> getRelevantSpans(
			SemX blob, int index, Double d, List<List<QuantSpan>> clusters) {
		List<IntPair> relevantSpans = new ArrayList<IntPair>();
		for(QuantSpan qs : clusters.get(index/2)) {
			if(Tools.safeEquals(d, Tools.getValue(qs))) {
				relevantSpans.add(new IntPair(qs.start, qs.end));
			}
		}
		return relevantSpans;
	}
	
	public List<String> pairwiseFeatures(
			Double d1, int index1, Double d2, int index2, SemX blob, Lattice lattice) {
		List<String> features = new ArrayList<>();
		List<IntPair> spans1 = getRelevantSpans(blob, index1, d1, lattice.clusters);
		List<IntPair> spans2 = getRelevantSpans(blob, index2, d2, lattice.clusters);
		int pos1 = blob.ta.getTokenIdFromCharacterOffset(spans1.get(0).getFirst());
		int pos2 = blob.ta.getTokenIdFromCharacterOffset(spans2.get(0).getFirst());
		int sent1 = blob.ta.getSentenceFromToken(pos1).getSentenceId();
		int sent2 = blob.ta.getSentenceFromToken(pos2).getSentenceId();
		if(sent1 == sent2) {
			features.add("SameSentence");
			for(int i=Math.min(pos1, pos2)+1; i<Math.max(pos1, pos2); i++) {
				features.add("WordsInBetween_"+blob.ta.getToken(i));
			}
			for(int i=Math.min(pos1, pos2)+1; i<Math.max(pos1, pos2)-1; i++) {
				features.add("WordsInBetween_"+blob.ta.getToken(i)+"_"+blob.ta.getToken(i+1));
			}
		} else {
			features.add("DifferentSentence");
		}
		return features;
	}
	
	public List<String> globalFeatures(SemX blob) {
		List<String> features = new ArrayList<>();
		return features;	
	}
	
	public List<String> singleFeatures(Double d, int index, SemX blob, Lattice lattice) {
		// Ready the data structures
		List<IntPair> spans = getRelevantSpans(blob, index, d, lattice.clusters);
		if(spans.size() == 0) {
			System.out.println("Number not found : "+d);
			System.out.println(blob.simulProb.index+" :Text : "+blob.ta);
			System.out.println("Quantities : "+Arrays.asList(blob.quantities));
		}
		return singleFeatures(spans.get(0), blob);
	}
	
	public List<String> singleFeatures(IntPair span, SemX blob) {
		List<String> features = new ArrayList<>();
		int pos = blob.ta.getTokenIdFromCharacterOffset(span.getFirst());
		Sentence sent = blob.ta.getSentenceFromToken(pos);
		String tokens[] = new String[blob.ta.size()];
		for(int i=0; i<blob.ta.size(); ++i) {
			if(NumberUtils.isNumber(blob.ta.getToken(i))) {
				tokens[i] = "NUMBER";
			} else if(blob.ta.getToken(i).contains("$") 
					|| blob.ta.getToken(i).contains("dollar") 
					|| blob.ta.getToken(i).contains("cents")) {
				tokens[i] = "MONEY_UNIT";
			} else {
				tokens[i] = blob.lemmas.get(i).getLabel();
			}
		}
		// Nearby unigrams and bigrams
		for(int i = Math.max(pos-3, sent.getStartSpan());
				i <= Math.min(pos+3, sent.getEndSpan()-1); ++i) {
			features.add("Unigram_"+(i-pos)+"_"+tokens[i]);
			features.add("Unigram_"+tokens[i]);
		}
		for(int i = Math.max(pos-3, sent.getStartSpan());
				i <= Math.min(pos+3, sent.getEndSpan()-1)-1; ++i) {
			features.add("Bigram_"+(i-pos)+"_"+tokens[i]+"_"+tokens[i+1]);
			features.add("Bigram_"+tokens[i]+"_"+tokens[i+1]);
		}
		return features;
	}
}