package it.patamau.parser;

import it.patama.data.SaveData;
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
	
	public static final Date parseFiletime(final InputStream stream) throws IOException{
		byte[] t = new byte[8];
		stream.read(t);
		long ft = bytesToLong(t);
		ft/=10000; //nanoseconds
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(ft);
		c.set(Calendar.YEAR, c.get(Calendar.YEAR)-369); //MSDN starts 1601, Java starts 1970 
		return c.getTime();
	}
	
	private static final long parseLong64(final InputStream s) throws IOException{
		byte[] data = new byte[8];
		if(s.read(data)<0) throw new IOException("Unexpected end of stream");
		return bytesToLong(data);
	}
	
	private static final int parseInt16(final InputStream s) throws IOException{
		byte[] data = new byte[2];
		if(s.read(data)<0) throw new IOException("Unexpected end of stream");
		return bytesToInt(data);
	}
	
	private static final int parseInt32(final InputStream s) throws IOException{
		byte[] data = new byte[4];
		if(s.read(data)<0) throw new IOException("Unexpected end of stream");
		return bytesToInt(data);
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
		byte[] str = new byte[(int)siz];
		s.read(str);
		return new String(str);
	}
	
	private static final ImageIcon parseScreenshotData(final InputStream s) throws IOException{
		long shotWidth = parseInt32(s);
		long shotHeight = parseInt32(s);
		System.out.println("Screenshot size "+shotWidth+"x"+shotHeight);
		int imgsize = (int) (3*shotWidth*shotHeight);
		byte[] data = new byte[imgsize];
		int len, pos = 0;
		while((len = s.read(data, pos, imgsize-pos))>=0){
			pos+=len;
			System.out.println("Image read status "+pos+"/"+imgsize+" = "+((float)pos/(float)imgsize*100f)+"%");
			if(pos>=imgsize) break;
		}
		
		BufferedImage img = new BufferedImage((int)shotWidth, (int)shotHeight, BufferedImage.TYPE_INT_RGB);
		int i=0;
		for(int y=0; y<shotHeight; ++y){
			for(int x=0; x<shotWidth; ++x){
				int color = bytesToColor(data[i++],data[i++],data[i++]);
				img.setRGB(x, y, color);
			}
		}
		return new ImageIcon(img);
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
			InputStream s = new FileInputStream("C:\\Users\\Matteo Pedrotti\\Documents\\My Games\\Skyrim\\Saves\\quicksave.ess");
			save = Parser.parse(s);
			s.close();
			GUI.showQuickPick(save);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
