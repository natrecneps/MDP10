package com.example.spencer.mdp10;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Transmit extends MainActivity{

    Button btn_Send;
    TextView tv_Receive;
    EditText et_Send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_Send = (Button)findViewById(R.id.btn_Send);
        et_Send = (EditText)findViewById(R.id.et_Send);
        tv_Receive = (TextView)findViewById(R.id.tv_Receive);

        btn_Send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String Message = et_Send.getText().toString();
                if (Message.matches("")){
                    Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    tv_Receive .setText(Message);
                }
            }
        });
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem itemToHide = menu.findItem(R.id.transmit);
        MenuItem itemToShow = menu.findItem(R.id.bluetooth);
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
