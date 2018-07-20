package calculoPesos;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;


public class main {
	
	public static void main(String[] args) throws IOException{
            knn[] knns = new knn[3];
            
            ArrayList<TrainRecord>[] trainingSet = new ArrayList[3];
            ArrayList<TestRecord>[] testingSet = new ArrayList[3];
            double[] pesosozone = null;
            double[] pesosparkinson = null;
            double[] pesosheart = null;
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
            final int K = 1;
            
            //1-NN
            for(int i = 0; i < 5; i++){
                
                int ptest = i;
                trainingSet[0] = getTrainRecords(particionesheart, ptest);
                testingSet[0] = getTestRecords(particionesheart, ptest);
                NumOfAttributes = testingSet[0].get(0).attributes.length;
                if(i == 0){
                    pesosheart = new double[NumOfAttributes];
                    Arrays.fill(pesosheart, 1.0); 
                }
                knns[0] = new knn(testingFile[0],trainingFile[0],K,2,trainingSet[0],testingSet[0],pesosheart,ptest);
                
                trainingSet[1] = getTrainRecords(particionesozone, ptest);
                testingSet[1] = getTestRecords(particionesozone, ptest);
                NumOfAttributes = testingSet[1].get(0).attributes.length;
                if(i == 0){
                    pesosozone = new double[NumOfAttributes];
                    Arrays.fill(pesosozone, 1.0); 
                }
                knns[1] = new knn(testingFile[1],trainingFile[1],K,2,trainingSet[1],testingSet[1],pesosozone,ptest);

                trainingSet[2] = getTrainRecords(particionesparkinson, ptest);
                testingSet[2] = getTestRecords(particionesparkinson, ptest);
                NumOfAttributes = testingSet[2].get(0).attributes.length;
                if(i == 0){
                    pesosparkinson = new double[NumOfAttributes];
                    Arrays.fill(pesosparkinson, 1.0); 
                }
                knns[2] = new knn(testingFile[2],trainingFile[2],K,2,trainingSet[2],testingSet[2],pesosparkinson,ptest);

                IntStream.range(0, 3)
                        .parallel()
                        .forEach(k -> {
                    try {
                        knns[k].knn("1-NN");
                    } catch (IOException ex) {
                        Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                        });
            } 
            
            System.out.println("1-NN terminado");
            
            //Greedy
            double[][] pesosini = new double[3][];
            pesosini[0] = new double[pesosheart.length];
            pesosini[1] = new double[pesosozone.length];
            pesosini[2] = new double[pesosparkinson.length];
            Arrays.fill(pesosini[0], 0);
            Arrays.fill(pesosini[1], 0);
            Arrays.fill(pesosini[2], 0);
            Greedy greedys[] = new Greedy[3];
            double[][] pesos = new double[3][];
           
            
            for(int i = 0; i < 5;i++){
                int ptest = i;
                
                trainingSet[0] = getTrainRecords(particionesheart, ptest);
                testingSet[0] = getTestRecords(particionesheart, ptest);
                
                trainingSet[1] = getTrainRecords(particionesozone, ptest);
                testingSet[1] = getTestRecords(particionesozone, ptest);
                
                trainingSet[2] = getTrainRecords(particionesparkinson, ptest);
                testingSet[2] = getTestRecords(particionesparkinson, ptest);
                
                for(int j = 0; j < 3; j++){
                    greedys[j] = new Greedy(trainingFile[j], 1, 2, trainingSet[j], pesosini[j], ptest);
                }
                
                 IntStream.range(0, 3)
                    .parallel()
                    .forEach(j -> {
                try {
                    pesos[j] = greedys[j].Greedy();
                } catch (IOException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                }
                    });
                 
                 for(int j = 0; j < 3;j++){
                    knns[j] = new knn(testingFile[j],trainingFile[j],1,2,trainingSet[j],testingSet[j],pesos[j],ptest);
                }
                
                IntStream.range(0, 3)
                        .parallel()
                        .forEach(j -> {
                    try {
                        knns[j].knn("Greedy");
                    } catch (IOException ex) {
                        Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                        });
            }
            System.out.println("Greedy terminado");

            //BL
            BL[][] bls = new BL[5][3];
            ArrayList<TrainRecord>[][] trainingSetBL = new ArrayList[5][3];
            ArrayList<TestRecord>[][] testingSetBL = new ArrayList[5][3];
            double[][][] pesosBL = new double[5][3][];
            knn[][] knnsBL = new knn[5][3];
            Random rnd = new Random(3395);
            //incializamos los pesos
            for(int i = 0;i < pesosozone.length; i++){
                pesosozone[i] = rnd.nextDouble();
            }
            for(int i = 0;i < pesosheart.length; i++){
                pesosheart[i] = rnd.nextDouble();
            }
            for(int i = 0;i < pesosparkinson.length; i++){
                pesosparkinson[i] = rnd.nextDouble();
            }
            pesosini[0] = pesosheart;
            pesosini[1] = pesosozone;
            pesosini[2] = pesosparkinson;

            IntStream.range(0, 5)
                    .parallel()
                    .forEach(i -> {
                        int ptest = i;
                
                        trainingSetBL[i][0] = getTrainRecords(particionesheart, ptest);
                        testingSetBL[i][0] = getTestRecords(particionesheart, ptest);

                        trainingSetBL[i][1] = getTrainRecords(particionesozone, ptest);
                        testingSetBL[i][1] = getTestRecords(particionesozone, ptest);

                        trainingSetBL[i][2] = getTrainRecords(particionesparkinson, ptest);
                        testingSetBL[i][2] = getTestRecords(particionesparkinson, ptest);

                        for(int j = 0; j < 3; j++){
                            bls[i][j] = new BL(trainingFile[j], K, 2, trainingSetBL[i][j], pesosini[j], ptest, rnd);
                        }

                        IntStream.range(0, 3)
                            .parallel()
                            .forEach(j -> {
                        try {
                            pesosBL[i][j] = bls[i][j].BL();
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                            });

                        for(int j = 0; j < 3;j++){
                            knnsBL[i][j] = new knn(testingFile[j],trainingFile[j],K,2,trainingSetBL[i][j],testingSetBL[i][j],pesosBL[i][j],ptest);
                        }

                        IntStream.range(0, 3)
                                .parallel()
                                .forEach(j -> {
                            try {
                                knnsBL[i][j].knn("BL");
                            } catch (IOException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                                });
                        System.out.println("Particion "+i+"-BL terminada");
                    });
            System.out.println("BL terminado");
            
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
