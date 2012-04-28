package persistence;

/* A category is a discrete value in which an Attribute can take.  In other words, if an Attribute
 * represents a boolean datatype, then there are two categories: True and False.  A Category has two
 * important values at the end of its life cycle: Weight and Info.  These are used to calculate the
 * Entropy of the Category's parent Attribute.
 */

public class Category {
	
	public int Value = 0;	//Value in which the category represents
	public int NumEntries = 0;	//The number of entries associated with this category
	public int NumPos = 0;	//The number of positive Results associated with this category
	public int NumNeg = 0;	//The number of negative Results associated with this cateogry
	float weight = 0; //The weight of this cateogry (The probability that an entry will fall in this category)
	float prbPos = 0; //The probability that, given an entry from this category, the Result value for this entry is positive.
	float prbNeg = 0;  //The probability that, given an entry from this category, the Result value is negative.	
	double info = 0; //The calculated information associated with this Category: I(V)
	
	public Category(int categoryValue)
	{
		Value = categoryValue;
	}
	
	//Calculates based on: catEntries (# of entries belonging in this category), totalEntries (# entries in the Result column), 
	//posEntries (# of entries which correlate to a 1 in the Result column), and negEntries (# of entries which correlate to 0 in Result column)
	public void calculateStatistics(int catEntries, int totalEntries, int posEntries, int negEntries)
	{
		weight = (float)catEntries/totalEntries;
		prbPos = (float)posEntries/catEntries;
		prbNeg = (float)negEntries/catEntries;
		
		double log10a = Math.log10(prbPos);
		double log10Denoma = Math.log10(2);
		double log2a = log10a/log10Denoma;
		if (Double.isInfinite(log2a))
			log2a = 0;
		
		double log10b = Math.log10(prbNeg);
		double log10Denomb = Math.log10(2);
		double log2b = log10b/log10Denomb;
		if (Double.isInfinite(log2b))
			log2b = 0;
		
		info = -1 * ((prbPos*log2a) + (prbNeg * log2b));
	}
	
	//Calculates if the NumEntries, NumPos, and NumNeg values were already found.
	public void calculateStatistics(int totalEntries)
	{
		weight = (float)NumEntries/totalEntries;
		prbPos = (float)NumPos/NumEntries;
		prbNeg = (float)NumNeg/NumEntries;
		
		double log10a = Math.log10(prbPos);
		double log10Denoma = Math.log10(2);
		double log2a = log10a/log10Denoma;
		if (Double.isInfinite(log2a))
			log2a = 0;
		
		double log10b = Math.log10(prbNeg);
		double log10Denomb = Math.log10(2);
		double log2b = log10b/log10Denomb;
		if (Double.isInfinite(log2b))
			log2b = 0;
		
		info = -1 * ((prbPos*log2a) + (prbNeg * log2b));
		/*info = -1 * 
				((prbPos * (Math.log10(prbPos)/Math.log10(2))) + 
				(prbNeg * (Math.log10(prbNeg)/Math.log10(2))));*/
		int x = 0;
	}
	public float getWeight()
	{
		return weight;
	}
	public double getInfo()
	{
		return info;
	}
}
