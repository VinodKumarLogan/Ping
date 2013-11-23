package com.example.pingv2;

import android.os.Bundle;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.app.Activity;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.net.*;


public class MainActivity extends Activity {
	EditText us,pw,ip;
	TextView l;
	Button reg,log;
	private final static int port1 = 50000;
	public static DatagramSocket socket;
	public static ServerSocket TCPSocket ;
	public String intToIp(int i) {

		   return   (i & 0xFF)+ "." +
		               ((i >> 8 ) & 0xFF) + "." +
		               ((i >> 16 ) & 0xFF) + "." +((i >> 24 ) & 0xFF )
		                ;
		}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		reg = (Button) findViewById(R.id.register);
		log = (Button) findViewById(R.id.login);
		ip = (EditText) findViewById(R.id.ipaddr);
		us = (EditText) findViewById(R.id.username);
		pw = (EditText) findViewById(R.id.password);
		l = (TextView) findViewById(R.id.logscreen);

		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		wifiInfo.getIpAddress();
		int ipAddress = wifiInfo.getIpAddress();
		final String IP = intToIp(ipAddress);
		
		
		l.setMovementMethod(ScrollingMovementMethod.getInstance());
		try {
			socket = new DatagramSocket(null);
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(IP.toString(), port1));
			
			socket = new DatagramSocket(null);
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(IP.toString(), port1));
			l.setText("Socket Created "+IP+" "+port1);
		}
		catch(Exception e) {
			l.setText("Exception : "+e);
		}	
		reg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//socket.disconnect();
				Intent i = new Intent(MainActivity.this, Register.class);
				i.putExtra("hostip",IP);
				i.putExtra("serverip",ip.getText().toString());
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				finish();
				startActivity(i);
			}
		});
		new Thread(new Runnable() {
			public void run() {
				while(true)
				{
					try {
						byte[] receiveData = new byte[1024];
						final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						socket.receive(receivePacket);
						final String sentence = new String(receivePacket.getData(),0, receivePacket.getLength()); 
						
						if(receivePacket.getAddress().toString().substring(1).equalsIgnoreCase(ip.getText().toString())) {
							runOnUiThread(new Runnable() { 
								public void run() {
									if(!sentence.toString().substring(0, 16).equals("Login Successful".toString()))
										l.setText(sentence.toString().substring(0,16));
									else {
										Intent i = new Intent(MainActivity.this, Chat.class);
										i.putExtra("hostip",IP);
										i.putExtra("username",us.getText().toString());
										i.putExtra("serverip",ip.getText().toString());
										i.putExtra("uid",sentence.toString().substring(17));
										i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										finish();
										startActivity(i);
									}
								}
							});
						}
						else {
							runOnUiThread(new Runnable() { 
								public void run() {
									l.setText(receivePacket.getAddress().toString().substring(1)+" "+ip.getText().toString());
								}
							});
						}
					}
					catch(Exception e) {
						final Exception x = e; 
						runOnUiThread(new Runnable() { 
							public void run() {
								l.setText("Exception at receiver: "+x);
							}
						});					
					}
				}
			}
		}).start();
		
		log.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
				    public void run() {
				    	try {
				    		InetAddress ipaddr = InetAddress.getByName(ip.getText().toString());
				    		byte[] sendData;
				    		String sentence = "login "+us.getText().toString()+" "+pw.getText();
				    		sendData = sentence.getBytes();
				    		DatagramPacket sendPacket =new DatagramPacket(sendData, sendData.length,ipaddr,50000);
				    		socket.send(sendPacket);
				    	}
				    	catch(Exception e) {
				    		final Exception x = e; 
				    		runOnUiThread(new Runnable() { 
				    			public void run() {
				    				l.setText("Exception at sender: "+x);
				    			}
				    		});
				    	}
				    }
				  }).start();				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
