package samplesocket.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subcriber;


public class MainActivity extends ActionBarActivity {

    private EditText text;
    private EditText et_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        text = (EditText) findViewById(R.id.text);
        et_msg = (EditText) findViewById(R.id.et_msg);

        Intent serviceIntent = new Intent(this,CommunicationService.class);
        startService(serviceIntent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subcriber(tag = CommunicationService.TAG_RECEIVE)
    private void messageReceived(String msg) {
        text.setText(msg);
    }

    public void sendMsg(View v){
        EventBus.getDefault().post(et_msg.getText().toString(),CommunicationService.TAG_SEND);
    }



}
