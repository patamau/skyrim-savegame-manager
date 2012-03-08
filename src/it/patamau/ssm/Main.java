package it.patamau.ssm;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import it.patamau.ssm.data.ProfileManager;
import it.patamau.ssm.gui.GUI;

public class Main {
	
	public static final String VERSION = "0.3b";
	
	public static String DEF_SAVE_PATH = (new JFileChooser()).getFileSystemView().getDefaultDirectory().getAbsolutePath()+File.separator+"My Games"+File.separator+"Skyrim"+File.separator+"Saves";
	public static String DEF_LOG_FILE = "ssm.log";

	public static void main(final String args[]){
		//initialize logger
		try {
			final Handler logFileHandler = new FileHandler(DEF_LOG_FILE);
			Logger.getLogger("").addHandler(logFileHandler);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error creating log file "+e, "Initialization error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} 
		
		//initializat profile manager
		final ProfileManager manager = new ProfileManager();
		manager.setProfilesFolder(new File("."));
		manager.setSavesFolder(new File(DEF_SAVE_PATH));
		try {
			manager.loadProperties();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error loading properties file: "+e, "Initialization error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		
		//start gui
		final GUI gui = new GUI(manager);
		gui.pack();
		gui.setLocationRelativeTo(null);
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				gui.setVisible(true);
			}
		});
	}
}
