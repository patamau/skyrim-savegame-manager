package it.patamau.util;

import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class TestLogger {
	
	@Test
	public void testLoggerLog() throws Exception {
		Logger logger = Logger.getLogger("test");
		logger.log(Logger.L_DEBUG, "Hello", " world", "! ","DEBUG");
		Logger.setLevel(Logger.L_ERROR);
		Thread.sleep(1000);
		logger.log(Logger.L_INFO, "Hello", " world", "! ", "INFO"); //should not print
		Logger.addStream(new PrintStream(new FileOutputStream("test.log")));
		logger.log(Logger.L_ERROR, "this should be printed on file too");
		Thread.sleep(1000);
		Logger.clearStreams();
		Logger.addStream(new PrintStream(System.err));
		logger.log(Logger.L_FATAL, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHHHHH!!!");
	}

	@Test
	public void testLoggerBasics(){
		Logger logger = Logger.getLogger("test");
		assertNotNull(logger);
		Logger logger2 = Logger.getLogger("test");
		assertEquals(logger, logger2);
	}
}
