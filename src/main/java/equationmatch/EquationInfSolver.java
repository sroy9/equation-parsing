package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import structure.Equation;
import structure.Operation;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class EquationInfSolver extends AbstractInferenceSolver 
implements Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private EquationFeatureExtractor featGen;
	
	public EquationInfSolver(EquationFeatureExtractor featGen) {
		this.featGen=featGen;
	}
	
	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		
		return null;
	}

	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		Lattice l1 = (Lattice) arg1;
		Lattice l2 = (Lattice) arg2;
		if(l1.equals(l2))
		{
			return 0;
		}
		else {
//			System.out.println("LOSS!");
			return 1;
		}
	}

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector wv, IInstance arg1, IStructure arg2) throws Exception {
		Blob blob = (Blob) arg1;
		Lattice gold = (Lattice) arg2;
		Map<String, List<QuantSpan>> clusterMap = blob.simulProb.clusterMap;
		List<Pair<Lattice, Double>> tmpLatticeList = 
				new ArrayList<Pair<Lattice, Double>>();
		Lattice best = null;
		BoundedPriorityQueue<Pair<Lattice, Double>> beam = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(50);
		beam.add(new Pair<Lattice, Double>(new Lattice(), 0.0));
		// Enumerate all equations
		for(int i = 0; i < 2; i++) {
			// Transfer states from beam to tmpLatticeList
			Iterator<Pair<Lattice, Double>> it = beam.iterator();
			tmpLatticeList.clear();
			for(;it.hasNext();) {
				tmpLatticeList.add(it.next());
			}
			beam.clear();
			for(QuantSpan qs : clusterMap.get("E1")) {
				for(Pair<Lattice, Double> pair : tmpLatticeList) {
					Lattice tmpLattice = new Lattice(pair.getFirst());
					beam.add(new Pair<>(tmpLattice, pair.getSecond()));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A1.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A1"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A1.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A1"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A2.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A2"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A2.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A2"))));
				}
				it = beam.iterator();
				tmpLatticeList.clear();
				for(;it.hasNext();) {
					tmpLatticeList.add(it.next());
				}
				beam.clear();
			}

			for(QuantSpan qs : clusterMap.get("E2")) {
				for(Pair<Lattice, Double> pair : tmpLatticeList) {
					Lattice tmpLattice = new Lattice(pair.getFirst());
					beam.add(new Pair<>(tmpLattice, pair.getSecond()));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B1.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B1"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B1.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B1"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B2.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B2"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B2.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B2"))));
				}
				it = beam.iterator();
				tmpLatticeList.clear();
				for(;it.hasNext();) {
					tmpLatticeList.add(it.next());
				}
				beam.clear();
			}
			
			for(QuantSpan qs : clusterMap.get("E3")) {
				for(Pair<Lattice, Double> pair : tmpLatticeList) {
					Lattice tmpLattice = new Lattice(pair.getFirst());
					beam.add(new Pair<>(tmpLattice, pair.getSecond()));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).C.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "C"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).C.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "C"))));
				}
				best = beam.element().getFirst();
				it = beam.iterator();
				tmpLatticeList.clear();
				for(;it.hasNext();) {
					tmpLatticeList.add(it.next());
				}
				beam.clear();
			}
		}
		return best;
	}

}
