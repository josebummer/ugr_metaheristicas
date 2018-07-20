package calculoPesos;
//This class implements Metric interface and is used to calculate EuclideanDistance
public class EuclideanDistanceGreedyPesos implements Metric {

	@Override
	//L2
	public double getDistance(Record s, Record e,double[] pesos) {
		assert s.attributes.length == e.attributes.length : "s and e are different types of records!";
		int numOfAttributes = s.attributes.length;
		double sum2 = 0;
		
		for(int i = 0; i < numOfAttributes; i ++){
                    sum2 += (pesos[i]*(s.attributes[i] - e.attributes[i]))*(pesos[i]*(s.attributes[i] - e.attributes[i]));
                }
		
		return sum2;
	}

}
