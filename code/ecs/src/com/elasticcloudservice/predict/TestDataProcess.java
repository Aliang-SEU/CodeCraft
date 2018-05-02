package com.elasticcloudservice.predict;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TestDataProcess {
	public static Map<String,Integer> read(String filePath,String startTime,String endTime)
	{
		 Map<String,Integer> testFlavors=new HashMap<>();
		 File file = new File(filePath);
	        // 当文件不存在或者不可读时
	        if ((!isFileExists(file)) || (!file.canRead()))
	        {
	            System.out.println("file [" + filePath + "] is not exist or cannot read!!!");
	            return null;
	        }
	        
	        BufferedReader br = null;
	        FileReader fb = null;
	        try
	        {
	            fb = new FileReader(file);
	            br = new BufferedReader(fb);

	            String str = null;
	            boolean startSign=false;
	            while ((str = br.readLine()) != null)
	            {            	
	            	String[] words=str.split(" ");  
	            	if(words[0].equals(endTime))
            			break;
	            	if(words[0].equals(startTime)||startSign)
	            	{
	            		startSign=true;	
	            		for(int i=2;i<words.length;i++)
	            		{
	            			String flavorName="flavor"+(i-1);
	            			if(testFlavors.containsKey(flavorName))
	            				testFlavors.put(flavorName, testFlavors.get(flavorName)+Integer.valueOf(words[i]));
	            			else
	            				testFlavors.put(flavorName,Integer.valueOf(words[i]));
	            				
	            		}
	            	}
	            }
	        }
	        catch (IOException e)
	        {
	            e.printStackTrace();
	        }
	        finally
	        {
	            closeQuietly(br);
	            closeQuietly(fb);
	        }

	        return testFlavors;		
	}
    private static void closeQuietly(Closeable closeable)
    {
        try
        {
            if (closeable != null)
            {
                closeable.close();
            }
        }
        catch (IOException e)
        {
        }
    }
	private static boolean isFileExists(final File file)
    {
        if (file.exists() && file.isFile())
        {
            return true;
        }

        return false;
    }

}
