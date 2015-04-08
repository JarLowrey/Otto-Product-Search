package simple_counts;
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
	Learn myLearnedInformation;
	
	public Predict(String trainDataFileName,String testDataFileName) throws IOException{
		myLearnedInformation = new Learn(trainDataFileName);

		System.out.println("Classifying data");
		
		parseFile(testDataFileName);
		
		classifyParsedData();
	}
	
	/**
	 * Iterate through the rows of parsed data and find the maximum log likelihood estimation of the data belonging to each classification.
	 * Print the best classification to the screen.
	 */
	private void classifyParsedData(){
		for(DataRow currData : rowsOfData){			
			double[] probOfDataRowBelongingToEachClass = new double[myLearnedInformation.getNumClassifications()];
			
			//find the log probability of the currentData belonging to each data feature
			for(int i=0;i<probOfDataRowBelongingToEachClass.length;i++){
				probOfDataRowBelongingToEachClass[i] = myLearnedInformation.logProbability(i, currData.getFeatures() );
//				System.out.println("Class "+(1+i)+" Probability " +probs[i]);
			}

			//find the max log prob. The corresponding index indicates the max and thus the classification of the currData
			double max= - Double.MAX_VALUE;
			int maxIndex =0;
			for(int i=0;i<probOfDataRowBelongingToEachClass.length;i++){
				if(probOfDataRowBelongingToEachClass[i]>max){
					max = probOfDataRowBelongingToEachClass[i];
					maxIndex=i;
				}
			}
			
			currData.setClassification(myLearnedInformation.classifierName(maxIndex));
//			System.out.println("ID "+ currData.getId() +" :: Class :: "+l.classifierName(maxIndex) );
		}
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
			int[] intFeatures = new int[strFeatures.length-1];//Only include features (not id)
			final int id = Integer.parseInt(strFeatures[0]);
			
			for(int i=0;i<intFeatures.length;i++){
				intFeatures[i] = Integer.parseInt(strFeatures[i+1]);//i+1 since first column is ID
			}

			rowsOfData.add( new DataRow(id,intFeatures) );
			
			try{//Scanner throws error when it cannot find nextLine(). Thus EOF is reached, break out of while loop
				strFeatures = in.nextLine().split(",");
			}catch(NoSuchElementException e){
				break;
			}
		}
		in.close();
	}
}
