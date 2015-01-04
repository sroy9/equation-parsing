package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class JointInfSolver extends AbstractInferenceSolver implements 
		Serializable {

	private static final long serialVersionUID = -4893813648991200683L;
	private JointFeatGen featGen;
	private SLModel relationModel;
	private SLModel equationModel;
	public List<List<SemY>> probTemplates;
	
	public JointInfSolver(JointFeatGen featGen, String relModel, String eqModel, 
			List<List<SemY>> templates) throws Exception {
		this.featGen = featGen;
		this.relationModel = SLModel.loadModel(relModel);
		this.equationModel = SLModel.loadModel(eqModel);
		this.probTemplates = templates;
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
		List<JointY> list = enumerateJointYsRespectingTemplates(prob, probTemplates);
		if(list.size() == 0) list = enumerateJointYs(prob);
//		List<JointY> list = enumerateJointYs(prob);
		for(JointY y : list) {
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
	
	public List<JointY> enumerateJointYsRespectingTemplates(
			JointX prob, List<List<SemY>> templates) throws Exception {
		List<JointY> list1 = enumerateJointYs(prob);
		List<JointY> list2 = new ArrayList<>();
		for(JointY y : list1) {
			// Make a copy and nullify numbers
			JointY copy = new JointY(y);
			for(SemY eq1 : copy.semYs) {
				for(int j=0; j<5; ++j) {
					for(int k=0; k<eq1.terms.get(j).size(); ++k) {
						eq1.terms.get(j).get(k).setSecond(null);
					}
				}
			}
			boolean allow = false;
			for(List<SemY> template : templates) {
				if(isSameTemplate(copy.semYs, template)) {
					allow = true;
					break;
				}
			}
			if(allow) list2.add(y);
		}
		return list2;
	}
	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {
		JointY y1 = (JointY) gold;
		JointY y2 = (JointY) pred;
		return JointY.getLoss(y1, y2);
	}
	
	public static List<List<SemY>> extractTemplates(SLProblem slProb) {
		List<List<SemY>> templates = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			JointY gold = (JointY) struct;
			JointY y = new JointY(gold);
			for(SemY eq1 : y.semYs) {
				for(int j=0; j<5; ++j) {
					for(int k=0; k<eq1.terms.get(j).size(); ++k) {
						eq1.terms.get(j).get(k).setSecond(null);
					}
				}
			}
			boolean alreadyPresent = false;
			for(List<SemY> eq2 : templates) {
				if(isSameTemplate(y.semYs, eq2)) {
					alreadyPresent = true; 
					break;
				}
			}
			if(!alreadyPresent) {
				templates.add(y.semYs);
			}
		}
		System.out.println("Number of templates : "+templates.size());
		return templates;
	}

	public static boolean isSameTemplate(List<SemY> semYs, List<SemY> eq2) {
		if(semYs.size() != eq2.size()) return false;
		if(semYs.size() == 1 && SemY.getLoss(semYs.get(0), eq2.get(0))<0.001) {
			return true;
		}
		if(semYs.size() == 2) {
			if(SemY.getLoss(semYs.get(0), eq2.get(0)) < 0.001 && 
					SemY.getLoss(semYs.get(1), eq2.get(1)) < 0.001) {
				return true;
			}
			if(SemY.getLoss(semYs.get(0), eq2.get(1)) < 0.001 && 
					SemY.getLoss(semYs.get(1), eq2.get(0)) < 0.001) {
				return true;
			}
		}
		return false;
	}
}
