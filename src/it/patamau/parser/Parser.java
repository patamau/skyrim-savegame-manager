package it.patamau.parser;

import it.patamau.data.SaveData;
import it.patamau.gui.GUI;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ImageIcon;

public class Parser {
	
	public static final long bytesToLong(final byte[] b){
	    long num = 0;
	    for(int i=b.length; i>0; ){
	    	num |= b[--i] & 0xFF;
	    	if(i>0) num <<= 8;
	    }
	    return num;
	}
	
	public static final int bytesToInt(final byte[] b){
	    int num = 0;
	    for(int i=b.length; i>0; ){
	    	num |= b[--i] & 0xFF;
	    	if(i>0) num <<= 8;
	    }
	    return num;
	}
	
	public static final int bytesToColor(final byte r, final byte g, final byte b){
	    int i = 0;
	    i |= r & 0xFF;
	    i <<= 8;
	    i |= g & 0xFF;
	    i <<= 8;
	    i |= b & 0xFF;
	    return i;
	}
	
	private static byte[] 
			buffer8 = new byte[8],
			buffer4 = new byte[4],
			buffer3 = new byte[3],
			buffer2 = new byte[2];
	
	public static final Date parseFiletime(final InputStream stream) throws IOException{
		byte[] t = buffer8;
		stream.read(t);
		long ft = bytesToLong(t);
		ft/=10000; //nanoseconds
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(ft);
		c.set(Calendar.YEAR, c.get(Calendar.YEAR)-369); //MSDN starts 1601, Java starts 1970 
		return c.getTime();
	}
	
	private static final int parseInt16(final InputStream s) throws IOException{
		byte[] data = buffer2;
		if(s.read(data)<0) throw new IOException("Unexpected end of stream");
		return bytesToInt(data);
	}
	
	private static final int parseInt24(final InputStream s) throws IOException{
		byte[] data = buffer3;
		if(s.read(data)<0) throw new IOException("Unexpected end of stream");
		return bytesToInt(data);
	}
	
	private static final int parseInt32(final InputStream s) throws IOException{
		byte[] data = buffer4;
		if(s.read(data)<0) throw new IOException("Unexpected end of stream");
		return bytesToInt(data);
	}
	
	private static final long parseLong64(final InputStream s) throws IOException{
		byte[] data = buffer8;
		if(s.read(data)<0) throw new IOException("Unexpected end of stream");
		return bytesToLong(data);
	}
	
	private static void parseMagic(final InputStream s) throws IOException{
		int ch;
		for(int i=0; i<13; ++i){
			ch = s.read();
			if(ch<0) return;
		}
		System.out.println("Magic ok");
	}
	
	private static final String parseString(final InputStream s) throws IOException{
		int siz = parseInt16(s);
		byte[] str = new byte[siz];
		s.read(str);
		return new String(str);
	}
	
	private static final ImageIcon parseScreenshotData(final InputStream s) throws IOException{
		int shotWidth = parseInt32(s);
		int shotHeight = parseInt32(s);
		System.out.println("Screenshot size "+shotWidth+"x"+shotHeight);
		int imgsize = 3*shotWidth*shotHeight;
		byte[] data = new byte[imgsize];
		int len, pos = 0;
		while((len = s.read(data, pos, imgsize-pos))>=0){
			pos+=len;
			System.out.println("Image read status "+pos+"/"+imgsize+" = "+((float)pos/(float)imgsize*100f)+"%");
			if(pos>=imgsize) break;
		}
		
		BufferedImage img = new BufferedImage(shotWidth, shotHeight, BufferedImage.TYPE_INT_RGB);
		int i=0;
		for(int y=0; y<shotHeight; ++y){
			for(int x=0; x<shotWidth; ++x){
				int color = bytesToColor(data[i++],data[i++],data[i++]);
				img.setRGB(x, y, color);
			}
		}
		return new ImageIcon(img);
	}
	
	public static final void parsePluginInfo(final InputStream s) throws IOException {
		int pluginCount = s.read();
		System.out.println("Plugins "+pluginCount);
		for(int i=0; i<pluginCount; ++i){
			String plugin = parseString(s);
			System.out.println("Plugin "+plugin);
		}
	}
	
