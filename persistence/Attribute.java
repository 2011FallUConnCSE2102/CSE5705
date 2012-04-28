
package persistence;

/* An Attribute is equivalent to a parameter.  It is the columns of the database, or the evaluation values of the state.
 * An Attribute holds a value that is potentially unique from each other Attribute, so it is important to generalize
 * the Attribute range with Categories.  The important value here is the Entropy of the Attribute.  Is is calculated
 * by adding the product of each of it's Category's Weight and Info function.  The lower the Entropy, the better it is
 * for estimation. (Information Gain = 1 - Entropy)
 * 
 * Note:  "Result" means the Win/Lose field: the target column we are trying to gain information on.
 */
public class Attribute {
	
	public String Name;
	public Category[] attrCats;
	public double Entropy = 0;
	public int Size = 0;
	
	public Attribute(String name, Category[] categories)
	{
		Name = name;
		attrCats = categories;
	}
	public double calculateAttrEntropy()
	{
		//Iterate through the Attribute's categories and calculate their Weight and Info function.
		for (int i = 0; i < attrCats.length; i++)
		{
			attrCats[i].calculateStatistics(Size);
			//After calculation, use it for Entropy calculation
			Entropy = Entropy + (attrCats[i].getWeight() * attrCats[i].getInfo());
		}
		return Entropy;
	}
}
