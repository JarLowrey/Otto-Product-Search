import java.util.ArrayList;


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
	public int numOfDataWithThisValue(){
		return ids.size();
	}
}
