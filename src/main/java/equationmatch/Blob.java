package equationmatch;

import java.util.List;

import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class Blob implements IInstance {

	public SimulProb simulProb;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<QuantSpan> quantities;

	public Blob(SimulProb simulProb) throws Exception {
		this.simulProb = simulProb;
		this.quantities = simulProb.quantities;
		ta = new TextAnnotation("", "", simulProb.question);
		posTags = Tools.curator.getTextAnnotationWithSingleView(
				simulProb.question, ViewNames.POS, false)
				.getView(ViewNames.POS).getConstituents();
		lemmas = Tools.curator.getTextAnnotationWithSingleView(
				simulProb.question, ViewNames.LEMMA, false)
				.getView(ViewNames.LEMMA).getConstituents();
	}
	
	public Lattice getGold() {
		return new Lattice(simulProb.equations);
	}
}
