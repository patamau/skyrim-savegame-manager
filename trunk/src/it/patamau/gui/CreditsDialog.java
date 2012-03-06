package it.patamau.gui;

import it.patamau.Main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CreditsDialog extends JDialog {

	private static final long serialVersionUID = 1837417904493457259L;

	public CreditsDialog(final JFrame frame){
		super(frame);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(createBody(), BorderLayout.CENTER);
		this.setTitle("About...");
		this.pack();
		this.setModal(true);
		this.setLocationRelativeTo(frame);
		this.setResizable(false);
	}

	private JComponent createBody() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.white);
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=gc.gridy=0;
		gc.insets = new Insets(5,5,5,5);
		JLabel title = new JLabel("Skyrim Savegame Manager - "+Main.VERSION);	
		JTextField site = new JTextField("http://code.google.com/p/skyrim-savegame-manager/");
		site.setBorder(null);
		site.setDisabledTextColor(Color.BLACK);
		site.setBackground(null);
		site.setEditable(false);
		JTextField author = new JTextField("Matteo Pedrotti <patamau@gmail.com>");
		author.setBorder(null);
		author.setDisabledTextColor(Color.BLACK);
		author.setBackground(null);
		author.setEditable(false);
		JLabel date = new JLabel("2012/03/06");
		JLabel credits = new JLabel("<html><small>Thanks to<br/> " +
				"Bethesda Softworks for this great game<br/>" +
				"www.uesp.net for Save File Format details<br/>" +
				"Marte for precious advices<br/>" +
				"Metalheart for extreme testing<br/>" +
				"</small></html>");
		panel.add(title,gc);
		++gc.gridy;
		panel.add(date,gc);
		++gc.gridy;
		panel.add(author,gc);
		++gc.gridy;
		panel.add(site,gc);
		++gc.gridy;
		panel.add(credits,gc);
		return panel;
	}
	
	
}
