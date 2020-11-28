package com.example.ztestgolibaar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import local.Local;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    TextView hello;
    Button startservice, stopservice,bindService ,unbindService, connect;
    private MyService.MyBinder myBinder;

    protected static MainActivity mMainActivity;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MyService.MyBinder) service;
            myBinder.connect();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hello = (TextView)findViewById(R.id.hello);
        startservice = (Button)findViewById(R.id.startservice);
        stopservice = (Button)findViewById(R.id.stopservice);
        connect  = (Button)findViewById(R.id.startVpnService);
        long result =Local.add(10 ,5);
        hello.setText(result + "");

        bindService = (Button) findViewById(R.id.bind_service);
        unbindService = (Button) findViewById(R.id.unbind_service);
        bindService.setOnClickListener(this);
        unbindService.setOnClickListener(this);
        startservice.setOnClickListener(this);
        stopservice.setOnClickListener(this);
        connect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startservice:
//                startVPN();
                Intent startIntent = new Intent(this, MyService.class);
                startService(startIntent);
                break;
            case R.id.stopservice:
                Intent stopIntent = new Intent(this, MyService.class);
                stopService(stopIntent);
                break;
            case R.id.bind_service:
                Intent bindIntent = new Intent(this, MyService.class);
                bindService(bindIntent, connection, BIND_AUTO_CREATE);
                break;
            case R.id.unbind_service:
                unbindService(connection);
                break;
            case R.id.startVpnService:

//                Intent i = new Intent(getApplicationContext(), MyService.class);
                Intent vpn = VpnService.prepare(MainActivity.this);
//                getApplicationContext().startService(i);
                if(vpn != null) {
                    //get vpn permission for first time
                    startActivityForResult(vpn, 0);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            startVPN();
//            Intent startIntent = new Intent(this, MyService.class);
//            MainActivity.this.startService(startIntent);
//            bindService(startIntent, connection, BIND_AUTO_CREATE);
        }
    }


    void startVPN(){
        Intent startIntent = new Intent(this, MyService.class);
        MainActivity.this.startService(startIntent);
        bindService(startIntent, connection, BIND_AUTO_CREATE);
    }

}

