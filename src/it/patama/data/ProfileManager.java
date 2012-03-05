package it.patama.data;

import it.patamau.Main;
import it.patamau.gui.GUI;
import it.patamau.parser.Parser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ProfileManager {
	
	public void unpackZip(final File sourceFile, final File destinationFolder) throws IOException {
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
	 * Creates an archive for the given profile name
	 * @param save
	 * @throws IOException
	 */
	public File createZip(final String name, final File folder) throws IOException {
		byte[] buf = new byte[1024];
		
	    File outFile = new File(name+".zip");
	    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));

	    for (File f: folder.listFiles()) {
			if(!f.isFile()) continue;
			if(!f.getName().endsWith(".ess")) continue;
			FileInputStream fis = new FileInputStream(f);
			SaveData s = Parser.parse(fis);
			fis.close();
			if(!s.getName().equals(name)) continue;
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
	 * Get all the profiles from the given folder.
	 * Profiles are zip files containing save data.
	 * @param folder
	 * @return
	 * @throws IOException
	 */
	public List<ProfileData> getProfiles(final File folder) throws IOException {
		List<ProfileData> profiles = new ArrayList<ProfileData>();
		for(File f: folder.listFiles()){
			if(!f.getName().endsWith(".zip")) continue;
			System.out.println("Parsing "+f.getName());
			ProfileData pd = new ProfileData(f);
			pd.addAll(getSaves(f));
			System.out.println("Parsed "+pd.getSaves().size()+" saves");
			profiles.add(pd);
 		}		
		return profiles;
	}
	
	public Map<String, List<SaveData>> getAll(final File folder) throws IOException {
		Map<String, List<SaveData>> map = new HashMap<String, List<SaveData>>();
		
		for(File f: folder.listFiles()){
			if(!f.getName().endsWith(".ess")) continue;
			FileInputStream in = new FileInputStream(f);
			SaveData save = Parser.parse(in);
			save.setSaveFile(f);
			in.close();
			System.out.println("Parsed "+save.getName()+" ("+f+")");
			List<SaveData> list = map.get(save.getName());
			if(list==null){
				list = new ArrayList<SaveData>();
				map.put(save.getName(),list);
			}
			list.add(save);
 		}		
		
		return map;
	}
	
	/**
	 * Parses the save data from the given zip file
	 * @param folder
	 * @return
	 * @throws IOException
	 */
	public List<SaveData> getSaves(final File zipFile) throws IOException {
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
	
	/**
	 * Retrieve the save data of the most recent
	 * file of the given folder
	 * @return
	 * @throws IOException
	 */
	public SaveData getLatestSave(final File folder) throws IOException{
		File recent = null;
		long best = 0;
		for(File f : folder.listFiles()){
			if(!f.isFile()) continue;
			if(!f.getName().endsWith(".ess")) continue;
			if(f.getName().startsWith("quicksave")||
					f.getName().startsWith("autosave")){
				if(f.lastModified()>best){
					best = f.lastModified();
					recent = f;
				}
			}
		}
		System.out.println("Latest save is "+recent);
		InputStream in = new FileInputStream(recent);
		SaveData save = Parser.parse(in);
		save.setSaveFile(recent);
		in.close();
		return save;
	}
	
	public static void main(String args[]){
		ProfileManager man = new ProfileManager();
		SaveData save;
		try {
			Map<String, List<SaveData>> map = man.getAll(new File(Main.DEF_SAVE_PATH));
			for(String k: map.keySet()){
				System.out.println("Profile "+k+" with "+map.get(k).size()+" saves");
				File zip = man.createZip(k, new File(Main.DEF_SAVE_PATH));
			}
			System.out.println("Loading latest save from "+Main.DEF_SAVE_PATH);
			save = man.getLatestSave(new File(Main.DEF_SAVE_PATH));
			System.out.println(save.getName()+" "+save.getLevel());
			for(ProfileData sd: man.getProfiles(new File("."))){
				GUI.showPicture(sd.getData());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
