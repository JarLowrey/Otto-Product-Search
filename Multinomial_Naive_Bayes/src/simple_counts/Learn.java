package simple_counts;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Given the name of a formatted data file for training, parse the file. Organize data into different product classifications where each classification has a corresponding set of data
 * that tracks the number of times a given data value occurs 
 * @author James
 *
 */
public class Learn {

	public static final double SMOOTHING_PRIOR = 1;
	
	private ArrayList<ProductClass> classifications = new ArrayList<ProductClass>();
	private int numFeatures;
	
	public Learn(String filename) throws IOException{
		parseFile(filename);
	}
	
	/**
	 * Given a set of data features and a class, find the probability of the set of data belonging to that class. This is actual application of the Multinomial Naive Bayes.
	 * @param whichClass
	 * @param featureData
	 * 		array of data features belonging to a product.
	 * @return
	 */
	public double logProbability(int whichClass,int[] featureData){
		ProductClass p = classifications.get(whichClass);
		
		//find log probability of data feature using multinomial naive bayes 
		double logProb = 0;
		for(int i=0;i<numFeatures;i++){
			final double numerator = p.getCountOfDataWithValue(i, featureData[i]) + SMOOTHING_PRIOR;
			final double denominator = p.totalCount(i) + SMOOTHING_PRIOR * numFeatures;
			final double probOfFeature = numerator / denominator ;
			logProb += Math.log(probOfFeature);
		}
			
		return logProb;
	}
	
	/**
	 * Assign the data in the given file to each product classification
	 * @throws IOException 
	 */
	private void parseFile(String filename) throws IOException{
		System.out.println("Learning Data");
		
		Scanner in = new Scanner(Paths.get(filename));
		in.nextLine();//first line is labels (trash it)
		
		String[] features = in.nextLine().split(",");
		numFeatures = features.length-2;//first column is id and last column is classification number. Thus -2 columns
		
		while(true){
			ProductClass c = getClassification(features[features.length-1]);
			int id = Integer.parseInt(features[0]);
			
			for(int i=0;i<numFeatures;i++){
				final int dataVal = Integer.parseInt(features[i+1]);//must shift 1 as features[0] is the id, dataVal must refer to a feature's value
				
				c.addData(dataVal,i,id);
			}
			
			try{//Scanner throws error when it cannot find nextLine(). Thus EOF is reached, break out of while loop
				features = in.nextLine().split(",");
			}catch(NoSuchElementException e){
				break;
			}
		}
		
		in.close();
		
	}
	
	/**
	 * Find product class in classifications ArrayList. If it does not exist, add it and return it.
	 * @param classificationName
	 * 		name of the desired classification
	 * @return
	 * 		ProductClass in classifications ArrayList.
	 */
	private ProductClass getClassification(String classificationName){
		//search for pre-existing classification name 
		int index=-1;
		for(int i=0;i<classifications.size();i++){
			if(classifications.get(i).getName().equals(classificationName)){
				index=i;
				break;
			}
		}
		
		ProductClass c;
		if( index < 0 ){//classification does not exist, create and add a new one
			c = new ProductClass(classificationName,numFeatures);
			classifications.add(c);
		}else{//classification already exists
			c = classifications.get(index);
		}
		return c;
	}
	
	public int getNumClassifications(){
		return classifications.size();
	}
	
	public String classifierName(int whichClasifier){
		return classifications.get(whichClasifier).getName();
	}
}
