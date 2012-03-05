package it.patamau.gui;

import it.patama.data.SaveData;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class GUI extends JFrame {
	
	public static void showPicture(final SaveData data){
		JFrame frame = new JFrame();
		frame.setTitle(data.getName()+" "+data.getLevel()+" "+data.getLocation()+" "+data.getDate());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		JLabel label = new JLabel();
		label.setIcon(data.getScreenshot());
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public GUI(){
		super();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(createLeftPanel(), BorderLayout.WEST);
		this.getContentPane().add(createRightPanel(), BorderLayout.CENTER);
	}
	
	/**
	 * Contains a list of all the available player saves
	 * @return
	 */
	private JPanel createLeftPanel(){
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder(1));
		return panel;
	}
	
	/**
	 * Shows the currently selected save game
	 * @return
	 */
	private JPanel createRightPanel(){
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		return panel;
	}
}
