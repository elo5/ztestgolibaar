package com.example.ztestgolibaar;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    public interface Prefs {
        String NAME = "connection";
        String SERVER_ADDRESS = "server.address";
        String SERVER_PORT = "server.port";
        String SHARED_SECRET = "shared.secret";
        String PROXY_HOSTNAME = "proxyhost";
        String PROXY_PORT = "proxyport";
        String ALLOW = "allow";
        String PACKAGES = "packages";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView serverAddress = findViewById(R.id.address);
        final TextView serverPort = findViewById(R.id.port);
        final TextView sharedSecret = findViewById(R.id.secret);
        final TextView proxyHost = findViewById(R.id.proxyhost);
        final TextView proxyPort = findViewById(R.id.proxyport);

        final RadioButton allowed = findViewById(R.id.allowed);
        final TextView packages = findViewById(R.id.packages);

//        ListenAddr: ":36623",
//         RemoteAddr: "34.94.217.10:36623",
      String sPassword =   "wxUENU6GRV8GU6q6zLftQ3DGK/bwc8dM48pXcmlKMVyXMgLms1gAZW6sVvNSkNaTtuggsEk5Gty1LXQ+052mEb//jh/dySNjKds8YN93mIfXj+oke5Jo2ZowHeGroC4LlsJG+YkOQuvEO/fQxbuEIfoSuXGfYYxAgSXnVIteLMFNGHh1ma1diGIiZOK4o+kDoUSFlKk/8Xb0wL1agBAoje4BDO9vDeU4fkfe/mvPGxT8r/ubvAV9N9E9S5x6GQ+VOq4z9dXapDR5qE++fzbYbVtBscgcUIoHpVETZiZIzSqyyxaDZ+Ts8h5ZCghVnmqn+OAJfCf91KIXbIK00i+Rzg==";



        final SharedPreferences prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE);
        serverAddress.setText(prefs.getString(Prefs.SERVER_ADDRESS, "34.94.217.10"));
        int serverPortPrefValue = prefs.getInt(Prefs.SERVER_PORT, 36623);
        serverPort.setText(String.valueOf(serverPortPrefValue == 0 ? "" : serverPortPrefValue));
        sharedSecret.setText(prefs.getString(Prefs.SHARED_SECRET, sPassword));
        proxyHost.setText(prefs.getString(Prefs.PROXY_HOSTNAME, "192.168.0.105"));
        int proxyPortPrefValue = prefs.getInt(Prefs.PROXY_PORT, 9910);
        proxyPort.setText(proxyPortPrefValue == 0 ? "" : String.valueOf(proxyPortPrefValue));

        allowed.setChecked(prefs.getBoolean(Prefs.ALLOW, true));
        packages.setText(String.join(", ", prefs.getStringSet(
                Prefs.PACKAGES, Collections.emptySet())));

        findViewById(R.id.connect).setOnClickListener(v -> {
            if (!checkProxyConfigs(proxyHost.getText().toString(),
                    proxyPort.getText().toString())) {
                return;
            }

            final Set<String> packageSet =
                    Arrays.stream(packages.getText().toString().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toSet());
            if (!checkPackages(packageSet)) {
                return;
            }

            int serverPortNum;
            try {
                serverPortNum = Integer.parseInt(serverPort.getText().toString());
            } catch (NumberFormatException e) {
                serverPortNum = 0;
            }
            int proxyPortNum;
            try {
                proxyPortNum = Integer.parseInt(proxyPort.getText().toString());
            } catch (NumberFormatException e) {
                proxyPortNum = 0;
            }
            prefs.edit()
                    .putString(Prefs.SERVER_ADDRESS, serverAddress.getText().toString())
                    .putInt(Prefs.SERVER_PORT, serverPortNum)
                    .putString(Prefs.SHARED_SECRET, sharedSecret.getText().toString())
                    .putString(Prefs.PROXY_HOSTNAME, proxyHost.getText().toString())
                    .putInt(Prefs.PROXY_PORT, proxyPortNum)
                    .putBoolean(Prefs.ALLOW, allowed.isChecked())
                    .putStringSet(Prefs.PACKAGES, packageSet)
                    .commit();
            Intent intent = VpnService.prepare(MainActivity.this);
            if (intent != null) {
                startActivityForResult(intent, 0);
            } else {
                onActivityResult(0, RESULT_OK, null);
            }
        });
        findViewById(R.id.disconnect).setOnClickListener(v -> {
            startService(getServiceIntent().setAction(MyVpnService.ACTION_DISCONNECT));
        });
    }

    private boolean checkProxyConfigs(String proxyHost, String proxyPort) {
        final boolean hasIncompleteProxyConfigs = proxyHost.isEmpty() != proxyPort.isEmpty();
        if (hasIncompleteProxyConfigs) {
            Toast.makeText(this, R.string.incomplete_proxy_settings, Toast.LENGTH_SHORT).show();
        }
        return !hasIncompleteProxyConfigs;
    }

    private boolean checkPackages(Set<String> packageNames) {
        final boolean hasCorrectPackageNames = packageNames.isEmpty() ||
                getPackageManager().getInstalledPackages(0).stream()
                        .map(pi -> pi.packageName)
                        .collect(Collectors.toSet())
                        .containsAll(packageNames);
        if (!hasCorrectPackageNames) {
            Toast.makeText(this, R.string.unknown_package_names, Toast.LENGTH_SHORT).show();
        }
        return hasCorrectPackageNames;
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result == RESULT_OK) {
            startService(getServiceIntent().setAction(MyVpnService.ACTION_CONNECT));
        }
    }

    private Intent getServiceIntent() {
        return new Intent(this, MyVpnService.class);
    }
}


