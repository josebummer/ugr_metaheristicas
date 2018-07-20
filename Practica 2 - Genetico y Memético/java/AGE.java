/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2mh;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

/**
 *
 * @author jose
 */
public class AGE {
    int N; // Tamanio poblacion
    double Pc; // Probabilidad de cruce
    double Pm; // Probabilidad de mutacion por gen
    ArrayList<double[]> P; //poblacion
    int icruce; //Indica que tipo de cruce vamos a utilizar
    Random rnd;
    int genes; // Tamanio del vector de cromosomas
    ArrayList<Double> E; //valor de las evaluaciones
    double[] mejor;
    double Emejor;
    double[] bestMejor;
    double EBestMejor;
    int evaluaciones;
    TreeMap<Double,ArrayList<double[]>> ordenados;
    Cruce cruce;
    int fich;
    
    public AGE(int tamanioPoblacion, double Pc, double Pm,int cruce, int genes,Random rnd){
        this.N = tamanioPoblacion;
        this.Pc = Pc;
        this.Pm = Pm;
        this.rnd = rnd;
        this.P = new ArrayList<>();
        this.E = new ArrayList<>();
        this.genes = genes;
        this.mejor = new double[genes];
        this.Emejor = 0.0;
        this.bestMejor = new double[genes];
        this.EBestMejor = 0.0;
        this.icruce = cruce;
        this.ordenados = new TreeMap<>();
        this.evaluaciones = 0;
        if(this.icruce != 0 && this.icruce != 1){
            System.out.println("xlizado no existente!");
            return;
        }
        
        if(this.icruce == 0) this.cruce = new BLX();
        else this.cruce = new CA();
    }
    
    private void addValueToMap(double key, double[] value){
        ArrayList<double[]> aux = this.ordenados.get(key);
        
        if(aux == null){
            aux = new ArrayList<>();
        }
        else{
            this.ordenados.remove(key);
        }
        aux.add(value.clone());
        this.ordenados.put(key, aux);

    }
    
    private void removeFirstValueToMap(){
        double key = this.ordenados.firstKey();
        ArrayList<double[]> aux = this.ordenados.get(key);
        this.ordenados.remove(key);
        
        aux.remove(0);
        
        if(!aux.isEmpty()) this.ordenados.put(key, aux);

    }
    
    public double[] AGE(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet,int ptest,String alg) throws IOException, CloneNotSupportedException{
        if(trainingFile.contains("ozone")) this.fich = 0;
        else if(trainingFile.contains("parkinson")) this.fich = 1;
        else if(trainingFile.contains("heart")) this.fich = 2;
        
        ArrayList<TestRecord> testingSet = new ArrayList<>();
        for(int i = 0; i < trainingSet.size(); i++){
            testingSet.add(new TestRecord(trainingSet.get(i).attributes.clone(), trainingSet.get(i).classLabel));
        }
        final long startTime = System.currentTimeMillis();
        inicializarPoblacion(ptest);
        evaluar(trainingFile, testFile, K, metricType, trainingSet, testingSet, ptest);
        while(this.evaluaciones < 15000){
            seleccion();
            cruce();
            mutacion();
            reemplazo(trainingFile, testFile, K, metricType, trainingSet, testingSet, ptest);
            evaluarMejor();
        }
        final long endTime = System.currentTimeMillis();
        
        double total_time = (endTime - startTime) / (double)1000;
        
        outputFile(testingSet, trainingFile,total_time,alg,ptest,this.bestMejor);
        
        return this.bestMejor;
    }
    
    public void inicializarPoblacion(int ptest){
        this.P.clear();
        for(int i = 0; i < this.N; i++){
            Random rnd = new Random(331995+((fich*5*this.N)+(ptest*this.N+i)));
            double[] aux = new double[this.genes];
            for(int j = 0; j < this.genes;j++){
                aux[j] = rnd.nextDouble();
            }
            this.P.add(aux.clone());
        }
    }
    
