package it.patamau;

import it.patamau.gui.GUI;

public class Main {
	
	public static String SAVE_PATH = "C:\\Users\\Matteo Pedrotti\\Documents\\My Games\\Skyrim\\Saves";

	public static void main(final String args[]){
		GUI gui = new GUI();
		gui.pack();
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}
}
