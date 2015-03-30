import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class Learn {

	ArrayList<ProductClass> classifications = new ArrayList<ProductClass>();
	private int numFeatures;
	
	public Learn(String filename) throws IOException{
		
		parseFile(filename);
	}
	
	public double logProbability(int whichClass,int[] featureData){
		ProductClass p = classifications.get(whichClass);
		double prob=0;
		double max = - Double.MAX_VALUE;
		for(int i=0;i<numFeatures;i++){
			if(featureData[i] != 0){
				double probOfFeature = p.getCountOfDataWithValue(i, featureData[i]) / (double) p.totalCount(i) ;
				if( probOfFeature > max){
					max = probOfFeature;
				}
			}
		}

		double logSum = 0;
		for(int i=0;i<numFeatures;i++){
			if(featureData[i] != 0){
//				logSum += Math.exp( p.getCountOfDataWithValue(i, featureData[i]) - max);
//				prob+=p.getCountOfDataWithValue(i, featureData[i]);
				prob += Math.log( p.getCountOfDataWithValue(i, featureData[i]) ) - Math.log( (double) p.totalCount(i) );
//				prob *= (double) p.getCountOfDataWithValue(i, featureData[i]) / (double) p.totalCount(i) ;
//				System.out.println(featureData[i]+ " "+ p.getCountOfDataWithValue(i, featureData[i])+" "+p.totalCount(i));
			}
		}
		
		return prob;//max + Math.log(logSum);
	}
	
	/**
	 * Assign the data in the given file to each product classification
	 * @throws IOException 
	 */
	private void parseFile(String filename) throws IOException{
		Scanner in = new Scanner(Paths.get(filename));
		in.nextLine();//first line is labels
		
		String[] features = in.nextLine().split(",");
		numFeatures = features.length-2;
		
		while(true){
			ProductClass c = getClassification(features[features.length-1]);
			int id = Integer.parseInt(features[0]);
			
			for(int i=1;i<features.length-1;i++){
				int dataVal = Integer.parseInt(features[i]);
				
				c.addData(dataVal,i-1,id);
			}
			
			try{
				features = in.nextLine().split(",");
			}catch(NoSuchElementException e){//EOF
				break;
			}
		}
		
		in.close();
		
	}
	
	/**
	 * Find product class in classifications ArrayList. If it does not exist, add it and return.
	 * @param classificationName
	 * 		name of the desired classification
	 * @return
	 * 		ProductClass in classifications ArrayList.
	 */
	private ProductClass getClassification(String classificationName){
		int index=-1;
		for(int i=0;i<classifications.size();i++){
			if(classifications.get(i).getName().equals(classificationName)){
				index=i;
				break;
			}
		}
		
		ProductClass c;
		if( index < 0 ){
			c = new ProductClass(classificationName,numFeatures);
			classifications.add(c);
		}else{
			c = classifications.get(index);
		}
		return c;
	}
	
	public int getNumClasses(){
		return classifications.size();
	}
}
