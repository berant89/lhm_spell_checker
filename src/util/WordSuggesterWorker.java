/**
 * 
 */
package util;

import java.util.Set;

import gui.MainPanel;

/**
 * @author berant89
 * The class that will execute the worderSuggester function and store
 * the results into the MainPanel Concurrent Hashmap that it was created from
 */
public class WordSuggesterWorker implements Runnable {
	private Thread t; //The thread to execute the runnable
	private Set<String> fWorkerWords; //The set of the words the worker will process
	private MainPanel fPanel; //The panel the worker was created from. Used to get the hashmap

	/**
	 * Constructor
	 * @param words A Set containing Strings that will be processed
	 * @param pPanel The MainPanel the worker is created from. Contains the HashMap
	 */
	public WordSuggesterWorker(Set<String> words, MainPanel	pPanel) {
		super();
		fWorkerWords = words;
		fPanel = pPanel;
	}
	
	/**
	 * Adds a given word to the class Set
	 * @param word A String that will be added
	 */
	public void addSetElement(String word)
	{
		fWorkerWords.add(word);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//Get the dictionary object from the Panel
		WordDictionary wordDict = fPanel.getWordDictionary();
		for(String word : fWorkerWords) {
			/**
			 * Run the suggester if the word doesn't exist in the dictionary,
			 * then store the result into the concurrent hashmap of the parent panel
			 */
			if(!wordDict.contains(word.toLowerCase())) {
				fPanel.addElementToCHMap(word, wordDict.wordSuggester(word));
			}
		}
		fWorkerWords.clear(); //Clear the word set for the next run
	}
	
	/**
	 * Prepares the thread and runs it.
	 */
	public void start() {
		if(t == null)
		{
			t = new Thread(this);
			t.start();
			t = null;
		}
	}

}
