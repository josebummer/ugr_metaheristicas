package practica2mh;
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

            
            //AGG-BLX
            System.out.println("Comenzando AGG-BLX");
            AGG[][] aggs = new AGG[5][3];
            double[][][] pesosAGG = new double[5][3][];
            knn[][] knnsAGG = new knn[5][3];

            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            aggs[i][j] = new AGG(30, 0.7, 0.001, 0,trainingSet[i][j].get(0).attributes.length,rnd[i][j]);
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAGG[i][j] = aggs[i][j].AGG(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AGG-BLX");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAGG[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAGG[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAGG[i][j].knn("AGG-BLX",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AGG-BLX terminada");
                    });
            System.out.println("AGG-BLX terminado");
            
            
            //AGG-CA
            System.out.println("Comenzando AGG-CA");
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            aggs[i][j] = new AGG(30, 0.7, 0.001, 1,trainingSet[i][j].get(0).attributes.length,rnd[i][j] );
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAGG[i][j] = aggs[i][j].AGG(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AGG-CA");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAGG[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAGG[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAGG[i][j].knn("AGG-CA",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AGG-CA terminada");
                    });
            System.out.println("AGG-CA terminado");
            
            
            //AGE-BLX
            System.out.println("Comenzando AGE-BLX");
            AGE[][] ages = new AGE[5][3];
            double[][][] pesosAGE = new double[5][3][];
            knn[][] knnsAGE = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            ages[i][j] = new AGE(30, 1.0, 0.001, 0,trainingSet[i][j].get(0).attributes.length ,rnd[i][j]);
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAGE[i][j] = ages[i][j].AGE(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AGE-BLX");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAGE[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAGE[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAGE[i][j].knn("AGE-BLX",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AGE-BLX terminada");
                    });
            System.out.println("AGE-BLX terminado");
            
            
            //AGE-CA
            System.out.println("Comenzando AGE-CA");
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                          for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                          }

                        for(int j = 0; j < 3; j++){
                            ages[i][j] = new AGE(30, 1.0, 0.001, 1,trainingSet[i][j].get(0).attributes.length,rnd[i][j] );
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAGE[i][j] = ages[i][j].AGE(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AGE-CA");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAGE[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAGE[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAGE[i][j].knn("AGE-CA",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AGE-CA terminada");
                    });
            System.out.println("AGE-CA terminado");
            
            
            //AM-10-1.0
            System.out.println("Comenzando AM-10-1.0");
            AM_10_1[][] ams = new AM_10_1[5][3];
            double[][][] pesosAM = new double[5][3][];
            knn[][] knnsAM = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            ams[i][j] = new AM_10_1(10, 0.7, 0.001, 0,trainingSet[i][j].get(0).attributes.length ,rnd[i][j]);
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAM[i][j] = ams[i][j].AM_10_1(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AM-10-1.0-BLX");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAM[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAM[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAM[i][j].knn("AM-10-1.0-BLX",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AM-10-1.0-BLX terminada");
                    });
            System.out.println("AM-10-1.0-BLX terminado");

            //AM-10-0.1
            System.out.println("Comenzando AM-10-0.1");
            AM_10_01[][] ams01 = new AM_10_01[5][3];
            double[][][] pesosAM01 = new double[5][3][];
            knn[][] knnsAM01 = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            ams01[i][j] = new AM_10_01(10, 0.7, 0.001, 0,trainingSet[i][j].get(0).attributes.length,rnd[i][j]);
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAM01[i][j] = ams01[i][j].AM_10_01(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AM-10-0.1-BLX");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAM01[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAM01[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAM01[i][j].knn("AM-10-0.1-BLX",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AM-10-0.1-BLX terminada");
                    });
            System.out.println("AM-10-0.1-BLX terminado");


            //AM-10-0.1mej
            System.out.println("Comenzando AM-10-0.1mej");
            AM_10_01mej[][] ams01m = new AM_10_01mej[5][3];
            double[][][] pesosAM01m = new double[5][3][];
            knn[][] knnsAM01m = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            ams01m[i][j] = new AM_10_01mej(10, 0.7, 0.001, 0,trainingSet[i][j].get(0).attributes.length,rnd[i][j]);
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAM01m[i][j] = ams01m[i][j].AM_10_01mej(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AM-10-0.1mej-BLX");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAM01m[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAM01m[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAM01m[i][j].knn("AM-10-0.1mej-BLX",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AM-10-0.1mej-BLX terminada");
                    });
            System.out.println("AM-10-0.1mej-BLX terminado");
            
            /////////////////////////////////////////////////////////////EXTRA///////////////////////////////////////////////////////////
            
            //AM-1-0.1
            System.out.println("Comenzando AM-1-0.1");
            AM_1_01[][] ams1 = new AM_1_01[5][3];
            double[][][] pesosAM1 = new double[5][3][];
            knn[][] knnsAM1 = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            ams1[i][j] = new AM_1_01(10, 0.7, 0.001, 0,trainingSet[i][j].get(0).attributes.length,rnd[i][j]);
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAM1[i][j] = ams1[i][j].AM_1_01(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AM-1-0.1-BLX");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAM1[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAM1[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAM1[i][j].knn("AM-1-0.1-BLX",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AM-1-0.1-BLX terminada");
                    });
            System.out.println("AM-1-0.1-BLX terminado");


            //AM-1-1.0
            System.out.println("Comenzando AM-1-1.0");
            AM_1_1[][] ams11 = new AM_1_1[5][3];
            double[][][] pesosAM11 = new double[5][3][];
            knn[][] knnsAM11 = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            ams11[i][j] = new AM_1_1(10, 0.7, 0.001, 0,trainingSet[i][j].get(0).attributes.length ,rnd[i][j]);
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAM11[i][j] = ams11[i][j].AM_1_1(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AM-1-1.0-BLX");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAM11[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAM11[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAM11[i][j].knn("AM-1-1.0-BLX",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AM-1-1.0-BLX terminada");
                    });
            System.out.println("AM-1-1.0-BLX terminado");
            
            
            //AM-1-0.1mej
            System.out.println("Comenzando AM-1-0.1mej");
            AM_1_01mej[][] ams1m = new AM_1_01mej[5][3];
            double[][][] pesosAM1m = new double[5][3][];
            knn[][] knnsAM1m = new knn[5][3];
            
            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                        for(int j = 0; j < 3; j++){
                            rnd[i][j] = new Random(3395+(i*3+j));
                        }

                        for(int j = 0; j < 3; j++){
                            ams1m[i][j] = new AM_1_01mej(10, 0.7, 0.001, 0,trainingSet[i][j].get(0).attributes.length,rnd[i][j]);
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosAM1m[i][j] = ams1m[i][j].AM_1_01mej(trainingFile[j], testingFile[j], K, 2, trainingSet[i][j], ptest,"AM-1-0.1mej-BLX");
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }   catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            });

                        for(int j = 0; j < 3;j++){
                            try {
                                knnsAM1m[i][j] = new knn(trainingFile[j],testingFile[j],K,2,trainingSet[i][j],testingSet[i][j],pesosAM1m[i][j],ptest,false);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsAM1m[i][j].knn("AM-1-0.1mej-BLX",true);
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-AM-1-0.1mej-BLX terminada");
                    });
            System.out.println("AM-1-0.1mej-BLX terminado");
            
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
