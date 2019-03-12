package c4985.androidgps;

import android.content.Context;
import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Connector {

    private MyLocationListener locationListener;

    private String ipAddress;
    private int port;
    private Socket socket;

    public Connector(MyLocationListener locationListener, String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.locationListener = locationListener;
    }

    public void connect() {
        new ConnectAdapter().execute();
    }

    public void disconnect() {
        try {
            socket.close();
            if (locationListener != null) {
                locationListener.setInactive();
            }
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

    private class ConnectAdapter extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                socket = new Socket(ipAddress, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (locationListener != null) {
                locationListener.setActive();
            }
        }
    }
}
