/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculoPesos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author jose
 */
public class Greedy {
    String trainingFile;
    int K;
    int metricType;
    ArrayList<TrainRecord> trainingSet;
    ArrayList<TestRecord> testingSet;
    double[] pesos;
    int ptest;
    
    public Greedy(String trainingFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, double[] pesos,int ptest){
        this.trainingFile = trainingFile;
        this.K = K;
        this.metricType = metricType;
        this.trainingSet = new ArrayList<>();
        for(int i = 0; i < trainingSet.size(); i++){
            this.trainingSet.add(new TrainRecord(trainingSet.get(i).attributes, trainingSet.get(i).classLabel));
        }
        this.testingSet = new ArrayList<>();
        for(int i = 0; i < this.trainingSet.size(); i++){
            this.testingSet.add(new TestRecord(this.trainingSet.get(i).attributes, this.trainingSet.get(i).classLabel));
        }
        this.pesos = pesos.clone();
        this.ptest = ptest;
    }
    public double[] Greedy() throws IOException{
		//get the current time
		
		// make sure the input arguments are legal
		if(K <= 0){
			System.out.println("K should be larger than 0!");
			System.exit(-1);
		}
		
		// metricType should be within [0,2];
		if(metricType > 2 || metricType <0){
			System.out.println("metricType is not within the range [0,2]. Please try again later");
			System.exit(-1);
		}
		
                //determine the type of metric according to metricType
                Metric metric = null;
                if(metricType == 0)
                    metric = new CosineSimilarity();
                else if(metricType == 1)
                    metric = new L1Distance();
                else if (metricType == 2)
                    metric = new EuclideanDistanceGreedy();
                else{
                    System.out.println("The entered metric_type is wrong!");
                    System.exit(-1);
                }
                
                double maxaument = 0;
                
                final long startTime = System.currentTimeMillis();
                int NumberOfAttributes = testingSet.get(0).attributes.length;
                for(int i = 0; i < 5; i++){
                    for(int j = 0; j < testingSet.size(); j++){
                        ArrayList<TrainRecord> neighborsFriends = findKNearestNeighborsFriends(testingSet.get(j), metric, pesos,testingSet.get(j).classLabel);
                        double[] valueFriend = classifyValueFriend(neighborsFriends);

                        ArrayList<TrainRecord> neighborsEnemy = findKNearestNeighborsEnemy(testingSet.get(j), metric, pesos,testingSet.get(j).classLabel,1);
                        double[] valueEnemy = classifyValueEnemy(neighborsEnemy);

                        for(int k = 0; k < pesos.length; k++){
                            pesos[k] += Math.abs(testingSet.get(j).attributes[k] - valueEnemy[k]);
                            pesos[k] -= Math.abs(testingSet.get(j).attributes[k] - valueFriend[k]);

                            pesos[k] = (pesos[k] < 0.0)?0.0:pesos[k];
                        }

                        double max = 0.0;
                        for(int k = 0; k < pesos.length; k++){
                            max = (pesos[k] > max)?pesos[k]:max;
                        }
                        for(int k = 0; k < pesos.length; k++){
                            pesos[k] = pesos[k]/max;
                        }
                    }
                }
                final long endTime = System.currentTimeMillis();
                //Output a file containing predicted labels for TestRecords
                
                double total_time = (endTime - startTime) / (double)1000;
                outputFile(testingSet, trainingFile,total_time,"Greedy",ptest);
                
                return pesos;
		
	}
        
        public ArrayList<TrainRecord> findKNearestNeighborsFriends(TestRecord testRecord, Metric metric, double[] pesos,int label){
		int NumOfTrainingSet = trainingSet.size();
		assert K <= NumOfTrainingSet : "K is lager than the length of trainingSet!";
		
		//Update KNN: take the case when testRecord has multiple neighbors with the same distance into consideration
		//Solution: Update the size of container holding the neighbors
		ArrayList<TrainRecord> neighbors = new ArrayList<>();
		
		//initialization, put the first K trainRecords into the above arrayList
		int index;
                boolean salir = false;
		for(index = 0; index < trainingSet.size() && !salir; index++){
                    trainingSet.get(index).distance = metric.getDistance(trainingSet.get(index), testRecord,pesos);
                    if(trainingSet.get(index).classLabel == label && trainingSet.get(index).distance != 0){
                        neighbors.add(trainingSet.get(index));
                        salir = true;
                    }
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
			if(neighbors.get(maxIndex).distance > trainingSet.get(index).distance && trainingSet.get(index).classLabel == label && trainingSet.get(index).distance != 0)
				neighbors.set(maxIndex, trainingSet.get(index));
		}
		
		return neighbors;
	}
        
        public ArrayList<TrainRecord> findKNearestNeighborsEnemy(TestRecord testRecord, Metric metric, double[] pesos,int label,int K){
		int NumOfTrainingSet = trainingSet.size();
		assert K <= NumOfTrainingSet : "K is lager than the length of trainingSet!";
		
		//Update KNN: take the case when testRecord has multiple neighbors with the same distance into consideration
		//Solution: Update the size of container holding the neighbors
		ArrayList<TrainRecord> neighbors = new ArrayList<>();
		
		//initialization, put the first K trainRecords into the above arrayList
		int index;
                boolean salir = false;
		for(index = 0; index < testingSet.size() && !salir; index++){
			trainingSet.get(index).distance = metric.getDistance(trainingSet.get(index), testRecord,pesos);
                        if(trainingSet.get(index).classLabel != label){
                            neighbors.add(trainingSet.get(index));
                            salir = true;
                        }
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
			if(neighbors.get(maxIndex).distance > trainingSet.get(index).distance && trainingSet.get(index).classLabel != label)
				neighbors.set(maxIndex, trainingSet.get(index));
		}
		
		return neighbors;
	}

        public double[] classifyValueFriend(ArrayList<TrainRecord> neighbors){
            double res[] = null;
		for(int i = 0; i < neighbors.size(); i++){
                    if(neighbors.get(i).distance != 0.0){
                        return neighbors.get(i).attributes;
                    }
                }
                
                return res;
	}
        
        public double[] classifyValueEnemy(ArrayList<TrainRecord> neighbors){
            return neighbors.get(0).attributes;
                
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
        
        public void outputFile(ArrayList<TestRecord> testRecords, String trainFilePath, double total_time,String alg,int ptest) throws IOException{
		//construct the predication file name
		StringBuilder predictName = new StringBuilder();
		for(int i = 10; i < trainFilePath.length(); i ++){
			if(trainFilePath.charAt(i) != '_')
				predictName.append(trainFilePath.charAt(i));
			else
				break;
		}
		String predictPath = "src/output/"+predictName.toString()+"_"+alg+"-ptrain"+ptest+".txt";
		
		//ouput the prediction labels
		File file = new File(predictPath);
		if(!file.exists())
			file.createNewFile();
		
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		
                bw.write("total_time "+total_time+"s");
                bw.newLine();
                for(int i = 0; i < this.pesos.length; i++){
                    bw.write(this.pesos[i]+" ");
                }
                bw.newLine();
		
		bw.close();
		fw.close();
		
	}
}
