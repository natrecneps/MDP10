package com.example.spencer.mdp10;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ToggleButton tglbtn_On;
    Button btn_Scan, btn_Discoverable, btn_ListPairedDevices;
    ListView list;

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<String> mDeviceList = new ArrayList<String>();

    private final static int REQUEST_ENABLED = 1;
    private final static int REQUEST_DISCOVERABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tglbtn_On = (ToggleButton) findViewById(R.id.Tglbtn_On);
        btn_Scan = (Button) findViewById(R.id.btn_Scan);
        btn_Discoverable = (Button) findViewById(R.id.btn_Discoverable);
        btn_ListPairedDevices = (Button) findViewById(R.id.btn_ListPairedDevices);
        list = (ListView) findViewById(R.id.List);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ToggleOff();

        tglbtn_On.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (bluetoothAdapter == null) {
                        //Device doesn't support Bluetooth
                        Toast.makeText(getApplicationContext(),"Device does not support Bluetooth.",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (!bluetoothAdapter.isEnabled()){
                            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),REQUEST_ENABLED);
                            Toast.makeText(getApplicationContext(),"Bluetooth is now enabled.",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Bluetooth is already turned on.",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    bluetoothAdapter.disable();
                    Toast.makeText(getApplicationContext(),"Bluetooth is now disabled.",Toast.LENGTH_SHORT).show();
                    list.setAdapter(null);
                }
            }
        });

        btn_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetoothAdapter.isEnabled()){
                    Toast.makeText(getApplicationContext(),"Turn on Bluetooth to scan for devices nearby.",Toast.LENGTH_SHORT).show();
                }
                else {
                    bluetoothAdapter.startDiscovery();
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(Receiver, filter);
                }
            }
        });

        btn_Discoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make the device discoverable and set the discover time
                if (!bluetoothAdapter.isDiscovering()){
                    Intent i = new Intent(bluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    i.putExtra(bluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(i);
                    Toast.makeText(getApplicationContext(),"Bluetooth is now discoverable for 300 seconds.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_ListPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //list previously paired devices
                pairedDevices = bluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0){
                    ArrayList deviceList = new ArrayList();

                    for (BluetoothDevice bt : pairedDevices) deviceList.add(bt.getName());

                    Toast.makeText(getApplicationContext(),"Showing list of paired devices.",Toast.LENGTH_SHORT).show();

                    final ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, deviceList);

                    list.setAdapter(arrayAdapter);
                }
                else if (pairedDevices.size() < 0){
                    Toast.makeText(getApplicationContext(),"No paired devices.",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Turn on Bluetooth to see list of paired devices.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private final BroadcastReceiver Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device.getName() + "\n" + device.getAddress());
                list.setAdapter(new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, mDeviceList));
            }
        }
    };

    @Override
    protected void onDestroy(){
        unregisterReceiver(Receiver);
        super.onDestroy();
    }

    private void ToggleOff(){
        if (bluetoothAdapter.isEnabled()){
            tglbtn_On.setChecked(true);
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
}
