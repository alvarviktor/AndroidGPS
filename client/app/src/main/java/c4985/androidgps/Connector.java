package c4985.androidgps;

import android.content.Context;
import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connector {

    private MyLocationListener locationListener;

    private String ipAddress;
    private int port;
    private Socket socket;
    private boolean isConnected;
    private Lock lock;

    public Connector(MyLocationListener locationListener, String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.locationListener = locationListener;
        this.lock = new ReentrantLock();
    }

    public void connect() {
        new ConnectAdapter().start();
    }

    public void disconnect() {
        try {
            socket.close();
            isConnected = false;
//            if (locationListener != null) {
//                locationListener.setInactive();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(String data) {
        try {
            OutputStream outToServer = socket.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unlock() {
        if (isConnected) {
            lock.unlock();
        }
    }

    private class ConnectAdapter extends Thread {

        @Override
        public void run() {
            try {
                socket = new Socket(ipAddress, port);
                isConnected = true;
                while (isConnected) {
//                    try {
//                        if (lock.tryLock(10, TimeUnit.SECONDS)) {
//                            sendData(locationListener.locationMsg);
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    if (locationListener.canSend) {
                        locationListener.canSend = false;
                        sendData(locationListener.locationMsg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
