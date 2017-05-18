/**
 * 
 */
package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author berant89
 * Dictionary class that loads a given file and stores it into a hashset
 */
public class WordDictionary extends HashSet<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4398697275845357848L;
	//Create logger to monitor behaviour of class
	private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
	private static final int fDLDistance = 2; //The allowable distance between two words
	
	/**
	 * Class constructor
	 * @param path The file path of the plain text file to load
	 */
	public WordDictionary(String path) {
		super();
		loadDictionary(path);
	}
	
	/**
	 * Loads the text file containing words into the hashset
	 * @param path The file path of the plain text file to load
	 */
	private void loadDictionary(String path) {
		//Attempt to open the file and read its contents
		try {
			super.addAll(Files.readAllLines(Paths.get(path)));
			LOGGER.log(Level.INFO, "Dictionary loaded. Entries: " + Integer.toString(this.size()));
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	/**
	 * Returns a list of possible words based on the provided misspelt word
	 * @param word A string containing the misspelt word
	 * @return A List of strings containing possible words
	 */
	public List<String> wordSuggester(String word) {
		LOGGER.log(Level.INFO, "Begin wordSuggester using Damerau-Levenshtein Distance algorithm for '" + word +"'");
		int dlDistance; //The calculated distance
		List<DLEntry<String, Integer>> alternates = new ArrayList<>(); //List of possible words and their distance
		for(String d : this) {
			dlDistance = DamerauLevenshteinDistance.computeDistance(d, word);
			/**
			 * Only add words with a distance less than has been set. This determines
			 * how many dictionary words will be associated to the wrong word
			 */
			if(dlDistance<fDLDistance) {
				alternates.add(new DLEntry<String, Integer>(d, dlDistance));
			}
		}
		
		/**
		 * Sort the List by distance then in alphabetical order
		 */
		Collections.sort(alternates, new Comparator<DLEntry<String, Integer>>() {

			@Override
			public int compare(DLEntry<String, Integer> o1, DLEntry<String, Integer> o2) {
				int iComp = o1.getValue().compareTo(o2.getValue()); //Compare the distances
				if(iComp != 0)
				{
					return iComp;
				}
				else
				{
					return o1.getKey().compareTo(o2.getKey()); //Compare the strings
				}
			}
		});
        
		/**
		 * After the sort, extract just the words into a new list
		 */
        List<String> orderedWords = new ArrayList<>();
        for (Entry<String, Integer> entry : alternates) {
        	orderedWords.add(entry.getKey());
        }
		
		return orderedWords;
	}
}
