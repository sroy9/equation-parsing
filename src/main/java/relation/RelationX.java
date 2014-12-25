package relation;

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
	public List<Constituent> dependencyParse;
	public List<QuantSpan> quantities;
	public int index;

	public RelationX(SimulProb simulProb, int index) throws Exception {
		quantities = simulProb.quantities;
		this.index = index;
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
