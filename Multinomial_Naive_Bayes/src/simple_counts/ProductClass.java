package simple_counts;
import java.util.ArrayList;

/**
 * Classifications of product types
 * @author James
 *
 */
public class ProductClass {
	
	private ArrayList<DataValue>[] dataValuesForEachFeature;//array of size #features, where each entry in the array is an ArrayList containing DataValue class entries
	private String name;
	
	public ProductClass(String classificationName,int numFeatures){
		name = classificationName; 
		dataValuesForEachFeature = (ArrayList<DataValue>[])new ArrayList[numFeatures];
		for (int i = 0; i < numFeatures; i++) {
		    dataValuesForEachFeature[i] = new ArrayList<DataValue>();
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
		ArrayList<DataValue> featureData = dataValuesForEachFeature[featureNumber];
		
		//search for the DataValue with a value equal to the passed parameter
		int index=-1;		
		for(int i=0;i<featureData.size();i++){
			if( featureData.get(i).value == dataValue ){
				index=i;
				break;
			}
		}
		
		DataValue d;
		if( index < 0 ){//the DataValue was not found, create a new one
			d=new DataValue(dataValue);
			featureData.add(d);
		}else{//DataValue found
			d = featureData.get(index);
		}
		//add id to the DataValue
		d.addId(id);
	}
	
	/**
	 * in the specified feature, find how many times a given dataValue occurs
	 * @param feature
	 * @param dataValue
	 * @return
	 */
	public int getCountOfDataWithValue(int feature,int dataValue){
		ArrayList<DataValue> featureData = dataValuesForEachFeature[feature];
		
		//search through the ArrayList at given feature until desired value is found
		for(int i=0;i<featureData.size();i++){
			if(featureData.get(i).value == dataValue){
				return featureData.get(i).getCount();
			}
		}
		//not found, return 0
		return 0;
	}
	
	/**
	 * 
	 * @return
	 * 		total number of all unique data values in this ProductClass's feature
	 */
	public int totalCount(int featureNumber){
		int count = 0;
		for( DataValue d : dataValuesForEachFeature[featureNumber] ){
			count += d.getCount();
		}
		return count;
	}
	
}
