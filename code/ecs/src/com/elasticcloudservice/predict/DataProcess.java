package com.elasticcloudservice.predict;

import java.util.LinkedHashMap;
import java.util.Map;

import com.algorithm.BPNetWork;
import com.algorithm.LstmNetwork;
import com.algorithm.NeuralNetwork;
import com.algorithm.arima.ARIMA;
import com.basic.data.Flavor;
import com.basic.data.Server;
import com.filetool.main.Main;

/**
 * 数据处理与算法模型
 * @author hzl
 *
 */
public class DataProcess {
	
	private Server[] servers;	//服务器的规格
	private Map<String, Flavor>  VMMap = new LinkedHashMap<>();	//保存需要预测的虚拟机
	public String predictStartTime, predictEndTime;  //起止预测时间
	private String trainStartTime, trainEndTime; 	//训练数据内起止训练时间
	private double[][] historyDataIndexByFlavor;	//训练数据
	private double[][] historyDataIndexByData;	//训练数据
	private int trainRange;
	private Map<String, Integer> flavorMap = new LinkedHashMap<>();
	private static final int flavorRange = 18;
	
	public boolean cpuOrMem;//预测类型
	
	public LinkedHashMap<String, Flavor> getVMMap() {
		return (LinkedHashMap<String, Flavor>) VMMap;
	}

