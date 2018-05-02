package com.filetool.main;

import com.elasticcloudservice.predict.Predict;
import com.filetool.util.FileUtil;
import com.filetool.util.LogUtil;

/**
 * 
 * @author hzl
 *
 */
public class Main {
	//本地测试开关
	public static boolean debug = false;
	//模型切换
	public static int model = 1;
	//测试样本
	public static String test = "test2";
	
	public static void main(String[] args) {

		String ecsDataPath;
		String inputFilePath;
		String resultFilePath;
		
		if(debug == true) {
			ecsDataPath = "E:\\Desktop\\test\\test2\\train.txt";
			inputFilePath = "E:\\Desktop\\test\\test2\\input.txt";
			resultFilePath = "E:\\Desktop\\test\\test2\\output.txt"; 
		}
		else {
			ecsDataPath = args[0];
			inputFilePath = args[1];
			resultFilePath = args[2];
		}
		
		
		LogUtil.printLog("Begin");

		// 读取输入文件
		String[] ecsContent = FileUtil.read(ecsDataPath, null);
		String[] inputContent = FileUtil.read(inputFilePath, null);

		// 功能实现入口
		String[] resultContents = Predict.predictVm(ecsContent, inputContent);

		// 写入输出文件
		if (hasResults(resultContents)) {
			FileUtil.write(resultFilePath, resultContents, false);
		} else {
			FileUtil.write(resultFilePath, new String[] { "NA" }, false);
		}
		LogUtil.printLog("End");
	}

	private static boolean hasResults(String[] resultContents) {
		if (resultContents == null) {
			return false;
		}
		for (String contents : resultContents) {
			if (contents != null && !contents.trim().isEmpty()) {
				return true;
			}
		}
		return false;
	}

}
