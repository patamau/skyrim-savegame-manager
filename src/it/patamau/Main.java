package it.patamau;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import it.patama.data.ProfileManager;
import it.patamau.gui.GUI;

public class Main {
	
	public static String DEF_SAVE_PATH = (new JFileChooser()).getFileSystemView().getDefaultDirectory().getAbsolutePath()+File.separator+"My Games"+File.separator+"Skyrim"+File.separator+"Saves";  

	public static void main(final String args[]){
		ProfileManager manager = new ProfileManager();
		try {
			manager.load(new File("."));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		GUI gui = new GUI(manager);
		gui.pack();
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}
}
