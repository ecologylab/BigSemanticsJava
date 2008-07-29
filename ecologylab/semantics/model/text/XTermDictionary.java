package ecologylab.semantics.model.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

//import org.jvyaml.YAML;

public class XTermDictionary {

	private static HashMap<String, Double> frequencyList = null;
	private static HashMap<String,XTerm> dictionary = null;
	
	public static double 	lowestFrequency  = Double.MAX_VALUE, 
							highestFrequency = Double.MIN_VALUE, 
							lowestIDF  = Double.MAX_VALUE, 
							highestIDF = Double.MIN_VALUE;
	
	
	/**
	 * Loads the dictionary term/frequency map from a YAML file, 
	 * then generates the IDF Mapping.
	 * @param yamlTermFrequency file from which to load the term/frequency map
	 * @throws FileNotFoundException
	 */
	public static void createDictionary(File yamlTermFrequency) throws FileNotFoundException {
		loadFromYaml(yamlTermFrequency);
		generateDictionary();
	}
	
	/**
	 * Modifies the passed in Vector by pair-wise multiplying it
	 * with the inverse document frequency vector 
	 * @param v The Vector to change to tf*IDF weighting.
	 */
	
	public static boolean contains(String stem) {
		return dictionary.containsKey(stem);
	}
	
	public static void main(String[] arg) throws Exception {
		createDictionary(new File("/Users/jmole/frequency.yaml"));
//		for (String stem : frequencyList.keySet())
//			System.out.println(stem + ": " + frequencyList.get(stem));
//		for (String stem : inverseDocumentFrequency.indexSet())
//			System.out.println(stem + ": " + inverseDocumentFrequency.get(stem));
//		
		java.util.ArrayList<String> stems = new java.util.ArrayList<String>();
		for (String s : frequencyList.keySet())
			stems.add(s);
		
		XVector<String>[] vectors = new XVector[10000];
		for (int i=0; i<vectors.length; i++) {
			vectors[i] = new XVector<String>();
			for (int j=0; j<20; j++) {
				int rand = (int)Math.rint(Math.random()*(stems.size()-1));
				vectors[i].add(stems.get(rand), Math.random());
			}
		}
		System.out.println("Vector length = 20");
		long time = System.nanoTime();
		for (XVector v : vectors) {
			int rand = (int)Math.rint(Math.random()*(vectors.length-1));
			v.dot(vectors[rand]);
		}
		time = System.nanoTime() - time;
		System.out.println("Time for 10000 random dot product calculations: " + time/1000/1000.0 + "ms.");
				
		time = System.nanoTime();
		for (XVector v : vectors) {
			int rand = (int)Math.rint(Math.random()*(vectors.length-1));
			v.add(vectors[rand]);
		}
		time = System.nanoTime() - time;
		System.out.println("Time for 10000 random add calculations: " + time/1000/1000.0 + "ms.");
		
		time = System.nanoTime();
		for (XVector v : vectors) {
			int rand = (int)Math.rint(Math.random()*(vectors.length-1));
			v.multiply(vectors[rand]);
		}
		time = System.nanoTime() - time;
		System.out.println("Time for 10000 random multiply calculations: " + time/1000/1000.0 + "ms.");
		
		
	}
	
	@SuppressWarnings("unchecked")
	private static void loadFromYaml(File yamlTermFrequency) throws FileNotFoundException {
		//frequencyList = (HashMap)YAML.load(new FileReader(yamlTermFrequency));
		for(String stem : frequencyList.keySet()) {
			
			double freq = (double)frequencyList.get(stem);
			if 		(freq < lowestFrequency)  lowestFrequency  = freq;
			else if (freq > highestFrequency) highestFrequency = freq;
		}
	}

	private static void generateDictionary() {
		dictionary = new HashMap<String, XTerm>(frequencyList.size());
		int corpusSize = frequencyList.size();
		for(String stem : frequencyList.keySet()) {
			double idf = Math.log(highestFrequency/frequencyList.get(stem));
			dictionary.put(stem, new XTerm(stem,idf));
			if 		(idf < lowestIDF)  lowestIDF  = idf;
			else if (idf > highestIDF) highestIDF = idf;
		}
	}

}