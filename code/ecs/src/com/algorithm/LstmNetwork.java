package com.algorithm;

import java.util.ArrayList;

/**
 * LSTM
 * @author hzl
 *
 */
public class LstmNetwork{
	private LstmParam lstmParam;
	private ArrayList<LstmNode> lstm_node_list;
	private ArrayList<double[]> x_list;
	private LstmNode last_node;
	private LossLayer loss_layer = new LossLayer();
	
	public LstmNetwork(int mem_cell_ct,int x_dim) {
		this(new LstmParam(mem_cell_ct, x_dim));
	}
	public LstmNetwork(LstmParam lstmParam) {
		this.lstmParam = lstmParam;
		this.lstm_node_list = new ArrayList<LstmNode>();
		this.x_list = new ArrayList<>();
	}
	public double[] getPredict(int idx) {
		return lstm_node_list.get(idx).getLstmState().h;
	}
	
	/**
	 * 计算loss
	 * @param y_list
	 * @param loss_layer
	 * @return
	 */
	public double y_list_is(double[][] y_list) {
		
		if(x_list.size() != y_list.length) {
			throw new RuntimeException("length not match");
		}
		int idx = x_list.size() - 1;
		double loss = loss_layer.loss(lstm_node_list.get(idx).getLstmState().h, y_list[idx]);
		double[] diff_h = loss_layer.bottom_diff(lstm_node_list.get(idx).getLstmState().h,  y_list[idx]);
		double[] diff_s = new double[lstmParam.mem_cell_ct];
		
		lstm_node_list.get(idx).top_diff_is(diff_h, diff_s);
		
		idx -= 1;
		
		while(idx >= 0) {
			loss = loss + loss_layer.loss(lstm_node_list.get(idx).getLstmState().h, y_list[idx]);
			diff_h = loss_layer.bottom_diff(lstm_node_list.get(idx).getLstmState().h, y_list[idx]);
			for(int i = 0; i < diff_h.length; ++i) {
				diff_h[i] += lstm_node_list.get(idx + 1).getLstmState().bottom_diff_h[i];
			}
			diff_s = lstm_node_list.get(idx + 1).getLstmState().bottom_diff_s;
			lstm_node_list.get(idx).top_diff_is(diff_h, diff_s);
			idx -= 1;
		}
		return loss;
	}
	
