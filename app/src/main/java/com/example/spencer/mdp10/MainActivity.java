package com.example.spencer.mdp10;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    public final static String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;

    Button btn_OnOff, btn_Scan, btn_Discoverable, btn_Send, btn_PairedDevices;
    ListView list;

    private static final UUID MY_UUID_INSECURE =
            //UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mBTDevices2 = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter, mDeviceListAdapter2;

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
                        //Clear the list if Bluetooth is turned off
                        clearListBTOff();
                        Toast.makeText(getApplicationContext(),"Bluetooth is now turned off.",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE_TURNING_OFF");
                        Toast.makeText(getApplicationContext(),"Turning off bluetooth...",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE_ON");
                        Toast.makeText(getApplicationContext(),"Bluetooth is now turned on.",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE_TURNING_ON");
                        Toast.makeText(getApplicationContext(),"Turning on bluetooth...",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(),"Discovery is now turned on.",Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(),"Device is successfully paired",Toast.LENGTH_SHORT).show();
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case 2: creating a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                    Toast.makeText(getApplicationContext(),"Pairing with the other device...",Toast.LENGTH_SHORT).show();
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

        //for toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_OnOff = (Button) findViewById(R.id.btn_OnOff);
        btn_Scan = (Button) findViewById(R.id.btn_Scan);
        btn_Discoverable = (Button) findViewById(R.id.btn_Discoverable);
        list = (ListView) findViewById(R.id.List);
        btn_PairedDevices = (Button) findViewById(R.id.btn_PairedDevices);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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

        btn_PairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (!mBluetoothAdapter.isEnabled()){
                    Log.d(TAG, "btnPairedDevices: Bluetooth is not turned on");
                    Toast.makeText(getApplicationContext(),"Turn on Bluetooth to list paired devices.",Toast.LENGTH_SHORT).show();
                }
                else {
                    mBluetoothAdapter.cancelDiscovery();

                    Log.d(TAG, "btnPairedDevices: listing paired devices");

                    //Prevent duplicate
                    if (mDeviceListAdapter2 != null){
                        mBTDevices2.clear();
                        mDeviceListAdapter2.clear();
                    }
                    else {
                        mBTDevices2.clear();
                    }

                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size()>0) {
                        for (BluetoothDevice device : pairedDevices) {
                            mBTDevices.clear(); //prevent clicking on mBTDevices
                            mBTDevices2.add(device);
                        }
                        mDeviceListAdapter2 = new DeviceListAdapter(getApplicationContext(), R.layout.device_adapter_view, mBTDevices2);
                        list.setAdapter(mDeviceListAdapter2);
                    }
                }

            }
        });

        btn_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Clear the list to prevent duplicate when Scan button is pressed.
                if (mDeviceListAdapter != null){
                    mBTDevices.clear();
                    mDeviceListAdapter.clear();
                }
                else {
                    mBTDevices.clear();
                }

                if (!mBluetoothAdapter.isEnabled()){
                    Toast.makeText(getApplicationContext(),"Turn on Bluetooth to scan for devices.",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "btn_Scan: Bluetooth is not turned on.");
                }
                else {
                    Log.d(TAG, "btn_Scan: Looking for unpaired devices.");

                    if (mBluetoothAdapter.isDiscovering()){
                        mBluetoothAdapter.cancelDiscovery();
                        Log.d(TAG, "btn_Scan: Cancelling discovery.");

                        mBluetoothAdapter.startDiscovery();
                        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                        Toast.makeText(getApplicationContext(),"Scanning for nearby devices...",Toast.LENGTH_SHORT).show();
                    }
                    if (!mBluetoothAdapter.isDiscovering()){

                        mBluetoothAdapter.startDiscovery();
                        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                        Toast.makeText(getApplicationContext(),"Scanning for nearby devices...",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btn_Discoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btn_Discoverable: Making device discoverable for 300 seconds.");

                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);

                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(mBroadcastReceiver2, intentFilter);
            }
        });
    }

    public void enableDisableBT(){
        if (mBluetoothAdapter == null){
            Toast.makeText(getApplicationContext(),"Bluetooth is not supported on your device.",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "enableDisableBT:Does not have BT capabilities.");
        }
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            Log.d(TAG, "enableDisableBT: enabling BT");
        }
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();

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
            try {
                if (mBTDevices.get(position).getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "Trying to pair with " + deviceName);
                    mBTDevices.get(position).createBond();

                    mBTDevice = mBTDevices.get(position);
                    //mBluetoothChat = new BluetoothChatService(MainActivity.this);
                }
                else if (mBTDevices.get(position).getBondState() == BluetoothDevice.BOND_BONDED) {
                    //Start chat if already paired
                    StartChat(mBTDevices.get(position));
                }
                else {
                    Log.d(TAG, "onItemClick: did nothing.");
                }
            }
            catch (Exception e) {
                Log.d(TAG, "Error pairing device");
                Toast.makeText(getApplicationContext(),"Please try again later.",Toast.LENGTH_SHORT).show();
            }
        } //method ends here
    }

    public void StartChat(BluetoothDevice selectedDevice){
        Intent chatIntent = new Intent(MainActivity.this, TransmitActivity.class);
        chatIntent.putExtra("btDevice", selectedDevice);
        startActivity(chatIntent);
    }

    private void clearListBTOff(){
        if (mDeviceListAdapter != null){
            mDeviceListAdapter.clear();
        }
        if (mDeviceListAdapter2 != null){
            mDeviceListAdapter2.clear();
        }
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

    }
}
