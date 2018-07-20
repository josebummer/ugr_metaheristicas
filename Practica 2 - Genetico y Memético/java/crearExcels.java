/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author jose
 */
public class crearExcels {
    
    static FileWriter fichero = null;
    static PrintWriter pw = null;
    
    public static void parse(String fich_in) throws FileNotFoundException{
        int particion = 0;
        double tasa_clas, tasa_red, F,T;
        
        pw.println(",Ozone,,,,Parkinson,,,,Spectf-heart");
        pw.println(",%_clas,%_red,F,T,%_clas,%_red,F,T,%_clas,%_red,F,T");
        
        while(particion < 5){
            String fich_in_test_ozone = "./src/output/ozone-320.arff_".concat(fich_in.concat("-testp").concat(Integer.toString(particion).concat(".txt")));
            String fich_in_train_ozone = "./src/output/ozone-320.arff_".concat(fich_in.concat("-ptrain").concat(Integer.toString(particion).concat(".txt")));
            
            String fich_in_test_parkinson = "./src/output/parkinsons.arff_".concat(fich_in.concat("-testp").concat(Integer.toString(particion).concat(".txt")));
            String fich_in_train_parkinson = "./src/output/parkinsons.arff_".concat(fich_in.concat("-ptrain").concat(Integer.toString(particion).concat(".txt")));
            
            String fich_in_test_heart = "./src/output/spectf-heart.arff_".concat(fich_in.concat("-testp").concat(Integer.toString(particion).concat(".txt")));
            String fich_in_train_heart = "./src/output/spectf-heart.arff_".concat(fich_in.concat("-ptrain").concat(Integer.toString(particion).concat(".txt")));
            
            Scanner input_test_ozone = new Scanner(new File(fich_in_test_ozone));
            Scanner input_train_ozone = new Scanner(new File(fich_in_train_ozone));
            
            Scanner input_test_parkinson = new Scanner(new File(fich_in_test_parkinson));
            Scanner input_train_parkinson = new Scanner(new File(fich_in_train_parkinson));
            
            Scanner input_test_heart = new Scanner(new File(fich_in_test_heart));
            Scanner input_train_heart = new Scanner(new File(fich_in_train_heart));
            
            String part = "Particion".concat(Integer.toString(particion));
            
            int firstIndex, lastIndex;
            
            String data = input_test_ozone.nextLine();
            firstIndex = data.indexOf("tasa_clas")+10;
            lastIndex = data.indexOf("%");
            tasa_clas = Double.parseDouble(data.substring(firstIndex, lastIndex));
            
            data = input_test_ozone.nextLine();
            firstIndex = data.indexOf("tasa_red")+9;
            lastIndex = data.indexOf("%");
            tasa_red = Double.parseDouble(data.substring(firstIndex, lastIndex));
            
            data = input_test_ozone.nextLine();
            firstIndex = data.indexOf("Agregacion")+11;
            F = Double.parseDouble(data.substring(firstIndex));
            
            data = input_train_ozone.nextLine();
            firstIndex = data.indexOf("total_time")+11;
            lastIndex = data.lastIndexOf("s");
            T = Double.parseDouble(data.substring(firstIndex,lastIndex));
            
            pw.print("\""+part+"\"");
            pw.print(",\"".concat(Double.toString(tasa_clas).replace(".",","))+"\"");
            pw.print(",\"".concat(Double.toString(tasa_red).replace(".",","))+"\"");
            pw.print(",\"".concat(Double.toString(F).replace(".",","))+"\"");
            pw.print(",\"".concat(Double.toString(T).replace(".",","))+"\"");
            
            data = input_test_parkinson.nextLine();
            firstIndex = data.indexOf("tasa_clas")+10;
            lastIndex = data.indexOf("%");
            tasa_clas = Double.parseDouble(data.substring(firstIndex, lastIndex));
            
            data = input_test_parkinson.nextLine();
            firstIndex = data.indexOf("tasa_red")+9;
            lastIndex = data.indexOf("%");
            tasa_red = Double.parseDouble(data.substring(firstIndex, lastIndex));
            
            data = input_test_parkinson.nextLine();
            firstIndex = data.indexOf("Agregacion")+11;
            F = Double.parseDouble(data.substring(firstIndex));
            
            data = input_train_parkinson.nextLine();
            firstIndex = data.indexOf("total_time")+11;
            lastIndex = data.lastIndexOf("s");
            T = Double.parseDouble(data.substring(firstIndex,lastIndex));
            
            pw.print(",\"".concat(Double.toString(tasa_clas).replace(".",","))+"\"");
            pw.print(",\"".concat(Double.toString(tasa_red).replace(".",","))+"\"");
            pw.print(",\"".concat(Double.toString(F).replace(".",","))+"\"");
            pw.print(",\"".concat(Double.toString(T).replace(".",","))+"\"");
            
            data = input_test_heart.nextLine();
            firstIndex = data.indexOf("tasa_clas")+10;
            lastIndex = data.indexOf("%");
            tasa_clas = Double.parseDouble(data.substring(firstIndex, lastIndex));
            
            data = input_test_heart.nextLine();
            firstIndex = data.indexOf("tasa_red")+9;
            lastIndex = data.indexOf("%");
            tasa_red = Double.parseDouble(data.substring(firstIndex, lastIndex));
            
            data = input_test_heart.nextLine();
            firstIndex = data.indexOf("Agregacion")+11;
            F = Double.parseDouble(data.substring(firstIndex));
            
            data = input_train_heart.nextLine();
            firstIndex = data.indexOf("total_time")+11;
            lastIndex = data.lastIndexOf("s");
            T = Double.parseDouble(data.substring(firstIndex,lastIndex));
            
            pw.print(",\"".concat(Double.toString(tasa_clas).replace(".",","))+"\"");
            pw.print(",\"".concat(Double.toString(tasa_red).replace(".",","))+"\"");
            pw.print(",\"".concat(Double.toString(F).replace(".",","))+"\"");
            pw.print(",\"".concat(Double.toString(T).replace(".",","))+"\"");
            pw.println();
            
            input_test_ozone.close();
            input_train_ozone.close();
            
            input_test_parkinson.close();
            input_train_parkinson.close();
            
            input_test_heart.close();
            input_train_heart.close();
            
            particion++;
        }
        pw.println("Media,=SUMA(B3:B7)/5,=SUMA(C3:C7)/5,=SUMA(D3:D7)/5,=SUMA(E3:E7)/5,=SUMA(F3:F7)/5,=SUMA(G3:G7)/5,=SUMA(H3:H7)/5,=SUMA(I3:I7)/5,=SUMA(J3:J7)/5,=SUMA(K3:K7)/5,=SUMA(L3:L7)/5,=SUMA(M3:M7)/5");
    }
    
    public static String siguienteFichero(int i){
        String res = null;
        switch(i){
            case 0:
                res = "AGG-BLX";
            break;
            case 1:
                res = "AGG-CA";
            break;
            case 2:
                res = "AGE-BLX";
            break;
            case 3:
                res = "AGE-CA";
            break;
            case 4:
                res = "AM-10-0.1-BLX";
            break;
            case 5:
                res = "AM-10-0.1mej-BLX";
            break;
            case 6:
                res = "AM-10-1.0-BLX";
            break;
            case 7:
                res = "AM-1-0.1-BLX";
            break;
            case 8:
                res = "AM-1-0.1mej-BLX";
            break;
            case 9:
                res = "AM-1-1.0-BLX";
            break;
        }
        return res;
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        try
        {
            for(int i = 0; i < 10;i++){
                String fich = siguienteFichero(i);
                
                fichero = new FileWriter("./src/excel/".concat(fich.concat(".csv")));
                pw = new PrintWriter(fichero);
                
                parse(fich);
                
                pw.close();
                fichero.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
                if (null != fichero)
                   fichero.close();
                } catch (Exception e2) {
                   e2.printStackTrace();
                }
        }
    }
    
}
