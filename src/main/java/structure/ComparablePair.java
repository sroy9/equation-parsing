package structure;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

public abstract class ComparablePair<T> extends Pair<T, Double> implements Comparable<T>{

	public ComparablePair(T t, Double score) {
		super(t, score);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4715233082983654916L;

	@Override
	public int compareTo(T o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
