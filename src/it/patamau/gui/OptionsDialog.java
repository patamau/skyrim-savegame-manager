package it.patamau.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class OptionsDialog extends JDialog implements ActionListener {

	private File savesFolder, profilesFolder;
	
	private JButton savesFolderButton, profilesFolderButton, okButton, cancelButton;
	
	public OptionsDialog(final JFrame frame, final File savesFolder, final File profilesFolder){
		super(frame);
		this.setTitle("Options");
		this.savesFolder = savesFolder;
		this.profilesFolder = profilesFolder;
		savesFolderButton = new JButton(savesFolder.getPath());
		savesFolderButton.addActionListener(this);
		profilesFolderButton = new JButton(profilesFolder.getPath());
		profilesFolderButton.addActionListener(this);
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder("Skyrim Saves Folder"));
		p.add(savesFolderButton,BorderLayout.CENTER);
		
		JPanel b = new JPanel();
		b.setLayout(new BorderLayout());
		b.setBorder(BorderFactory.createTitledBorder("Profiles Folder"));
		b.add(profilesFolderButton,BorderLayout.CENTER);
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(4,4,4,4);
		gc.gridx=0;
		gc.gridy=0;
		gc.gridwidth=2;
		gc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(p,gc);
		++gc.gridy;
		panel.add(b,gc);
		gc.gridwidth=1;
		gc.weightx=0.5;
		gc.fill = GridBagConstraints.NONE;
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		++gc.gridy;
		panel.add(okButton,gc);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		++gc.gridx;
		panel.add(cancelButton,gc);
		
		this.getContentPane().add(panel);
		this.pack();
		this.setLocationRelativeTo(frame);
	}
	
	private File getFile(final File src){
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile(src);
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ch = fc.showOpenDialog(this);
		if(ch==JFileChooser.APPROVE_OPTION){
			return fc.getSelectedFile();
		}else{
			return null;
		}
	}

	public void actionPerformed(ActionEvent ev) {
		Object src = ev.getSource();
		if(src==savesFolderButton){
			File _savesFolder = getFile(savesFolder);
			if(_savesFolder!=null){
				savesFolder = _savesFolder;
				savesFolderButton.setText(savesFolder.getPath());
			}
		}else if(src==profilesFolderButton){
			File _profilesFolder = getFile(profilesFolder);
			if(_profilesFolder!=null){
				profilesFolder = _profilesFolder;
				profilesFolderButton.setText(profilesFolder.getPath());
			}
		}else if(src==okButton){
			this.dispose();
		}else if(src==cancelButton){
			savesFolder = null;
			profilesFolder = null;
			this.dispose();
		}
	}

	public File getSavesFolder() {
		return savesFolder;
	}
	
	public File getProfilesFolder() {
		return profilesFolder;
	}
}
