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
	
	private final byte[] buffer;
	private InputStream stream;
	
	public static final long bytesToLong(final byte[] b, final int len){
	    long num = 0;
	    for(int i=len; i>0; ){
	    	num |= b[--i] & 0xFF;
	    	if(i>0) num <<= 8;
	    }
	    return num;
	}
	
	public static final int bytesToInt(final byte[] b, final int len){
	    int num = 0;
	    for(int i=len; i>0; ){
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
	
	public Parser(){
		this.buffer = new byte[65535];
	}
	
	public Date parseFiletime() throws IOException{
		stream.read(buffer,0,8);
		long ft = bytesToLong(buffer,8);
		ft/=10000; //nanoseconds
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(ft);
		c.set(Calendar.YEAR, c.get(Calendar.YEAR)-369); //MSDN starts 1601, Java starts 1970 
		return c.getTime();
	}
	
	private int parseInt16() throws IOException{
		if(stream.read(buffer, 0, 2)<0) throw new IOException("Unexpected end of stream");
		return bytesToInt(buffer, 2);
	}
	
	private int parseInt24() throws IOException{
		if(stream.read(buffer, 0, 3)<0) throw new IOException("Unexpected end of stream");
		return bytesToInt(buffer, 3);
	}
	
	private int parseInt32() throws IOException{
		if(stream.read(buffer, 0, 4)<0) throw new IOException("Unexpected end of stream");
		return bytesToInt(buffer, 4);
	}
	
	private long parseLong64() throws IOException{
		if(stream.read(buffer,0,8)<0) throw new IOException("Unexpected end of stream");
		return bytesToLong(buffer,8);
	}
	
	private void parseMagic() throws IOException{
		if(stream.read(buffer,0,13)<0) throw new IOException("Unexpected end of stream");
		logger.debug("Magic ok");
	}
	
	private String parseString() throws IOException{
		int siz = parseInt16();
		stream.read(buffer,0,siz);
		return new String(buffer, 0, siz);
	}
	
	private ImageIcon parseScreenshotData() throws IOException{
		int shotWidth = parseInt32();
		int shotHeight = parseInt32();
		logger.debug("Screenshot size ",shotWidth,"x",shotHeight);
		int imgsize = 3*shotWidth*shotHeight;
		byte[] data = new byte[imgsize];
		int len, pos = 0;
		while((len = stream.read(data, pos, imgsize-pos))>=0){
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
	
	public void parsePluginInfo() throws IOException {
		int pluginCount = stream.read();
		logger.debug("Plugins ",pluginCount);
		for(int i=0; i<pluginCount; ++i){
			String plugin = parseString();
			logger.debug("Plugin ",plugin);
		}
	}
	
	public void parseMiscStats() throws IOException {
		int count = parseInt32();
		for(int i=0; i<count; ++i){
			String name = parseString();
			int category = stream.read() & 0xFF;
			int value = parseInt32();
			logger.debug(name," ",category," ",value);
		}
	}
	
	public int parseRefId() throws IOException {
		int b = stream.read() & 0xFF;
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
		num |= (stream.read() & 0xFF);
		num <<= 8;
		num |= (stream.read() & 0xFF);
		return num;
	}
	
	public int parseVsVal() throws IOException {
		//memo: F=1111 8=1000 C=1100 3=0011
		int b = stream.read() & 0xFF;
		int val = (b & 0xC0) >> 6;
		logger.debug("vsval key is ",val);
		int num = (b & 0x3F);
		for(int i=0; i<val; ++i){
			num <<= 8;
			int a = stream.read() & 0xFF;
			num |= a;
		}
		int st = Integer.numberOfLeadingZeros(num);
		num <<= st;
		num = Integer.reverse(num);
		return num;
	}
	
	public void parseGlobalVariables() throws IOException {
		int num = parseVsVal();
		logger.debug("Global variables are ",num);
		for(int i=0; i<num; ++i){
			int ref = parseRefId();
			float value = Float.intBitsToFloat(parseInt32());
			logger.debug(i,": ",Integer.toHexString(ref),"=",value);
		}
	}
	
	public void parsePlayerLocation() throws IOException {
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
		stream.skip(30);
	}
	
	private void parseGlobalData() throws IOException {
		int type = parseInt32();
		int length = parseInt32();
		logger.debug("GlobalData type=",type," length=",length);
		switch(type){
			case 0: //Misc Stats
				parseMiscStats();
			break;
			case 1:
				parsePlayerLocation();
			break;
			case 3:
				parseGlobalVariables();
			break;
			default:
				logger.debug("Unsupported GlobalData type ",type);
				stream.skip(length);
			break;
		}
	}
	
	public void parseChangeForm() throws IOException{
		int formID = parseRefId();
		int changeFlags = parseInt32();
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
			length1 = parseInt16();
			length2 = parseInt16();
			break;
		case 2:
			length1 = parseInt16();
			length2 = parseInt16();
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
	
	public SaveData parse(final InputStream stream) throws IOException{
		this.stream = stream;
		parseMagic();
		int hsize = parseInt32();
		logger.debug("Header size is ",hsize);
		int version = parseInt32();
		logger.debug("Version is ",version);
		int saveNumber = parseInt32();
		logger.debug("Save number is ",saveNumber);
		String name = parseString();
		logger.debug("Name is ",name);
		int playerLevel = parseInt32();
		logger.debug("Level is ",playerLevel);
		String playerLocation = parseString();
		logger.debug("Location is ",playerLocation);
		String gameDate = parseString();
		logger.debug("Date is ",gameDate);
		String playerRace = parseString();
		logger.debug("Race is ",playerRace);		
		int u1 = parseInt16();
		float u2 = Float.intBitsToFloat(parseInt32());
		float u3 = Float.intBitsToFloat(parseInt32());
		logger.debug("Unknown data ",u1," ",u2," ",u3);
		Date filetime = parseFiletime();
		logger.debug("Filetime is ",filetime);
		//FIXME: only load the screenshot when required to save parsing time and memory (HUGE improvements)
		ImageIcon screenshot = parseScreenshotData();
		
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