	public void x_list_clear() {
		x_list.clear();
	}
	/**
	 * 一次前向传播
	 * @param x
	 * @return
	 */
	public double[] x_list_add(double[] x) {
		x_list.add(x);
		
		if (x_list.size() > lstm_node_list.size()) {
			//添加新的lstm节点
			LstmState lstm_state = new LstmState(lstmParam.mem_cell_ct, lstmParam.x_dim);
		    lstm_node_list.add(new LstmNode(lstmParam, lstm_state));
		}
		
		int idx = x_list.size() - 1;
		
		if(idx == 0) {
			//第一次输入节点
			lstm_node_list.get(idx).bottom_data_is(x, null, null);
		}
		else {
			double[] s_prev = lstm_node_list.get(idx - 1).getLstmState().s;
			double[] h_prev = lstm_node_list.get(idx - 1).getLstmState().h;
			lstm_node_list.get(idx).bottom_data_is(x, s_prev, h_prev);
		}
		return lstm_node_list.get(idx).getLstmState().h;
	}	
	/**
	 * LSTM预测主函数
	 * @param input
	 * @param y_list
	 * @param learn_rate
	 * @param loss_layer
	 * @return
	 */
	public double[] predict(double[] input) {
		if(last_node == null)
			last_node = lstm_node_list.get(lstm_node_list.size() - 1);
		
		LstmState lstm_state = new LstmState(lstmParam.mem_cell_ct, lstmParam.x_dim);
		LstmNode cur_node = new LstmNode(lstmParam, lstm_state);
		
		double[] s_prev = last_node.getLstmState().s;
		double[] h_prev = last_node.getLstmState().h;
		cur_node.bottom_data_is(input, s_prev, h_prev);
		last_node = cur_node;
		
		return cur_node.getLstmState().h;
	}
	/**
	 * 预测函数 需要从头开始预测
	 * @param input
	 * @return
	 */
	public double[] predict1(double[] input) {
		return x_list_add(input);
	}
	/**
	 * LSTM训练主程序
	 * @param input
	 * @param y_list
	 * @param learn_rate
	 * @param loss_layer
	 * @return
	 */
	public double train(double[][] input, double[][] y_list, double learn_rate) {
		
		for(int j = 0; j < y_list.length; j++) {
			x_list_add(input[j]);
		}
		
		double loss = y_list_is(y_list);
        lstmParam.applyDiff(learn_rate);
        x_list_clear();
        
        return loss;
	}
	/**
	 * 测试用例
	 * @param args
	 */
	public static void main(String[] args) {
		 int mem_cell_ct = 43;
		 int x_dim = 7;
		 LstmNetwork lstm_net = new LstmNetwork(mem_cell_ct, x_dim);
		 double[][] input = {{0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 2},
                 {0, 0, 0, 0, 0, 2, 2}, {0, 0, 0, 0, 2, 2, 3}, {0, 0, 0, 2, 2, 3, 3}, {0, 0, 2, 2, 3, 3, 3},
                 {0, 2, 2, 3, 3, 3, 3}, {2, 2, 3, 3, 3, 3, 3}, {2, 3, 3, 3, 3, 3, 3}, {3, 3, 3, 3, 3, 3, 1},
                 {3, 3, 3, 3, 3, 1, 2}, {3, 3, 3, 3, 1, 2, 1}, {3, 3, 3, 1, 2, 1, 1}, {3, 3, 1, 2, 1, 1, 1},
                 {3, 1, 2, 1, 1, 1, 2}, {1, 2, 1, 1, 1, 2, 2}, {2, 1, 1, 1, 2, 2, 2}, {1, 1, 1, 2, 2, 2, 2},
                 {1, 1, 2, 2, 2, 2, 1}, {1, 2, 2, 2, 2, 1, 1}, {2, 2, 2, 2, 1, 1, 1}, {2, 2, 2, 1, 1, 1, 1},
                 {2, 2, 1, 1, 1, 1, 0}, {2, 1, 1, 1, 1, 0, 0}, {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1},
                 {1, 1, 0, 0, 1, 1, 1}, {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 1, 3, 3}, {0, 1, 1, 1, 3, 3, 3},
                 {1, 1, 1, 3, 3, 3, 3}, {1, 1, 3, 3, 3, 3, 3}, {1, 3, 3, 3, 3, 3, 2}, {3, 3, 3, 3, 3, 2, 2},
                 {3, 3, 3, 3, 2, 2, 3}, {3, 3, 3, 2, 2, 3, 1}, {3, 3, 2, 2, 3, 1, 1}, {3, 2, 2, 3, 1, 1, 1},
                 {2, 2, 3, 1, 1, 1, 2}, {2, 3, 1, 1, 1, 2, 2}, {3, 1, 1, 1, 2, 2, 2}};
		 double[][] y_list = {{0}, {0}, {2}, {2}, {3}, {3}, {3}, 
		    		{3}, {3}, {3}, {1}, {2}, {1}, {1}, {1}, {2}, {2}, {2}, {2}, 
		    		{1}, {1}, {1}, {1}, {0}, {0}, {1}, {1}, {1}, 
		    		{3}, {3}, {3}, {3}, {3}, {2}, {2}, {3}, {1}, 
		    		{1}, {1}, {2}, {2}, {2}, {2}};
		 
		for(int i = 0; i < 5000; ++i) {
			System.out.print("iter:" + i + " ");
			double loss = lstm_net.train(input, y_list, 0.002);
			
			System.out.print("[");
			for(int idx = 0; idx < y_list.length; ++idx) {
				double[] result = lstm_net.getPredict(idx);;
				System.out.print(Math.round(result[0]) + " ");
			}
			System.out.print("]");
			System.out.println("loss:" + loss);
		}
		/**Test
		 * 
		 */
		for(int i = 0; i < input.length; ++i) {
			System.out.print(Math.round(lstm_net.predict(input[i])[0]) + " ");
		}
		System.out.println();
	}
}
class LossLayer{
	public double loss(double[] pred, double[] label) {
		double loss = 0.0;
		for(int i = 0; i < label.length; ++i) {
			loss += Math.pow(pred[i] - label[i], 2);
		}
		return loss / label.length;
	}
	public double[] bottom_diff(double[] pred, double[] label) {
		double[] diff = new double[pred.length];
		
		for(int i = 0; i < label.length; ++i) {
			diff[i] = (pred[i] - label[i]);
		}
		return diff;
	}
}
class LstmNode {
	private LstmParam lstmParam;
	private LstmState lstmState;
	private double[] xc, s_prev, h_prev;
	
	public LstmParam getLstmParam() {
		return lstmParam;
	}

	public void setLstmParam(LstmParam lstmParam) {
		this.lstmParam = lstmParam;
	}

	public LstmState getLstmState() {
		return lstmState;
	}

