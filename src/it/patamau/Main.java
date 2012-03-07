package it.patamau;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import it.patamau.data.ProfileManager;
import it.patamau.gui.GUI;

public class Main {
	
	public static final String VERSION = "0.3b";
	
	public static String DEF_SAVE_PATH = (new JFileChooser()).getFileSystemView().getDefaultDirectory().getAbsolutePath()+File.separator+"My Games"+File.separator+"Skyrim"+File.separator+"Saves";
	
	public static int DEF_WWIDTH = 800, DEF_WHEIGHT = 600;

	public static void main(final String args[]){
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
		final GUI gui = new GUI(manager);
		//gui.setSize(DEF_WWIDTH, DEF_WHEIGHT);
		gui.pack();
		gui.setLocationRelativeTo(null);
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				gui.setVisible(true);
			}
		});
	}
}