	/**
	 *  数据处理
	 */
	
	
	public Map<String, Integer> dataPredictModel0(){
		double[][] processedData = new double[flavorRange][trainRange];
		int predictRange = DateTools.getIndex(predictEndTime) - DateTools.getIndex(predictStartTime);
		int N = 8;
		/*
		 * 只处理需要预测的数据
		 */

		for(Flavor flavor : VMMap.values()) {
			double[][] x = new double[trainRange - N][N];
			double[][] y = new double[trainRange - N][1]; 
	
			int id = flavor.id - 1;
			
			
			int max = (int)historyDataIndexByFlavor[id][0];
			int secondMax = (int) historyDataIndexByFlavor[id][0];
			int pos = 0;
			for(int i = 0; i < trainRange; ++i)
				if(historyDataIndexByFlavor[id][i] > max ) {
					secondMax = max;
					max = (int) historyDataIndexByFlavor[id][i];
					pos = i;
				}
			if(max - secondMax > sum(historyDataIndexByFlavor[id], 0, trainRange) / trainRange)
				historyDataIndexByFlavor[id][pos] = 0;
			
			System.arraycopy(historyDataIndexByFlavor[id], 0 , processedData[id], 0, trainRange);
	
			for(int i = N; i < trainRange; ++i) {
				System.arraycopy(processedData[id], i - N , x[i - N], 0, N);
				y[i - N][0] = processedData[id][i];
			}
			
			/**
			 * 在线训练
			 */
			
			LstmNetwork lstm_net = new LstmNetwork(x.length, x[0].length);
			for(int i = 0; i <2000; ++i) {
				//System.out.println(flavor.name + " iter:" + i + " ");
				double loss = lstm_net.train(x, y, 0.0001);
				//System.out.println("loss:" + loss);
			}
			
			if(Main.debug == true) {
				System.out.print(flavor.name);
				System.out.print(": [");
				for(int idx = 0; idx < y.length; ++idx) {
					System.out.print((int)y[idx][0] + " ");
				}
				System.out.println("]");
				System.out.print(flavor.name);
				System.out.print(": [");
				for(int idx = 0; idx < y.length; ++idx) {
				double[] result = lstm_net.getPredict(idx);
					System.out.print(Math.round(result[0]) + " ");
				}
				System.out.println("]");
				System.out.println();
			}
			double[] result = lstm_net.getPredict(y.length - 1);
			double[] temp = new double[y[0].length];
			System.arraycopy(result, 0, temp, 0, temp.length);

			//进行预测
			double[] cur_x = arrayShift(x[trainRange - N - 1], temp);
			
			//预测到第N天 代表前N-1的总和
			double sum = 0;
			for(int i = 0; i < predictRange; ++i) {
				result = lstm_net.predict(cur_x);
				System.arraycopy(result, 0, temp, 0, temp.length);
				cur_x = arrayShift(cur_x, temp);
				sum += result[0];
			}
			int predictNum = (int) Math.round(sum);
			flavorMap.put(flavor.name, predictNum);
		}
		
		return flavorMap;
	}
	public Map<String, Integer> dataPredictModel2() {
		double[][] processedData = new double[flavorRange][trainRange];
		int predictRange = DateTools.getIndex(predictEndTime) - DateTools.getIndex(predictStartTime);
		int N = predictRange + 1;
		/*
		 * 只处理需要预测的数据
		 */

		for(Flavor flavor : VMMap.values()) {
			double[][] x = new double[trainRange - predictRange][predictRange];
			double[][] y = new double[trainRange - predictRange][1]; 
	
			int id = flavor.id;
			int index = id - 1;
			
			for(int i = N; i <= trainRange; ++i) {
				processedData[index][i - 1] = sum(historyDataIndexByFlavor[index], i - N, i);
				System.arraycopy(processedData[index], i - N , x[i - N], 0, predictRange);
				y[i - N][0] = processedData[index][i - 1];
			}
			
			/**
			 * 在线训练
			 */
			
			LstmNetwork lstm_net = new LstmNetwork(x.length, x[0].length);
			for(int i = 0; i <5000; ++i) {
				//System.out.println(flavor.name + " iter:" + i + " ");
				double loss = lstm_net.train(x, y, 0.001);
				//System.out.println("loss:" + loss);
			}
			System.out.print(flavor.name);
			System.out.print(": [");
			for(int idx = 0; idx < y.length; ++idx) {
				System.out.print((int)y[idx][0] + " ");
			}
			System.out.println("]");
			System.out.print(flavor.name);
			System.out.print(": [");
			for(int idx = 0; idx < y.length; ++idx) {
			double[] result = lstm_net.getPredict(idx);
				System.out.print(Math.round(result[0]) + " ");
			}
			System.out.println("]");
			System.out.println();
			double[] result = lstm_net.getPredict(y.length - 1);
			double[] temp = new double[y[0].length];
			System.arraycopy(result, 0, temp, 0, temp.length);

			//进行预测
			double[] cur_x = arrayShift(x[trainRange - predictRange - 1], temp);
			
			//预测到第N天 代表前N-1的总和
			for(int i = 0; i < N; ++i) {
				result = lstm_net.predict(cur_x);
				System.arraycopy(result, 0, temp, 0, temp.length);
				cur_x = arrayShift(cur_x, temp);
				for(int j = 0; j < temp.length; ++j) {
					//System.out.print(Math.round(temp[j]) + " ");
				}
			}
			int predictNum = (int) Math.round(temp[0]);
			flavorMap.put(flavor.name, predictNum);
		}
		
		return flavorMap;
	}
	/**
	 * BP神经网络
	 * @return
	 */
	public Map<String, Integer> dataPredictModel3() {
		double[][] processedData = new double[flavorRange][trainRange];
		int predictRange = DateTools.getIndex(predictEndTime) - DateTools.getIndex(predictStartTime);
		int N = predictRange + 1;
		/*
		 * 只处理需要预测的数据
		 */

		for(Flavor flavor : VMMap.values()) {
			double[][] x = new double[trainRange - predictRange][predictRange];
			double[][] y = new double[trainRange - predictRange][1]; 
	
			int id = flavor.id - 1;
			boolean filter = false;
			

			int max = (int) historyDataIndexByFlavor[id][0];
			int secondMax = (int) historyDataIndexByFlavor[id][0];
			int pos = 0;
			for (int i = 0; i < trainRange; ++i)
				if (historyDataIndexByFlavor[id][i] > max) {
					secondMax = max;
					max = (int) historyDataIndexByFlavor[id][i];
					pos = i;
				}
			if (filter == true) {
				if (max - secondMax > sum(historyDataIndexByFlavor[id], 0, trainRange) / trainRange)
					historyDataIndexByFlavor[id][pos] = 0;
			}
			int lastSum = (int) (sum(historyDataIndexByFlavor[id], trainRange - predictRange, trainRange));

			//System.arraycopy(historyDataIndexByFlavor[id], 0, processedData[id], 0, N);
			//数据归一化
			for(int i = 0; i < trainRange; i++) {
				historyDataIndexByFlavor[id][i] = historyDataIndexByFlavor[id][i] * 1.0 / max;
			}
			
			for(int i = 0; i <= trainRange; ++i) {
				if(i < N) {
					processedData[id][i] = historyDataIndexByFlavor[id][i];
				}else {
					processedData[id][i - 1] = sum(historyDataIndexByFlavor[id], i - N, i);
					System.arraycopy(processedData[id], i - N , x[i - N], 0, predictRange);
					y[i - N][0] = processedData[id][i - 1];
				}
			}
			/**
			 * 在线训练
			 */
			
			NeuralNetwork bp = new NeuralNetwork(x[0].length,  x[0].length + 1, 1);
			bp.train(x, y, 1000, 0.001, 0.1);
			double[] result = bp.predict(x[x.length - 1]);
			double[] temp = new double[y[0].length];

			//进行预测
			double[] cur_x = x[x.length - 1];
			
			//预测到第N天 代表前N-1的总和
			for(int i = 0; i < N; ++i) {
				result = bp.predict(cur_x);
				System.arraycopy(result, 0, temp, 0, temp.length);
				cur_x = arrayShift(cur_x, temp);
			}

			int predictNum = (int) Math.round(temp[0]) * max;
			/**
			 * 保证预测范围
			 */
			
			/*if(predictNum < lastSum / 1.4)
				predictNum = (int) (lastSum / 1.3) + 1;
			else if(predictNum > lastSum * 1.3)
				predictNum = (int) (lastSum * 1.2) + 1;
			*/
			if(predictNum < lastSum / 1.4 || predictNum > lastSum * 1.3) {
				predictNum = lastSum;
			}
			/*
			if(predictNum < lastSum / 1.78)
				predictNum = (int) (lastSum / 1.6);
			if(predictNum > lastSum * 1.3)
				predictNum = (int) (lastSum * 1.21) - 1;*/
			
			flavorMap.put(flavor.name, predictNum);
		}
		return flavorMap;
	}
	public Map<String, Integer> dataPredictModel6(){
		int predictRange = DateTools.getIndex(predictEndTime) - DateTools.getIndex(predictStartTime) + 2;
		
		for(Flavor flavor : VMMap.values()) {

			int id = flavor.id - 1;
			double max = 0.0;
			double min = 0.0;
			double sum = 0.0;
			for(int i = 0; i < trainRange; i++) {
				sum +=  historyDataIndexByFlavor[id][i];
				min = Math.min(min, historyDataIndexByFlavor[id][i]);
				max = Math.max(max, historyDataIndexByFlavor[id][i]);	
			}
			max = 20.0;
			
			double[][] x = new double[trainRange - predictRange][predictRange];
			double[][] y = new double[trainRange - predictRange][1]; 
	
			for(int i = 0; i < trainRange - predictRange; i++) {
				for(int j = 0; j < predictRange; j++) {
					x[i][j] = norm(historyDataIndexByFlavor[id][i + j], min, max);
				}
				y[i][0] = norm(historyDataIndexByFlavor[id][i + predictRange], min, max); 
			}
			
			double[] cur_x = new double[predictRange];
			for(int i = 0; i < predictRange; i++)
				cur_x[i] = (historyDataIndexByFlavor[id][trainRange - predictRange + i] - min) / max;
			//神经网络模型
			BPNetWork bp = new BPNetWork(x[0].length,  10 , 1);
			bp.train(x, y, 80000, 0.01, 0.0);
				
			double[] result = new double[1];
			double[] temp = new double[y[0].length];
			int difDay =  DateTools.getIndex(predictStartTime) - DateTools.getIndex(trainEndTime);
			for(int i = 0; i < difDay; ++i) {
				result = bp.predict(cur_x);
				System.arraycopy(result, 0, temp, 0, temp.length);
				cur_x = arrayShift(cur_x, temp);
			}
			
			double predictNum = 0.0;
			for(int i = 0; i < predictRange; ++i) {
				result = bp.predict(cur_x);
				predictNum += result[0] * max + min;
				System.arraycopy(result, 0, temp, 0, temp.length);
				cur_x = arrayShift(cur_x, temp);
			}
			System.out.println(flavor.name + ":" +Math.round(predictNum));
			flavorMap.put(flavor.name, (int)Math.round(predictNum));
		}
		return flavorMap;
	}
	private double norm(double x, double min, double max) {
		return (x - min) / max + 0.01;
	}
	public Map<String, Integer> dataPredictModel5(){
	
		double[][] processedData = new double[flavorRange][trainRange];
		int predictRange = DateTools.getIndex(predictEndTime) - DateTools.getIndex(trainEndTime);
		int N = predictRange + 1;
		/*
		 * 只处理需要预测的数据
		 */

		for(Flavor flavor : VMMap.values()) {
			double[][] x = new double[trainRange - predictRange][predictRange];
			double[][] y = new double[trainRange - predictRange][1]; 
	
			int id = flavor.id - 1;
			boolean filter = false;
			
			if(filter == true) {
				int max = (int)historyDataIndexByFlavor[id][0];
				int secondMax = (int) historyDataIndexByFlavor[id][0];
				int pos = 0;
				for(int i = 0; i < trainRange; ++i)
					if(historyDataIndexByFlavor[id][i] > max ) {
						secondMax = max;
						max = (int) historyDataIndexByFlavor[id][i];
						pos = i;
					}
				if(max - secondMax > sum(historyDataIndexByFlavor[id], 0, trainRange) / trainRange)
					historyDataIndexByFlavor[id][pos] = secondMax+sum(historyDataIndexByFlavor[id], 0, trainRange) / trainRange;
			}

			System.arraycopy(historyDataIndexByFlavor[id], 0, processedData[id], 0, N);
			for(int i = N; i <= trainRange; ++i) {
				processedData[id][i - 1] = sum(historyDataIndexByFlavor[id], i - N, i);
				System.arraycopy(processedData[id], i - N , x[i - N], 0, predictRange);
				y[i - N][0] = processedData[id][i - 1];
			}
			double[] data = new double[trainRange - predictRange];
			
			for(int i = 0; i <data.length; ++i) {
				data[i] = y[i][0];
			}
			
			ARIMA arima=new ARIMA(data); 
			
			int []model=arima.getARIMAmodel();
			
			int result = 0; 


			//预测到第N天 代表前N-1的总和
			for(int i = 0; i < N; ++i) {
				result = arima.aftDeal(arima.predictValue(model[0],model[1]));
			}
			
			int predictNum = (int) Math.round(result);
			
			int lastSum = (int) (sum(historyDataIndexByFlavor[id], trainRange - predictRange, trainRange));
			
			if(predictNum < lastSum / 1.6)
				predictNum = (int) (lastSum / 1.5);
			else if(predictNum > lastSum * 1.3)
				predictNum = (int) (lastSum * 1.2);
			
			flavorMap.put(flavor.name, predictNum);
		}
		return flavorMap;

	}
	/**
	 * 直连model
	 * @return
	 */
	public Map<String, Integer> dataPredictModel4() {
		double[][] processedData = new double[flavorRange][trainRange];
		int predictRange = DateTools.getIndex(predictEndTime) - DateTools.getIndex(predictStartTime);
		int N = predictRange + 1;
		/*
		 * 只处理需要预测的数据
		 */

		for(Flavor flavor : VMMap.values()) {
			int predictNum = 0;
			int id = flavor.id - 1;
			
			predictNum = (int) (sum(historyDataIndexByFlavor[id], trainRange - 14 , trainRange));
			predictNum = (int) (predictNum * (1.5 *  predictRange) / 14) ;
			
			flavorMap.put(flavor.name, predictNum);
		}
		return flavorMap;
	}
	private double[] arrayShift(double[] a, double[] num) {
		double[] result = new double[a.length];
		System.arraycopy(a, num.length, result, 0, a.length - num.length);
		System.arraycopy(num, 0, result, a.length - num.length, num.length);
		return result;
	}
	/**
	 * 范围求和
	 */
	private double sum(double[] a, int start, int end) {
		double sum = 0;
		for(int i = start; i < end; ++i)
			sum += a[i];
		return sum;
	}
	
