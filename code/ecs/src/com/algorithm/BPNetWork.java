package com.algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import com.filetool.main.Main;

public class BPNetWork {
	//输入节点数目、隐层节点数目、输出层节点数目
	private int input_n, hidden_n, output_n;
	private final int NUM = 20;
	private int idx = 0;
	//输入节点、隐层节点、输出节点
	private double[] input_cells, hidden_cells, output_cells;

	//输入层参数、输出层参数
	private double[][] input_weights, output_weights;
		
	private double[][] input_correction, output_correction;
	
	private double[][] hidden_d, input_d;
	
	public BPNetWork(int input_n, int hidden_n, int output_n) {
		this.input_n = input_n;
		this.hidden_n = hidden_n;
		this.output_n = output_n;
		
		reshape();
		randomActivate();
	}

	/**
	 * 重新分配内存
	 */
	private void reshape() {
		if(input_n < 0 ||  hidden_n < 0 || output_n < 0)
			throw new RuntimeException("parameters error");
		
		this.input_cells = new double[input_n];
		for(int i = 0; i < input_n; ++i ) {
			this.input_cells[i] = 0.0;
		}
		this.hidden_cells = new double[hidden_n];
		for(int i = 0; i < hidden_n; ++i ) {
			this.hidden_cells[i] = 0.0;
		}
		this.output_cells = new double[output_n];
		for(int i = 0; i < output_n; ++i ) {
			this.output_cells[i] = 0.0;
		}
		
		this.input_weights = new double[NUM][NUM];
		this.output_weights = new double[NUM][NUM]; 
		
		this.input_correction = new double[input_n][hidden_n]; 
		this.output_correction = new double[hidden_n][output_n]; 
		
		this.input_d = new double[input_n][hidden_n];
		this.hidden_d = new double[hidden_n][output_n];
	}
	/**
	 * 随机给予权值
	 */
	private void randomActivate() {
		ObjectInputStream rId ;
		try {
			if (Main.model == 1) {
				InputStream inputStream = BPNetWork.class.getResourceAsStream("model1.dat");
				rId = new ObjectInputStream(inputStream);
				for(int i = 0; i < NUM; i++) {
					for(int j = 0; j < NUM; j++)
						input_weights[i][j] = rId.readDouble();
				}
				for(int i = 0; i < NUM; i++) {
					for(int j = 0; j < NUM; j++)
						output_weights[i][j] = rId.readDouble();
				}
			}
		} catch (IOException e) {

			e.printStackTrace();
			for(int i = 0; i < input_n; ++i) {
				for(int j = 0; j < hidden_n; ++j) {
					this.input_weights[i][j] = Tools.rand(-0.2, 0.2);
				}
			}
			for(int i = 0; i < hidden_n; ++i) {
				for(int j = 0; j < output_n; ++j) {
					this.output_weights[i][j] = Tools.rand(-0.2, 0.2);
				}
			}
		}
	}
	
	/**
	 * 预测给定的输入
	 */
	public double[] predict(double[] input) {
		
		
		//激活输入层
		for(int i = 0; i < input_n; ++i) {
			this.input_cells[i] = input[i];
		}
		//激活隐层
		for(int j = 0; j < hidden_n; ++j) {
			 double total = 0.0d;
			 for(int i = 0; i < input_n; ++i) {
				  total += input_cells[i] * input_weights[i][j];
			 }
	         hidden_cells[j] = Tools.sigmoid(total);
		}
		//激活输出层
		for(int j = 0; j < output_n; ++j) {
			 double total = 0.0d;
			 for(int i = 0; i < hidden_n; ++i) {
				  total += hidden_cells[i] * output_weights[i][j];
			 }
			 output_cells[j] = Tools.sigmoid(total);
		}
		return output_cells;
	}
	/**
	 * 反向传播
	 * @param input
	 * @param label
	 * @param learn_rate
	 * @param correct
	 * @return
	 */
	public double backPropagate(double[] input, double[] label, double learn_rate, double correct) {
		
		predict(input);
		
		double[] output_deltas = new double[output_n];
		for(int i = 0; i < output_deltas.length; ++i) {
			double error = label[i] - output_cells[i];
			output_deltas[i] = -error;
		}
		
		for(int i = 0; i < hidden_n; i++) {
			double t = 0.0;
			for(int j = 0; j < output_n; j++) {
				t += output_deltas[j] * output_weights[i][j];
			}
			for(int j = 0; j < input_n; j++) {
				input_d[j][i] = hidden_cells[i] * (1 - hidden_cells[i]) * input_cells[j] * t;
			}
		}
		
		for(int i = 0; i < hidden_n; ++i) {
			for(int j = 0; j < output_n; ++j) {
				hidden_d[i][j] = 0.2 * hidden_d[i][j] + output_deltas[j] * hidden_cells[i];
				output_weights[i][j] -= learn_rate * hidden_d[i][j] ;// + correct * output_correction[i][j];
				output_correction[i][j] = hidden_d[i][j] ;
			}
		}
		
		for(int i = 0; i < input_n; ++i) {
			for(int j = 0; j < hidden_n; ++j) {
				double change = input_d[i][j];
				input_weights[i][j] -= learn_rate * change;// + correct * input_correction[i][j];
				input_correction[i][j] = change;
			}
		}
		
		double error = 0.0d;
        //for(int i = 0; i < output_cells.length; ++i) {
          //  error += 0.5 * Math.pow(label[i] - output_cells[i], 2);
        //}
		return error;
	}
	public double curError(double[] x, double[] y) {
		   double error = 0;
		   predict(x);
	       for(int i = 0; i < output_cells.length; ++i) {
	           error += 0.5 * Math.pow(y[i] - output_cells[i], 2);
	       }
		    return error;
	}
	public void train(double[][] cases, double[][] label, int max_iter, double learn_rate, double correct) {
		for(int iter = 0; iter < max_iter; ++iter) {
			double error = 0.0;
			for(int j = 0; j < cases.length; ++j) {
				backPropagate(cases[j], label[j], learn_rate, correct);
			}
			/*for(int j = 0; j < cases.length; ++j) {
				error += curError(cases[j], label[j]);
			}*/
			//error /= cases.length;
			//System.out.println("iter:" + iter + " error:" + error / cases.length);
			//if(error < 0.00001) break;
		}
	}
}

