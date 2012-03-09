package it.patamau.ssm.data;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class SaveData {

	private String 
		name,
		race,
		location,
		date;
	private Date
		filetime;
	private File
		saveFile;
	private int 
		level;
	private ImageIcon
		screenshot;
	private final Map<Integer, Map<String, Integer>>
		globalData;
	
	public SaveData(){
		name = "N/A";
		race = "N/A";
		location = "N/A";
		date = "N/A";
		level = -1;
		screenshot = null;
		filetime = new Date(0);
		saveFile = null;
		globalData = new HashMap<Integer, Map<String, Integer>>();
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

	public File getSaveFile() {
		return saveFile;
	}

	public void setSaveFile(final File saveFile) {
		this.saveFile = saveFile;
	}
	
	public void addGlobalData(final Integer type, final String key, final Integer value){
		Map<String, Integer> map = globalData.get(type);
		if(map==null){
			map = new HashMap<String, Integer>();
			globalData.put(type, map);
		}
		map.put(key, value);
	}
	
	public Map<String, Integer> getGlobalData(final Integer type){
		return globalData.get(type);
	}
	
	public void clearGlobalData(){
		globalData.clear();
	}
	
	public String toString(){
		return saveFile.getName();
	}
}
