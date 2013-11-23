package com.example.pingv2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Register extends Activity {
	DatagramSocket socket;
	TextView t;
	EditText u,p,cp;
	Button r,b;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		Intent intent = getIntent();
		final String hostIP = intent.getStringExtra("hostip");
	    final String serverIP = intent.getStringExtra("serverip");
	    r = (Button) findViewById(R.id.reg);
	    b = (Button) findViewById(R.id.back);
		u = (EditText) findViewById(R.id.reguser);
		p = (EditText) findViewById(R.id.regpasswd);
		cp = (EditText) findViewById(R.id.regcon);
		t = (TextView) findViewById(R.id.reglog);
		//Creating Socket
	    try {
	    	socket = new DatagramSocket(null);
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(hostIP.toString(), 50000));
			socket.setSoTimeout(0); 
			runOnUiThread(new Runnable() { 
				public void run() {
					t.setText("Socket Created "+hostIP+" "+50000);
				}
			});
		}
		catch(Exception e) {
			final Exception x = e; 
			runOnUiThread(new Runnable() { 
				public void run() {
					t.setText("Exception at receiver: "+x);
				}
			});
		}
	    //Receiving thread
	    new Thread(new Runnable() {
			public void run() {
				while(true)
				{
					try {
						byte[] receiveData = new byte[1024];
						final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						socket.receive(receivePacket);
						final String sentence = new String(receivePacket.getData(),0, receivePacket.getLength()); 						
						if(receivePacket.getAddress().toString().substring(1).equalsIgnoreCase(serverIP)) {
							runOnUiThread(new Runnable() { 
								public void run() {
									if(sentence.substring(0, 9).equals("Registered")) 
										t.setText("Login with the new username to continue");
									else
										t.setText(sentence);
								}
							});
						}
						else {
							runOnUiThread(new Runnable() { 
								public void run() {
									t.setText(receivePacket.getAddress().toString().substring(1));
								}
							});
						}
					}
					catch(Exception e) {
						final Exception x = e; 
						runOnUiThread(new Runnable() { 
							public void run() {
								t.setText("Exception at receiver: "+x);
							}
						});					
					}
				}
			}
		}).start();
	    //Back button	  
	    b.setOnClickListener(new View.OnClickListener() {
	    	@Override
			public void onClick(View v) {
	    		Intent i = new Intent(Register.this, MainActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				finish();
				startActivity(i);
	    	}
	    });
	  //Sending thread
	    r.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
				    public void run() {
				    	try {
				    		if(!p.getText().toString().equals(cp.getText().toString())) {
				    			runOnUiThread(new Runnable() { 
					    			public void run() {
					    				t.setText("Passwords donot match");
					    			}
					    		});
				    		}
				    		else {
				    			InetAddress ipaddr = InetAddress.getByName(serverIP);
				    			byte[] sendData;
				    			String sentence = "register "+u.getText().toString()+" "+p.getText();
				    			sendData = sentence.getBytes();
				    			DatagramPacket sendPacket =new DatagramPacket(sendData, sendData.length,ipaddr,50000);
				    			socket.send(sendPacket);
				    		}
				    	}
				    	catch(Exception e) {
				    		final Exception x = e; 
				    		runOnUiThread(new Runnable() { 
				    			public void run() {
				    				t.setText("Exception at sender: "+x);
				    			}
				    		});
				    	}
				    }
				  }).start();				
			}
		});
	}
}

