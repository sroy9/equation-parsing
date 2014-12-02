package sl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import structure.Blob;
import structure.Clustering;
import structure.Expression;
import structure.Lattice;
import structure.Mention;
import structure.Operation;
import structure.SimulProb;
import structure.Span;
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

public class LatticeFeatureExtractor extends AbstractFeatureGenerator implements
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1810851154558168679L;
	/**
	 * This function returns a feature vector \Phi(x,y) based on an
	 * instance-structure pair.
	 * 
	 * @return Feature Vector \Phi(x,y), where x is the input instance and y is
	 *         the output structure
	 */
	public Lexiconer lm = null;

	public LatticeFeatureExtractor(Lexiconer lm) {
		this.lm = lm;
	}

	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		Blob blob = (Blob) arg0;
		Lattice l = (Lattice) arg1;
		List<String> features = FeatureVectorCacher.getFeature(blob, l);
		FeatureVectorBuffer fb = new FeatureVectorBuffer();

		if (features == null) {
			System.out.println("not in cache");
			features = new ArrayList<>();
			try {
				features.addAll(extractFeatures(blob, l));
				features.addAll(FeatureExtraction
						.getConjunctions(extractFeatures(blob, l)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!features.isEmpty())
				FeatureVectorCacher.cache(blob,l,features);
		}
		
		for (String feature : features) {
			if (!lm.containFeature(feature) && lm.isAllowNewFeatures()) {
				lm.addFeature(feature);
			}
			if (lm.containFeature(feature)) {
				fb.addFeature(lm.getFeatureId(feature), 1.0);
			}
		}
		// System.out.println(lm.getNumOfFeature());
		// System.out.println(fb.toFeatureVector().getNumActiveFeatures());
		return fb.toFeatureVector();
	}

	private List<String> extractFeatures(Blob blob, Lattice l) throws Exception {
		// System.out.println("*********************");
		// System.out.println(blob.simulProb.index+" : "+blob.simulProb.question);
		// System.out.println("Equations :");
		// for(Expression eq : blob.simulProb.equations) {
		// System.out.println(eq.toString());
		// }
		// System.out.println("Spans :");
		// for(Span span : blob.simulProb.spans) {
		// System.out.println(span.label+" : "+blob.simulProb.question.substring(
		// span.ip.getFirst(), span.ip.getSecond())+ " : "+span.ip);
		// }
		// System.out.println("Cluster Map :");
		// for(String entity : blob.simulProb.clusterMap.keySet()) {
		// System.out.println(entity + " : " + Arrays.asList(
		// blob.simulProb.clusterMap.get(entity).mentionLocMap.keySet()));
		// }
		// System.out.println("TermMap");
		// for(String str : blob.termMap.keySet()) {
		// System.out.println(str + " : " +blob.termMap.get(str));
		// }
		// System.out.println("Paths :");
		for (String path : l.edgesWithOps) {
			// System.out.println(path);
			l.termList.add(getExpression(blob, path));
			l.opList.add(getOperation(blob, path));
		}
		List<String> feats = new ArrayList<>();
		feats.addAll(getStructureFeatures(blob, l));
		feats.addAll(getGlobalFeatures(blob, l));
		return feats;
	}

	public List<String> getStructureFeatures(Blob blob, Lattice l)
			throws Exception {
		List<String> features = new ArrayList<>();
		if (l.termList.size() == 1) {
			features.add("Op:" + l.opList.get(0));
			features.add("TermSize:"
					+ l.termList.get(0).get(0).getYield().size() + "_"
					+ l.termList.get(0).get(1).getYield().size());
		} else if (l.termList.size() == 2) {
			features.add("Op:" + l.opList.get(0) + "_" + l.opList.get(1));
			features.add("TermSize:"
					+ l.termList.get(0).get(0).getYield().size() + "_"
					+ l.termList.get(0).get(1).getYield().size());
			features.add("TermSize:"
					+ l.termList.get(1).get(0).getYield().size() + "_"
					+ l.termList.get(1).get(1).getYield().size());
		}
		return features;
	}

	public List<String> getGlobalFeatures(Blob blob, Lattice l)
			throws Exception {
		List<String> features = new ArrayList<>();
		for (String feature : FeatureExtraction
				.getUnigrams(blob.simulProb.question)) {
			features.add("Unigram_" + feature);
		}
		for (String feature : FeatureExtraction
				.getBigrams(blob.simulProb.question)) {
			features.add("Bigram_" + feature);
		}
		return features;
	}

	List<Expression> getExpression(Blob blob, String path1) {
		List<Expression> ans = new ArrayList<>();
		String[] parts = path1.split("_");
		int expr1ind = Integer.parseInt(parts[0]);
		int expr2ind = Integer.parseInt(parts[2]);
		int expr3ind = -100;
		if (parts.length >= 4) {
			expr3ind = Integer.parseInt(parts[3]);
		}
		if (expr1ind == 0)
			ans.add(blob.termMap.get("E1").get(0));
		else
			ans.add(blob.termMap.get("E1").get(1));
		if (expr2ind == 0)
			ans.add(blob.termMap.get("E2").get(0));
		else
			ans.add(blob.termMap.get("E2").get(1));
		if (parts.length >= 4) {
			if (expr3ind == 0)
				ans.add(blob.termMap.get("E3").get(0));
			else
				ans.add(blob.termMap.get("E3").get(1));
		}
		return ans;
	}

	Operation getOperation(Blob blob, String path1) {
		String[] parts = path1.split("_");
		return Operation.valueOf(parts[1]);
	}
}