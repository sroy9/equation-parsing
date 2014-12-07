package experiment;

import java.util.Arrays;
import java.util.List;

import parser.DocReader;
import structure.Equation;
import structure.SimulProb;
import structure.Span;
import utils.Params;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
/**
 * 
 * @author subhroroy
 *
 */
public class Experiment {
	
	public static void main(String[] args) throws Exception {
		InteractiveShell<Experiment> tester = new InteractiveShell<Experiment>(
				Experiment.class);
		if (args.length == 0)
			tester.showDocumentation();
		else 
			tester.runCommand(args);
	}

	@CommandDescription(description="Check data")
	public static void testDataQuality() throws Exception {
		DocReader dr = new DocReader();
		List<SimulProb> simulProbList = dr.readSimulProbFromBratDir(
				Params.annotationDir);
		for(SimulProb simulProb : simulProbList) {
			System.out.println(simulProb.index+" : "+simulProb.question);
			System.out.println("Quantities :");
			for(QuantSpan qs : simulProb.quantities) {
				System.out.println(simulProb.question.substring(
						qs.start, qs.end)+" : "+qs);
			}
			System.out.println("Spans :");
			for(Span span : simulProb.spans) {
				System.out.println(span.label+" : "+simulProb.question.substring(
						span.ip.getFirst(), span.ip.getSecond())+ " : "+span.ip);
			}
			System.out.println("Cluster Map :");
			for(String entity : simulProb.clusterMap.keySet()) {
				System.out.println(entity + " : " + Arrays.asList(
						simulProb.clusterMap.get(entity)));
			}
			System.out.println("Equations :");
			for(Equation eq : simulProb.equations) {
				System.out.println(eq.toString());
			}
		}
	}
	
}