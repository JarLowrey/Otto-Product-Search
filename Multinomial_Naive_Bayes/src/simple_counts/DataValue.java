package simple_counts;
import java.util.ArrayList;

/**
 * Keep track of the number of ids (ie a row of data) that have a given value (0,1,2...)
 * @author James
 *
 */
public class DataValue {

	public int value;
	ArrayList<Integer> ids;//used to keep track of count. This could be replaced with a simple 'int count=0' and would work the same
	
	public DataValue(int dataValue){
		ids = new ArrayList<Integer>();
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
