package com.example.pingv2;
public class Messages {
	//private variables
    int _id;
    String _message;
    public Messages(){
         
    }
    public Messages(String _message){
        this._message = _message;
    }
    public int getID(){
        return this._id;
    }
    public void setID(int id){
        this._id = id;
    }
    public String getMessage(){
        return this._message;
    }
    public void setMessage(String message){
        this._message = message;
    }
}