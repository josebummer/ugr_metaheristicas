/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2mh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author jose
 */
public class knn {
    String trainingFile;
    String testFile;
    int K;
    int metricType;
    ArrayList<TrainRecord> trainingSet;
    ArrayList<TestRecord> testingSet;
    double[] pesos;
    int ptest;
    boolean leave;
    
    public knn(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, ArrayList<TestRecord> testingSet, double[] pesos,int ptest, boolean leave) throws CloneNotSupportedException{
        this.trainingFile = trainingFile;
        this.testFile = testFile;
        this.K = K;
        this.metricType = metricType;
        this.trainingSet = new ArrayList<>();
        for(int i = 0; i < trainingSet.size(); i++){
            this.trainingSet.add((TrainRecord) trainingSet.get(i).clone());
        }
        this.testingSet = new ArrayList<>();
        for(int i = 0; i < testingSet.size(); i++){
            this.testingSet.add((TestRecord) testingSet.get(i).clone());
        }
        this.pesos = pesos.clone();
        this.ptest = ptest;
        this.leave = leave;
    }
    public double knn(String alg,boolean escribe) throws IOException, CloneNotSupportedException{
		//get the current time
		
		// make sure the input arguments are legal
		if(K <= 0){
			System.out.println("K should be larger than 0!");
			return -1;
		}
		
		// metricType should be within [0,2];
		if(metricType > 2 || metricType <0){
			System.out.println("metricType is not within the range [0,2]. Please try again later");
			return -1;
		}
		
		//TrainingFile and testFile should be the same group
		String trainGroup = extractGroupName(trainingFile);
		String testGroup = extractGroupName(testFile);
		
		if(!trainGroup.equals(testGroup)){
			System.out.println("trainingFile and testFile are illegal!");
			return -1;
		}
		
		
                //determine the type of metric according to metricType
                Metric metric;
                if(metricType == 0)
                    metric = new CosineSimilarity();
                else if(metricType == 1)
                    metric = new L1Distance();
                else if (metricType == 2){
                    metric = new EuclideanDistance();
                }
                else{
                    System.out.println("The entered metric_type is wrong!");
                    return -1;
                }
                //test those TestRecords one by one
                int numOfTestingRecord = testingSet.size();
                for(int i = 0; i < numOfTestingRecord; i ++){
                    ArrayList<TrainRecord> neighbors;
                    if(this.leave) neighbors = findKNearestNeighborsLeaveOneOut(testingSet.get(i), metric);
                    else neighbors = findKNearestNeighbors(testingSet.get(i), metric);
                    int classLabel = classify(neighbors);
                    testingSet.get(i).predictedLabel = classLabel; //assign the predicted label to TestRecord
                }
                //calculate the accuracy
                int correctPrediction = 0;
                for(int j = 0; j < numOfTestingRecord; j ++){
                    if(testingSet.get(j).predictedLabel == testingSet.get(j).classLabel)
                        correctPrediction ++;
                }
                //Output a file containing predicted labels for TestRecords
                double tasa_clas = ((double)correctPrediction / (double)numOfTestingRecord)*100.0;
                int eliminados = 0;
                for(int i = 0; i < pesos.length; i++){
                    if(pesos[i] < 0.2) eliminados++;
                }

                double tasa_red = 100.0*((1.0*eliminados)/(1.0*pesos.length));
                
                if(escribe) writeOutput(tasa_clas, tasa_red, alg);
                return main.F(tasa_clas,tasa_red);
		
	}
    
    public void writeOutput(double tasa_clas, double tasa_red, String alg) throws IOException{
        FileManager.outputFile(testingSet, trainingFile,tasa_clas,tasa_red,main.F(tasa_clas,tasa_red),alg,ptest);
    }
    // Find K nearest neighbors of testRecord within trainingSet 
	public ArrayList<TrainRecord> findKNearestNeighbors(TestRecord testRecord, Metric metric) throws CloneNotSupportedException{
		int NumOfTrainingSet = trainingSet.size();
		assert K <= NumOfTrainingSet : "K is lager than the length of trainingSet!";
		
		//Update KNN: take the case when testRecord has multiple neighbors with the same distance into consideration
		//Solution: Update the size of container holding the neighbors
		ArrayList<TrainRecord> neighbors = new ArrayList<>();
		
		//initialization, put the first K trainRecords into the above arrayList
		int index;
		for(index = 0; index < K; index++){
                    trainingSet.get(index).distance = metric.getDistance(trainingSet.get(index), testRecord,pesos);
                    neighbors.add((TrainRecord) trainingSet.get(index).clone());
		}
		
		//go through the remaining records in the trainingSet to find K nearest neighbors
		for(index = K; index < NumOfTrainingSet; index ++){
			trainingSet.get(index).distance = metric.getDistance(trainingSet.get(index), testRecord,pesos);
			
			//get the index of the neighbor with the largest distance to testRecord
			int maxIndex = 0;
			for(int i = 1; i < K; i ++){
				if(neighbors.get(i).distance > neighbors.get(maxIndex).distance)
					maxIndex = i;
			}
			
			//add the current trainingSet[index] into neighbors if applicable
			if(neighbors.get(maxIndex).distance > trainingSet.get(index).distance)
				neighbors.set(maxIndex, (TrainRecord) trainingSet.get(index).clone());
		}
		
		return neighbors;
	}
        
