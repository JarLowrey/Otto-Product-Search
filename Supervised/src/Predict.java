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
	}
	
	/**
	 * Iterate through the rows of parsed data and find the maximum log likelihood estimation of the data belonging to each classification.
	 * Print the best classification to the screen.
	 */
	private void classifyParsedData(){
		for(DataRow currData : rowsOfData){			
			double[] probs = new double[l.getNumClassifications()];
			
			//find the log probability of the currentData belonging to each data feature
			for(int i=0;i<probs.length;i++){
				probs[i] = l.logProbability(i, currData.features );
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
			
			System.out.println("ID "+ currData.id +" :: Class :: "+l.classifierName(maxIndex) );
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
