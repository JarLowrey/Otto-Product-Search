
/**
 * Data with an Id and array of Integer features
 * @author James
 *
 */
public class DataRow {

	int id;
	int[] features;
	public DataRow(int rowId,int[] rowFeatures){
		id=rowId;
		features = rowFeatures;
	}
}
