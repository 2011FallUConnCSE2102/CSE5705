package persistence;

public class Learner {

	Attribute[] attributes = new Attribute[DBHandler.NUMPARAMS];
	int[][] dbResults;

	public double[] entropyValues = new double[DBHandler.NUMPARAMS];
	
	public Learner()
	{
		
	}
	
	public void calculateAllEntropy(DBHandler handler)
	{
		attributes = handler.buildAttributeCollection();
		for (int i = 0; i < attributes.length; i++)
		{
			entropyValues[i] = attributes[i].calculateAttrEntropy();
		}
	}
}
