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
import java.util.Random;

/**
 *
 * @author jose
 */
public class ILS {
    String trainingFile;
    String testFile;
    int K;
    int metricType;
    ArrayList<TrainRecord> trainingSet;
    ArrayList<TestRecord> testingSet;
    int ptest;
    Random rnd;
    
    public ILS(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, int ptest, Random rnd) throws CloneNotSupportedException{
        this.trainingFile = trainingFile;
        this.testFile = testFile;
        this.K = K;
        this.metricType = metricType;
        this.trainingSet = new ArrayList<>();
        this.testingSet = new ArrayList<>();
        for(int i = 0; i < trainingSet.size(); i++){
            this.trainingSet.add((TrainRecord) trainingSet.get(i).clone());
        }
        for(int i = 0; i < trainingSet.size(); i++){
            this.testingSet.add(new TestRecord(trainingSet.get(i).attributes.clone(), trainingSet.get(i).classLabel));
        }
        this.ptest = ptest;
        this.rnd = rnd;
    }
    
    public double[] ils() throws IOException, CloneNotSupportedException{
        final long startTime = System.currentTimeMillis();
        
        double [] sol = generarSolucionInicial();
        BL bl = new BL(trainingFile,K,metricType,trainingSet,sol,0,rnd);
        sol = bl.BL();
        knn knns = new knn(trainingFile,testFile,K,metricType,trainingSet,testingSet,sol,0,true);
        double f = knns.knn("ils", false);
        
        
        for(int i = 0 ;i < 14;i++){
            double [] newSol = mutacion(sol);
            bl = new BL(trainingFile,K,metricType,trainingSet,newSol,0,rnd);
            newSol = bl.BL();
            
            knn knnNew = new knn(trainingFile,testFile,K,metricType,trainingSet,testingSet,newSol,0,true);

            double newF = knnNew.knn("ils", false);
            
            if(newF > f){
                sol = newSol.clone();
                f = newF;
            }
        }
        
        final long endTime = System.currentTimeMillis();
        
        double total_time = (endTime - startTime) / (double)1000;
        
        outputFile(testingSet, trainingFile,total_time,"ILS",ptest,sol);
        
        return sol;
    }
    
    private double [] generarSolucionInicial(){
        int tam = trainingSet.get(0).attributes.length;
        double [] res = new double[tam];
        
        for(int i = 0; i < tam; i++){
            res[i] = rnd.nextDouble();
        }
        
        return res;
    }
    
    public double [] mutacion(double[] sol){  
        double[] res = sol.clone();
        int tam = trainingSet.get(0).attributes.length;
        int k;
        ArrayList<Integer> posiciones = new ArrayList();
        
        int t = (int)(0.1*(double)tam);
        
        for(int i = 0 ; i < t; i++){
            do{
                k = rnd.nextInt(tam);
            } while(posiciones.contains(k));
            posiciones.add(k);
            
            res[k] = sol[k]+(rnd.nextGaussian()*0.4);
        
            res[k] = (res[k] < 0.0)?0.0:res[k];
            res[k] = (res[k] > 1.0)?1.0:res[k];
        }
        
        return res;
    }
    
    public void outputFile(ArrayList<TestRecord> testRecords, String trainFilePath, double total_time,String alg,int ptest,double[] pesos) throws IOException{
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
                for(int i = 0; i < pesos.length; i++){
                    bw.write(pesos[i]+" ");
                }
                bw.newLine();
		
		bw.close();
		fw.close();
		
    }
}