	public static final void parseMiscStats(final InputStream s) throws IOException {
		int count = parseInt32(s);
		for(int i=0; i<count; ++i){
			String name = parseString(s);
			int category = s.read();
			int value = parseInt32(s);
			System.out.println(name+" "+category+" "+value);
		}
	}
	
	public static final int parseRefId(final InputStream s) throws IOException {
		int b = s.read();
		int code = (b & 0xC0) >> 6;
		int num = (b & 0x3F) << 8;
		num |= (s.read() & 0xFF);
		num <<= 8;
		num |= (s.read() & 0xFF);
		return num;
	}
	
	public static final int parseVsVal(final InputStream s) throws IOException {
		//F=1111 8=1000 C=1100 3=0011
		int b = s.read();
		int val = (b & 0xC0) >> 6;
		System.out.println("vsval key is "+val);
		int num = (b & 0x3F);
		for(int i=0; i<val; ++i){
			num <<= 8;
			int a = s.read() & 0xFF;
			num |= a;
		}
		int st = Integer.numberOfLeadingZeros(num);
		num <<= st;
		num = Integer.reverse(num);
		return num;
	}
	
	public static final void parseGlobalVariables(final InputStream s) throws IOException {
		int num = parseVsVal(s);
		System.out.println("Global variables are "+num);
		for(int i=0; i<num; ++i){
			int ref = parseRefId(s);
			float value = Float.intBitsToFloat(parseInt32(s));
			System.out.println(i+": "+Integer.toHexString(ref)+"="+value);
		}
	}
	
	private static final void parseGlobalData(final InputStream s) throws IOException {
		int type = parseInt32(s);
		int length = parseInt32(s);
		System.out.println("GlobalData type="+type+" length="+length);
		switch(type){
			case 0: //Misc Stats
				parseMiscStats(s);
			break;
			case 3:
				parseGlobalVariables(s);
			break;
			default:
				System.out.println("Unsupported GlobalData type "+type);
				s.skip(length);
			break;
		}
	}
	
	public static SaveData parse(final InputStream stream) throws IOException{
		parseMagic(stream);
		int hsize = parseInt32(stream);
		System.out.println("Header size is "+hsize);
		int version = parseInt32(stream);
		System.out.println("Version is "+version);
		int saveNumber = parseInt32(stream);
		System.out.println("Save number is "+saveNumber);
		String name = parseString(stream);
		System.out.println("Name is "+name);
		int playerLevel = parseInt32(stream);
		System.out.println("Level is "+playerLevel);
		String playerLocation = parseString(stream);
		System.out.println("Location is "+playerLocation);
		String gameDate = parseString(stream);
		System.out.println("Date is "+gameDate);
		String playerRace = parseString(stream);
		System.out.println("Race is "+playerRace);		
		int u1 = parseInt16(stream);
		float u2 = Float.intBitsToFloat(parseInt32(stream));
		float u3 = Float.intBitsToFloat(parseInt32(stream));
		System.out.println("Unknown data "+u1+" "+u2+" "+u3);
		Date filetime = parseFiletime(stream);
		System.out.println("Filetime is "+filetime);
		ImageIcon screenshot = parseScreenshotData(stream);
		int formVersion = stream.read();
		System.out.println("Form version is "+formVersion);
		int pluginInfoSize = parseInt32(stream);
		System.out.println("PluginInfo size is "+pluginInfoSize);
		stream.skip(pluginInfoSize);
		for(int i=0; i<25; ++i){
			int val = parseInt32(stream);
			System.out.println("FileLocationTable value "+val);
		}
		parseGlobalData(stream);
		parseGlobalData(stream);
		parseGlobalData(stream);
		parseGlobalData(stream);
		System.out.println("Done");
		
		SaveData save = new SaveData();
		save.setName(name);
		save.setDate(gameDate);
		save.setLevel(playerLevel);
		save.setLocation(playerLocation);
		save.setRace(playerRace);
		save.setScreenshot(screenshot);
		save.setFiletime(filetime);
		return save;
	}
	
	public static void main(String args[]){
		SaveData save;
		try {
			InputStream s = new FileInputStream(System.getProperty("user.home")+"\\Documenti\\My Games\\Skyrim\\Saves\\autosave3.ess");
			save = Parser.parse(s);
			s.close();
			GUI.showQuickPick(save);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
