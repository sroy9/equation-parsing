package structure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class Clustering implements IStructure {
	
	public List<IntPair> mentions;
	public Set<Set<Integer>> clusters;
	
	public Clustering(List<IntPair> mentions, Set<Set<Integer>> clusters) {
		this.mentions = mentions;
		this.clusters = clusters;
	}
	
	public Clustering(List<IntPair> mentions) {
		this.mentions = mentions;
		clusters = new HashSet<Set<Integer>>();
		for(int i = 0; i < mentions.size(); ++i) {
			Set<Integer> set = new HashSet<Integer>();
			set.add(i);
			clusters.add(set);
		}
	}
	
	public boolean doesBelongToSameCluster(int a, int b) {
		for(Set<Integer> cluster : clusters) {
			if(cluster.contains(a) && cluster.contains(b)) {
				return true;
			}
		}
		return false;
	}
}
