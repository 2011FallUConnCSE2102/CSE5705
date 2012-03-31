package valuation;

public class WeightedFeature {
	Weight weight;
	Feature feature;
	public WeightedFeature(){
		
	}
public WeightedFeature(Weight w){
		weight = w;
	}
public WeightedFeature(Feature f){
	feature = f;
}
public WeightedFeature(Weight w, Feature f){
	weight = w;
	feature = f;
}
public Weight getWeight(){
	return weight;
}
public Feature getFeature(){
	return feature;
}
public void setWeight(Weight w){
	weight = w;
}
public void setFeature(Feature f){
	feature = f;
}


}
