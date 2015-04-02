import java.util.ArrayList;

/**
 * 
 * @author James
 *
 */
public class ProductClass {
	
	private ArrayList<DataValue>[] data;
	private String name;
	
	public ProductClass(String classificationName,int numFeatures){
		name = classificationName; 
		data = (ArrayList<DataValue>[])new ArrayList[numFeatures];
		for (int i = 0; i < numFeatures; i++) {
		    data[i] = new ArrayList<DataValue>();
		}
	}
	public String getName(){
		return name;
	}

	/**
	 * Find DataValue in data ArrayList. If it does not exist, add it and return.
	 * @param dataValue
	 * 		value of the desired data
	 * @return
	 * 		DataValue in classifications ArrayList.
	 */
	public void addData(int dataValue,int featureNumber,int id){
		ArrayList<DataValue> featureData = data[featureNumber];
		int index=-1;
		
		for(int i=0;i<featureData.size();i++){
			if( featureData.get(i).value == dataValue ){
				index=i;
				break;
			}
		}
		
		DataValue d;
		if( index < 0 ){
			d=new DataValue(dataValue);
			featureData.add(d);
		}else{
			d = featureData.get(index);
		}
		d.addId(id);
	}
	
	public int getCountOfDataWithValue(int feature,int dataValue){
		ArrayList<DataValue> featureData = data[feature];
		for(int i=0;i<featureData.size();i++){
			if(featureData.get(i).value == dataValue){
				return featureData.get(i).getCount();
			}
		}
		
		return 0;
	}
	
	/**
	 * 
	 * @return
	 * 		total number of all unique data values in this ProductClass's feature
	 */
	public int totalCount(int featureNumber){
		int count = 0;
		for( DataValue d : data[featureNumber] ){
			count += d.getCount();
		}
		return count;
	}
	
}
