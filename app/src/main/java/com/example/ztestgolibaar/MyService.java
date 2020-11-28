package com.example.ztestgolibaar;//

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import local.Local;

//import tun2socks.PacketFlow;
//import tun2socks.Tun2socks;

import android.os.RemoteException;
import android.util.Log;

// Created by  on 20/11/2020.
//
public class MyService extends VpnService   {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);

    public static final String TAG = "MyService";

    public static final String CHANNEL_ID = "GOSOCKS_SERVICE";
    private ParcelFileDescriptor mInterface;
    private static final int BUFSIZE = 4096;

    private Thread mThread;
    private Future<?> deviceToTunnelFuture;
    private Future<?> tunnelToDeviceFuture;


    private MyBinder mBinder = new MyBinder();



    @Nullable
    @Override
    public IBinder onBind(Intent intent) { // 和 activity  建立 关联、通信
        return mBinder;
    }

    @Override
    public void onCreate() { // 只会在第一次创建的时候调用一次
        super.onCreate();

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(CHANNEL_ID);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
    }

    private PendingIntent mPendingIntent;
    private ParcelFileDescriptor mFileDescriptor;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        createNotificationChannel();
//
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);
//
//        if (this.mPendingIntent  == null) this.mPendingIntent = pendingIntent;
//
//        Notification notification = new Notification.Builder(getApplication()).
//                setSmallIcon(R.mipmap.ic_launcher).build();
//        startForeground(1, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.mFileDescriptor == null) {
            return;
        }
        try {
            this.mFileDescriptor.close();
            this.mFileDescriptor = null;
        } catch (IOException ex) {
        }
    }

    class MyBinder extends Binder {

        public void connect() {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    Local.runMain();

//                    try {
//                        configure();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }


                }
            }).start();
        }
    }
//
//    @Override
//    public void run() {
//        configure();
//    }

    private void configure()
    {
        Log.i(TAG, "Configure");

        try {
            Builder builder = new Builder();
            builder.setSession("gosocks");
            builder.setMtu(1500);//Util.tunVPN_MTU);
            builder.addAddress("10.8.0.2", 32);

            builder.addDnsServer("114.114.114.114");
            builder.addDnsServer("8.8.8.8");
            builder.addDnsServer("8.8.4.4");

            Log.i(TAG, "Routing all traffic");
//            int o1 = 95, o2 = 179, o3 = 201, o4 = 97;
//            int _1, _2, _3, _4;
//            //octave 1
//            for(_1=1; _1 < 255; _1++) {
//                if (_1 == 127 || _1 == o1)
//                    continue;
//                builder.addRoute(_1 + ".0.0.0", 8); //Redirect all traffic
//            }
//            //octave 2
//            for(_2=1; _2 < 255; _2++) {
//                if (_2 == o2)
//                    continue;
//                builder.addRoute(_1 + "." + _2 + ".0.0", 16); //Redirect all traffic
//            }
//            //octave 3
//            for(_3=1; _3 < 255; _3++) {
//                if (_3 == o3)
//                    continue;
//                builder.addRoute(_1 + "." + _2 + "." + _3 + ".0", 24); //Redirect all traffic
//            }
//            //octave 4
//            for(_4=1; _4 < 255; _4++) {
//                if (_4 == o4)
//                    continue;
//                builder.addRoute(_1 + "." + _2 + "." + _3 + "." + _4, 32); //Redirect all traffic
//            }
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                builder.allowFamily(android.system.OsConstants.AF_INET6);
//            }

            mInterface = builder.establish();

//            deviceToTunnelFuture = EXECUTOR_SERVICE.submit(new Runnable() {
//                @Override
//                public void run() {
//                    forwardVpnServiceToTunnel(mInterface.getFileDescriptor());
//                }
//            });
//            tunnelToDeviceFuture = EXECUTOR_SERVICE.submit(new Runnable() {
//                @Override
//                public void run() {
//                    forwardTunnelToVpnService(mInterface.getFileDescriptor());
//                }
//            });

            if (mInterface == null){
                this.onDestroy();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            this.onDestroy();
        }
    }


//    private void forwardVpnServiceToTunnel(FileDescriptor vpnFileDescriptor){
//        final FileOutputStream vpnOutput = new FileOutputStream(vpnFileDescriptor);
//        Tun2socks.startSocks(new PacketFlow() {
//            @Override
//            public void writePacket(byte[] buffers) {
//                try {
//                    vpnOutput.write(buffers);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, "127.0.0.1", 8080);
//    }
//
//    private void forwardTunnelToVpnService(FileDescriptor vpnFileDescriptor){
//        final FileInputStream vpnInput = new FileInputStream(vpnFileDescriptor);
//        byte[] buffer = new byte[BUFSIZE];
//        int w;
//        while (true) {
//            // blocking receive
//            w = -1;
//            try {
//                w = vpnInput.read(buffer);
//            } catch (IOException e) {
//                e.printStackTrace();
//                break;
//            }
//            if (w == -1) {
//                break;
//            }
//            Tun2socks.inputPacket(buffer);
//        }
//    }

}
