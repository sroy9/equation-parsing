package structure;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;

 
/**
 * Dataset generated with serialID and
 * public String question;
	public String answer;
	public String operation;
	public List<MySpan> quantities;
	public IntPair relevantQuantityIndex;
	public Quantity ans;
	
	If description is changed, dataset needs to be generated again and saved.
	
 * @author subhroroy
 *
 */

public class SimpleProb implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public String question;
	public String answer;
	public String operation;
	public List<QuantSpan> quantities;
	public IntPair relevantQuantityIndex;
	public Quantity ans;
	public Quantifier quantifier;
	
	public SimpleProb(String q,String a){
		question = q;
		answer = a;
		quantifier = new Quantifier();
	}
	
	public SimpleProb(String q, String a, String op, double val, String unit){
		question = q;
		answer = a;
		operation = op;
		ans = new Quantity("=", val, unit);
	}
	
	public void extractQuantities() throws IOException{
		List<QuantSpan> spanArray = quantifier.getSpans(question, true);
		quantities = new ArrayList<QuantSpan>();
		for(QuantSpan span:spanArray){
			if(span.object instanceof Quantity){
				quantities.add(span);
			}
		}
	}
	
	
	// Returns indices in sorted order
	public IntPair getRelevantQuantityIndices(){
		IntPair relevantQuantityIndex=new IntPair(0,1);
		
		for(int i=0;i<quantities.size();i++){
			for(int j=i+1;j<quantities.size();j++){
				double val1 = ((Quantity)quantities.get(i).object).value;
				double val2 = ((Quantity)quantities.get(j).object).value;
				
				if(equals(val1+val2,ans.value) && operation.equals("add")){
					relevantQuantityIndex = new IntPair(i, j);
				}
				if((equals(val1-val2,ans.value) || equals(val2-val1,ans.value)) && operation.equals("sub")){
					relevantQuantityIndex = new IntPair(i, j);
				}
//				if(val2-val1==ans.value && operation.equals("sub")){
//					relevantQuantityIndex = new IntPair(j, i);
//				}
				if(equals(val1*val2,ans.value) && operation.equals("mul")){
					relevantQuantityIndex = new IntPair(i, j);
				}
				if((equals(val1/val2,ans.value)||equals(val2/val1,ans.value)) 
						&& operation.equals("div")){
					relevantQuantityIndex = new IntPair(i, j);
				}
//				if(val2/val1==ans.value && operation.equals("div")){
//					relevantQuantityIndex = new IntPair(j, i);
//				}
				
			}
		}
		
		return relevantQuantityIndex;
	}
	
	// Returns indices in the order of operation
	public IntPair getRelevantQuantityIndicesOrdered(){
		IntPair relevantQuantityIndex=null;
		
		for(int i=0;i<quantities.size();i++){
			for(int j=i+1;j<quantities.size();j++){
				double val1 = ((Quantity)quantities.get(i).object).value;
				double val2 = ((Quantity)quantities.get(j).object).value;
				
				if(equals(val1+val2,ans.value) && operation.equals("add")){
					relevantQuantityIndex = new IntPair(i, j);
				}
				if(equals(val1-val2,ans.value) && operation.equals("sub")){
					relevantQuantityIndex = new IntPair(i, j);
				}
				if(equals(val2-val1,ans.value) && operation.equals("sub")){
					relevantQuantityIndex = new IntPair(j, i);
				}
				if(equals(val1*val2,ans.value) && operation.equals("mul")){
					relevantQuantityIndex = new IntPair(i, j);
				}
				if(equals(val1/val2,ans.value) && operation.equals("div")){
					relevantQuantityIndex = new IntPair(i, j);
				}
				if(equals(val2/val1,ans.value) && operation.equals("div")){
					relevantQuantityIndex = new IntPair(j, i);
				}
				
			}
		}
		
		return relevantQuantityIndex;
	}
	
	public double eval(IntPair pair,int op){
		double val1 = ((Quantity)quantities.get(pair.getFirst()).object).value;
		double val2 = ((Quantity)quantities.get(pair.getSecond()).object).value;
		if(op==0){
			return val1+val2;
		}
		if(op==1){
			return val1-val2;
		}
		if(op==2){
			return val1*val2;
		}
		if(op==3){
			return val1/val2;
		}
		return  100.;
		
	}
	
	public boolean isValid(){
		
		if(operation.equals("add")){
			for(int i=0;i<quantities.size();i++){
				for(int j=i+1;j<quantities.size();j++){
					double val1 = ((Quantity)quantities.get(i).object).value;
					double val2 = ((Quantity)quantities.get(j).object).value;
					if(equals(val1+val2,ans.value)){
						return true;
					}
					
				}
			}
			return false;
		}
		if(operation.equals("sub")){
			for(int i=0;i<quantities.size();i++){
				for(int j=i+1;j<quantities.size();j++){
					double val1 = ((Quantity)quantities.get(i).object).value;
					double val2 = ((Quantity)quantities.get(j).object).value;
					System.out.println(ans.value+" "+(val1-val2)+" "+(val2-val1));
					if(equals((val1-val2),ans.value) || equals((val2-val1),ans.value)){
						return true;
					}
					
				}
			}
			return false;
		}
		if(operation.equals("mul")){
			for(int i=0;i<quantities.size();i++){
				for(int j=i+1;j<quantities.size();j++){
					double val1 = ((Quantity)quantities.get(i).object).value;
					double val2 = ((Quantity)quantities.get(j).object).value;
					if(equals(val1*val2,ans.value)){
						return true;
					}
					
				}
			}
			return false;
		}
		if(operation.equals("div")){
			for(int i=0;i<quantities.size();i++){
				for(int j=i+1;j<quantities.size();j++){
					double val1 = ((Quantity)quantities.get(i).object).value;
					double val2 = ((Quantity)quantities.get(j).object).value;
					if(equals(val1/val2,ans.value) || equals(val2/val1,ans.value)){
						return true;
					}
					
				}
			}
			return false;
		}
		return false;
	}
	
	public static boolean equals(double a, double b){
		double e=0.0001;
		if(a>=b-e && a<=b+e){
			return true;
		}
		return false;
	}
	
	public void print(){
		System.out.println("Q: "+question);
		System.out.println("A: "+answer);
		System.out.println("Gold operation: "+operation);
		System.out.println("Gold Answer: "+ans.value);
	}
	
}
