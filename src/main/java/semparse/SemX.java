package semparse;

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

public class SemX implements IInstance {

	public TextAnnotation ta;
	public List<QuantSpan> relationQuantities;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> dependencyParse;
	public List<QuantSpan> quantities;

	public SemX(SimulProb simulProb, String relation) throws Exception {
		quantities = simulProb.quantities;
		for(int i=0; i<simulProb.relations.size(); ++i) {
			String str = simulProb.relations.get(i);
			if(str.equals(relation) || str.equals("BOTH")) {
				relationQuantities.add(quantities.get(i));
			}
		}
		ta = new TextAnnotation("", "", simulProb.question);
		posTags = Tools.curator.getTextAnnotationWithSingleView(
				simulProb.question, ViewNames.POS, false)
				.getView(ViewNames.POS).getConstituents();
		lemmas = Tools.curator.getTextAnnotationWithSingleView(
				simulProb.question, ViewNames.LEMMA, false)
				.getView(ViewNames.LEMMA).getConstituents();
//		dependencyParse = Tools.curator.getTextAnnotationWithSingleView(
//				simulProb.question, ViewNames.DEPENDENCY, false)
//				.getView(ViewNames.DEPENDENCY).getConstituents();
	}
}
