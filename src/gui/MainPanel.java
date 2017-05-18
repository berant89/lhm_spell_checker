/**
 * 
 */
package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import util.WordDictionary;
import util.WordSuggesterWorker;

/**
 * @author berant89
 *
 */
public class MainPanel extends JPanel {
	private JTextArea fTextEditor; //Where the loaded text will be stored
	//Buttons
	private JButton fLoadDictButt; //Button to load the dictionary file
	private JButton fLoadTextButt; //Button to load the text file
	private JButton fSpellCheckButt; //Button to run the spell checker
	private JButton fSaveTextButt; //Button to save the text in the text area
	private JButton fReplaceWordButt; //Button to replace words in the text area
	
	private JList<String> fErrorWords; //Contains list of words that are misspelt
	private JList<String> fSuggestWords; //Contains list of suggested words for a misspelt word
	
	private ConcurrentHashMap<String, List<String>> fSuggestionsCon; //
	private List<WordSuggesterWorker> fWorkers; //List of workers
	private static final int fNumWorkers = 6; //Number of workers to run
	private WordDictionary fWordDict;

	/**
	 * 
	 */
	private static final long serialVersionUID = -6717737431929246686L;

	/**
	 * Constructor
	 */
	public MainPanel() {
		super(new BorderLayout());
		fSuggestionsCon = new ConcurrentHashMap<>();
		fWorkers = new ArrayList<>();
		//Prepare the new workers
		for(int i = 0; i < fNumWorkers; i++) {
			fWorkers.add(new WordSuggesterWorker(new HashSet<>(), this));
		}
		addControls();
		addListeners();
	}
	
	/**
	 * Adds the provided key and value to the concurrent HashMap
	 * @param key A String containing the error word
	 * @param value A List of strings containing the suggested words
	 */
	public void addElementToCHMap(String key, List<String> value) {
		fSuggestionsCon.put(key, value);
	}
	
	/**
	 * Retrieves the WordDictionary object
	 * @return A WordDictionary object containing a set of words
	 */
	public WordDictionary getWordDictionary() {
		return fWordDict;
	}
	
