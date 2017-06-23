package settings;

public enum FieldWeight {

	LABEL ("label", 1.0),
	EXACT_SYNONYM ("exactSynonym", 0.8),
	OTHER_SYNONYM ("otherSynonym", 0.5),
	DEFINITION ("definition", 0.2);

	String label;
	double weight;


	FieldWeight(String s, double w)
	{
		label = s;
		weight = w;

	}


	
	public double getWeight()
	{
		return weight;
	}

	public String getWeightLabel() {return label;}

    public FieldWeight getFieldType(){return this;}
	
    public String toString()
    {
    	return label;
    }
	
	
}
