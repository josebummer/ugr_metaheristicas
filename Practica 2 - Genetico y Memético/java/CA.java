package practica2mh;
//This class implements Metric interface and is used to calculate EuclideanDistance

import java.util.ArrayList;
import java.util.Random;

public class CA implements Cruce {

	@Override
	public ArrayList<double[]> cruce(ArrayList<double[]> P,double Pc, Random rnd) {

            ArrayList<double[]> res = new ArrayList<>();

            int parejas = (int)(Pc*(double)P.size());
            int genes = P.get(0).length;

            for( int i = 0; i < parejas; i+=2){
                double des1[] = new double[genes];
                double des2[] = new double[genes];
                
                for(int j = 0; j < genes; j++){
                    des1[j] = (P.get(i)[j] + P.get(i+1)[j])/2;
                    
                    des1[j] = (des1[j] < 0.0)?0.0:des1[j];
                    des1[j] = (des1[j] > 1.0)?1.0:des1[j];
                }
                double alpha = rnd.nextDouble();
                for(int j = 0; j < genes; j++){
                    des2[j] = alpha*P.get(i)[j] + (1-alpha)*P.get(i+1)[j];
                    
                    des2[j] = (des2[j] < 0.0)?0.0:des2[j];
                    des2[j] = (des2[j] > 1.0)?1.0:des2[j];
                }
                
                res.add(des1.clone());
                res.add(des2.clone());
            }
            
            int ini = P.size() - (P.size() - res.size());
            for(int i = ini; i < P.size(); i++){
                res.add(P.get(i).clone());
            }
            
            return res;
	}
}
