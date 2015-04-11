package samplesocket.sample;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subcriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by malkes on 10/04/15.
 */
public class CommunicationService extends Service {

    public final static String TAG_SEND = "send";
    public final static String TAG_RECEIVE = "receive";


    private ServerSocket mServerSocket;
    private Handler mUpdateConversationHandler;
    private Thread mServerThread = null;
    private static final int SERVERPORT = 6000;
    private List<Socket> mClients = new ArrayList<>();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mUpdateConversationHandler = new Handler();
        mServerThread = new Thread(new ServerThread());
        mServerThread.start();

        EventBus.getDefault().register(this);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mServerThread.interrupt();
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                mServerSocket = new ServerSocket(SERVERPORT);

            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = mServerSocket.accept();
                    if(!mClients.contains(socket)){
                        mClients.add(socket);
                    }

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    String read = input.readLine();
                    sendMessageToClients(read);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendMessageToClients(String message){
        EventBus.getDefault().post(message,TAG_RECEIVE);
    }

    @Subcriber(tag = TAG_SEND)
    private void messageReceived(String msg) {
           for (Socket client : mClients){
           PrintWriter printWriter = null;
           try {
               printWriter = new PrintWriter(client.getOutputStream(), true);
               printWriter.write("Server says:" + msg + "\n");  //write the message to output stream
               printWriter.flush();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
