package com.example.spencer.mdp10;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    public final static String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;
    BluetoothChatService mBluetoothChat;

    Button btn_OnOff, btn_Scan, btn_Discoverable, btn_Send, btn_StartConnection;
    ListView list;
    EditText etSend;

    TextView incomingMessages;
    StringBuilder messages;

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter;

    //Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE_TURNING_ON");
                        break;
                }
            }
        }
    };

    //Discoverability mode on/off or expire
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode  = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, mBluetoothAdapter.ERROR);

                switch(mode){
                    //Device is in Discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled. Able to receive connection.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability disabled. Not able to receive connection.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }
            }
        }
    };

    //Broadcast Receiver for listing devices that are not yet paired
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive:" + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                list.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //case 1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case 2: creating a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case 3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    @Override
    protected void onDestroy(){
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
        super.onDestroy();
        Log.d(TAG, "onDestroy is called.");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //avoid automatically appear android keyboard when activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_OnOff = (Button) findViewById(R.id.btn_OnOff);
        btn_Scan = (Button) findViewById(R.id.btn_Scan);
        btn_Discoverable = (Button) findViewById(R.id.btn_Discoverable);
        list = (ListView) findViewById(R.id.List);

        btn_StartConnection = (Button) findViewById(R.id.btn_StartConnection);
        btn_Send = (Button) findViewById(R.id.btn_Send);
        etSend = (EditText) findViewById(R.id.editText);

        incomingMessages = (TextView) findViewById(R.id.tv_IncomingMessage);
        messages = new StringBuilder();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        mBTDevices = new ArrayList<>();

        list.setOnItemClickListener(MainActivity.this);

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        btn_OnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                enableDisableBT();
                Log.d(TAG, "onClick: enabling/disabling bluetooth");
            }
        });

        btn_StartConnection.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startConnection();
            }
        });

        btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothChat.write(bytes);

                etSend.setText("");
            }
        });

        btn_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

                if (mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Cancelling discovery.");

                    //Check BT permissions in manifest
                    //checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
                if (!mBluetoothAdapter.isDiscovering()){

                    //check BT permissions in manifest
                    //checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
            }
        });

        btn_Discoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnEnable_Disable_Discoverable: Making device discoverable for 300 seconds.");

                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);

                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(mBroadcastReceiver2, intentFilter);
            }
        });
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            messages.append(text + "\n");

            incomingMessages.setText(messages);
        }
    };

    //method for starting connection
    //remember the connection will fail and app will crash if you haven't paired first
    public void startConnection(){
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    //Starting chat service method
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothChat.startClient(device, uuid);
    }

    public void enableDisableBT(){
        if (mBluetoothAdapter == null){
            Toast.makeText(getApplicationContext(),"Bluetooth is not supported on your device.",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "enableDisableBT:Does not have BT capabilities.");
        }
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            Toast.makeText(getApplicationContext(),"Bluetooth is now enabled",Toast.LENGTH_SHORT).show();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            Log.d(TAG, "enableDisableBT: enabling BT");
        }
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(),"Bluetooth is now disabled",Toast.LENGTH_SHORT).show();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            Log.d(TAG, "enableDisableBT: disabling BT");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //first cancel discovery because it is very memory intensive
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You clicked on a device.");
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond
        //NOTE: Requires API 17+?
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(position).createBond();

            mBTDevice = mBTDevices.get(position);
            mBluetoothChat = new BluetoothChatService(MainActivity.this);
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem itemToHide = menu.findItem(R.id.bluetooth);
        MenuItem itemToShow = menu.findItem(R.id.transmit);
        itemToHide.setVisible(false);
        itemToShow.setVisible(true);
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

    }
}
