package relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class RelationX implements IInstance {

	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<QuantSpan> quantities;
	public List<String> relations;
	public int index;

	public RelationX(SimulProb simulProb, int index) throws Exception {
		quantities = simulProb.quantities;
		this.index = index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		lemmas = simulProb.lemmas;
		relations = new ArrayList<>();
		for(int i=0; i<index; i++) {
			relations.add(simulProb.relations.get(i));
		}
		boolean needsSwap = false;
		for(String relation : relations) {
			if(relation.equals("R2")) needsSwap = true;
			if(relation.equals("R1")) break; 
		}
		// This ensures R1 always appears before R2
		if(needsSwap) {
			for(int i=0; i<relations.size(); ++i) {
				if(relations.get(i).equals("R1")) {
					relations.set(i, "R2");
				} else if(relations.get(i).equals("R2")) {
					relations.set(i, "R1");
				}
			}
		}
	}
}
