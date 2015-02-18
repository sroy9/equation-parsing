package structure;

public class Trigger {
	
	public int index;
	public String label;
	public Double num;
	
	public Trigger(int index, String label, Double num) {
		this.index = index;
		this.label = label;
		this.num = num;
	}
	
	@Override
	public String toString() {
		return "("+index+", "+label+", "+num+")";
	}
	

}