	/**
	 * Adds the controls to the panel and sets their location on the BorderLayout
	 */
	private void addControls() {
		//Create panel for the northern slot of the border
		JPanel northPanel = new JPanel();
		northPanel.add(fLoadDictButt = new JButton("Load Dictionary"));
		northPanel.add(fLoadTextButt = new JButton("Load Text File"));
		northPanel.add(fSpellCheckButt = new JButton("Spell Check"));
		northPanel.add(fSaveTextButt = new JButton("Save Text File"));
		add(northPanel, BorderLayout.NORTH);
		
		/**
		 * Add the JTextArea into a scrollpane then insert it into the center.
		 * Disable horizontal scroll and enable vertical. Enable JTextArea
		 * word and line wraps.
		 */
		add(new JScrollPane(fTextEditor = new JTextArea(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		fTextEditor.setWrapStyleWord(true);
		fTextEditor.setLineWrap(true);
		
		//Create panel for the southern slot of the border
		JPanel southPanel = new JPanel();
		southPanel.add(new JScrollPane(fErrorWords = new JList<>()));
		southPanel.add(new JScrollPane(fSuggestWords = new JList<>()));
		southPanel.add(fReplaceWordButt = new JButton("Replace All Occurences"));
		add(southPanel, BorderLayout.SOUTH);
		
		fReplaceWordButt.setEnabled(false);
	}
	
	/**
	 * Adds the respective listener to the controls of the panel
	 */
	private void addListeners() {
		/**
		 * Button listener to load the dictionary file
		 */
		fLoadDictButt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser loadFile = new JFileChooser();
				loadFile.setDialogTitle("Load Dictionary File");
				loadFile.setFileFilter(new FileNameExtensionFilter("Normal Text File (*.txt)", "txt", "text"));
				 
				if (loadFile.showOpenDialog(loadFile) == JFileChooser.APPROVE_OPTION) {
				    fWordDict = new WordDictionary(loadFile.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		/**
		 * Button listener to load the text file
		 */
		fLoadTextButt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser loadFile = new JFileChooser();
				loadFile.setDialogTitle("Load Text File");
				loadFile.setFileFilter(new FileNameExtensionFilter("Normal Text File (*.txt)", "txt", "text"));
				
				if (loadFile.showOpenDialog(loadFile) == JFileChooser.APPROVE_OPTION) {
				    try {
						BufferedReader br = new BufferedReader(new FileReader(loadFile.getSelectedFile().getAbsolutePath()));
						fTextEditor.read(br, null);
						br.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		/**
		 * Button listener to execute spell checker
		 */
		fSpellCheckButt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fWordDict != null) {
					Set<String> words = new HashSet<>(); //Set used to avoid processing the same word more than once
					Pattern p = Pattern.compile("\\w[\\w-]+('\\w*)?"); //Pattern to grab words and words with hyphens in between
					Matcher m = p.matcher(fTextEditor.getText());
					while(m.find()) {
						words.add(fTextEditor.getText().substring(m.start(), m.end()));
					}
					fSuggestionsCon.clear();
					
					int idx = 0;
					DefaultListModel<String> listModel = new DefaultListModel<>(); //To be stored into the JList object
					for(String word : words) {
						if(!fWordDict.contains(word) {
							listModel.addElement(word);
							fWorkers.get(idx++ %fNumWorkers).addSetElement(word);
						}
					}
					for(WordSuggesterWorker worker : fWorkers) {
						worker.start();
					}
					fErrorWords.setModel(listModel);
				}
				else {
					JOptionPane.showMessageDialog(null, "Please import the dictionary before running the spell checker!");
				}
			}
		});
		
		/**
		 * Button listener to save the text in the editor to a text file
		 */
		fSaveTextButt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser saveFile = new JFileChooser();
				saveFile.setDialogTitle("Save Text File");
				saveFile.setFileFilter(new FileNameExtensionFilter("Normal Text File (*.txt)", "txt", "text"));
				if (saveFile.showSaveDialog(saveFile) == JFileChooser.APPROVE_OPTION) {
					try {
						//Check if the full file path has the .txt at the end
						String filePath = saveFile.getSelectedFile().getAbsolutePath();
						int fpLen = filePath.length();
						if(!filePath.substring(fpLen - 4, fpLen).equals(".txt")) {
							filePath += ".txt";
						}
						//Begin writing file
	                    BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
	                    fTextEditor.write(bw);
	                    bw.close();
	                    fTextEditor.setText("");
	                    fTextEditor.requestFocus();
	                }
	                catch(Exception e2) {
	                	e2.printStackTrace();
	                }
				}
			}
		});
		
		/**
		 * Button listener to replace the error word with the suggested word from their respective JLists
		 */
		fReplaceWordButt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String keyWord = fErrorWords.getSelectedValue();
				String newWord = fSuggestWords.getSelectedValue();
				//Update replace all occurrences in the text area.
				fTextEditor.setText(fTextEditor.getText().replaceAll(keyWord, newWord));
				
				//Update both JLists
				DefaultListModel<String> suggestWordModel = (DefaultListModel<String>) fSuggestWords.getModel();
				DefaultListModel<String> errorWordModel = (DefaultListModel<String>) fErrorWords.getModel();
				suggestWordModel.removeAllElements();
				errorWordModel.remove(fErrorWords.getSelectedIndex());
				fSuggestionsCon.remove(keyWord); //Update the hashmap
				fReplaceWordButt.setEnabled(false);
			}
		});
		
		/**
		 * JList Selection listener to populate the suggested words JList and toggle the replace button off 
		 */
		fErrorWords.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!fErrorWords.isSelectionEmpty()) {
					DefaultListModel<String> listModel = new DefaultListModel<>(); //To be stored into the JList object
					for(String word : fSuggestionsCon.get(fErrorWords.getSelectedValue())) {
						listModel.addElement(word);
					}
					fSuggestWords.setModel(listModel);
					fReplaceWordButt.setEnabled(false);
				}
			}
		});
		
		/**
		 * JList Selection listener to toggle the replace button on 
		 */
		fSuggestWords.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!fErrorWords.isSelectionEmpty()) {
					fReplaceWordButt.setEnabled(true);
				}
			}
		});
	}

}
