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
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

/**
 *
 * @author jose
 */
public class AM_1_01mej {
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
    TreeMap<Double,ArrayList<double[]>> ordenados;
    int evaluaciones;
    Cruce cruce;
    int fich;
    
    public AM_1_01mej(int tamanioPoblacion, double Pc, double Pm,int cruce, int genes,Random rnd){
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
        this.ordenados = new TreeMap<>();
        this.EBestMejor = 0.0;
        this.icruce = cruce;
        this.evaluaciones = 0;
        if(this.icruce != 0 && this.icruce != 1){
            System.out.println("xlizado no existente!");
            return;
        }
        
        if(this.icruce == 0) this.cruce = new BLX();
        else this.cruce = new CA();
    }
    
    public void busquedaLocal(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet, ArrayList<TestRecord> testingSet,int ptest) throws IOException, CloneNotSupportedException{
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
        int elementos = (int)(0.1*(double)this.P.size());
        int fin = this.P.size() - (elementos+1);
        for(int i = this.P.size()-1; i > fin; i--){
            BL bl = new BL(trainingFile, K, metricType, trainingSet, this.P.get(i), ptest, rnd);
            ArrayList<Double> aux = new ArrayList<>();
            aux.add((double)this.evaluaciones);
            this.P.set(i, bl.BL(aux).clone());
            this.evaluaciones = (int)(double)aux.get(0);
            this.E.set(i, aux.get(1));
            if(aux.get(1) > this.Emejor){
                this.mejor = this.P.get(i).clone();
                this.Emejor = aux.get(1);
            }

            if(this.Emejor > this.EBestMejor){
                this.bestMejor = this.mejor.clone();
                this.EBestMejor = this.Emejor;
            }
        }
    }
    
    public double[] AM_1_01mej(String trainingFile, String testFile, int K, int metricType, ArrayList<TrainRecord> trainingSet,int ptest,String alg) throws IOException, CloneNotSupportedException{
        if(trainingFile.contains("ozone")) this.fich = 0;
        else if(trainingFile.contains("parkinson")) this.fich = 1;
        else if(trainingFile.contains("heart")) this.fich = 2;
        
        ArrayList<TestRecord> testingSet = new ArrayList<>();
        for(int i = 0; i < trainingSet.size(); i++){
            testingSet.add(new TestRecord(trainingSet.get(i).attributes.clone(), trainingSet.get(i).classLabel));
        }
        final long startTime = System.currentTimeMillis();
        int generaciones = 0;
        inicializarPoblacion(ptest);
        evaluar(trainingFile, testFile, K, metricType, trainingSet, testingSet, ptest);
        while(this.evaluaciones < 15000){
            busquedaLocal(trainingFile, testFile, K, metricType, trainingSet, testingSet, ptest);
            seleccion();
            cruce();
            mutacion();
            reemplazo();
            evaluar(trainingFile, testFile, K, metricType, trainingSet, testingSet, ptest);
            generaciones++;
        }
        final long endTime = System.currentTimeMillis();
        
        double total_time = (endTime - startTime) / (double)1000;
        
        outputFile(testingSet, trainingFile,total_time,alg,ptest,this.bestMejor);
        
        return this.bestMejor;
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
        this.Emejor = 0.0;
        this.mejor = new double[this.genes];
        this.ordenados.clear();
        for(int i = 0; i < this.P.size(); i++){
            knn knns = new knn(trainingFile, testFile, K, metricType, trainingSet, testingSet, this.P.get(i), ptest,true);
            double f = knns.knn("AGG",false);
            addValueToMap(f, this.P.get(i));
            if(f > this.Emejor){
                this.mejor = this.P.get(i).clone();
                this.Emejor = f;
            }
            this.evaluaciones++;
            this.E.add(f);
        }
        
        if(this.Emejor > this.EBestMejor){
            this.bestMejor = this.mejor.clone();
            this.EBestMejor = this.Emejor;
        }
    }
    
    public void seleccion(){
        int par1,par2;
        ArrayList<double[]> aux = new ArrayList<>();
        
        for(int i = 0; i < this.N; i++){
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
    
    public void reemplazo(){
        this.P.remove(this.rnd.nextInt(this.P.size()));
        this.P.add(this.mejor.clone());
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
