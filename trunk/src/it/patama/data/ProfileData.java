package it.patama.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileData {

	private final List<SaveData> data;
	private File zipFile;
	private boolean current;
	
	public ProfileData(final File zipFile){
		this.data = new ArrayList<SaveData>();
		this.zipFile = zipFile;
	}
	
	public ProfileData(){
		this.data = new ArrayList<SaveData>();
	}
	
	public SaveData getData(){
		if(data.size()==0) return null;
		return data.get(0);
	}

	public List<SaveData> getSaves() {
		return data;
	}

	public void addSave(final SaveData data) {
		this.data.add(data);
	}
	
	public void addAll(final List<SaveData> data){
		this.data.addAll(data);
	}
	
	public void clearData(){
		this.data.clear();
	}

	public File getZipFile() {
		return zipFile;
	}

	public void setZipFile(final File zipFile) {
		this.zipFile = zipFile;
	}
	
	public String toString(){
		if(current){
			return "<html>"+data.get(0).getName()+" <small>current</small></html>";
		}
		return data.get(0).getName();
	}
	
	public void setCurrent(final boolean current){
		this.current = current;
	}
	
	public boolean getCurrent(){
		return current;
	}
}
