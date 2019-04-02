package cn.jingedawang.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.jingedawang.bluetoothdemo.OptometryData;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MyData
{
    //缓存接受到的验光仪的缓存
    public char[] OptometryBuffer = new char[4096];
    public char[] MyTextBuffer = new char[4096];

    public char GetTextHead=0;      //获取Text帧头的计数器

    public int TextHeadCounter=0;      //获取Text帧头的计数器

    public int ENDCounter=0;      //获取Text帧头的计数器

    public char R_eye_times=0;

    public char L_eye_times=0;
}


public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;

    private TextView txtIsConnected;
    private EditText edtReceivedMessage;
    private EditText edtSentMessage;
    private EditText edtSendMessage;
    private Button btnSend;
    private Button btnPairedDevices;
    private CheckBox HEX;

    private BluetoothAdapter mBluetoothAdapter;
    private ConnectedThread mConnectedThread;

    OptometryData OptD = new OptometryData();

    String h = "";


    MyData myData = new MyData();


    public char HeadGetOne=0;      //获取帧头的计数器
    public int GetByteCounter=0;   //获取字节的计数器

    //ArrayList list=new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        int i=0;
        for(i=0; i<2048; i++)
        {
            myData.OptometryBuffer[i]=(char)i;
            myData.MyTextBuffer[i]=(char)i;
        }

