import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class Predict {

	ArrayList<int[]> rowsOfData = new ArrayList<int[]>();
	
	public Predict(String trainDataFileName,String testDataFileName) throws IOException{
		Learn l = new Learn(trainDataFileName);
		
		parseFile(testDataFileName);
		
		for(int k=0;k<5;k++){
//		for(int k=0;k<rowsOfData.size();k++){
			double[] probs = new double[l.getNumClasses()];
			
			for(int i=0;i<probs.length;i++){
				probs[i] = l.logProbability(i, rowsOfData.get(k) );
				System.out.println("Class "+(1+i)+" Probability " +probs[i]);
			}

			double max= - Double.MAX_VALUE;
			int maxIndex =0;
			for(int i=0;i<probs.length;i++){
				if(probs[i]>max){
					max = probs[i];
					maxIndex=i;
				}
			}
//			double min= Double.MAX_VALUE;
//			int minIndex =0;
//			for(int i=0;i<probs.length;i++){
//				if(probs[i] < min){
//					min = probs[i];
//					minIndex=i;
//				}
//			}
			System.out.println("ID "+(k+1) +" :: Class "+(maxIndex+1));
		}
	}

	private void parseFile(String testDataFileName ) throws IOException{
		Scanner in = new Scanner(Paths.get(testDataFileName));
		in.nextLine();//first line is labels
		
		String[] features = in.nextLine().split(",");
		
		while(true){
			int[] idAndFeatures = new int[features.length];
			
			for(int i=1;i<features.length;i++){
				idAndFeatures[i] = Integer.parseInt(features[i]);
			}

			rowsOfData.add(idAndFeatures);
			
			try{
				features = in.nextLine().split(",");
			}catch(NoSuchElementException e){//EOF
				break;
			}
		}
		in.close();
	}
}
