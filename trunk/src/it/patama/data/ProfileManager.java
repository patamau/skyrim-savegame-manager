package it.patama.data;

import it.patamau.Main;
import it.patamau.gui.GUI;
import it.patamau.parser.Parser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ProfileManager {
	
	private static final String 
		KEY_PROFILES_FOLDER = "profilesFolder",
		KEY_SAVES_FOLDER = "savesFolder";

	public static String DEF_PROP_FILE = "ssm.cfg";
	
	private File profilesFolder, savesFolder;
	
	private final Properties properties;
	
	private final Map<String, ProfileData> profiles;
	private final Map<String, ProfileData> current;
	
	public ProfileManager(){
		profiles = new HashMap<String, ProfileData>();
		current = new HashMap<String, ProfileData>();
		properties = new Properties();
	}
	
	public Properties getProperties(){
		return properties;
	}
	
	public void loadProperties() throws IOException{
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(DEF_PROP_FILE);
			properties.load(fis);
			String t; 
			t = properties.getProperty(KEY_PROFILES_FOLDER);
			if(t!=null) profilesFolder = new File(t);
			t = properties.getProperty(KEY_SAVES_FOLDER);
			if(t!=null) savesFolder = new File(t);
		}catch(FileNotFoundException e){
			saveProperties();
		}finally{
			if(fis!=null){
				fis.close();
			}
		}
	}
	
	public void saveProperties() throws IOException{
		System.out.println("Saving configuration to "+DEF_PROP_FILE);
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(DEF_PROP_FILE);
			properties.setProperty(KEY_PROFILES_FOLDER, profilesFolder.getAbsolutePath());
			properties.setProperty(KEY_SAVES_FOLDER, savesFolder.getAbsolutePath());
			properties.store(fos, "Skyrim Savegame Manager - "+Main.VERSION);
		}catch(FileNotFoundException e){
			throw new IOException(e);
		}finally{
			if(fos!=null){
				fos.close();
			}
		}
	}
	
	public void setProfilesFolder(final File profilesFolder){
		this.profilesFolder = profilesFolder;
	}
	
	public void setSavesFolder(final File savesFolder){
		this.savesFolder = savesFolder;
	}
	
	public Collection<String> getProfileNames(){
		return profiles.keySet();
	}
	
	public Collection<ProfileData> getProfiles(){
		return profiles.values();
	}
	
	public ProfileData getProfile(final String name){
		return profiles.get(name);
	}
	
	public Map<String, ProfileData> getCurrent(){
		return current;
	}
	
	public void clear(){
		profiles.clear();
		current.clear();
	}
	
	public void removeSaves() throws IOException {
		for(File f: savesFolder.listFiles()){
			if(!f.getName().endsWith(".ess")) continue;
			if(!f.delete()){
				throw new IOException("File "+f+" cannot be removed");
			}
		}
	}
	
	/**
	 * Unpack the given profile to the save folder
	 * @param name
	 * @throws IOException
	 */
	public void deployProfile(final ProfileData profile) throws IOException {
		if(profile.getZipFile()==null||!profile.getZipFile().exists()){
			throw new IOException("No such archive available ("+profile.getZipFile()+")");
		}
		unpackZip(profile.getZipFile(), savesFolder);
	}
	
	public static void unpackZip(final File sourceFile, final File destinationFolder) throws IOException {
		byte[] buf = new byte[1024];
		System.out.println("Opening "+sourceFile.getName());
		ZipFile zf = new ZipFile(sourceFile);
	    for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements();){
	    	ZipEntry entry = e.nextElement();
	    	System.out.println("Unpacking "+entry.getName());
	    	BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destinationFolder.getAbsoluteFile()+File.separator+entry.getName()));
	    	InputStream in = zf.getInputStream(entry);
	    	int len;
	    	while((len=in.read(buf))>=0){
	    		out.write(buf,0,len);
	    	}
	    	out.close();
	    	in.close();
	    }
	    zf.close();
	}
	
	/**
	 * Creates an archive for the given profile name using the files found in the source folder.
	 * The output file is [name].zip and is placed in the destination folder
	 * @param save
	 * @throws IOException
	 */
	public static File createZip(final String profileName, final File source, final File destination) throws IOException {
		byte[] buf = new byte[1024];
		
	    File outFile = new File(destination, profileName+".zip");
	    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));

	    for (File f: source.listFiles()) {
			if(!f.isFile()) continue;
			if(!f.getName().endsWith(".ess")) continue;
			FileInputStream fis = new FileInputStream(f);
			SaveData s = Parser.parse(fis);
			fis.close();
			if(!s.getName().equals(profileName)) continue;
	        FileInputStream in = new FileInputStream(f);
	        out.putNextEntry(new ZipEntry(f.getName()));
	        System.out.println("Adding save game "+f.getName());
	        int len;
	        while ((len = in.read(buf)) >= 0) {
	            out.write(buf, 0, len);
	        }
	        out.closeEntry();
	        in.close();
	    }
	    out.close();
	    return outFile;
	}
	
	/**
	 * Load all the profiles from the given folder.
	 * Profiles are zip files containing save data.
	 * @param folder
	 * @return
	 * @throws IOException
	 */
	public void load() throws IOException {
		current.clear();
		current.putAll(getAll(savesFolder));
		profiles.clear();
		for(File f: profilesFolder.listFiles()){
			if(!f.getName().endsWith(".zip")) continue;
			System.out.println("Parsing "+f.getName());
			ProfileData pd = new ProfileData(f);
			pd.addAll(getZipSaves(f));
			if(current.containsKey(pd.getData().getName())){
				pd.setCurrent(true);
			}
			System.out.println("Parsed "+pd.getSaves().size()+" saves");
			profiles.put(pd.getData().getName(), pd);
 		}		
	}
	
	public void backup() throws IOException {
		for(String name: current.keySet()){
			createZip(name, savesFolder, profilesFolder);
		}
	}
	
	public static Map<String, ProfileData> getAll(final File folder) throws IOException {
		Map<String, ProfileData> map = new HashMap<String, ProfileData>();
		
		for(File f: folder.listFiles()){
			if(!f.getName().endsWith(".ess")) continue;
			FileInputStream in = new FileInputStream(f);
			SaveData save = Parser.parse(in);
			save.setSaveFile(f);
			in.close();
			System.out.println("Parsed "+save.getName()+" ("+f+")");
			ProfileData profile = map.get(save.getName());
			if(profile==null){
				profile = new ProfileData();
				map.put(save.getName(),profile);
			}
			profile.addSave(save);
 		}		
		
		return map;
	}
	
	/**
	 * Parses the save data from the given zip file
	 * @param folder
	 * @return
	 * @throws IOException
	 */
	public static List<SaveData> getZipSaves(final File zipFile) throws IOException {
		List<SaveData> saves = new ArrayList<SaveData>();
		ZipFile zf = new ZipFile(zipFile);
		SaveData latest = null;
	    for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements();){
	    	ZipEntry entry = e.nextElement();
	    	SaveData save = Parser.parse(zf.getInputStream(entry));
	    	save.setSaveFile(new File(entry.getName()));
	    	if(latest!=null){
	    		long st = save.getFiletime().getTime();
	    		long best = latest.getFiletime().getTime();
	    		if(st>best){
	    			latest = save;
	    			saves.add(0, save);
	    		}else{
	    			saves.add(save);
	    		}
	    	}else{
	    		latest = save;
	    		saves.add(save);
	    	}
	    	System.out.println("Save data "+save.getName()+" "+save.getLevel()+" "+save.getDate()+" "+save.getLocation());
	    }
	    zf.close();
		return saves;
	}
	
	public static void deleteZipEntry(final File zipFile, final String[] files) throws IOException {
		       // get a temp file
		File tempFile = File.createTempFile(zipFile.getName(), null);
		       // delete it, otherwise you cannot rename your existing zip to it.
		tempFile.delete();
		tempFile.deleteOnExit();
		if (!zipFile.renameTo(tempFile)){
		    throw new IOException("Could not rename the file "+zipFile.getAbsolutePath()+" to "+tempFile.getAbsolutePath());
		}
		final byte[] buf = new byte[1024];

		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
		    String name = entry.getName();
		    boolean toBeDeleted = false;
		    for (String f : files) {
		        if (f.equals(name)) {
		            toBeDeleted = true;
		            break;
		        }
		    }
		    if (!toBeDeleted) {
		        // Add ZIP entry to output stream.
		        zout.putNextEntry(new ZipEntry(name));
		        // Transfer bytes from the ZIP file to the output file
		        int len;
		        while ((len = zin.read(buf)) > 0) {
		            zout.write(buf, 0, len);
		        }
		    }
		    entry = zin.getNextEntry();
		}
		// Close the streams        
		zin.close();
		// Compress the files
		// Complete the ZIP file
		zout.close();
		tempFile.delete();
	}
	
	/**
	 * Retrieve the save data from the given save folder
	 * @return
	 * @throws IOException
	 */
	public static List<SaveData> getFolderSaves(final File folder) throws IOException{
		List<SaveData> saves = new ArrayList<SaveData>();
		for(File f : folder.listFiles()){
			if(!f.isFile()) continue;
			if(!f.getName().endsWith(".ess")) continue;
			InputStream in = new FileInputStream(f);
			SaveData save = Parser.parse(in);
			save.setSaveFile(f);
			in.close();
			saves.add(save);
		}
		return saves;
	}
	
	public static void main(String args[]){
		SaveData save;
		try {
			Map<String, ProfileData> map = ProfileManager.getAll(new File(Main.DEF_SAVE_PATH));
			for(String k: map.keySet()){
				System.out.println("Profile "+k+" with "+map.get(k).getSaves().size()+" saves");
				ProfileManager.createZip(k, new File(Main.DEF_SAVE_PATH), new File("."));
			}
			System.out.println("Loading latest save from "+Main.DEF_SAVE_PATH);
			save = ProfileManager.getFolderSaves(new File(Main.DEF_SAVE_PATH)).get(0);
			System.out.println(save.getName()+" "+save.getLevel());
			ProfileManager man = new ProfileManager();
			man.setProfilesFolder(new File("."));
			man.load();
			for(ProfileData sd: man.getProfiles()){
				GUI.showQuickPick(sd.getData());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public File getSavesFolder() {
		return this.savesFolder;
	}
	
	public File getProfilesFolder() {
		return this.profilesFolder;
	}
}
