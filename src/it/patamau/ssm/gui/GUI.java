package it.patamau.ssm.gui;

import it.patamau.ssm.Main;
import it.patamau.ssm.data.ProfileData;
import it.patamau.ssm.data.ProfileManager;
import it.patamau.ssm.data.SaveData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUI extends JFrame implements ListSelectionListener, ActionListener {

	private static final long serialVersionUID = 6536812766780851565L;
	
	public static final SimpleDateFormat filetimeFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");

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
	private JMenuItem refreshItem, optionsItem, exitItem, aboutItem;
	private JButton  deployButton, deleteButton, removeButton;
	private JLabel nameLabel, levelLabel, dateLabel, locationLabel, raceLabel, screenshotLabel, filenameLabel, filetimeLabel;
	private JLabel statusLabel;
	private JProgressBar progressBar;

	public GUI(final ProfileManager manager){
		super();
		this.setTitle("Skyrim Savegame Manager - "+Main.VERSION);
		this.manager = manager;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout());
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createCenterPanel());
		splitPane.setDividerLocation(150);
		splitPane.setContinuousLayout(true);
		this.getContentPane().add(splitPane, BorderLayout.CENTER);
		this.getContentPane().add(createStatusPanel(), BorderLayout.SOUTH);
		this.setJMenuBar(createMenuBar());
		this.refresh();
	}
	
	private JComponent createStatusPanel(){
		JPanel panel = new JPanel();
		statusLabel = new JLabel("Ready");
		panel.add(statusLabel);
		progressBar = new JProgressBar();
		panel.add(progressBar);
		return panel;
	}
	
	private JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		optionsItem = new JMenuItem("Options...");
		optionsItem.addActionListener(this);
		refreshItem = new JMenuItem("Refresh");
		refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
		refreshItem.setToolTipText("Reload all the profiles and the save files");
		refreshItem.addActionListener(this);
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,Event.CTRL_MASK));
		
		fileMenu.add(optionsItem);
		fileMenu.add(refreshItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitItem);
		
		JMenu helpMenu = new JMenu("Help");
		aboutItem = new JMenuItem("About...");
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);
		
		menuBar.add(fileMenu);
		menuBar.add(Box.createGlue());
		menuBar.add(helpMenu);
		return menuBar;
	}
	
	/**
	 * Contains a list of all the available player saves
	 * @return
	 */
	private JComponent createLeftPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		profileList = new JList(new DefaultListModel());
		profileListModel = (DefaultListModel)profileList.getModel();
		profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		profileList.setLayoutOrientation(JList.VERTICAL);
		profileList.addListSelectionListener(this);
		JScrollPane listScroller = new JScrollPane(profileList);
		listScroller.setMinimumSize(new Dimension(150,100));
		panel.add(listScroller,BorderLayout.CENTER);
		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);
		removeButton.setEnabled(false);
		panel.add(removeButton, BorderLayout.SOUTH);
		deployButton = new JButton("Deploy");
		deployButton.addActionListener(this);
		deployButton.setEnabled(false);
		panel.add(deployButton, BorderLayout.NORTH);
		return panel;
	}
	
	/**
	 * Shows the currently selected save game
	 * @return
	 */
	private JComponent createCenterPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		savesList = new JList(new DefaultListModel());
		savesListModel = (DefaultListModel)savesList.getModel();
		savesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		savesList.setLayoutOrientation(JList.VERTICAL);
		savesList.addListSelectionListener(this);
		JScrollPane listScroller = new JScrollPane(savesList);
		listScroller.setMinimumSize(new Dimension(150,100));
		listScroller.setPreferredSize(new Dimension(300,350));
		deleteButton = new JButton("Delete");
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(this);
		panel.add(deleteButton, BorderLayout.SOUTH);
		panel.add(listScroller, BorderLayout.CENTER);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, createRightPanel());
		splitPane.setDividerLocation(300);
		splitPane.setContinuousLayout(true);
		return splitPane;
	}
	
	private static JLabel addLabel(final String label, final JPanel panel, final GridBagConstraints gc){
		JLabel l = new JLabel(label);
		l.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		l.setHorizontalAlignment(JLabel.RIGHT);
		panel.add(l, gc);
		++gc.gridx;
		JLabel n = new JLabel();
		panel.add(n, gc);
		++gc.gridy;
		gc.gridx=0;
		return n;
	}
	
	private JComponent createRightPanel(){
		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		final GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(2,2,2,2);
		nameLabel = addLabel("Name: ", panel, gc);
		levelLabel = addLabel("Level: ", panel, gc);
		raceLabel = addLabel("Race: ", panel, gc);
		dateLabel = addLabel("Date: ", panel, gc);
		locationLabel = addLabel("Location: ", panel, gc);
		filenameLabel = addLabel("Filename: ", panel, gc);
		filetimeLabel = addLabel("Last modified: ", panel, gc);
		gc.gridwidth=2;
		screenshotLabel = new JLabel();
		panel.add(screenshotLabel, gc);
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(480,350));
		return scrollPane;
	}
	
	private void setSaveData(final SaveData save){
    	nameLabel.setText(save.getName());
    	levelLabel.setText(Integer.toString(save.getLevel()));
    	dateLabel.setText(save.getDate());
    	locationLabel.setText(save.getLocation());
    	raceLabel.setText(save.getRace());
    	if(save.getSaveFile()!=null){
    		filenameLabel.setText(save.getSaveFile().getName());
    	}else{
    		filenameLabel.setText("N/A");
    	}
    	filetimeLabel.setText(filetimeFormat.format(save.getFiletime()));
    	screenshotLabel.setIcon(save.getScreenshot());
	}
	
	private void onProfileSelected(){
		final int i = profileList.getSelectedIndex();
		if (i < 0){
			savesListModel.removeAllElements();
        	deployButton.setEnabled(false);
        	removeButton.setEnabled(false);
        	savesList.setSelectedIndex(-1);
		} else if (i == 0) {
        	Map<String, ProfileData> current = manager.getCurrent();
        	savesListModel.removeAllElements();
        	for(ProfileData profile: current.values()){
        		for(SaveData save: profile.getSaves()){
        			savesListModel.addElement(save);
        		}
        	}
        	savesList.setSelectedIndex(0);
        	deployButton.setEnabled(true);
        	deployButton.setText("Backup");
        	removeButton.setEnabled(true);
        	removeButton.setText("Clear");
        } else {
        	ProfileData profile = (ProfileData)profileList.getSelectedValue();
        	savesListModel.removeAllElements();
        	for(SaveData save: profile.getSaves()){
        		savesListModel.addElement(save);
        	}
        	savesList.setSelectedIndex(0);
        	savesList.revalidate();
        	deployButton.setEnabled(true);
        	deployButton.setText("Deploy");
        	removeButton.setEnabled(true);
        	removeButton.setText("Remove");
        }
	}
	
	private void onSaveSelected(){
		final int i = savesList.getSelectedIndex();
		if (i < 0) {
			setSaveData(new SaveData());
        	deleteButton.setEnabled(false);
        } else {
        	SaveData save = (SaveData)savesList.getSelectedValue();
        	setSaveData(save);
        	if(savesListModel.getSize()>0||profileList.getSelectedIndex()==0){
        		deleteButton.setEnabled(true);
        	}else{
	        	deleteButton.setEnabled(false);
        	}
        }
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			final Object src = e.getSource();
			if(src == profileList){
				onProfileSelected();
			}else if(src == savesList){
				onSaveSelected();
			}
	    }
	}
	
	private void refresh(){
		try {
			manager.load();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error refreshing data: "+e.getMessage(), "Refresh error", JOptionPane.ERROR_MESSAGE);
			showOptions();
			return;
		}
		profileListModel.removeAllElements();
		profileListModel.addElement("-- CURRENT --");
		for(ProfileData profile: manager.getProfiles()){
			profileListModel.addElement(profile);
		}
		profileList.setSelectedIndex(0);
	}
	
	private void backup(){
		Map<String, ProfileData> current = manager.getCurrent();
		if(current.size()>0){
			boolean found = false;
			for(String n: current.keySet()){
				if(manager.getProfileNames().contains(n)){
					found=true;
					break;
				}
			}
			if(found){
				int ch = JOptionPane.showConfirmDialog(this, "One ore more profiles will be overwritten: are you sure you want to overwrite?", "Confirm overwrite", JOptionPane.YES_NO_OPTION);
				if(ch != JOptionPane.YES_OPTION) return;
			}
		}
		try {
			manager.backup();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Backup error", JOptionPane.ERROR_MESSAGE);
		}
		refresh();
	}
	
	private void deploy(){
		int i = profileList.getSelectedIndex();
		ProfileData profile;
		if(i<0){
			JOptionPane.showMessageDialog(this, "Please select a valid profile to be used", "Invalid profile", JOptionPane.WARNING_MESSAGE);
			return;
		}else if(i==0){
			backup();
			return;
		}else{
			profile = (ProfileData)profileList.getSelectedValue();
		}
		if(manager.getCurrent().size()>0){
			int ch = JOptionPane.showConfirmDialog(this, "Do you want to remove the current saves?", "Remove current saves", JOptionPane.YES_NO_CANCEL_OPTION);
			if(ch==JOptionPane.YES_OPTION){
				try{
					manager.removeSaves();
				}catch(IOException e){
					JOptionPane.showMessageDialog(this, e.getMessage(), "Clear error", JOptionPane.ERROR_MESSAGE);
				}
			}else if(ch==JOptionPane.CANCEL_OPTION){
				return;
			}
		}
		try{
			manager.deployProfile(profile);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Deploy error", JOptionPane.ERROR_MESSAGE);
		}
		refresh();
	}
	
	private boolean deleteSaveFile(){
		SaveData save = (SaveData)this.savesList.getSelectedValue();
		if(save==null){
			JOptionPane.showMessageDialog(this, "No save entry selected", "Delete error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(save.getSaveFile().exists()){
			int ch = JOptionPane.showConfirmDialog(this, "Save file "+save.getSaveFile().getName()+" will be lost forever. Are you sure?", "Delete save", JOptionPane.YES_NO_OPTION);
			if(ch!=JOptionPane.YES_OPTION) return false;
			if(!save.getSaveFile().delete()){
				JOptionPane.showMessageDialog(this, "Unable to delete "+save.getSaveFile().getName(), "Delete error", JOptionPane.ERROR_MESSAGE);
			}
			return true;
		}else{
			JOptionPane.showMessageDialog(this, "Save file "+save.getSaveFile().getName()+" does not exist!", "Delete error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private boolean deleteProfileEntry(){
		ProfileData profile = (ProfileData)this.profileList.getSelectedValue();
		if(profile.getZipFile().exists()){
			SaveData save = (SaveData)this.savesList.getSelectedValue();
			if(save==null){
				JOptionPane.showMessageDialog(this, "No save entry selected", "Delete error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(profile.getSaves().size()>1){
				int ch = JOptionPane.showConfirmDialog(this, "Save file "+save.getSaveFile().getName()+" will be lost forever. Are you sure?", "Delete save", JOptionPane.YES_NO_OPTION);
				if(ch!=JOptionPane.YES_OPTION) return false;
				try {
					ProfileManager.deleteZipEntry(profile.getZipFile(), new String[]{save.getSaveFile().getName()});
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, "Unable to delete "+save.getSaveFile().getName()+" from the profile", "Delete error", JOptionPane.ERROR_MESSAGE);
				}
			}else{
				remove(); //forward to the remove action
			}
			return true;
		}else{
			JOptionPane.showMessageDialog(this, "No such profile archive "+profile.getZipFile()+" exists", "Profile error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private void delete(){
		boolean done = false;
		int i = this.profileList.getSelectedIndex();
		if(i<0){
			JOptionPane.showMessageDialog(this, "Please select a valid profile to be used", "Invalid profile", JOptionPane.WARNING_MESSAGE);
		}else if(i==0){ //CURRENT
			done = deleteSaveFile();
		}else{
			done = deleteProfileEntry();
		}
		if(done) refresh();
	}
	
	private void showOptions(){
		OptionsDialog dialog = new OptionsDialog(this, manager.getSavesFolder(), manager.getProfilesFolder());
		dialog.setModal(true);
		dialog.setVisible(true);
		File sf = dialog.getSavesFolder();
		File pf = dialog.getProfilesFolder();
		if(sf!=null){
			System.out.println(sf);
			manager.setSavesFolder(sf);
		}
		if(pf!=null){
			System.out.println(pf);
			manager.setProfilesFolder(pf);
		}
		if(sf!=null||pf!=null){
			try {
				manager.saveProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
			refresh();
		}
	}
	
	private boolean clearCurrent(){
		if(manager.getCurrent().size()>0){
			int ch = JOptionPane.showConfirmDialog(this, "All current saves will be lost forever. Are you sure?", "Remove all saves", JOptionPane.YES_NO_OPTION);
			if(ch!=JOptionPane.YES_OPTION) return false;
			try{
				manager.removeSaves();
			}catch(IOException e){
				JOptionPane.showMessageDialog(this, e.getMessage(), "Clear error", JOptionPane.ERROR_MESSAGE);
			}
			return true;
		}else{
			return false;
		}
	}
	
	private boolean removeProfile(){
		ProfileData profile = (ProfileData)this.profileList.getSelectedValue();
		int ch = JOptionPane.showConfirmDialog(this, profile.getData().getName()+" profile will be lost forever. Are you sure?", "Remove profile", JOptionPane.YES_NO_OPTION);
		if(ch!=JOptionPane.YES_OPTION) return false;
		if(profile.getZipFile().exists()){
			if(!profile.getZipFile().delete()){
				JOptionPane.showMessageDialog(this, "Unable to delete "+profile.getZipFile(), "Delete error", JOptionPane.ERROR_MESSAGE);
			}
			return true;
		}else{
			JOptionPane.showMessageDialog(this, "Profile archive "+profile.getZipFile()+" does not exist", "Remove error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private void remove(){
		boolean done = false;
		final int i = profileList.getSelectedIndex();
		if(i<0){
			JOptionPane.showMessageDialog(this, "No valid profile entry selected", "Remove error", JOptionPane.ERROR_MESSAGE);
		}else if(i==0){
			done = clearCurrent();
		}else{
			done = removeProfile();
		}
		if(done) refresh(); //XXX: refresh will update current status for the profiles and update the lists...
	}

	public void actionPerformed(ActionEvent ev) {
		Object src = ev.getSource();
		if(src == optionsItem){
			showOptions();
		} else if(src == exitItem){
			this.dispose();
		} else if(src == refreshItem){
			statusLabel.setText("Refreshing...");
			refresh();
			statusLabel.setText("Done");
		} else if(src == aboutItem){
			CreditsDialog dialog = new CreditsDialog(this);
			dialog.setVisible(true);
		}else if(src == deployButton){
			Thread t = new Thread(){
				public void run(){
					statusLabel.setText("Working...");
					progressBar.setIndeterminate(true);
					deploy();
					progressBar.setIndeterminate(false);
					statusLabel.setText("Done");
				}
			};
			t.start();
		}else if(src == deleteButton){
			statusLabel.setText("Deleting...");
			delete();
			statusLabel.setText("Done");
		}else if(src == removeButton){
			statusLabel.setText("Removing...");
			remove();
			statusLabel.setText("Done");
		}
	}
}