//public class MainActivity extends AppCompatActivity implements View.OnClickListener  {
//
//    TextView hello;
//    Button startservice, stopservice,bindService ,unbindService, connect;
//    private MyService.MyBinder myBinder;
//
//    private ServiceConnection connection = new ServiceConnection() {
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//        }
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            myBinder = (MyService.MyBinder) service;
//            myBinder.connect();
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)  {
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        hello = (TextView)findViewById(R.id.hello);
//        startservice = (Button)findViewById(R.id.startservice);
//        stopservice = (Button)findViewById(R.id.stopservice);
//        connect  = (Button)findViewById(R.id.startVpnService);
////        long result =Local.add(10 ,5);
////        hello.setText(result + "");
//
//        bindService = (Button) findViewById(R.id.bind_service);
//        unbindService = (Button) findViewById(R.id.unbind_service);
//        bindService.setOnClickListener(this);
//        unbindService.setOnClickListener(this);
//        startservice.setOnClickListener(this);
//        stopservice.setOnClickListener(this);
//        connect.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.startservice:
//                Intent startIntent = new Intent(this, MyService.class);
//                startService(startIntent);
//                break;
//            case R.id.stopservice:
//                Intent stopIntent = new Intent(this, MyService.class);
//                stopService(stopIntent);
//                break;
//            case R.id.bind_service:
//                Intent bindIntent = new Intent(this, MyService.class);
//                bindService(bindIntent, connection, BIND_AUTO_CREATE);
//                break;
//            case R.id.unbind_service:
//                unbindService(connection);
//                break;
//            case R.id.startVpnService:
//
////                Intent i = new Intent(getApplicationContext(), MyService.class);
//                Intent vpn = VpnService.prepare(MainActivity.this);
////                getApplicationContext().startService(i);
//                if(vpn != null) {
//                    //get vpn permission for first time
//                    startActivityForResult(vpn, 0);
//                }else {
//                    startVPN();
//                }
//                break;
//            default:
//                break;
//        }
//    }
//
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 0 && resultCode == RESULT_OK) {
//            startVPN();
//        }
//    }
//
//
//    void startVPN(){
//        Intent startIntent = new Intent(this, MyService.class);
//        MainActivity.this.startService(startIntent);
//        bindService(startIntent, connection, BIND_AUTO_CREATE);
//    }
//
//}
//
