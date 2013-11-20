package com.hostzi.ping;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import android.view.View;

public class Chat extends Activity {
	Spinner clientList;
	TextView chatBox,logName,logWindow;
	Button backButton,sendButton,refresh;
	EditText chatMessage;
	DatagramSocket socket;
	public String temp;
	List<String> activeClientName = new ArrayList<String>();
	int[] activeClientUID;
	public ArrayAdapter<String> dataAdapter;
	String sentence;
	String inpData;
	String err;
	String[] tempVar;
	List<String> msgs = new ArrayList<String>();
	public int find(int[] array, int value) {
	    int t=0;
		for(int i=0; i<array.length; i++) 
	         if(array[i] == value)
	             t= i;
		return t;
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.chat_window);
	    Intent intent = getIntent();
		final String hostIP = intent.getStringExtra("hostip");
	    final String serverIP = intent.getStringExtra("serverip");
	    final String userName = intent.getStringExtra("username");
	    final String uid = intent.getStringExtra("uid");
	    backButton = (Button) findViewById(R.id.chatback);
	    sendButton = (Button) findViewById(R.id.send);
	    refresh = (Button) findViewById(R.id.refresh);
	    chatMessage = (EditText) findViewById(R.id.sendmsg);
		chatBox = (TextView) findViewById(R.id.recvmsg);
		logWindow = (TextView) findViewById(R.id.log);
		logName = (TextView) findViewById(R.id.loggedin);
		clientList = (Spinner) findViewById(R.id.clientlist);
	    logName.setText("Logged in as "+userName);
	    chatBox.setMovementMethod(new ScrollingMovementMethod());
	  //Creating Socket
	    
	    try {
	    	socket = new DatagramSocket(null);
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(hostIP.toString(), 50000));
			socket.setSoTimeout(0); 
			runOnUiThread(new Runnable() { 
				public void run() {
					logWindow.setText("Socket Created "+hostIP+" "+50000);
				}
			});
		}
		catch(Exception e) {
			final Exception x = e; 
			runOnUiThread(new Runnable() { 
				public void run() {
					logWindow.setText("Exception at receiver: "+x);
				}
			});
		}
	    
		dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, activeClientName);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		clientList.setAdapter(dataAdapter);
		
		new Thread(new Runnable() {
			public void run() {
				while(true)
				{
					try {
						byte[] receiveData = new byte[1024];
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						socket.receive(receivePacket);
						sentence = new String(receivePacket.getData(),0, receivePacket.getLength()); 
						String[] temp = sentence.split(" ");
						if(temp[0].equals("msg"))
						{
							String name = activeClientName.get(find(activeClientUID,Integer.parseInt(temp[1].toString()))); 
							msgs.add(name+" : "+sentence.substring((temp[0].length()+1+temp[1].length()+1)));
							runOnUiThread(new Runnable() { 
								public void run() {
									String temp="";
									for(String m : msgs)
										temp+=m+"\n";
									final String disp = temp;
									runOnUiThread(new Runnable() {										
						    			public void run() {
						    				chatBox.setText(disp);
						    			}
						    		});
								}
							});
						}
						else {
							inpData = sentence.substring(9,sentence.length()-1);
							int i = 0 ;
							activeClientName.clear();
							activeClientUID = new int[inpData.toString().split(",").length];
							for (String val: inpData.toString().split(",")) {
								if (val.toCharArray()[0]!=' ')
									tempVar = val.toString().substring(1, val.length()).split(" ");
								else
									tempVar = val.toString().substring(2, val.length()).split(" "); 
								activeClientName.add(tempVar[0]);
								activeClientUID[i] = Integer.parseInt(tempVar[1].replaceAll("'",""));
								i++;
							}
							runOnUiThread(new Runnable() { 
				    			public void run() {
				    				clientList.setAdapter(dataAdapter);
				    				logWindow.setText(tempVar[0]+" "+tempVar[1]);
				    			}
				    		});
						}
					}
					catch(Exception e) {
						final Exception x = e; 
						runOnUiThread(new Runnable() { 
							public void run() {
								logWindow.setText("Exception at receiver: "+x);
							}
						});					
					}
				}
			}
		}).start();
		
		sendButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
				    public void run() {
				    	try {
				    		String selectedClient = clientList.getSelectedItem().toString();
							int i = activeClientName.indexOf(selectedClient);
							int ToUID = activeClientUID[i];
							String message = "msg "+uid+" "+ToUID+" "+chatMessage.getText().toString();
				    		InetAddress ipaddr = InetAddress.getByName(serverIP.toString());
				    		byte[] sendData;
				    		sendData = message.getBytes();
				    		DatagramPacket sendPacket =new DatagramPacket(sendData, sendData.length,ipaddr,50000);
				    		socket.send(sendPacket);
				    	}
				    	catch(Exception e) {
				    		final Exception x = e; 
				    		runOnUiThread(new Runnable() { 
				    			public void run() {
				    				logWindow.setText("Exception at sender: "+x);
				    			}
				    		});
				    	}
				    }
				  }).start();			
				// TODO Auto-generated method stub
				
			}
		});
		
		
		backButton.setOnClickListener(new View.OnClickListener() {
	    	@Override
			public void onClick(View v) {
	    		Intent i = new Intent(Chat.this, MainActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				finish();
				startActivity(i);
	    	}
	    });
		refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
				    public void run() {
				    	try {
				    		InetAddress ipaddr = InetAddress.getByName(serverIP.toString());
				    		byte[] sendData;
				    		String sentence = "list clients";
				    		sendData = sentence.getBytes();
				    		DatagramPacket sendPacket =new DatagramPacket(sendData, sendData.length,ipaddr,50000);
				    		socket.send(sendPacket);
				    	}
				    	catch(Exception e) {
				    		final Exception x = e; 
				    		runOnUiThread(new Runnable() { 
				    			public void run() {
				    				chatBox.setText("Exception at sender: "+x);
				    			}
				    		});
				    	}
				    }
				  }).start();
				// TODO Auto-generated method stub
				
			}
		});
	    // TODO Auto-generated method stub
	}
}
