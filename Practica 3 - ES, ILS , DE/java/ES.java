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
public class ES {
    String trainingFile;
    String testFile;
    int K;
    int metricType;
    ArrayList<TrainRecord> trainingSet;
    ArrayList<TestRecord> testingSet;
    int ptest;
    Random rnd;
    
    public ES(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, int ptest, Random rnd) throws CloneNotSupportedException{
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
    
    public double [] ef() throws CloneNotSupportedException, IOException{
        int max_vecinos = 10*trainingSet.get(0).attributes.length;
        int max_exitos = (int)(0.4*(double)max_vecinos);
        int vecinos = -1;
        int exitos = -1;
        
         final long startTime = System.currentTimeMillis();
        
        double[] sol = generarSolucionInicial();
        double [] bestSol = sol.clone();
        knn knns = new knn(trainingFile,testFile,K,metricType,trainingSet,testingSet,bestSol,0,true);
        double bestCost = knns.knn("ef", false);
        double f = bestCost;
        double TIni = calcularTemperaturaInicial(0.3,0.3,bestCost);
        double TFin = 0.001;
        
        if(TIni < TFin) return sol;
        
        double TAct = TIni;
        
        double M = 15000/(max_vecinos);
        double beta = (TIni-TFin)/(M*TIni*TFin);
        
        while(TAct > TFin && exitos != 0){
            vecinos = 0;
            exitos = 0;
            while(vecinos < max_vecinos && exitos < max_exitos){
               double [] newSol = this.mutacion(sol);
               vecinos++;
               
               knn knnNew = new knn(trainingFile,testFile,K,metricType,trainingSet,testingSet,newSol,0,true);
               
               double newF = knnNew.knn("ef", false);
               
               double dif = f - newF;
               
               if((dif < 0) || (rnd.nextDouble() <= Math.exp(-dif/(1*TAct)))){
                   exitos++;
                   sol = newSol.clone();
                   f = newF;
                   if(newF > bestCost){
                       bestSol = sol.clone();
                       bestCost = newF;
                   }
               }
            }
            TAct = actualizarTemperatura(TAct, beta);
        }
        
        final long endTime = System.currentTimeMillis();
        
        double total_time = (endTime - startTime) / (double)1000;
        
        outputFile(testingSet, trainingFile,total_time,"EF",ptest,bestSol);
        
        return bestSol;
    }
    
    private double calcularTemperaturaInicial(double mu, double nu, double f) throws CloneNotSupportedException, IOException{
        return (mu*f)/(-Math.log(nu));
    }
    
    public double [] mutacion(double[] sol){  
        double[] res = sol.clone();
        
        int k = rnd.nextInt(sol.length);
        
        res[k] = sol[k]+(rnd.nextGaussian()*0.3);
        
        res[k] = (res[k] < 0.0)?0.0:res[k];
        res[k] = (res[k] > 1.0)?1.0:res[k];

        return res;
    }
    
    private double actualizarTemperatura(double TAct, double beta){
        return (TAct/(1+beta*TAct));
    }
    
    private double [] generarSolucionInicial(){
        int tam = trainingSet.get(0).attributes.length;
        double [] res = new double[tam];
        
        for(int i = 0; i < tam; i++){
            res[i] = rnd.nextDouble();
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