    public void evaluar(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, ArrayList<TestRecord> testingSet,int ptest) throws IOException, CloneNotSupportedException{
        this.E.clear();
        this.ordenados.clear();
        this.mejor = new double[this.genes];
        this.Emejor = 0.0;
        for(int i = 0; i < this.P.size(); i++){
            knn knns = new knn(trainingFile, testFile, K, metricType, trainingSet, testingSet, this.P.get(i), ptest,true);
            double f = knns.knn("AGE",false);
            this.evaluaciones++;
            this.addValueToMap(f, this.P.get(i));
            this.E.add(f);
        }
        
        this.mejor = this.ordenados.get(this.ordenados.lastKey()).get(0).clone();
        this.Emejor = this.ordenados.lastKey();
        
        if(this.Emejor > this.EBestMejor){
            this.bestMejor = this.mejor.clone();
            this.EBestMejor = this.Emejor;
        }
    }
    
    public void evaluarMejor(){
        this.mejor = this.ordenados.get(this.ordenados.lastKey()).get(0).clone();
        this.Emejor = this.ordenados.lastKey();
        
        if(this.Emejor > this.EBestMejor){
            this.bestMejor = this.mejor.clone();
            this.EBestMejor = this.Emejor;
        }
    }
    
    public void seleccion(){
        int par1,par2;
        ArrayList<double[]> aux = new ArrayList<>();
        
        for(int i = 0; i < 2; i++){
            do{
                par1 = this.rnd.nextInt(this.P.size());
                par2 = this.rnd.nextInt(this.P.size());
            }while(par1==par2);
            
            int top = (this.E.get(par1) > this.E.get(par2))?par1:par2;
            
            aux.add(this.P.get(top).clone());
        }
        
        this.P = aux;
    }
    
    public void cruce(){
        this.P = cruce.cruce(this.P, this.Pc,this.rnd);
    }
    
    public void mutacion(){ 
        int mutados = (int)(this.Pm*(double)this.P.size()*(double)this.genes);
        for(int i = 0; i < mutados; i++){
            int fila = rnd.nextInt(this.P.size());
            int colum = rnd.nextInt(this.genes);
            
            this.P.get(fila)[colum] = this.P.get(fila)[colum]+(rnd.nextGaussian()*0.3);
            
            this.P.get(fila)[colum] = (this.P.get(fila)[colum] < 0.0)?0.0:this.P.get(fila)[colum];
            this.P.get(fila)[colum] = (this.P.get(fila)[colum] > 1.0)?1.0:this.P.get(fila)[colum];
        }
    }
    
    private void evaluarUnicoValor(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, ArrayList<TestRecord> testingSet,int ptest,double[] pesos) throws IOException, CloneNotSupportedException{
        knn knns = new knn(trainingFile, testFile, K, metricType, trainingSet, testingSet, pesos, ptest,true);
        double f = knns.knn("AGE",false);
        this.evaluaciones++;
        this.E.add(f);
    }
    
    public void reemplazo(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, ArrayList<TestRecord> testingSet,int ptest) throws IOException, CloneNotSupportedException{
        this.E.clear();
        for(int i = 0; i < this.P.size();i++){
            this.evaluarUnicoValor(trainingFile, testFile, K, metricType, trainingSet, testingSet, ptest,this.P.get(i));
        }
        
        for(int i = 0; i < this.E.size(); i++){
            this.addValueToMap(this.E.get(i), this.P.get(i));
        }
        
        for(int i = 0; i < this.E.size(); i++){
            this.removeFirstValueToMap();
        }
        
        this.P.clear();
        this.E.clear();
        Iterator it = this.ordenados.keySet().iterator();
        while(it.hasNext()){
          double key = (double) it.next();
          ArrayList<double[]> elemento = this.ordenados.get(key);
          for(double[] valor : elemento){
              this.P.add(valor.clone());
              this.E.add(key);
          }
        }
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
