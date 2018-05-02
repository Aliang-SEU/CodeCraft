package com.elasticcloudservice.predict;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTools {
	
	public static String startTime; 
	
	public static int getIndex(String data){
		try {
			return daysBetween(startTime, data);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	} 
	
	/**
	 * 获取给定时间之间相差的天数
	 * @param smdate
	 * @param bdate
	 * @return
	 * @throws ParseException
	 */
	public static int daysBetween(String smdate,String bdate) throws ParseException{  
	    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
	    Calendar cal = Calendar.getInstance();    
	    cal.setTime(sdf.parse(smdate));    
	    long time1 = cal.getTimeInMillis();                 
	    cal.setTime(sdf.parse(bdate));    
	    long time2 = cal.getTimeInMillis();         
	    long between_days=(time2-time1) / (1000*3600*24);  
            
	    return Integer.parseInt(String.valueOf(between_days));     
    }  
}
