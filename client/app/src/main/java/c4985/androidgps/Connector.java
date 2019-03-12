package c4985.androidgps;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.Socket;

public class Connector {

    private Context caller;

    private String ipAddress;
    private int port;
    private Socket socket;
    private boolean canSend = false;

    public Connector(Context context, String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.caller = context;

        new ConnectAdapter().execute();
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData() {
        if (canSend) {

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
            canSend = true;
        }
    }
}
