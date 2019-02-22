package cn.jingedawang.bluetoothdemo;


import java.util.Arrays;

class EyesData {

    public String S1;       //验光仪第一测量的数据    //球镜
    public String C1;                             //柱镜
    public String A1;                             //轴位
    public String S2;       //验光仪第二测量的数据
    public String C2;
    public String A2;
    public String S3;       //验光仪第三测量的数据
    public String C3;
    public String A3;
    public String AVG_S;    //三次测量数据的均值
    public String AVG_C;
    public String AVG_A;
    public String S_E;      //等效球镜度
}


public class OptometryData {

    //右眼的数据
    EyesData RightEyes = new EyesData();
    //左眼的数据
    EyesData LeftEyes = new EyesData();
    //瞳距
    public String PD;                   //Pupil Distance 瞳距


    //获取右眼的数据
    public EyesData getRightEyes() {
        return RightEyes;
    }

    //获取左眼的数据
    public EyesData getLeftEyes() {
        return LeftEyes;
    }

    //获取瞳距
    public String getPD() {
        return PD;
    }

    //设置右眼的数据
    public void setRightEyes(EyesData ReyeData)
    {
        RightEyes = ReyeData;
    }

    //设置左眼的数据
    public void setLeftEyes(EyesData ReyeData)
    {
        LeftEyes = ReyeData;
    }

    //设置瞳距
    public void setPD(String pd) {
        PD = pd;
    }
}

