package gov.gsa.ocfo.aloha.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class FileUtil {
	private final static String RESOURCES_PATH = "/resources/sql/";
	
	public static Properties getPropertiesFile(String fileName)  throws IOException{
		InputStream inStream = FileUtil.class.getResourceAsStream(RESOURCES_PATH + fileName);
		Properties props = new Properties();
		props.load(inStream);
		inStream.close();
		return props;
	}		
	public static String readTextFile(String fileName) throws IOException  {
        StringBuffer fileData = new StringBuffer();
		InputStream inStream = FileUtil.class.getResourceAsStream(RESOURCES_PATH + fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