	public void setLstmState(LstmState lstmState) {
		this.lstmState = lstmState;
	}

	public double[] getXc() {
		return xc;
	}

	public void setXc(double[] xc) {
		this.xc = xc;
	}

	public double[] getS_prev() {
		return s_prev;
	}

	public void setS_prev(double[] s_prev) {
		this.s_prev = s_prev;
	}

	public double[] getH_prev() {
		return h_prev;
	}

	public void setH_prev(double[] h_prev) {
		this.h_prev = h_prev;
	}

	public LstmNode(LstmParam lstm_param, LstmState lstm_state){
		this.lstmParam = lstm_param;
		this.lstmState = lstm_state;
	}
	
	public void bottom_data_is(double[] x, double[] s_prev, double[] h_prev) {
		if(s_prev == null)
			s_prev = new double[lstmState.s.length];
		if(h_prev == null)
			h_prev = new double[lstmState.h.length];
		this.s_prev = s_prev;
		this.h_prev = h_prev;
		
		double[] xc = new double[x.length + h_prev.length];
		System.arraycopy(x, 0, xc, 0,  x.length);
		System.arraycopy(h_prev, 0, xc, x.length, h_prev.length);
		
		for(int i = 0; i < lstmState.mem_cell; ++i) {
			lstmState.g[i] = Math.tanh(lstmParam.bg[i] + Dot(lstmParam.wg[i], xc));
			lstmState.i[i] = Tools.sigmoid(lstmParam.bi[i] + Dot(lstmParam.wi[i], xc));
			lstmState.f[i] = Tools.sigmoid(lstmParam.bf[i] + Dot(lstmParam.wf[i], xc));
			lstmState.o[i] = Tools.sigmoid(lstmParam.bo[i] + Dot(lstmParam.wo[i], xc));
			lstmState.s[i] = lstmState.g[i] * lstmState.i[i] + s_prev[i] * lstmState.f[i];
			lstmState.h[i] = lstmState.s[i] * lstmState.o[i];
		}
		this.xc = xc;	
	}
	public void top_diff_is(double[] top_diff_h, double[] top_diff_s) {
		
		int length = lstmState.o.length;
				
		double[] _ds = new double[length];
		double[] _do = new double[length];
		double[] _di = new double[length];
		double[] _dg = new double[length];
		double[] _df = new double[length];
		double[] di_input = new double[length];
		double[] df_input = new double[length];
		double[] do_input = new double[length];
		double[] dg_input = new double[length];
		
		for(int i = 0; i < length; ++i) {
			_ds[i] = lstmState.o[i] * top_diff_h[i] + top_diff_s[i];
			_do[i] = lstmState.s[i] * top_diff_h[i];
			_di[i] = lstmState.g[i] *_ds[i];
			_dg[i] = lstmState.i[i] *_ds[i];
			_df[i] = s_prev[i] * _ds[i];
			
			di_input[i] = Tools.sigmodDerivate(lstmState.i[i]) * _di[i];
			df_input[i] = Tools.sigmodDerivate(lstmState.f[i]) * _df[i];
			do_input[i] = Tools.sigmodDerivate(lstmState.o[i]) * _do[i];
			dg_input[i] = Tools.tanhDerivative(lstmState.g[i]) * _dg[i];
		}
		for(int i = 0; i < lstmParam.wi_diff.length; ++i) {
			for(int j = 0; j < lstmParam.wi_diff[0].length; ++j) {
				lstmParam.wi_diff[i][j] += di_input[i] * xc[j]; 
				lstmParam.wf_diff[i][j] += df_input[i] * xc[j]; 
				lstmParam.wo_diff[i][j] += do_input[i] * xc[j]; 
				lstmParam.wg_diff[i][j] += dg_input[i] * xc[j]; 
			}
		}
		for(int i = 0; i < lstmParam.bi_diff.length; ++i) {
			lstmParam.bi_diff[i] += di_input[i];
			lstmParam.bf_diff[i] += df_input[i];
			lstmParam.bo_diff[i] += do_input[i];
			lstmParam.bg_diff[i] += dg_input[i];
		}
		double[] dxc = new double[xc.length];
		for(int i = 0; i < dxc.length; ++i) {
			double sum = 0.0;
			for(int j = 0; j < lstmParam.wg.length; ++j) {
				sum += lstmParam.wi[j][i] * di_input[j]
						+ lstmParam.wf[j][i] * df_input[j]
						+ lstmParam.wo[j][i] * do_input[j]
						+ lstmParam.wg[j][i] * dg_input[j];	
			}
			dxc[i] += sum;
		}
		for(int i = 0; i < lstmState.bottom_diff_s.length ; ++i) {
			lstmState.bottom_diff_s[i] = _ds[i] * lstmState.f[i];
			lstmState.bottom_diff_h[i] = dxc[lstmParam.x_dim + i];
		}
	}
	
