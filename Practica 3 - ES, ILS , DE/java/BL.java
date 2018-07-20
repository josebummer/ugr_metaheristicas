/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Practica3MH;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author jose
 */
public class BL {
    String trainingFile;
    int K;
    int metricType;
    ArrayList<TrainRecord> trainingSet;
    ArrayList<TestRecord> testingSet;
    double[] pesos;
    int ptest;
    Random rnd;
    
    public BL(String trainingFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, double[] pesos,int ptest,Random rnd){
        this.trainingFile = trainingFile;
        this.K = K;
        this.metricType = metricType;
        this.trainingSet = new ArrayList<>();
        for(int i = 0; i < trainingSet.size(); i++){
            this.trainingSet.add(new TrainRecord(trainingSet.get(i).attributes.clone(), trainingSet.get(i).classLabel));
        }
        this.testingSet = new ArrayList<>();
        for(int i = 0; i < this.trainingSet.size(); i++){
            this.testingSet.add(new TestRecord(this.trainingSet.get(i).attributes.clone(), this.trainingSet.get(i).classLabel));
        }
        this.pesos = pesos.clone();
        this.ptest = ptest;
        this.rnd = rnd;
    }
    
//    public boolean vecinoValido(double[] pesos){
//        boolean valido = false;
//        for(int i = 0; i < pesos.length && !valido; i++){
//            valido = (pesos[i] >= 0.2);
//        }
//        return valido;
//    }
    
    public double[] BL() throws IOException, CloneNotSupportedException{
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
                    metric = new EuclideanDistance();
                else{
                    System.out.println("The entered metric_type is wrong!");
                    System.exit(-1);
                }
                
                double maxaument = 0;
                double[] pesosnew = this.pesos.clone();
                
                int numberOfAttributes = testingSet.get(0).attributes.length;
                int parada = 20*numberOfAttributes;
                int numOfTestingRecord = testingSet.size();
                int correctPrediction = 0;
                
                //Primera vez
                
                for(int i = 0; i < numOfTestingRecord;i++){
                    ArrayList<TrainRecord> neighbors = findKNearestNeighbors(testingSet.get(i), metric,pesos);
                    int classLabel = classify(neighbors);
                    testingSet.get(i).predictedLabel = classLabel;
                    
                    if(classLabel == testingSet.get(i).classLabel)
                        correctPrediction ++;
                }
                
                double tasa_clas = ((1.0*correctPrediction) / (1.0*numOfTestingRecord))*100.0;
                int eliminados = 0;
                for(int j = 0; j < numberOfAttributes; j++){
                    if(pesos[j] < 0.2) eliminados++;
                }
                double tasa_red = 100.0*((1.0*eliminados)/(1.0*numberOfAttributes));

                maxaument = main.F(tasa_clas, tasa_red);
                int evaluaciones = 0;
                int mal = 0;
                
                //Seguimos
                while(evaluaciones < 1000 && mal < parada){
                    boolean mejorado = false;
                    for(int k = 0; k < numberOfAttributes && !mejorado; k++){
//                        do{
                            pesosnew = calcularPesoNuevo(k).clone();
//                        }while(!vecinoValido(pesosnew));
                        //test those TestRecords one by one
                        correctPrediction = 0;
                        
                        for(int j = 0; j < numOfTestingRecord;j++){
                            ArrayList<TrainRecord> neighbors = findKNearestNeighbors(testingSet.get(j), metric,pesosnew);
                            int classLabel = classify(neighbors);
                            testingSet.get(j).predictedLabel = classLabel;
                            
                            if(classLabel == testingSet.get(j).classLabel)
                                correctPrediction ++;
                        }

                        tasa_clas = ((1.0*correctPrediction) / (1.0*numOfTestingRecord))*100.0;
                        eliminados = 0;
                        for(int j = 0; j < numberOfAttributes; j++){
                            if(pesosnew[j] < 0.2) eliminados++;
                        }
                        tasa_red = 100.0*((1.0*eliminados)/(1.0*numberOfAttributes));

                        double aument = main.F(tasa_clas, tasa_red);
                        evaluaciones++;

                        if(aument > maxaument){
                            pesos = pesosnew.clone();
                            maxaument = aument;
                            mejorado = true;
                            mal = 0;
                        }
                        else mal++;
                    } 
                }
                
                //Output a file containing predicted labels for TestRecords
                
//                outputFile(testingSet, trainingFile,total_time,"BL",ptest);
                return pesos;
		
	}
    
    public double[] calcularPesoNuevo(int k){  
        double[] res = pesos.clone();
        
        res[k] = pesos[k]+(rnd.nextGaussian()*0.3);
        
        res[k] = (res[k] < 0.0)?0.0:res[k];
        res[k] = (res[k] > 1.0)?1.0:res[k];

        return res;
    }
    
    // Find K nearest neighbors of testRecord within trainingSet 
	public ArrayList<TrainRecord> findKNearestNeighbors(TestRecord testRecord, final Metric metric, final double[] pesos) throws CloneNotSupportedException{
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
                    if(trainingSet.get(index).distance != 0){
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
                       
                        if(trainingSet.isEmpty()) System.out.println("TRAININGG SIN TAMAÑOO");
                        
			//add the current trainingSet[index] into neighbors if applicable
			if(neighbors.get(maxIndex).distance > trainingSet.get(index).distance && trainingSet.get(index).distance != 0.0){
                            neighbors.set(maxIndex, (TrainRecord) trainingSet.get(index).clone());
                        }
		}
		
		return neighbors;
	}
	
	// Get the class label by using neighbors
	public int classify(ArrayList<TrainRecord> neighbors){
            if(neighbors.size() == 1) return neighbors.get(0).classLabel;
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
