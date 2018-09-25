package com.example.spencer.mdp10;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
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

public class Transmit extends MainActivity{

    Button btn_Send;
    TextView tv_Receive;
    EditText et_Send;
    StringBuilder messages;

    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
            //UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    BluetoothDevice mBTDevice;
    BluetoothChatService mBluetoothChat;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            messages.append(text + "\n");

            tv_Receive.setText(messages);
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
                btn_Send.setEnabled(true);
                Display("Connected to "+ device.getName()+ " - " + device.getAddress());
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                Log.d(TAG, "btReceiver: Disconnecting.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                Log.d(TAG, "btReceiver: Disconnected.");
                btn_Send.setEnabled(false);
                Display("Disconnected!");
                Display("Connecting again...");
                //startConnection();
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
        btn_Send.setEnabled(false);
        et_Send = (EditText)findViewById(R.id.et_Send);
        tv_Receive = (TextView)findViewById(R.id.tv_Receive);
        messages = new StringBuilder();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        mBluetoothChat = new BluetoothChatService(Transmit.this);
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

                    et_Send.setText("");
                }
            }
        });
    }

    public void startConnection(){
        Display("Connecting...");
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    //Starting chat service method
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

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
        MenuItem itemToHide = menu.findItem(R.id.bluetooth);
        MenuItem itemToHide2 = menu.findItem(R.id.transmit);
        itemToHide.setVisible(false);
        itemToHide2.setVisible(false);
        return true;
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.transmit:
                intent = new Intent(this, Transmit.class);
                startActivity(intent);
                break;
            case R.id.bluetooth:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent i = new Intent(Transmit.this, MainActivity.class);
        startActivity(i);
    }
}
