package it.patama.data;

import java.util.Date;

import javax.swing.ImageIcon;

public class SaveData {

	private String 
		name,
		race,
		location,
		date;
	private Date
		filetime;
	private int 
		level;
	private ImageIcon
		screenshot;
	
	public SaveData(){
		name = "N/A";
		race = "N/A";
		location = "N/A";
		date = "N/A";
		level = -1;
		screenshot = null;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getRace() {
		return race;
	}

	public void setRace(final String race) {
		this.race = race;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(final String location) {
		this.location = location;
	}

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(final int level) {
		this.level = level;
	}

	public ImageIcon getScreenshot() {
		return screenshot;
	}

	public void setScreenshot(final ImageIcon screenshot) {
		this.screenshot = screenshot;
	}
	
	public Date getFiletime(){
		return filetime;
	}
	
	public void setFiletime(final Date filetime){
		this.filetime = filetime;
	}
}
