package practica2mh;
//This class implements Metric interface and is used to calculate EuclideanDistance

import java.util.ArrayList;
import java.util.Random;

public class BLX implements Cruce {

	@Override
	public ArrayList<double[]> cruce(ArrayList<double[]> P,double Pc, Random rnd) {

            ArrayList<double[]> res = new ArrayList<>();

            int parejas = (int)(Pc*(double)P.size());
            int genes = P.get(0).length;

            for( int i = 0; i < parejas; i+=2){
                double des1[] = new double[genes];
                double des2[] = new double[genes];

                for(int j = 0; j < genes; j++){
                    double cmax = (P.get(i)[j] > P.get(i+1)[j])?P.get(i)[j]:P.get(i+1)[j];
                    double cmin = (P.get(i)[j] < P.get(i+1)[j])?P.get(i)[j]:P.get(i+1)[j];
                    double I = cmax-cmin;
                    double min = cmin-I*0.3;
                    double max = cmax+I*0.3;

                    des1[j] = min+(max-min)*rnd.nextDouble();
                    des2[j] = min+(max-min)*rnd.nextDouble();
                    
                    des1[j] = (des1[j] < 0.0)?0.0:des1[j];
                    des1[j] = (des1[j] > 1.0)?1.0:des1[j];
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