	private double Dot(double[] a, double[] b) {
		double sum = 0.0;
		for(int i = 0; i < a.length; ++i)
			sum += a[i] * b[i];
		return sum;
	} 
}


class LstmState{
	public int mem_cell, x_dim;
	public double[] g, i, f, o, s, h;
	public double[] bottom_diff_h;
	public double[] bottom_diff_s;
	
	public LstmState(int mem_cell, int x_dim) {
		this.mem_cell = mem_cell;
		this.x_dim = x_dim;
		
		this.g = new double[mem_cell];
		this.i = new double[mem_cell];
		this.f = new double[mem_cell];
		this.o = new double[mem_cell];
		this.s = new double[mem_cell];
		this.h = new double[mem_cell];
		this.bottom_diff_h = new double[mem_cell];
		this.bottom_diff_s = new double[mem_cell];
	}
}

class LstmParam{
	
	private static final double rand_bound = 0.02;
	int mem_cell_ct, x_dim, concat_len;
	double[][] wg, wi, wf, wo, wg_diff, wi_diff, wf_diff, wo_diff;
	double[] bg, bi, bf, bo, bg_diff, bi_diff, bf_diff, bo_diff;
	
	public LstmParam(int mem_cell_ct, int x_dim) {
		super();
		this.mem_cell_ct = mem_cell_ct;
		this.x_dim = x_dim;
		this.concat_len = mem_cell_ct + x_dim;		
		reshape();
	}

	public void reshape() {
		this.wg = new double[mem_cell_ct][concat_len];
		this.wi = new double[mem_cell_ct][concat_len];
		this.wf = new double[mem_cell_ct][concat_len];
		this.wo = new double[mem_cell_ct][concat_len];
		
		for(int i = 0; i < mem_cell_ct; ++i) {
			for(int j = 0; j < concat_len; ++j) {
				wg[i][j] =  Tools.rand(-rand_bound, rand_bound);
				wi[i][j] =  Tools.rand(-rand_bound, rand_bound);
				wf[i][j] =  Tools.rand(-rand_bound, rand_bound);
				wo[i][j] =  Tools.rand(-rand_bound, rand_bound);
			}
		}
		this.bg = new double[mem_cell_ct];
		this.bi = new double[mem_cell_ct];
		this.bf = new double[mem_cell_ct];
		this.bo = new double[mem_cell_ct];
		
		for(int i = 0; i < mem_cell_ct; ++i) {
			bg[i] = Tools.rand(-rand_bound, rand_bound);
			bi[i] = Tools.rand(-rand_bound, rand_bound);
			bf[i] = Tools.rand(-rand_bound, rand_bound);
			bo[i] = Tools.rand(-rand_bound, rand_bound);
		}
		
		this.wg_diff = new double[mem_cell_ct][concat_len];
		this.wi_diff = new double[mem_cell_ct][concat_len];
		this.wf_diff = new double[mem_cell_ct][concat_len];
		this.wo_diff = new double[mem_cell_ct][concat_len];
		
		this.bg_diff = new double[mem_cell_ct];
		this.bi_diff = new double[mem_cell_ct];
		this.bf_diff = new double[mem_cell_ct];
		this.bo_diff = new double[mem_cell_ct];
	}
	
	public void applyDiff(double learn_rate) {
		for(int i = 0; i < mem_cell_ct; ++i) {
			for(int j = 0; j < concat_len; ++j) {
				wg[i][j] -=  learn_rate * wg_diff[i][j];
				wi[i][j] -=  learn_rate * wi_diff[i][j];
				wf[i][j] -=  learn_rate * wf_diff[i][j];
				wo[i][j] -=  learn_rate * wo_diff[i][j];
				wg_diff[i][j] = 0.0;
				wi_diff[i][j] = 0.0;
				wf_diff[i][j] = 0.0;
				wo_diff[i][j] = 0.0;
			}
		}
		for(int i = 0; i < mem_cell_ct; ++i) {
			bg[i] -=  learn_rate * bg_diff[i];
			bi[i] -=  learn_rate * bi_diff[i];
			bf[i] -=  learn_rate * bf_diff[i];
			bo[i] -=  learn_rate * bo_diff[i];
			bg_diff[i] = 0.0;
			bi_diff[i] = 0.0;
			bf_diff[i] = 0.0;
			bo_diff[i] = 0.0;
		}
	}
}