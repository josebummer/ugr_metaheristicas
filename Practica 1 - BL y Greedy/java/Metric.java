package calculoPesos;
//basic metric interface

public interface Metric {
	double getDistance(Record s, Record e,double[] pesos);
}
