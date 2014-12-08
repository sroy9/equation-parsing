package mentiondetect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class VarSet implements IInstance {
	
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<QuantSpan> quantities;
	public SimulProb simulProb;
	
	public VarSet(SimulProb simulProb) throws Exception {
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
	
	public LabelSet getGold () {
		LabelSet gold = new LabelSet();
		for(QuantSpan qs : quantities) {
			boolean found = false;
			for(String entity : simulProb.clusterMap.keySet()) {
				for(QuantSpan qs1 : simulProb.clusterMap.get(entity)) {
					if(qs.equals(qs1)) {
						gold.addLabel(entity);
						found = true;
						break;
					}
				}
				if(found) break;
			}
		}
		assert gold.labels.size() == quantities.size();
		return gold;
	}
	
}
