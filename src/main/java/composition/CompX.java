package composition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import relation.RelationX;
import relation.RelationY;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class CompX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<QuantSpan> relationQuantities;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<Constituent> dependencyParse;
	public List<Pair<String, IntPair>> skeleton;
	public List<QuantSpan> quantities;
	public boolean isOneVar;

	public CompX() {
		relationQuantities = new ArrayList<>();
	}
	
	public CompX(SimulProb simulProb, String relation) throws Exception {
		problemIndex = simulProb.index;
		quantities = simulProb.quantities;
		relationQuantities = new ArrayList<>();
		isOneVar = simulProb.isOneVar;
		for(int i=0; i<simulProb.relations.size(); ++i) {
			String str = simulProb.relations.get(i);
			if(str.equals(relation) || str.equals("BOTH")) {
				relationQuantities.add(quantities.get(i));
			}
		}
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		lemmas = simulProb.lemmas;
		parse = simulProb.parse;
		skeleton = simulProb.skeleton;
	}
	
	public static List<CompX> extractEquationProbFromRelations(
			RelationX x, RelationY y) {
		List<CompX> list = new ArrayList<>();
		list.add(new CompX());
		list.add(new CompX());
		for(int i=0; i<y.relations.size(); ++i) {
			String relation = y.relations.get(i);
			if(relation.equals("R1") || relation.equals("BOTH")) {
				list.get(0).relationQuantities.add(x.quantities.get(i));
			}
			if(relation.equals("R2") || relation.equals("BOTH")) {
				list.get(1).relationQuantities.add(x.quantities.get(i));
			}
		}
		if(list.get(1).relationQuantities.size() == 0) {
			list.remove(1);
		}
		if(list.get(0).relationQuantities.size() == 0) {
			list.remove(0);
		}
		for(CompX sx : list) {
			sx.isOneVar = y.isOneVar;
			sx.lemmas = x.lemmas;
			sx.parse = x.parse;
			sx.posTags = x.posTags;
			sx.problemIndex = x.problemIndex;
			sx.quantities = x.quantities;
			sx.skeleton = x.skeleton;
			sx.ta = x.ta;
		}
		return list;
	}
}
