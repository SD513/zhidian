package com.bupt.zhidian.pojo;
import java.util.ArrayList;

/**
 * 重点：测量值与预测值之间取最优结果-让算法去算取折中的最优值
 *
 */
public class KalmanFilter {

    /**Kalman Filter*/
    private Float predict; //观察数据值
    private Float current; //观察数据值的下一条数据
    private Float estimate;//每一次计算出的-最终估计值
    private double pdelt; //系统测量误差-为计算高斯噪声方差
    private double mdelt; //系统测量误差-为计算高斯噪声方差
    private double Gauss; //高斯噪声方差
    private double kalmanGain;//估计方差
    //信任程度 因为实际中不同传感器精度不同昂贵的高精度传感器就可以更信任一些Ｒ可以小一些。  或者我建立的模型很优秀误差极小就可以更信任模型Ｑ可以小一些
    /*
    QR：
        Q模型误差与R测量误差的大小，是模型预测值与测量值的加权
            R固定，Q越大，代表越信任侧量值，
            Q无穷代表只用测量值；反之，
            Q越小代表越信任模型预测值，Q为零则是只用模型预测
        Q是系统过程噪声的协方差矩阵，而R则是观测噪声的协方差矩阵。后者和你选择的传感器息息相关,R是看传感器精度，Q是过程误差看环境的影响大不大，我一般Q取0.01
        R为大于0常数都可以 比如1. P初始值设为足够大的对角矩阵。Q大小影响收敛速度。可以试验几个数值。
        Q和R分别代表对预测值和测量值的置信度（反比），通过影响卡尔曼增益K的值，影响预测值和测量值的权重。越大的R代表越不相信测量值。
        q越小，越依赖系统模型，r越小，越依赖观测值
     */
    private final static double Q = 0.00001; //（自定义-调参用）
    private final static double R = 0.15; //（自定义-调参用

    public void initial(){
//        pdelt = 4;    //系统测量误差
//        mdelt = 3;
        pdelt = 4;   //系统测量误差
        mdelt = 3;  //估计方差
    }
    public Float KalmanFilter(Float oldValue,Float value){
        //(1)第一个估计值
        predict = oldValue;
        //第二个估计值
        current = value;
        //(2)高斯噪声方差
        Gauss = Math.sqrt(pdelt * pdelt + mdelt * mdelt) + Q;
        //(3)估计方差
        kalmanGain = Math.sqrt((Gauss * Gauss)/(Gauss * Gauss + pdelt * pdelt)) + R;
        //(4)最终估计值
        estimate = (float) (kalmanGain * (current - predict) + predict);
        //(5)新的估计方差，下一次不确定性的度量
        mdelt = Math.sqrt((1-kalmanGain) * Gauss * Gauss);

        return estimate;
    }
}