package Practica3MH;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;


public class main {
	public static void main(String[] args) throws IOException{
            knn[] knns = new knn[3];

            int NumOfAttributes = 0;
            String[] trainingFile = new String[3];
            String[] testingFile = new String[3];
            trainingFile[0] = "src/input/spectf-heart.arff";
            testingFile[0] = "src/input/spectf-heart.arff";
            trainingFile[1] = "src/input/ozone-320.arff";
            testingFile[1] = "src/input/ozone-320.arff";
            trainingFile[2] = "src/input/parkinsons.arff";
            testingFile[2] = "src/input/parkinsons.arff";
            ArrayList<TrainRecord>[] particionesheart = FileManager.readFile(trainingFile[0]);
            ArrayList<TrainRecord>[] particionesozone = FileManager.readFile(trainingFile[1]);
            ArrayList<TrainRecord>[] particionesparkinson = FileManager.readFile(trainingFile[2]);
            ArrayList<TrainRecord>[][] trainingSet = new ArrayList[5][3];
            ArrayList<TestRecord>[][] testingSet = new ArrayList[5][3];
            final int K = 1;
            Random rnd[][] = new Random[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        trainingSet[i][0] = getTrainRecords(particionesheart, i);
                        testingSet[i][0] = getTestRecords(particionesheart, i);

                        trainingSet[i][1] = getTrainRecords(particionesozone, i);
                        testingSet[i][1] = getTestRecords(particionesozone, i);

                        trainingSet[i][2] = getTrainRecords(particionesparkinson, i);
                        testingSet[i][2] = getTestRecords(particionesparkinson, i); 
                    });

            
            //Enfriamiento Simulado
            System.out.println("Comenzando Enfriamiento Simulado");
            ES[][] efs = new ES[5][3];
            double[][][] pesosef = new double[5][3][];
            knn[][] knnsef = new knn[5][3];

            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            try {
                                efs[i][j] = new ES(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],ptest,rnd[i][j]);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                            try {
                                pesosef[i][j] = efs[i][j].ef();
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsef[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosef[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsef[i][j].knn("ES",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-ES terminada");
                    });
            System.out.println("Enfriamiento Simulado terminado");


            //ILS
            System.out.println("Comenzando ILS");
            ILS[][] ils = new ILS[5][3];
            double[][][] pesosILS = new double[5][3][];
            knn[][] knnsILS = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            try {
                                ils[i][j] = new ILS(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],ptest,rnd[i][j]);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosILS[i][j] = ils[i][j].ils();
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsILS[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosILS[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsILS[i][j].knn("ILS",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-ILS terminada");
                    });
            System.out.println("ILS terminado");
            

            //Evolucion Diferencial DE/Rand/1
            System.out.println("Comenzando DE-R-1");
            DE[][] de1 = new DE[5][3];
            double[][][] pesosDE1 = new double[5][3][];
            knn[][] knnsDE1 = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            try {
                                de1[i][j] = new DE(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],ptest,rnd[i][j],50,0.5,0.5);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosDE1[i][j] = de1[i][j].de(0);
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsDE1[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosDE1[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsDE1[i][j].knn("DE-R-1",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-DE-R-1 terminada");
                    });
            System.out.println("DE-R-1 terminado");

            //Evolucion Diferencial DE/current-to-best/1
            System.out.println("Comenzando DE-C-1");
            DE[][] de2 = new DE[5][3];
            double[][][] pesosDE2 = new double[5][3][];
            knn[][] knnsDE2 = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            try {
                                de2[i][j] = new DE(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],ptest,rnd[i][j],50,0.5,0.5);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosDE2[i][j] = de2[i][j].de(1);
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsDE2[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosDE2[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsDE2[i][j].knn("DE-C-1",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-DE-C-1 terminada");
                    });
            System.out.println("DE-C-1 terminado");
            
	}
        
        public static ArrayList<TestRecord> getTestRecords(ArrayList<TrainRecord>[] particiones, int ptest){
            ArrayList<TestRecord> testingSet = new ArrayList<>();
            for(int i = 0; i < particiones[ptest].size();i++){
                testingSet.add(new TestRecord(particiones[ptest].get(i).attributes, particiones[ptest].get(i).classLabel));
            }
            
            return testingSet;
        }
        
        public static ArrayList<TrainRecord> getTrainRecords(ArrayList<TrainRecord>[] particiones, int ptest){
            ArrayList<TrainRecord> trainingSet = new ArrayList<>();
            for(int i = 0; i < particiones.length; i++){
                if(i != ptest) trainingSet.addAll(particiones[i]);
            }
            return trainingSet;
        }
        
        public static double F(double tasa_clas, double tasa_red){
            return (0.5*tasa_clas+0.5*tasa_red);
        }
}
