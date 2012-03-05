package it.patamau.gui;

import it.patama.data.ProfileData;
import it.patama.data.ProfileManager;
import it.patama.data.SaveData;

import java.awt.BorderLayout;
import java.awt.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUI extends JFrame implements ListSelectionListener {
	
	public static void showQuickPick(final SaveData data){
		JFrame frame = new JFrame();
		frame.setTitle(data.getName()+" "+data.getLevel()+" "+data.getLocation()+" "+data.getDate());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		JLabel label = new JLabel();
		label.setIcon(data.getScreenshot());
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private final ProfileManager manager;
	private JList profileList, savesList;
	private DefaultListModel profileListModel, savesListModel;

	public GUI(final ProfileManager manager){
		super();
		this.manager = manager;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(createLeftPanel(), BorderLayout.WEST);
		this.getContentPane().add(createCenterPanel(), BorderLayout.CENTER);
	}
	
	/**
	 * Contains a list of all the available player saves
	 * @return
	 */
	private JPanel createLeftPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder(1));
		profileList = new JList(new DefaultListModel());
		profileListModel = (DefaultListModel)profileList.getModel();
		profileListModel.addElement(" -- CURRENT --");
		for(String name: manager.getProfileNames()){
			profileListModel.addElement(name);
		}
		profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		profileList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		profileList.setVisibleRowCount(-1);
		profileList.addListSelectionListener(this);
		JScrollPane listScroller = new JScrollPane(profileList);
		panel.add(listScroller,BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * Shows the currently selected save game
	 * @return
	 */
	private JPanel createCenterPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder(1));
		savesList = new JList(new DefaultListModel());
		savesListModel = (DefaultListModel)savesList.getModel();
		savesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		savesList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		savesList.setVisibleRowCount(-1);
		//savesList.addListSelectionListener(this);
		JScrollPane listScroller = new JScrollPane(savesList);
		panel.add(listScroller,BorderLayout.CENTER);
		return panel;
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
	        if (profileList.getSelectedIndex() == -1) {
	        	//TODO: back to default profile
	        } else {
	        	String name = (String)profileList.getSelectedValue();
	        	ProfileData profile = manager.getProfile(name);
	        	savesListModel.removeAllElements();
	        	for(SaveData save: profile.getSaves()){
	        		savesListModel.addElement(save);
	        	}
	        }
	    }
	}
}