	/**
	 * 处理预测数据
	 * @param inputData
	 */
	public void parseInput(String[] inputData) {
		
		int numSeverType = 3;
		int dataBlock = 0;
		int i = 0;
		
		while(i < inputData.length){
		
			//跳过空行
			if(inputData[i].trim().length() == 0) {
				i++;
				continue;
			}
			
			//第一类信息为服务器的数据 依次为 CPU MEM(单位*1000) 硬盘(暂时不考虑)
			if(dataBlock == 0) {
				//服务器类型数量
				if(i == 0) {
					numSeverType = Integer.parseInt(inputData[i]);
					servers = new Server[numSeverType];
					i++;
				}
				else {
					int begin = i;
					int end = i + numSeverType;
					while(i < end)
					{
						String[] lineData = inputData[i].split(" ");
						ErrorCheck.checkLength(lineData.length, 4);
						
						String type = lineData[0];
						int cpu = Integer.parseInt(lineData[1]);
						int mem = Integer.parseInt(lineData[2]);
						int disk = Integer.parseInt(lineData[3]);
						
						servers[i-begin] = new Server(type,cpu, mem * 1024, null);
						
						i++;
					}
					dataBlock++;
					}	
			}
			//第二类数据为需要预测的虚拟的类型
			else if(dataBlock == 1) {
				int flavorNum = Integer.parseInt(inputData[i]);
				
				while(flavorNum-- > 0) {
					i++;
					String[] lineData = inputData[i].split(" ");
					String flavorName = lineData[0];
					int cpu = Integer.parseInt(lineData[1]);
					int mem = Integer.parseInt(lineData[2]);
					
					VMMap.put(flavorName, new Flavor(flavorName, cpu, mem));
					
				}
				dataBlock++; i++;
			}
			//需要预测的时间
			else if(dataBlock == 2) {
				predictStartTime = inputData[i];
				predictEndTime = inputData[i+1];
				break;
			}
		}		
	}
	/**
	 * 处理需要预测的文件数据
	 * @param inputData
	 */
	/*public void parseInput(String[] inputData) {
		int dataBlock = 0;
		int i = 0;
		
		while(i < inputData.length){

			//跳过空行
			if(inputData[i].trim().length() == 0) {
				i++;
				continue;
			}
			
			//第一类信息为服务器的数据 依次为 CPU MEM(单位*1000) 硬盘(暂时不考虑)
			if(dataBlock == 0) {
				String[] lineData = inputData[i].split(" ");
				ErrorCheck.checkLength(lineData.length, 3);
				
				int cpu = Integer.parseInt(lineData[0]);
				int mem = Integer.parseInt(lineData[1]);
				int disk = Integer.parseInt(lineData[2]);
				
				server = new Server(cpu, mem * 1024, null);
				
				dataBlock++; i++;
			}
			//第二类数据为需要预测的虚拟的类型
			else if(dataBlock == 1) {
				int flavorNum = Integer.parseInt(inputData[i]);
				
				while(flavorNum-- > 0) {
					i++;
					String[] lineData = inputData[i].split(" ");
					String flavorName = lineData[0];
					int cpu = Integer.parseInt(lineData[1]);
					int mem = Integer.parseInt(lineData[2]);
					
					VMMap.put(flavorName, new Flavor(flavorName, cpu, mem));
					
				}
				dataBlock++; i++;
			}
			//读取需要优化的维度
			else if(dataBlock == 2) {
				Flavor.setDim(inputData[i]);
				if(inputData[i].equals("CPU"))
					cpuOrMem=true;
				else
					cpuOrMem=false;
				dataBlock++;
				i++;
			}
			//需要预测的时间
			else if(dataBlock == 3) {
				predictStartTime = inputData[i];
				predictEndTime = inputData[i+1];
				break;
			}
		}		
	}*/
	/**
	 * 处理训练数据
	 * @param train_data
	 */
	public void parseTrain(String[] trainData) {
		
		trainEndTime = trainData[trainData.length - 1].split("\t")[2];
		
		for(int i = 0; i < trainData.length; ++i) {
			
			if(!trainData[i].contains("\t"))
				throw new RuntimeException("file parse fail");
			
			String[] lineData = trainData[i].split("\t");
			
			ErrorCheck.checkLength(lineData.length, 3);
			
			//记录训练数据的起止时间
			if(i == 0) {
				trainStartTime = lineData[2];
				DateTools.startTime = trainStartTime;
				trainRange = DateTools.getIndex(trainEndTime) + 1;
				historyDataIndexByFlavor = new double[flavorRange][trainRange];
				historyDataIndexByData = new double[trainRange][flavorRange];
			}
			else if(i == trainData.length - 1) {
				trainEndTime = lineData[2];
			}
			String flavorName = lineData[1];
			String data = lineData[2];
			
			int index = DateTools.getIndex(data);
			int flavor_id = Integer.parseInt(flavorName.substring(6));
			if(flavor_id <= flavorRange) {
				historyDataIndexByFlavor[flavor_id - 1][index] += 1;
				historyDataIndexByData[index][flavor_id - 1] += 1;
			}
		}
	}
	public Server[] getServers() {
		return servers;
	}

}