//        //右眼的数据
//        EyesData MyRightEyes = new EyesData();
//        //左眼的数据
//        EyesData MyLeftEyes = new EyesData();
//
//        MyRightEyes.S1 = "-5.00";
//        MyRightEyes.C1 = "-0.25";
//        MyRightEyes.A1 = "168";
//
//        MyRightEyes.S2 = "-4.75";
//        MyRightEyes.C2 = "-0.75";
//        MyRightEyes.A2 = "179";
//
//        MyRightEyes.S3 = "-4.75";
//        MyRightEyes.C3 = "-1.00";
//        MyRightEyes.A3 = "9";
//
//        MyRightEyes.AVG_S = "-4.75";
//        MyRightEyes.AVG_C = "-0.75";
//        MyRightEyes.AVG_A = "179";
//
//        MyRightEyes.S_E = "-5.25";
//
//
//        MyLeftEyes.S1 = "-4.75";
//        MyLeftEyes.C1 = "-1.00";
//        MyLeftEyes.A1 = "8";
//
//        MyLeftEyes.S2 = "-4.75";
//        MyLeftEyes.C2 = "-1.00";
//        MyLeftEyes.A2 = "7";
//
//        MyLeftEyes.S3 = "-5.00";
//        MyLeftEyes.C3 = "-0.75";
//        MyLeftEyes.A3 = "8";
//
//        MyLeftEyes.AVG_S = "-5.00";
//        MyLeftEyes.AVG_C = "-1.00";
//        MyLeftEyes.AVG_A = "8";
//
//        MyLeftEyes.S_E = "-5.50";
//
//
//
//
//        OptD.setRightEyes(MyRightEyes);
//        OptD.setLeftEyes(MyLeftEyes);
//        OptD.setPD("40.0");







        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtIsConnected = (TextView) findViewById(R.id.txtIsConnected);
        edtReceivedMessage = (EditText) findViewById(R.id.edtReceivedMessage);
        edtSentMessage = (EditText) findViewById(R.id.edtSentMessage);
        edtSendMessage = (EditText) findViewById(R.id.edtSendMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnPairedDevices = (Button) findViewById(R.id.btnPairedDevices);
        HEX = (CheckBox)findViewById(R.id.HEX);

        btnPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 获取蓝牙适配器
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
                }

                //请求开启蓝牙
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }

                //进入蓝牙设备连接界面
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), DevicesListActivity.class);
                startActivity(intent);

            }
        });

        //点击【发送】按钮后，将文本框中的文本按照ASCII码发送到已连接的蓝牙设备
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("Myeyes", "D:"+OptD.LeftEyes.AVG_S + "___" + OptD.RightEyes.AVG_S + "___" + OptD.PD);

                if (edtSendMessage.getText().toString().isEmpty()) {
                    return;
                }
                String sendStr = edtSendMessage.getText().toString();
                char[] chars = sendStr.toCharArray();
                byte[] bytes = new byte[chars.length];
                for (int i=0; i < chars.length; i++) {
                    bytes[i] = (byte) chars[i];
                }
                edtSentMessage.append(sendStr);
                mConnectedThread.write(bytes);


            }
        });

    }

    void GetByteData(char ByteData)
    {
        GetByteCounter = GetByteCounter + 1;


        //Log.d("Ble_Data", "Process Char ||" + ByteData + "||"+ GetByteCounter + " ||!");

        if(HeadGetOne==0x05)    //表示已经读取到了数据帧头了
        {
            myData.OptometryBuffer[GetByteCounter-1] = ByteData;

            String TempS="";
//            for(int i=0; i<5; i++)
//            {
//                TempS = TempS + myData.OptometryBuffer[i];
//            }
//
//            Log.d("Ble_Data", "Get One Data Frame " + GetByteCounter + " !" + TempS);

            if(myData.GetTextHead == 0x02)
            {
                //Log.d("Ble_Data", "Text Data Frame OK " + GetByteCounter + " !");
//
//                TempS="";
//                for(int i=0; i<(GetByteCounter - myData.TextHeadCounter ); i++)
//                {
//                    TempS = TempS + myData.OptometryBuffer[i+myData.TextHeadCounter];
//                }
//
//                Log.d("Ble_Data", "Get Text Data Frame " + (GetByteCounter - myData.TextHeadCounter ) + " !" + TempS);


                //2A 0D 04 -->>*加回车加EOT(04)
                if(ByteData==0x2A) //读取的数据有可能是数据帧的尾,是不是还需要看下一个字节是不是0D 04
                {
                    //先存起来
                    //list.add(0x40);
                    myData.ENDCounter=0x01;    //表示有可能读取到了一个数据帧尾
                    //Log.d("Ble_Data", "Get END Frame1 " + GetByteCounter + " !");
                }
                else {
                    if (myData.ENDCounter == 0x01)    //如果上一次读取到是0x2A -- 有可能是数据帧尾
                    {
                        if (ByteData == 0x0D)     //如果这次读取是0x0D -- 是不是还需要看下一个字节是不是04
                        {
                            myData.ENDCounter = 0x02;    //表示这是一个数据帧
                            //Log.d("Ble_Data", "Get END Frame2 " + GetByteCounter + " !");
                        }
                        else {
                            Log.d("Ble_Data", "Err END Frame2 " + GetByteCounter + " !");
                        }
                    }
                    else if (myData.ENDCounter == 0x02)    //如果上一次读取到是0x04 -- 有可能是数据帧尾
                    {
                        if (ByteData == 0x04)     //如果这次读取是0x04 -- 这是一个数据帧尾
                        {
                            //TempS = "";
                            for (int i = 0; i < (GetByteCounter - myData.TextHeadCounter); i++)
                            {

                                char TempC;

                                //TempC = myData.OptometryBuffer[myData.TextHeadCounter + i];

                                String GetEyesData="";

                                //右眼的数据
                                //如果OR
                                if ((myData.OptometryBuffer[myData.TextHeadCounter + i] == 0x4F) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 1] == 0x52))
                                {
                                    i = i + 9;
                                    for (int j = 0; j < 6; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+6;
                                    Log.d("RO1", "SPH " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    GetEyesData="";
                                    for (int j = 0; j < 6; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+6;
                                    Log.d("RO1", "SYL " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    GetEyesData="";
                                    for (int j = 0; j < 3; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+4;
                                    Log.d("RO1", "AXIS " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

//                                    i=i+6;
//                                    GetEyesData="";
//                                    for (int j = 0; j < 7; j++) {
//                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
//                                        GetEyesData = GetEyesData + TempC;
//                                    }
//                                    i=i+7;
//                                    Log.d("RO1", "S.E. " + GetEyesData + " !");
//                                    edtReceivedMessage.getText().append(GetEyesData);

                                    edtReceivedMessage.getText().append("\n");
                                }

                                //如果OAR
                                if ((myData.OptometryBuffer[myData.TextHeadCounter + i] == 0x4F) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 1] == 0x41) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 2] == 0x52))
                                {
                                    i = i + 4;
                                    for (int j = 0; j < 6; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+6;
                                    Log.d("RO1", "AVG_SPH " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    GetEyesData="";
                                    for (int j = 0; j < 6; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+6;
                                    Log.d("RO1", "AVG_SYL " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    GetEyesData="";
                                    for (int j = 0; j < 3; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+4;
                                    Log.d("RO1", "AVG_AXIS " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);


                                    edtReceivedMessage.getText().append("\n");
                                }

                                //如果EAR
                                if ((myData.OptometryBuffer[myData.TextHeadCounter + i] == 0x45) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 1] == 0x41) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 2] == 0x52))
                                {
                                    GetEyesData="";
                                    i = i + 4;
                                    for (int j = 0; j < 7; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+7;
                                    Log.d("RO1", "AVG_S.E. " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    edtReceivedMessage.getText().append("\n");
                                }


                                //左眼的数据
                                //如果OL
                                if ((myData.OptometryBuffer[myData.TextHeadCounter + i] == 0x4F) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 1] == 0x4C))
                                {
                                    i = i + 9;
                                    for (int j = 0; j < 6; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+6;
                                    Log.d("RO1", "SPH " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    GetEyesData="";
                                    for (int j = 0; j < 6; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+6;
                                    Log.d("RO1", "SYL " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    GetEyesData="";
                                    for (int j = 0; j < 3; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+4;
                                    Log.d("RO1", "AXIS " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

//                                    i=i+6;
//                                    GetEyesData="";
//                                    for (int j = 0; j < 7; j++) {
//                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
//                                        GetEyesData = GetEyesData + TempC;
//                                    }
//                                    i=i+7;
//                                    Log.d("RO1", "S.E. " + GetEyesData + " !");
//                                    edtReceivedMessage.getText().append(GetEyesData);

                                    edtReceivedMessage.getText().append("\n");
                                }

                                //如果OAL
                                if ((myData.OptometryBuffer[myData.TextHeadCounter + i] == 0x4F) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 1] == 0x41) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 2] == 0x4C))
                                {
                                    i = i + 4;
                                    for (int j = 0; j < 6; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+6;
                                    Log.d("RO1", "AVG_SPH " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    GetEyesData="";
                                    for (int j = 0; j < 6; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+6;
                                    Log.d("RO1", "AVG_SYL " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    GetEyesData="";
                                    for (int j = 0; j < 3; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+4;
                                    Log.d("RO1", "AVG_AXIS " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);


                                    edtReceivedMessage.getText().append("\n");
                                }

                                //如果EAL
                                if ((myData.OptometryBuffer[myData.TextHeadCounter + i] == 0x45) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 1] == 0x41) && (myData.OptometryBuffer[myData.TextHeadCounter + i + 2] == 0x4C))
                                {
                                    GetEyesData="";
                                    i = i + 4;
                                    for (int j = 0; j < 7; j++) {
                                        TempC = myData.OptometryBuffer[myData.TextHeadCounter + i + j];
                                        GetEyesData = GetEyesData + TempC;
                                    }
                                    i=i+7;
                                    Log.d("RO1", "AVG_S.E. " + GetEyesData + " !");
                                    edtReceivedMessage.getText().append(GetEyesData);

                                    edtReceivedMessage.getText().append("\n");
                                }

//                                if (TempC != 0x20) {
//                                    edtReceivedMessage.getText().append(TempC); //将接收到的数据追加到文本编辑器
//                                    //h = h + c;
//
//                                }

//                                String temp = Integer.toHexString(myData.OptometryBuffer[myData.TextHeadCounter + i] & 0xFF);       //0D
//                                if (temp.length() == 1) {
//                                    temp = "0" + temp;
//                                }
//                                TempS = TempS + " " + temp;
//
//                            Log.d("Ble_Data", "Text " + (GetByteCounter - myData.TextHeadCounter) + " !" + TempS);

                            }
                            //先存起来
                            myData.ENDCounter = 0x03;    //表示这是一个数据帧
                            //Log.d("Ble_Data", "Get END Frame3 " + GetByteCounter + " !");

                            HeadGetOne=0x00;
                            myData.GetTextHead=0;
                            myData.ENDCounter=0;
                            myData.TextHeadCounter=0;
                            for(int i=0; i<2048; i++)
                            {
                                myData.OptometryBuffer[i]=(char)0x00;
                                myData.MyTextBuffer[i]=(char)0x00;
                            };
                            GetByteCounter=0;
                        }
                        else
                            {
                            Log.d("Ble_Data", "Err END Frame3 ||" + ByteData + "||" + GetByteCounter + " ||!");
                        }
                    }
                }
                    //
            }

            //已经找到了数据帧头 接下来就是找Text字段头了
            //2A 0D		-->>*+回车 数据段帧头
            if((ByteData==0x2A)&&(myData.GetTextHead==0x00)) //读取的数据有可能是数据帧的头,是不是还需要看下一个字节是不是0D
            {
                //Log.d("Ble_Data", "Text Data Frame1 " + GetByteCounter + " !");
                //先存起来
                //list.add(0x40);
                myData.TextHeadCounter = GetByteCounter;
                //myData.TextBuffer = null;
                //myData.MyTextBuffer[0] = ByteData;
                myData.GetTextHead=0x01;    //表示有可能读取到了一个数据帧头

            }
            else {
                if (myData.GetTextHead == 0x01)    //如果上一次读取到是0x2A -- 有可能是数据帧头
                {
                    if (ByteData == 0x0D)     //如果这次读取是0xOD -- 就是Text段
                    {
                        //先存起来
                        //myData.TextHeadCounter = myData.TextHeadCounter + 1;
                        //myData.MyTextBuffer[myData.TextHeadCounter] = ByteData;
                        myData.TextHeadCounter = GetByteCounter;
                        myData.GetTextHead = 0x02;    //表示这是一个数据帧
                        //Log.d("Ble_Data", "Text Data Frame2 " + GetByteCounter + " !");
                    }
                }
//                else{
//                    Log.d("Ble_Data", "Text Err Frame2 " + GetByteCounter + " !");
//                    //myData.MyTextBuffer = null;
//                    //myData.GetTextHead=0;
//                }
            }
        }


        //40 53 34 30 0D	-->>@S40+回车 检测数据帧头
        if(ByteData==0x40) //读取的数据有可能是数据帧的头,是不是还需要看下一个字节是不是53 34 30 0D
        {
            //先存起来
            //list.add(0x40);
            myData.OptometryBuffer[0] = ByteData;
            HeadGetOne=0x01;    //表示有可能读取到了一个数据帧头
            //Log.d("Ble_Data", "Get One Data Frame1 " + GetByteCounter + " !");
        }
        else
        {
            if(HeadGetOne==0x01)    //如果上一次读取到是0x40 -- 有可能是数据帧头
            {
                if(ByteData==0x53)     //如果这次读取是0x53 -- 是不是还需要看下一个字节是不是34 30 0D
                {
                    //先存起来
                    myData.OptometryBuffer[1] = ByteData;
                    HeadGetOne=0x02;    //表示这是一个数据帧
                    //Log.d("Ble_Data", "Get One Data Frame2 " + GetByteCounter + " !");
                }
                else
                {
                    Log.d("Ble_Data", "Err Frame2 " + GetByteCounter + " !");
                    HeadGetOne=0x00;
                    myData.OptometryBuffer=null;
                    GetByteCounter=0;
                }
            }
            else if(HeadGetOne==0x02)    //如果上一次读取到是0x53 -- 有可能是数据帧头
            {
                if(ByteData==0x34)     //如果这次读取是0x0D -- 这是一个数据帧
                {
                    //先存起来
                    myData.OptometryBuffer[2] = ByteData;
                    HeadGetOne=0x03;    //表示这是一个数据帧
                    //Log.d("Ble_Data", "Get One Data Frame3 " + GetByteCounter + " !");
                }
                else
                {
                    Log.d("Ble_Data", "Err Frame3 ||" + ByteData + "||"+ GetByteCounter + " ||!");
                    HeadGetOne=0x00;
                    myData.OptometryBuffer=null;
                    GetByteCounter=0;
                }
            }
            else if(HeadGetOne==0x03)    //如果上一次读取到是0x34 -- 是不是还需要看下一个字节是不是30 0D
            {
                if(ByteData==0x30)     //如果这次读取是0x0D -- 这是一个数据帧
                {
                    //先存起来
                    myData.OptometryBuffer[3] = ByteData;
                    HeadGetOne=0x04;    //表示这是一个数据帧
                    //Log.d("Ble_Data", "Get One Data Frame4 " + GetByteCounter + " !");
                }
                else
                {
                    Log.d("Ble_Data", "Err Frame4 ||" + ByteData + "||"+ GetByteCounter + " ||!");
                    HeadGetOne=0x00;
                    myData.OptometryBuffer=null;
                    GetByteCounter=0;
                }
            }
            else if(HeadGetOne==0x04)    //如果上一次读取到是0x30 -- 是不是还需要看下一个字节是不是0D
            {
                if(ByteData==0x0D)     //如果这次读取是0x0D -- 这是一个数据帧头
                {
                    //先存起来
                    myData.OptometryBuffer[4] = ByteData;
                    HeadGetOne=0x05;    //表示这是一个数据帧
                    myData.GetTextHead=0x00;
                    //Log.d("Ble_Data", "Get One Data Frame5 " + GetByteCounter + " !");
                }
                else
                {
                    Log.d("Ble_Data", "Err Frame5 ||" + ByteData + "||"+ GetByteCounter + " ||!");
                    HeadGetOne=0x00;
                    myData.OptometryBuffer=null;
                    GetByteCounter=0;
                }
            }

        }


//            if(GetByteCounter==84)
//            {
//                TempS="";
//                String temp = Integer.toHexString(myData.OptometryBuffer[GetByteCounter-2] & 0xFF); //2A
//                if(temp.length() == 1){
//                    temp = "0" + temp;
//                }
//                TempS = TempS + " "+temp;
//                temp = Integer.toHexString(myData.OptometryBuffer[GetByteCounter-1] & 0xFF);       //0D
//                if(temp.length() == 1){
//                    temp = "0" + temp;
//                }
//                TempS = TempS + " "+temp;
//
//
//                //TempS = TempS + myData.OptometryBuffer[GetByteCounter-2] + myData.OptometryBuffer[GetByteCounter-1];
//
//                Log.d("Ble_Data", "Text " + GetByteCounter + " !" + TempS);
//                //开始Text Part
//                if((myData.OptometryBuffer[GetByteCounter-1]==0x0D) && (myData.OptometryBuffer[GetByteCounter-2]==0x2A) )
//                {
//                    Log.d("Ble_Data", "Get One Text Part" + GetByteCounter + " !");
//                }
//            }
//        }

        if(GetByteCounter>2048)
        {
            Log.d("Ble_Data", "Err OVER ||" + ByteData + "||"+ GetByteCounter + " ||!");
            HeadGetOne=0x00;
            myData.GetTextHead=0;
            myData.ENDCounter=0;
            myData.TextHeadCounter=0;
            for(int i=0; i<2048; i++)
            {
                myData.OptometryBuffer[i]=(char)0x00;
                myData.MyTextBuffer[i]=(char)0x00;
            };
            GetByteCounter=0;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //回到主界面后检查是否已成功连接蓝牙设备
        if (BluetoothUtils.getBluetoothSocket() == null || mConnectedThread != null) {
            txtIsConnected.setText("未连接");
            return;
        }

        txtIsConnected.setText("已连接");



        //已连接蓝牙设备，则接收数据，并显示到接收区文本框
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ConnectedThread.MESSAGE_READ:
                        byte[] buffer = (byte[]) msg.obj;
                        int length = msg.arg1;

                        for (int i=0; i < length; i++) {
                            char c = (char) buffer[i];

                            //GetByteData(c);

//                            if(c=='@')  //如果收到数据是STD2数据帧的帧头
//                            {
//                                h="";
//                            }
                              if(HEX.isChecked())
                              {
                                  String temp = Integer.toHexString(buffer[i] & 0xFF);
                                  if(temp.length() == 1){
                                      temp = "0" + temp;
                                  }

                                  edtReceivedMessage.getText().append(temp.toUpperCase()+" ");
                              }
                              else {
                                  edtReceivedMessage.getText().append(c); //将接收到的数据追加到文本编辑器
                              }
                            String temp = Integer.toHexString(buffer[i] & 0xFF);
                            if(temp.length() == 1){
                                temp = "0" + temp;
                            }
                            h = h + " "+temp;
                        }

                        //edtReceivedMessage.getText().append("\r\n");

                        Log.d("BLE", "Data: "+   h.replace(" ", "").replaceAll("\r|\n", ""));
                        Log.d("BLE", "Data: "+   h);

                       //String s = new String(buffer);

                        //Log.d("BLE", "D:"+   s);

                        break;
                }

            }
        };

        //启动蓝牙数据收发线程
        mConnectedThread = new ConnectedThread(BluetoothUtils.getBluetoothSocket(), handler);
        mConnectedThread.start();

    }
}
