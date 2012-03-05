package it.patama.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileData {

	private final List<SaveData> data;
	private File zipFile;
	
	public ProfileData(final File zipFile){
		this.data = new ArrayList<SaveData>();
		this.zipFile = zipFile;
	}
	
	public SaveData getData(){
		if(data.size()==0) return null;
		return data.get(0);
	}

	public List<SaveData> getSaves() {
		return data;
	}

	public void addSaves(final SaveData data) {
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
	
	
}
