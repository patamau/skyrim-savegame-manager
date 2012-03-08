package it.patamau.ssm.parser;

import it.patamau.ssm.data.SaveData;
import it.patamau.util.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ImageIcon;

public class Parser {
	
	private static final Logger logger = Logger.getLogger(Parser.class.getName());
	
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
		logger.debug("Magic ok");
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
		logger.debug("Screenshot size ",shotWidth,"x",shotHeight);
		int imgsize = 3*shotWidth*shotHeight;
		byte[] data = new byte[imgsize];
		int len, pos = 0;
		while((len = s.read(data, pos, imgsize-pos))>=0){
			pos+=len;
			logger.debug("Image read status ",pos,"/",imgsize," = ",((float)pos/(float)imgsize*100f),"%");
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
		logger.debug("Plugins ",pluginCount);
		for(int i=0; i<pluginCount; ++i){
			String plugin = parseString(s);
			logger.debug("Plugin ",plugin);
		}
	}
	
	public static final void parseMiscStats(final InputStream s) throws IOException {
		int count = parseInt32(s);
		for(int i=0; i<count; ++i){
			String name = parseString(s);
			int category = s.read();
			int value = parseInt32(s);
			logger.debug(name," ",category," ",value);
		}
	}
	
	public static final int parseRefId(final InputStream s) throws IOException {
		int b = s.read();
		int code = (b & 0xC0) >> 6;
		//XXX: I don't really need to know where the RefID is pointing at since I don't have the resources
		/*
		switch(code){
		case 0:
			logger.debug("RefID is FormID index");
			break;
		case 1:
			logger.debug("RefID is a Skyrim.esm index");
			break;
		case 2:
			logger.debug("RefID is a plugin index");
			break;
		default:
			logger.debug("RefID type is unknown");
			break;
		}
		*/
		int num = (b & 0x3F) << 8;
		num |= (s.read() & 0xFF);
		num <<= 8;
		num |= (s.read() & 0xFF);
		return num;
	}
	
	public static final int parseVsVal(final InputStream s) throws IOException {
		//memo: F=1111 8=1000 C=1100 3=0011
		int b = s.read();
		int val = (b & 0xC0) >> 6;
		logger.debug("vsval key is ",val);
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
		logger.debug("Global variables are ",num);
		for(int i=0; i<num; ++i){
			int ref = parseRefId(s);
			float value = Float.intBitsToFloat(parseInt32(s));
			logger.debug(i,": ",Integer.toHexString(ref),"=",value);
		}
	}
	
	public static final void parsePlayerLocation(final InputStream s) throws IOException {
		//FIXME: this isn't working!!!!!	
		/* 	
		int _xa = parseInt32(s);
		//1 11010100 01100000000000011111111
		float xa = Float.intBitsToFloat(_xa);
		int angleID = parseRefId(s);
		float ya = Float.intBitsToFloat(parseInt32(s));
		float za = Float.intBitsToFloat(parseInt32(s));
		logger.debug("RefID "+Integer.toHexString(angleID));
		int _x = parseInt32(s);
		//0 11100111 10001110011001101000001       
		logger.debug("X: "+Integer.toBinaryString(_x));
		float x = Float.intBitsToFloat(_x);
		float y = Float.intBitsToFloat(parseInt32(s));
		int coordID = parseRefId(s);
		logger.debug("RefID "+Integer.toHexString(coordID));
		float z = Float.intBitsToFloat(parseInt32(s));
		logger.debug("Player location "+x+","+y+","+z+"@"+xa+","+ya+","+za);
		*/
		s.skip(30);
	}
	
	private static final void parseGlobalData(final InputStream s) throws IOException {
		int type = parseInt32(s);
		int length = parseInt32(s);
		logger.debug("GlobalData type=",type," length=",length);
		switch(type){
			case 0: //Misc Stats
				parseMiscStats(s);
			break;
			case 1:
				parsePlayerLocation(s);
			break;
			case 3:
				parseGlobalVariables(s);
			break;
			default:
				logger.debug("Unsupported GlobalData type ",type);
				s.skip(length);
			break;
		}
	}
	
	public static void parseChangeForm(final InputStream stream) throws IOException{
		int formID = parseRefId(stream);
		int changeFlags = parseInt32(stream);
		int _type = stream.read() & 0xFF;
		int size = (_type & 0xC0) >> 6;
		int type = _type & 0x3F;
		int version = stream.read() & 0xFF;
		int length1 = 0, length2 = 0;
		switch(size){
		case 0:
			length1 = stream.read() & 0xFF;
			length2 = stream.read() & 0xFF;
			break;
		case 1:
			length1 = parseInt16(stream);
			length2 = parseInt16(stream);
			break;
		case 2:
			length1 = parseInt16(stream);
			length2 = parseInt16(stream);
			break;
		default:
			logger.debug("Undefined changeForm data length ",size);
			break;
		} 
		if(type==18){
			logger.debug("ChangeForm "+Integer.toHexString(formID)
					," ",Integer.toBinaryString(changeFlags)
					," ["+size+"] ",type,"x",length1,"b compressed ",length2,"b (",version,")");
			for(int i=0; i<length1; ++i){
				int val = stream.read() & 0xFF;
				logger.debug("DATA[",i,"] ",val);
			}
		}else{
			stream.skip(length1);
		}
	}
	
	public static SaveData parse(final InputStream stream) throws IOException{
		parseMagic(stream);
		int hsize = parseInt32(stream);
		logger.debug("Header size is ",hsize);
		int version = parseInt32(stream);
		logger.debug("Version is ",version);
		int saveNumber = parseInt32(stream);
		logger.debug("Save number is ",saveNumber);
		String name = parseString(stream);
		logger.debug("Name is ",name);
		int playerLevel = parseInt32(stream);
		logger.debug("Level is ",playerLevel);
		String playerLocation = parseString(stream);
		logger.debug("Location is ",playerLocation);
		String gameDate = parseString(stream);
		logger.debug("Date is ",gameDate);
		String playerRace = parseString(stream);
		logger.debug("Race is ",playerRace);		
		int u1 = parseInt16(stream);
		float u2 = Float.intBitsToFloat(parseInt32(stream));
		float u3 = Float.intBitsToFloat(parseInt32(stream));
		logger.debug("Unknown data ",u1," ",u2," ",u3);
		Date filetime = parseFiletime(stream);
		logger.debug("Filetime is ",filetime);
		//FIXME: only load the screenshot when required to save parsing time and memory (HUGE improvements)
		ImageIcon screenshot = parseScreenshotData(stream);
		
		//XXX: I'm sorry but the following data is pretty useless except GlobalData/MiscData which I'll give support soon :)
		//XXX: lazy tosser
		/*
		int formVersion = stream.read();
		logger.debug("Form version is ",formVersion);
		int pluginInfoSize = parseInt32(stream);
		logger.debug("PluginInfo size is ",pluginInfoSize);
		stream.skip(pluginInfoSize);
		int changeFormCount = 0;
		for(int i=0; i<25; ++i){
			int val = parseInt32(stream);
			if(9==i) changeFormCount=val;
			logger.debug("FileLocationTable value ",val);
		}
		for(int i=0; i<23; ++i){
			parseGlobalData(stream);
		}
		logger.debug("ChangeFormCount ",changeFormCount);
		for(int i=0; i< changeFormCount; ++i){
			parseChangeForm(stream);
		}
		logger.debug("Done");
		*/
		
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
}
