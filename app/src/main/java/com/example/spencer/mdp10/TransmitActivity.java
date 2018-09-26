package com.example.spencer.mdp10;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.UUID;

public class TransmitActivity extends MainActivity{
    public final static String TAG = "TransmitActivity";

    Button btn_Send;
    TextView tv_Receive;
    EditText et_Send;
    //StringBuilder messages;

    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
            //UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    BluetoothDevice mBTDevice;
    BluetoothChatService mBluetoothChat;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            //messages.append(mBTDevice.getName() + ": " + text + "\n");
            String incomingMessage = (mBTDevice.getName() + ": " + text);
            Display(incomingMessage);
        }
    };

    BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                Log.d(TAG, "btReceiver: Connected.");
                Display("Connected to "+ device.getName()+ " - " + device.getAddress());
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                Log.d(TAG, "btReceiver: Disconnecting.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                Log.d(TAG, "btReceiver: Disconnected.");
                Display("Connection was lost.");
                Display("Reconnecting...");
                btn_Send.setEnabled(false);
                //reconnect if disconnect
                startConnection();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(btReceiver, filter);

        //prevent android keyboard auto popup when activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //set toolbar to this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_Send = (Button)findViewById(R.id.btn_Send);
        et_Send = (EditText)findViewById(R.id.et_Send);
        tv_Receive = (TextView)findViewById(R.id.tv_Receive);
        //scrollable textview
        tv_Receive.setMovementMethod(new ScrollingMovementMethod());
        //messages = new StringBuilder();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        mBluetoothChat = new BluetoothChatService(TransmitActivity.this);
        mBTDevice = getIntent().getExtras().getParcelable("btDevice");
        startConnection();

        btn_Send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String Message = et_Send.getText().toString();
                if (Message.matches("")){
                    Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    byte[] bytes = et_Send.getText().toString().getBytes(Charset.defaultCharset());
                    mBluetoothChat.write(bytes);

                    //display my output in text view
                    String myName = mBluetoothAdapter.getName();
                    String outgoingMessage = (myName + ": " + et_Send.getText());
                    Display(outgoingMessage);
                    et_Send.setText("");
                }
            }
        });
    }

    public void startConnection(){
        Log.d(TAG, "startConnection: Calling startBTConnection");
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    //Starting chat service method
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        Display("Starting connection...");
        mBluetoothChat.startClient(device, uuid);
    }

    public void Display(final String s){
        tv_Receive.append(s + "\n");
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(btReceiver);
        unregisterReceiver(mReceiver);
        super.onDestroy();
        Log.d(TAG, "onDestroy is called.");
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //MenuItem itemToHide = menu.findItem(R.id.About);
        //itemToHide.setVisible(false);
        return true;
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.About:
                Toast.makeText(getApplicationContext(), "Made by Spencer Tan and Vo Hong Khanh of Group 10, Semester 1 18/19", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent i = new Intent(TransmitActivity.this, MainActivity.class);
        startActivity(i);
    }
}
