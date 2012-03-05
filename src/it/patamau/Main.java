package it.patamau;

import java.io.File;

import javax.swing.JFileChooser;

import it.patamau.gui.GUI;

public class Main {
	
	public static String DEF_SAVE_PATH = (new JFileChooser()).getFileSystemView().getDefaultDirectory().getAbsolutePath()+File.separator+"My Games"+File.separator+"Skyrim"+File.separator+"Saves";  

	public static void main(final String args[]){
		GUI gui = new GUI();
		gui.pack();
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}
}
