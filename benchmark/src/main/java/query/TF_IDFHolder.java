package query;

/**
 * Created by Anila Sahar Butt.
 */
public class TF_IDFHolder {

    private double tf=0;
    private double idf=0;
    private double tf_idf=0;


    public TF_IDFHolder(){
        this.tf= 0;
        this.idf= 0;
        this.tf_idf= 0;
    }

    public TF_IDFHolder(double tf, double idf, double tfIdf){
        this.tf= tf;
        this.idf= idf;
        this.tf_idf= tfIdf;
    }

    public void setTF(double tf){
        this.tf=tf;
    }

    public void setIDF(double idf){
        this.idf=idf;
    }

    public void setTF_IDF(double tfIdf){
        this.tf_idf=tfIdf;
    }

    public double getTF(){
        return this.tf;
    }

    public double getIDF(){
        return this.idf;
    }

    public double getTF_IDF(){
        return this.tf_idf;
    }
}
