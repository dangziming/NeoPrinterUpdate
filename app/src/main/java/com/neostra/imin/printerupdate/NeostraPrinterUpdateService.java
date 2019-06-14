package com.neostra.imin.printerupdate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import static java.lang.Thread.sleep;


public class NeostraPrinterUpdateService extends Service {
    private static final String TAG = "NeoPrinterUpdate";
    private final String FIRMWARE_VERSION = "9004";
    public SerialControl mComPort;//串口
    private GetVersionThread getVersion;//读取串口返回的信息
    private UpdatePrinterThread updatePrinter;
    private String currentVersion;
    private boolean isContinue = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"NeostraPrinterUpdateService onCreate()");
        openDefultPort();
        getVersion = new GetVersionThread();
        getVersion.start();
        updatePrinter = new UpdatePrinterThread();
        updatePrinter.start();
    }

    private void openDefultPort(){
        //默认打开/dev/ttyMT1 端口 波特率115200
        mComPort =  new SerialControl();
        mComPort.setPort("/dev/ttyMT1");
        mComPort.setBaudRate("115200");
        OpenComPort(mComPort);
        OpenPrint();//打印机上电
    }




    private class GetVersionThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(isContinue) {
                if(mComPort != null && mComPort.isOpen()){
                    mComPort.sendHex("1D6766");//打印机软件版本号
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    byte[] buffer=new byte[128];
                    int size = 0;
                    try {
                        size = mComPort.mInputStream.read(buffer);
                        Log.d(TAG,"GetVersionThread read size = " + size);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (size > 0){
                        String rendVersion = new String(buffer);
                        if(rendVersion.contains("software vision") && size == 22){
                            currentVersion = rendVersion.substring(17,21);
                            Log.d(TAG,"GetVersionThread current version  = " + currentVersion);
                            isContinue = false;
                        }
                    }
                }
            }
        }
    }



    private class UpdatePrinterThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(isContinue){
                //等待获取打印机的软件版本号
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(!currentVersion.equals(FIRMWARE_VERSION)){
                Log.d(TAG,"UpdatePrinterThread currentVersion != FIRMWARE_VERSION "
                        + currentVersion + "!=" + FIRMWARE_VERSION);
                printUpdate();//对打印机版本固件进行升级
            }

        }
    }

    private void printUpdate(){
        String  commandHex = "1B232355505047";
        try {
            InputStream is = getAssets().open("PT95-SGE-9004.bin");
            int length = is.available();
            String lenghexlittle = MyFunc.NeoByteArrToHex(MyFunc.intToBytesLittle(length));

            byte[] fileByte = new byte[length];
            is.read(fileByte);

            String xLen = MyFunc.NeoByteArrToHex(fileByte);
            int hexSum = MyFunc.makeChecksum(xLen);
            String hexSumlittle = MyFunc.NeoByteArrToHex(MyFunc.intToBytesLittle(hexSum));

            commandHex = commandHex + hexSumlittle + lenghexlittle + xLen;

            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mComPort!=null && mComPort.isOpen()){
            Log.d(TAG,"printUpdate() send update command");
            mComPort.sendHex(commandHex);
        }
    }

    //----------------------------------------------------串口控制类
    private class SerialControl extends SerialHelper{
        public SerialControl(){
        }

        @Override
        protected void onDataReceived(final ComBean ComRecData)
        {

        }
    }

    //----------------------------------------------------打开串口
    private void OpenComPort(SerialHelper ComPort){
        try
        {
            ComPort.open();
        } catch (SecurityException e) {
            Log.d(TAG,"打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            Log.d(TAG,"打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            Log.d(TAG,"打开串口失败:参数错误!");
        }
    }

    //----------------------------------------------------关闭串口
    private void CloseComPort(SerialHelper ComPort){
        if (ComPort!=null){
            ComPort.stopSend();
            ComPort.close();
        }
    }

    //----------------------------------------------------打印机上电
    private void OpenPrint(){
        try {
            //上电
            Log.d(TAG,"OpenPrint() printer power on");
            BufferedWriter bw = new BufferedWriter(new FileWriter("/sys/devices/platform/ns_power/ns_power"));
            bw.write("0x100");
            bw.close();
        } catch (IOException e) {
            Log.d(TAG, "Unable to write result file " + e.getMessage());
        }
    }

    //----------------------------------------------------打印机下电
    private void ClosePrint(){
        //下电
        Log.d(TAG,"ClosePrint() printer power off");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("/sys/devices/platform/ns_power/ns_power"));
            bw.write("0x101");
            bw.close();
        } catch (IOException e) {
            Log.d(TAG, "Unable to write result file " + e.getMessage());
        }
    }
}