        public ArrayList<TrainRecord> findKNearestNeighborsLeaveOneOut(TestRecord testRecord, Metric metric) throws CloneNotSupportedException{
		int NumOfTrainingSet = trainingSet.size();
		assert K <= NumOfTrainingSet : "K is lager than the length of trainingSet!";
		
		//Update KNN: take the case when testRecord has multiple neighbors with the same distance into consideration
		//Solution: Update the size of container holding the neighbors
		ArrayList<TrainRecord> neighbors = new ArrayList<>();
		
		//initialization, put the first K trainRecords into the above arrayList
		int index;
                boolean salir = false;
		for(index = 0; index < NumOfTrainingSet && !salir; index++){
                    trainingSet.get(index).distance = metric.getDistance(trainingSet.get(index), testRecord,pesos);
                    if(trainingSet.get(index).distance != 0.0){
                        neighbors.add((TrainRecord) trainingSet.get(index).clone());
                        if(neighbors.size() == K) salir = true;
                    }
		}
                
                if(neighbors.isEmpty()){
                    neighbors.add(new TrainRecord(testRecord.attributes.clone(), testRecord.classLabel+1));
                    return neighbors;
                }
		
		//go through the remaining records in the trainingSet to find K nearest neighbors
		for(index = K; index < NumOfTrainingSet; index ++){
			trainingSet.get(index).distance = metric.getDistance(trainingSet.get(index), testRecord,pesos);
			
			//get the index of the neighbor with the largest distance to testRecord
			int maxIndex = 0;
			for(int i = 1; i < K; i ++){
				if(neighbors.get(i).distance > neighbors.get(maxIndex).distance)
					maxIndex = i;
			}
			
			//add the current trainingSet[index] into neighbors if applicable
			if(neighbors.get(maxIndex).distance > trainingSet.get(index).distance && trainingSet.get(index).distance != 0.0)
				neighbors.set(maxIndex, (TrainRecord) trainingSet.get(index).clone());
		}
		
		return neighbors;
	}
	
	// Get the class label by using neighbors
	public int classify(ArrayList<TrainRecord> neighbors){
            if(neighbors.size() == 1) return neighbors.get(0).classLabel;
		//construct a HashMap to store <classLabel, weight>
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		int num = neighbors.size();
		
		for(int index = 0;index < num; index ++){
			TrainRecord temp = neighbors.get(index);
			int key = temp.classLabel;
		
			//if this classLabel does not exist in the HashMap, put <key, 1/(temp.distance)> into the HashMap
			if(!map.containsKey(key))
				map.put(key, 1 / temp.distance);
			
			//else, update the HashMap by adding the weight associating with that key
			else{
				double value = map.get(key);
				value += 1 / temp.distance;
				map.put(key, value);
			}
		}	
		
		//Find the most likely label
		double maxSimilarity = 0;
		int returnLabel = -1;
		Set<Integer> labelSet = map.keySet();
		Iterator<Integer> it = labelSet.iterator();
		
		//go through the HashMap by using keys 
		//and find the key with the highest weights 
		while(it.hasNext()){
			int label = it.next();
			double value = map.get(label);
			if(value > maxSimilarity){
				maxSimilarity = value;
				returnLabel = label;
			}
		}
                
		
		return returnLabel;   
	}
	
	public String extractGroupName(String filePath){
		StringBuilder groupName = new StringBuilder();
		for(int i = 15; i < filePath.length(); i ++){
			if(filePath.charAt(i) != '_')
				groupName.append(filePath.charAt(i));
			else
				break;
		}
		
		return groupName.toString();
	}
}
