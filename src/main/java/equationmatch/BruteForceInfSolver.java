package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import structure.Blob;
import structure.Expression;
import structure.Lattice;
import structure.Operation;
import edu.illinois.cs.cogcomp.core.math.ArgMax;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * Brute force search over all 64 possible eqns.
 * @author upadhya3
 *
 */
public class BruteForceInfSolver extends AbstractInferenceSolver implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5253748728743334706L;
	private AbstractFeatureGenerator featGen;
	
	public BruteForceInfSolver(AbstractFeatureGenerator  featGen) {
		this.featGen=featGen;
	}
	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		List<Lattice> Y = getPossibleLegalStructures(x);
		Float score,max=Float.NEGATIVE_INFINITY;
		IStructure argmax=null;
		for(IStructure y:Y)
		{
			score = featGen.decisionValue(wv, x, y);
			if(score>max)
			{
				max=score;
				argmax=y;
			}
		}
		return argmax;
	}

	public static List<Lattice> getPossibleLegalStructures(IInstance arg0) {
		Blob blob = (Blob) arg0;
		List<String> legalStructures = new ArrayList<String>();
		if(blob.termMap.containsKey("E1") && blob.termMap.containsKey("E2") 
				&& blob.termMap.containsKey("E3")) {
			for(int i = 0; i < blob.termMap.get("E1").size(); i++) {
				for(int j = 0;  j < blob.termMap.get("E2").size(); j++) {
					for(int k = 0; k < blob.termMap.get("E3").size(); k++) {
						for(Operation op : Operation.values()) {
							if(op == Operation.EQ || op == Operation.NONE
									|| op == Operation.COMPLETE) continue;
							legalStructures.add(i + "_" + op + "_" + j 
									+ "_" + k);
						}
					}
				}
			}
		}
		if(blob.termMap.containsKey("E1") && blob.termMap.containsKey("E2")) {
			for(int i = 0; i < blob.termMap.get("E1").size(); i++) {
				for(int j = 0;  j < blob.termMap.get("E2").size(); j++) {
					legalStructures.add(i+"_EQ_"+j);
				}
			}
		}
		List<Lattice> eqnPairs = new ArrayList<Lattice>();
		for(int i = 0; i < legalStructures.size(); i++) {
			if(allow(legalStructures.get(i), eqnPairs, blob)) {
				eqnPairs.add(new Lattice(Arrays.asList(legalStructures.get(i))));
			}
			for(int j = i+1; j < legalStructures.size(); j++) {
				if(allow(legalStructures.get(i), legalStructures.get(j), 
						eqnPairs, blob)) {
					eqnPairs.add(new Lattice(Arrays.asList(
							legalStructures.get(i), legalStructures.get(j))));
				}
			}
		}
		
		return eqnPairs;
	}
	
	public static boolean allow (String t1, List<Lattice> latticeList, Blob blob) {
		Lattice candidate = new Lattice(Arrays.asList(t1));
		for(Lattice lattice : latticeList) {
			if(lattice.equals(candidate)) {
				return false;
			}
		}
		if(blob.termMap.get("E1").size() == 1 && blob.termMap.get("E2").size() == 1) {
			return true;
		}
		return false;
	}
	
	public static boolean allow (
			String t1, String t2, List<Lattice> latticeList, Blob blob) {
		Lattice candidate = new Lattice(Arrays.asList(t1, t2));
		for(Lattice lattice : latticeList) {
			if(lattice.equals(candidate)) {
				return false;
			}
		}
		Set<String> s1 = new HashSet<>();
		Set<String> s2 = new HashSet<>();
		Set<String> s3 = new HashSet<>();
		String[] parts1 = t1.split("_");
		s1.add(parts1[0]);
		s2.add(parts1[2]);
		if(parts1.length >= 4) {
			s3.add(parts1[3]);
		}
		String[] parts2 = t2.split("_");
		s1.add(parts2[0]);
		s2.add(parts2[2]);
		if(parts2.length >= 4) {
			s3.add(parts2[3]);
		}
		if(blob.termMap.get("E1").size() != s1.size()) return false;
		if(blob.termMap.get("E2").size() != s2.size()) return false;
		if(blob.termMap.containsKey("E3")) {
			if(blob.termMap.get("E3").size() != s3.size()) return false;
			if(parts1.length >= 4 && parts2.length >= 4 && s3.size() == 1) {
				// Lets not share term in E3
				return false;
			}
		}
		if(!blob.termMap.containsKey("E3")) {
			if(s3.size() > 0) return false;
		}
		return true;
		
	}

	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		Lattice l1 = (Lattice) arg1;
		Lattice l2 = (Lattice) arg2;
		if(l1.equals(l2))
		{
			return 0;
		}
		else
		{
//			System.out.println("LOSS!");
			return 1;
		}
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector arg0,
			IInstance arg1, IStructure arg2) throws Exception {
		return getBestStructure(arg0, arg1);
	}

}
