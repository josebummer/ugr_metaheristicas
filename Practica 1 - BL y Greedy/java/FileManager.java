package calculoPesos;
//FileManager
// * ReadFile: read training files and test files
// * OutputFile: output predicted labels into a file

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class FileManager {
    
        public static double normalize(double x,double min,double max){
            return ((x-min)/(max-min));
        }
    
        //normalize data
        public static ArrayList<TrainRecord> normalizeData(ArrayList<TrainRecord> data){          
            ArrayList<TrainRecord> aux = new ArrayList();
            
            for(int i = 0; i < data.size(); i++){
                aux.add(new TrainRecord(data.get(i).attributes, data.get(i).classLabel));
            }
            
            int tam = aux.get(0).attributes.length;
            for(int j = 0; j < tam; j++){
                double max = aux.get(0).attributes[j];
                double min = aux.get(0).attributes[j];
                
                for(int i = 0; i < aux.size(); i++){
                    max = (aux.get(i).attributes[j] > max)?aux.get(i).attributes[j]:max;
                    min = (aux.get(i).attributes[j] < min)?aux.get(i).attributes[j]:min;
                }
                
                for(int i = 0; i < aux.size(); i++){
                    aux.get(i).attributes[j] = normalize(aux.get(i).attributes[j], min, max);
                }
            }
            return aux;
        }
	
	//read training files
	public static ArrayList<TrainRecord>[] readFile(String fileName) throws IOException{
		File file = new File(fileName);
		Scanner scanner = new Scanner(file).useLocale(Locale.US).useDelimiter(",");
                String line;
                int class1 = 0, class2 = 0;
                ArrayList<TrainRecord>[] particiones = new ArrayList[5];

		//read file
                int NumOfAttributes = 0;
                while(!(line = scanner.nextLine()).contains("@data")){
                    if(line.contains("@attribute V")) NumOfAttributes++;
                }
                
		//transform data from file into TrainRecord objects
		ArrayList<TrainRecord> records = new ArrayList<>();
		while(scanner.hasNext()){
			double[] attributes = new double[NumOfAttributes];
			int classLabel = -1;
			
			//Read a whole line for a TrainRecord
			for(int i = 0; i < NumOfAttributes; i ++){
				attributes[i] = scanner.nextDouble();
			}
			
			//Read classLabel
                        scanner.useDelimiter("\n");
                        String label = scanner.next();
                        label = label.substring(1,2);
                        try {
                            classLabel = Integer.parseInt(label);
                        } catch (NumberFormatException ex) {
                            System.out.println("numero desconocido");
                            System.exit(-1);
                        }
                        if(classLabel == 1) class1++;
                        else class2++;
                        
                        scanner.useDelimiter(",");
                        
                        if(scanner.hasNext()) scanner.nextLine();
			assert classLabel != -1 : "Reading class label is wrong!";
			
			records.add(new TrainRecord(attributes, classLabel));
		}
                
                records = normalizeData(records);
                ArrayList<TrainRecord> parte1 = new ArrayList<>();
                ArrayList<TrainRecord> parte2 = new ArrayList<>();
                ArrayList<TrainRecord> parte3 = new ArrayList<>();
                ArrayList<TrainRecord> parte4 = new ArrayList<>();
                ArrayList<TrainRecord> parte5 = new ArrayList<>();
                
                int nclase1 =  (int) Math.round(0.2*(class1*1.0));
                int nclase2 = (int) Math.round(0.2*(class2*1.0));
                
                //relleno parte1
                int n1 = 0;
                int n2 = 0;
                int index1 = 0;
                int index2 = 0;
                boolean lleno1 = false, lleno2 = false;
                while(!lleno1 || !lleno2){
                    if(records.get(index1).classLabel == 1 && n1 < nclase1){
                        parte1.add(new TrainRecord(records.get(index1).attributes, records.get(index1).classLabel));
                        n1++;
                        index1++;
                    }
                    else if (n1 < nclase1) index1++;
                        
                    if(records.get(index2).classLabel == 2 && n2 < nclase2){
                        parte1.add(new TrainRecord(records.get(index2).attributes, records.get(index2).classLabel));
                        n2++;
                        index2++;
                    }
                    else if( n2 < nclase2) index2++;
                    lleno1 = (n1 >= nclase1);
                    lleno2 = (n2 >= nclase2);
                }
                particiones[0] = parte1;
                
                //relleno parte2
                n1 = n2 = 0;
                lleno1 = lleno2 = false;
                while(!lleno1 || !lleno2){
                    if(records.get(index1).classLabel == 1 && n1 < nclase1){
                        parte2.add(new TrainRecord(records.get(index1).attributes, records.get(index1).classLabel));
                        n1++;
                        index1++;
                    }
                    else if (n1 < nclase1) index1++;
                    if(records.get(index2).classLabel == 2 && n2 < nclase2){
                        parte2.add(new TrainRecord(records.get(index2).attributes, records.get(index2).classLabel));
                        n2++;
                        index2++;
                    }
                    else if (n2 < nclase2) index2++;
                    lleno1 = (n1 >= nclase1);
                    lleno2 = (n2 >= nclase2);
                }
                particiones[1] = parte2;
                
                //relleno parte3
                n1 = n2 = 0;
                lleno1 = lleno2 = false;
                while(!lleno1 || !lleno2){
                    if(records.get(index1).classLabel == 1 && n1 < nclase1){
                        parte3.add(new TrainRecord(records.get(index1).attributes, records.get(index1).classLabel));
                        n1++;
                        index1++;
                    }
                    else if (n1 < nclase1) index1++;
                    if(records.get(index2).classLabel == 2 && n2 < nclase2){
                        parte3.add(new TrainRecord(records.get(index2).attributes, records.get(index2).classLabel));
                        n2++;
                        index2++;
                    }
                    else if (n2 < nclase2) index2++;
                    lleno1 = (n1 >= nclase1);
                    lleno2 = (n2 >= nclase2);
                }
                particiones[2] = parte3;
                
                //relleno parte4
                n1 = n2 = 0;
                lleno1 = lleno2 = false;
                while(!lleno1 || !lleno2){
                    if(records.get(index1).classLabel == 1 && n1 < nclase1){
                        parte4.add(new TrainRecord(records.get(index1).attributes, records.get(index1).classLabel));
                        n1++;
                        index1++;
                    }
                    else if (n1 < nclase1) index1++;
                    if(records.get(index2).classLabel == 2 && n2 < nclase2){
                        parte4.add(new TrainRecord(records.get(index2).attributes, records.get(index2).classLabel));
                        n2++;
                        index2++;
                    }
                    else if (n2 < nclase2) index2++;
                    lleno1 = (n1 >= nclase1);
                    lleno2 = (n2 >= nclase2);
                }
                particiones[3] = parte4;
                
                //relleno parte5
                while(index1 < records.size() || index2 < records.size()){
                    if(index1 < records.size() && records.get(index1).classLabel == 1){
                        parte5.add(new TrainRecord(records.get(index1).attributes, records.get(index1).classLabel));
                        index1++;
                    }
                    else index1++;
                    if(index2 < records.size() && records.get(index2).classLabel == 2){
                        parte5.add(new TrainRecord(records.get(index2).attributes, records.get(index2).classLabel));
                        index2++;
                    }
                    else index2++;
                }
                particiones[4] = parte5;
                
		return particiones;
	}
	
	public static void outputFile(ArrayList<TestRecord> testRecords, String trainFilePath,double tasa_clas, double tasa_red,double agregacion,String alg,int ptest) throws IOException{
		//construct the predication file name
		StringBuilder predictName = new StringBuilder();
		for(int i = 10; i < trainFilePath.length(); i ++){
			if(trainFilePath.charAt(i) != '_')
				predictName.append(trainFilePath.charAt(i));
			else
				break;
		}
		String predictPath = "src/output/"+predictName.toString()+"_"+alg+"-testp"+ptest+".txt";
                		
		//ouput the prediction labels
		File file = new File(predictPath);
		if(!file.exists())
			file.createNewFile();
		
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		
                bw.write("tasa_clas "+tasa_clas+"%");
                bw.newLine();
                bw.write("tasa_red "+tasa_red+"%");
                bw.newLine();
                bw.write("Agregacion "+agregacion);
                bw.newLine();
		
		bw.close();
		fw.close();
		
	}
}
