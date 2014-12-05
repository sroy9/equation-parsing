package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import structure.Operation;
import structure.Span;
import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;

public class Tools {
	
	public static NewCachingCurator curator;
	public static Quantifier quantifier;
	
	static {
		try {
			curator = new NewCachingCurator(
					"trollope.cs.illinois.edu", 
					9010, 
					Params.cacheLoc, 
					null);
			quantifier = new Quantifier();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static double sigmoid(double x) {
		return 1.0/(1+Math.pow(Math.E, -x))*2.0-1.0;
	}
	
	public static boolean doesIntersect(IntPair ip1, IntPair ip2) {
		if(ip1.getFirst() <= ip2.getFirst() && ip2.getFirst() < ip1.getSecond()) {
			return true;
		}
		if(ip2.getFirst() <= ip1.getFirst() && ip1.getFirst() < ip2.getSecond()) {
			return true;
		}
		return false;
	}
	
	// Returns maximal NPs with at most one quantity per NP, and no conjuction
	public static List<Span> getCandidateNPs(String text, List<QuantSpan> quantSpans) 
			throws Exception {
		List<Span> npSpans = new ArrayList<>();
		TextAnnotation ta = Tools.curator.getTextAnnotationWithSingleView(
				text, ViewNames.PARSE_STANFORD, false);
		List<Constituent> treeNodes = ta.getView(ViewNames.PARSE_STANFORD)
				.getConstituents();
		System.out.println("Parse Tree");
		for(Constituent cons : treeNodes) {
			System.out.println(cons.getLabel()+" : "+cons.getSurfaceString());
		}
		for(Constituent cons : treeNodes) {
			if(cons.getLabel().equals("NP") && 
					!cons.getSurfaceString().contains(" and ")) {
				int count = 0;
				for(QuantSpan qs : quantSpans) {
					if(cons.getStartCharOffset() <= qs.start && 
							qs.end <= cons.getEndCharOffset()) {
						count++;
					}
				}
				boolean allow = false;
				if(count <= 1) {
					allow = true;
					// Check if a parent can replace the child
					for(Constituent cons1 : treeNodes) {
						if(cons1 == cons) continue;
						if(!cons1.getLabel().equals("NP")) continue;
						if(cons1.getSurfaceString().contains(" and ")) continue;
						if(cons1.getStartSpan() <= cons.getStartSpan() && 
								cons1.getEndSpan() >= cons.getEndSpan()) {
							int count1 = 0;
							for(QuantSpan qs : quantSpans) {
								if(cons1.getStartCharOffset() <= qs.start && 
										qs.end <= cons1.getEndCharOffset()) {
									count1++;
								}
							}
							if(count1 <= 1) {
								allow = false;
							}
						}
					}
				}
				if(allow) {
					npSpans.add(new Span(null, new IntPair(
							cons.getStartCharOffset(), cons.getEndCharOffset())));
				}
			}
		}
		// Make the spans ordered
		Collections.sort(npSpans, new Comparator<Span>()  {  
		  @Override  
		  public int compare(Span o1, Span o2)  
		  {  
			  if(o1.ip.getFirst() < o2.ip.getSecond()) {
				  return -1;
			  } else {
				  return 1;
			  }
			    
		  }
		});
		return npSpans;		
	}
	
	public static Operation getOperationFromString(String op) {
		if(op.equals("ADD") || op.equals("+")) return Operation.ADD;
		if(op.equals("SUB") || op.equals("-")) return Operation.SUB;
		if(op.equals("MUL") || op.equals("*")) return Operation.MUL;
		if(op.equals("DIV") || op.equals("/")) return Operation.DIV;
		if(op.equals("EQ") || op.equals("=")) return Operation.EQ;
		return null;
	}
}
