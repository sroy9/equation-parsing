package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.w3c.dom.views.AbstractView;

import relation.RelationFeatGen;
import relation.RelationInfSolver;
import relation.RelationY;
import semparse.SemInfSolver;
import semparse.SemX;
import semparse.SemY;
import structure.EquationSolver;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class JointInfSolver extends AbstractInferenceSolver implements 
		Serializable {

	private static final long serialVersionUID = -4893813648991200683L;
	private JointFeatGen featGen;
	private SLModel relationModel;
	private SLModel equationModel;
	
	public JointInfSolver(JointFeatGen featGen, String relModel, String eqModel) 
			throws Exception {
		this.featGen = featGen;
		this.relationModel = SLModel.loadModel(relModel);
		this.equationModel = SLModel.loadModel(eqModel);
	}
	
	@Override
	public IStructure getBestStructure(WeightVector weight, IInstance ins)
			throws Exception {
		return getLossAugmentedBestStructure(weight, ins, null);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception {
		JointX prob = (JointX) ins;
		JointY gold = (JointY) goldStructure;
		JointY pred = null;
		Double bestScore = -Double.MAX_VALUE;
		for(JointY y : enumerateJointYs(prob)) {
			Double score = 1.0*weight.dotProduct(featGen.getFeatureVector(prob, y))
					+ (goldStructure == null ? 0.0 : JointY.getLoss(y, gold));
			if(score > bestScore) {
				bestScore = score;
				pred = y;
			}
		}
		return pred;
	}
	
	public List<JointY> enumerateJointYs(JointX prob) throws Exception {
		List<RelationY> list1 = new ArrayList<>();
		List<JointY> list2 = new ArrayList<>();
		relationModel.infSolver.getBestStructure(relationModel.wv, prob.relationX);
		for(Pair<RelationY, Double> pair : ((RelationInfSolver) relationModel.infSolver).beam) {
			list1.add(pair.getFirst());
		}
		for(RelationY relationY : list1) {
			List<SemX> clusters = SemX.extractEquationProbFromRelations(prob.relationX, relationY);
			if(clusters.size() == 1) {
				equationModel.infSolver.getBestStructure(equationModel.wv, clusters.get(0));
				for(Pair<SemY, Double> pair2 : ((SemInfSolver) equationModel.infSolver).beam) {
					List<SemY> equations = new ArrayList<>();
					equations.add(pair2.getFirst());
					list2.add(new JointY(relationY, equations));
				}
			} 
			if(clusters.size() == 2) {
				equationModel.infSolver.getBestStructure(equationModel.wv, clusters.get(0));
				List<Pair<SemY, Double>> l1 = new ArrayList<>();
				l1.addAll(((SemInfSolver) equationModel.infSolver).beam);
				equationModel.infSolver.getBestStructure(equationModel.wv, clusters.get(1));
				List<Pair<SemY, Double>> l2 = new ArrayList<>();
				l2.addAll(((SemInfSolver) equationModel.infSolver).beam);
				for(Pair<SemY, Double> pair2 : l1) {
					for(Pair<SemY, Double> pair3 : l2) {
						List<SemY> equations = new ArrayList<>();
						equations.add(pair2.getFirst());
						equations.add(pair3.getFirst());
						list2.add(new JointY(relationY, equations));
					}
				}
			}
			
		}
		return list2;
	}
	
	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {
		JointY y1 = (JointY) gold;
		JointY y2 = (JointY) pred;
		return JointY.getLoss(y1, y2);
	}
}
