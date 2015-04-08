package simple_counts;

/**
 * Data with an Id, array of Integer features, and a classification name
 * @author James
 *
 */
public class DataRow {

	private int id;
	private int[] features;
	private String classificationName;
	
	public DataRow(int rowId,int[] rowFeatures){
		id=rowId;
		features = rowFeatures;
	}
	
	public int getId(){
		return id;
	}
	public int[] getFeatures(){
		return features;
	}
	public String getClassificationName(){
		return classificationName;
	}
	public void setClassification(String name){
		classificationName = name;
	}
}
