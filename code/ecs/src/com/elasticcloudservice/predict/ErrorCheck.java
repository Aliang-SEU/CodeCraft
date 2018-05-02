package com.elasticcloudservice.predict;

/**
 * 参数检测
 * @author hzl
 *
 */
public class ErrorCheck {
	public static void checkLength(int len1, int len2) {
		if(len1 != len2)
			throw new RuntimeException("parameter length not match");
	}
}
