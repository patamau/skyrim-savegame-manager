package it.patamau.util;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * My fucking stupid logger.
 * Hell yeah
 * @author Matteo Pedrotti
 *
 */
public class Logger {
	
	public static final int 
		L_DEBUG = 0, //useful information for developers
		L_INFO = 1, //useful information for users
		L_WARN = 2, //an application problem (could be solved)
		L_ERROR = 3, //an application error
		L_FATAL = 4; //something causing the application to quit
	
	private static final String[] _levels = new String[]{
		"DEBUG",
		"INFO ",
		"WARN ",
		"ERROR",
		"FATAL"
	};

	private static int level = L_DEBUG;
	private final static Map<String, Logger> loggers = new HashMap<String,Logger>();
	private final static StringBuilder sbuild = new StringBuilder();
	private final static Collection<PrintStream> streams = new LinkedList<PrintStream>();
	private final static RuntimeMXBean runtimeMan = ManagementFactory.getRuntimeMXBean();
	
	static{
		streams.add(System.out);
	}
	
	private final String name;
	
	/**
	 * for you lazy bitch:
	 *    new Logger(MyClass.class.getName());
	 * @param clazz
	 */
	private Logger(final Class<?> clazz){
		this.name = clazz.getName();
	}
	
	private Logger(final String name){
		this.name = name;
	}
	
	public static void setLevel(final int _level){
		level = _level;
	}
	
	public static int getLevel(){
		return level;
	}
	
	public static void addStream(final PrintStream stream){
		streams.add(stream);
	}
	
	public static void removeStream(final PrintStream stream){
		streams.remove(stream);
	}
	
	public static Collection<PrintStream> getStreams(){
		return streams;
	}
	
	public static void clearStreams(){
		streams.clear();
	}
	
	public static Logger getLogger(final String name){
		Logger logger = loggers.get(name);
		if(null==logger){
			logger = new Logger(name);
			loggers.put(name, logger);
		}
		return logger;
	}
	
	private static void print(final String msg){
		for(PrintStream p: streams){
			p.println(msg);
		}
	}
	
	public Logger log(final int _level, final Object ... messages){
		if(level>_level) return this;
		synchronized(sbuild){
			sbuild.setLength(0);
			sbuild.append("[");
			sbuild.append(_levels[_level]);
			sbuild.append("]@");
			sbuild.append(runtimeMan.getUptime());
			sbuild.append(" ");
			sbuild.append(name);
			sbuild.append(" - ");
			for(Object m: messages){
				sbuild.append(m);
			}
			print(sbuild.toString());
		}
		return this;
	}
	
	public Logger debug(final Object ... messages){
		return log(L_DEBUG, messages);
	}
	
	public Logger info(final Object ... messages){
		return log(L_INFO, messages);
	}
	
	public Logger warning(final Object ... messages){
		return log(L_WARN, messages);
	}
	
	public Logger error(final Object ... messages){
		return log(L_ERROR, messages);
	}
	
	public Logger fatal(final Object ... messages){
		return log(L_FATAL, messages);
	}
}
