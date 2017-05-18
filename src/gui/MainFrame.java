/**
 * 
 */
package gui;

import java.awt.HeadlessException;

import javax.swing.JFrame;

/**
 * @author berant89
 * An extension of the JFrame class to quickly and easily run the GUI
 */
public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8026416994513756565L;

	/**
	 * @throws HeadlessException
	 */
	public MainFrame() throws HeadlessException {
		super();
		add(new MainPanel());
		setSize(900, 900);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}
