package it.patamau.ssm.test;

import it.patamau.ssm.data.SaveData;
import it.patamau.ssm.parser.Parser;
import it.patamau.util.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestParser {
	
	@BeforeClass
	public static void init(){
		Logger.setLevel(Logger.L_INFO);
	}
	
	@Test
	public void stressParser() throws Exception{
		List<SaveData> list = new LinkedList<SaveData>();
		for(int i=0; i<100; ++i){
			list.add(testParser());
		}
	}

	public SaveData testParser() throws Exception{
		InputStream s = new FileInputStream(System.getProperty("user.home")+"\\Documenti\\My Games\\Skyrim\\Saves\\quicksave.ess");
		SaveData save = Parser.parse(s);
		s.close();
		return save;
	}
}
