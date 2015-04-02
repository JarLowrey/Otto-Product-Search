import java.util.ArrayList;

/**
 * Keep track of the number of ids (ie a row of data) that have a given value (0,1,2...)
 * @author James
 *
 */
public class DataValue {

	public int value;
	ArrayList<Integer> ids;
	
	public DataValue(int dataValue){
		ids = new ArrayList<Integer>();
//		feature=featureNumber;
		value = dataValue;
	}
	
	public void addId(int id){
		ids.add(id);
	}
	/**
	 * 
	 * @return
	 * 		Total number of data rows that have this given value.
	 */
	public int getCount(){
		return ids.size();
	}
}
