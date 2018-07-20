package Practica3MH;
//Basic Record class
public class Record implements Cloneable {
	double[] attributes;
	int classLabel;
	
	Record(double[] attributes, int classLabel){
		this.attributes = attributes;
		this.classLabel = classLabel;
	}
        
        @Override
        public Object clone() throws CloneNotSupportedException{
            Record clone=(Record)super.clone();
            
            clone.attributes = this.attributes.clone();
            
            return clone;
        }
}
