package simple_counts;
import java.io.IOException;


public class Test {

	static Predict p;
	public static void main(String[] args) throws IOException{
		p = new Predict(args[0],args[1]);
		
		printAccuracy();
	}
	

	/**
	 * Assuming both files for Learn and predict are the train files (where the classifications are known), iterate through this program's assignment of classifications to see 
	 * the accuracy.
	 */
	private static void printAccuracy(){		
		final int[] endPointOfClassifications = {1930,18052,26056,28747,31486,45621,48460,52924};//hard coded values indicating transition from one class to another in the train data rows
		int currClassificationIndex=0;
		int numRight=0;
		
		for(int i=0;i<p.rowsOfData.size();i++){
			//check if current row was properly assigned
			if( p.rowsOfData.get(i).getClassificationName().equals(p.myLearnedInformation.classifierName(currClassificationIndex)) ){
				numRight++;
			}
			
			//check if we have moved on to next classifier
			for(int j=0;j<endPointOfClassifications.length;j++){
				if(endPointOfClassifications[j] == i){
					currClassificationIndex++;
					break;
				}
			}
		}
		
		System.out.println("Accuracy :: "+ (double)numRight/p.rowsOfData.size() );
	}
}
