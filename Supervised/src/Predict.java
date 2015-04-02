import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Given the name of a formatted data file for predicting/testing, classify each product/row into learned classifications
 * @author James
 *
 */
public class Predict {

	ArrayList<DataRow> rowsOfData = new ArrayList<DataRow>();
	Learn l;
	
	public Predict(String trainDataFileName,String testDataFileName) throws IOException{
		l = new Learn(trainDataFileName);
		
		parseFile(testDataFileName);
		
		classifyParsedData();
		
		printAccuracy();
	}
	
	/**
	 * Iterate through the rows of parsed data and find the maximum log likelihood estimation of the data belonging to each classification.
	 * Print the best classification to the screen.
	 */
	private void classifyParsedData(){
		System.out.println("Classifying data");
		
		for(DataRow currData : rowsOfData){			
			double[] probs = new double[l.getNumClassifications()];
			
			//find the log probability of the currentData belonging to each data feature
			for(int i=0;i<probs.length;i++){
				probs[i] = l.logProbability(i, currData.getFeatures() );
//				System.out.println("Class "+(1+i)+" Probability " +probs[i]);
			}

			//find the max in the probs array. The corresponding index indicates the classification of the currData
			double max= - Double.MAX_VALUE;
			int maxIndex =0;
			for(int i=0;i<probs.length;i++){
				if(probs[i]>max){
					max = probs[i];
					maxIndex=i;
				}
			}
			
			currData.setClassification(l.classifierName(maxIndex));
//			System.out.println("ID "+ currData.getId() +" :: Class :: "+l.classifierName(maxIndex) );
		}
	}
	
	/**
	 * Assuming both files for Learn and predict are the train files (where the classifications are known), iterate through this program's assignment of classifications to see 
	 * the accuracy.
	 */
	private void printAccuracy(){
		System.out.println("Finding classification Accuracy");
		
		final int[] endPointOfClassifications = {1930,18052,26056,28747,31486,45621,48460,52924};
		int currClassificationIndex=0;
		int numWrong=0;
		
		for(int i=0;i<rowsOfData.size();i++){
			//check if current row was properly assigned
			if( ! rowsOfData.get(i).getClassificationName().equals(l.classifierName(currClassificationIndex)) ){
				numWrong++;
			}
			
			//check if we have moved on to next classifier
			for(int j=0;j<endPointOfClassifications.length;j++){
				if(endPointOfClassifications[j] == i){
					currClassificationIndex++;
					break;
				}
			}
		}
		
		System.out.println("Accuracy :: "+(double)numWrong/rowsOfData.size());
	}
	
	/**
	 * Iterate through the file and add each
	 * @param testDataFileName
	 * @throws IOException
	 */
	private void parseFile(String testDataFileName ) throws IOException{
		Scanner in = new Scanner(Paths.get(testDataFileName));
		in.nextLine();//first line is composed of column labels (trash it)
		
		String[] strFeatures = in.nextLine().split(",");
		
		while(true){
			int[] intFeatures = new int[strFeatures.length-1];
			
			for(int i=0;i<intFeatures.length;i++){
				intFeatures[i] = Integer.parseInt(strFeatures[i+1]);
			}

			rowsOfData.add( new DataRow(Integer.parseInt(strFeatures[0]),intFeatures) );
			
			try{//Scanner throws error when it cannot find nextLine(). Thus EOF is reached, break out of while loop
				strFeatures = in.nextLine().split(",");
			}catch(NoSuchElementException e){
				break;
			}
		}
		in.close();
	}
}
