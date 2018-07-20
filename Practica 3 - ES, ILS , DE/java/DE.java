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
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author jose
 */
public class DE {
    String trainingFile;
    String testFile;
    int K;
    int metricType;
    ArrayList<TrainRecord> trainingSet;
    ArrayList<TestRecord> testingSet;
    int ptest;
    Random rnd;
    int evaluaciones;
    double CR;
    int tamP;
    double F;
    double[] mejor;
    double mejorCost;
    double[] bestSol;
    double bestCost;
    
    public DE(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, int ptest, Random rnd,int tamP, double CR,double F) throws CloneNotSupportedException{
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
        this.evaluaciones = 0;
        this.CR = CR;
        this.tamP = tamP;
        this.F = F;
        this.mejor = new double[this.trainingSet.get(0).attributes.length];
        this.mejorCost = 0;
        this.bestSol = new double[this.trainingSet.get(0).attributes.length];
        this.bestCost = 0;
    }
    
    public double [] de(int modelo) throws CloneNotSupportedException, IOException{
        ArrayList<double[]> P;
        ArrayList<Double> E;
        ArrayList<Double> newE = new ArrayList();
        ArrayList<double[]> newP = new ArrayList();
        knn knns;
        
        final long startTime = System.currentTimeMillis();
                
        P = inicializarPoblacion(this.tamP);
        E = evaluarPoblacion(P);
        
        while(this.evaluaciones < 15000){
            
            newP.clear();
            newE.clear();
            double[] hijo = new double[trainingSet.get(0).attributes.length];
            
            for(int i = 0; i < P.size(); i++){
                int[] padres = seleccionarPadres(P,i); //3 Padres
                
                for(int k = 0; k < trainingSet.get(0).attributes.length; k++){
                    //modelo = 0 --> DE/Rand/1
                    //modelo = 1 --> DE/current-to-best/1
                    if(rnd.nextDouble() < this.CR) hijo[k] = aplicarModelo(P,padres,modelo,k,i); 
                    else hijo[k] = P.get(i)[k];
                }
                knns = new knn(trainingFile,testFile,K,metricType,trainingSet,testingSet,hijo,0,true);
                newP.add(hijo.clone());
                newE.add(knns.knn("DE", false));
                this.evaluaciones++;
            }
            actualizarPoblacion(P,E,newP,newE); //Actualiza P y E.
        }
        final long endTime = System.currentTimeMillis();
        
        double total_time = (endTime - startTime) / (double)1000;
        
        if(modelo==0) outputFile(testingSet, trainingFile,total_time,"DE-R-1",ptest,bestSol);
        if(modelo==1) outputFile(testingSet, trainingFile,total_time,"DE-C-1",ptest,bestSol);
        
        return this.bestSol;
    }
    
    private ArrayList<double[]> inicializarPoblacion(int tam){
        ArrayList<double[]> res = new ArrayList();
        
        for(int i = 0 ; i < tam; i++){
            double [] aux = new double[trainingSet.get(0).attributes.length];
            for(int j = 0; j < trainingSet.get(0).attributes.length; j++){
                aux[j] = rnd.nextDouble();
            }
            res.add(aux.clone());
        }
        return res;
    }
    
    private ArrayList<Double> evaluarPoblacion(ArrayList<double[]> P) throws CloneNotSupportedException, IOException{
        knn knns;
        ArrayList<Double> res = new ArrayList();
        this.mejor = new double[trainingSet.get(0).attributes.length];
        this.mejorCost = 0;
        
        for(int i = 0; i < P.size(); i++){
            knns = new knn(trainingFile,testFile,K,metricType,trainingSet,testingSet,P.get(i),0,true);
            double f = knns.knn("DE", false);
            res.add(f);
            if(f > this.mejorCost){ // Mejor de la poblacion actual.
                this.mejor = P.get(i).clone();
                this.mejorCost = f;
            }
            this.evaluaciones++;
        }
        if(this.mejorCost > this.bestCost){ // Mejor global
            this.bestSol = this.mejor.clone();
            this.bestCost = this.mejorCost;
        }
        return res;
    }
    
    private int[] seleccionarPadres(ArrayList<double[]> P,int pos){
        int[] res = new int[3];
        ArrayList<Integer> padres = new ArrayList();
        padres.add(pos);
        int padre;
        
        for(int i = 0 ; i < 3; i++){
            do{
                padre = rnd.nextInt(P.size());
            } while(padres.contains(padre));
            padres.add(padre);
            res[i] = padre;
        }
        return res;
    }
    
    private double aplicarModelo(ArrayList<double[]> P, int[] padres, int modelo, int k, int i){
        double res;
        
        switch(modelo){
            case 0:
                res = (P.get(padres[0])[k]+this.F*(P.get(padres[1])[k]-P.get(padres[2])[k]));
                res = (res < 0.0)?0.0:res;
                res = (res > 1.0)?1.0:res;
                return res;
            case 1:
                res = (P.get(i)[k]+this.F*(this.mejor[k]-P.get(i)[k])+this.F*(P.get(padres[0])[k]-P.get(padres[1])[k]));
                res = (res < 0.0)?0.0:res;
                res = (res > 1.0)?1.0:res;
                return res;
        }
        return -1;
    }
    
    private void actualizarPoblacion(ArrayList<double[]> P,ArrayList<Double> E, ArrayList<double[]> newP, ArrayList<Double> newE){
        ArrayList<double[]> res = new ArrayList();
        ArrayList<Double> Eres = new ArrayList();
        this.mejor = new double[trainingSet.get(0).attributes.length];
        this.mejorCost = 0;
        
        for(int i = 0; i < P.size(); i++){
            if(E.get(i) > newE.get(i)){
                res.add(P.get(i).clone());
                Eres.add(E.get(i));
            }
            else{
                res.add(newP.get(i).clone());
                Eres.add(newE.get(i));
            }
            if(Eres.get(i) > this.mejorCost){
                this.mejor = res.get(i).clone();
                this.mejorCost = Eres.get(i);
            }
        }
        if(this.mejorCost > this.bestCost){
            this.bestSol = this.mejor.clone();
            this.bestCost = this.mejorCost;
        }
        
        Collections.copy(P, res);
        Collections.copy(E, Eres);
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
