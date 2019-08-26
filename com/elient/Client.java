package com.elient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Client extends Thread{

	private Socket c_socket ;
	private Client_chatFrame c_chatFrame;
	private Client_enterFrame c_enterFrame;
	private Client_singleFrame c_singleFrame;
	private DataInputStream dis = null;
	public DataOutputStream dos = null;
	private boolean flag_exit = false;
	
	private int threadID;
	
	public final Map<String, Client_singleFrame> c_singleFrames;
	public final List<String> username_online;
	public final List<Integer> censused;
	public String username = null;
	private String chat_re;
	//getter, setter????
	public Client_chatFrame getC_chatFrame() {
		return c_chatFrame;
	}
	public Client_singleFrame getC_singlFrame() {
		return c_singleFrame;
	}
	public void setC_singlFrame(Client_singleFrame c_singlFrame) {
		this.c_singleFrame = c_singlFrame;
	}
	public void setC_chatFrame(Client_chatFrame c_chatFrame) {
		this.c_chatFrame = c_chatFrame;
	}
	public Client_enterFrame getC_enterFrame() {
		return c_enterFrame;
	}
	private void setC_enterFrame(Client_enterFrame c_enterFrame) {
		this.c_enterFrame = c_enterFrame;
	}
	
	public int getThreadID() {
		return threadID;
	}
	private void setThreadID(int threadID) {
		this.threadID = threadID;
	}
	private Client(){
		c_singleFrames = new HashMap<>();
		username_online = new ArrayList<>();
		censused = new ArrayList<>();
//		signature = new ArrayList<String>();
	}
	
	public static void main(String[] args) {
		Client client = new Client();
		Client_enterFrame c_enterFrame = new Client_enterFrame(client);
		client.setC_enterFrame(c_enterFrame);
		c_enterFrame.setVisible(true);
	}
	
	public String login(String username, String hostIp, String hostPort) {
		this.username = username;
		String login_mess = null;
		try {
			c_socket = new Socket(hostIp, Integer.parseInt(hostPort));
		} catch (NumberFormatException e) {
			login_mess = "?????????port???,??????1024<port<65535";
			return login_mess;
		} catch (UnknownHostException e) {
			login_mess = "??????";
			return login_mess;
		} catch (IOException e) {
			login_mess = "?????????????";
			return login_mess;
		}
		return "true";
	}
	public void showChatFrame(String username) {
		getDataInit();
		c_chatFrame = new Client_chatFrame(this,username);
		c_chatFrame.setVisible(true);
		flag_exit = true;
		this.start();
		
	}
	private void getDataInit() {
		try {
			dis = new DataInputStream(c_socket.getInputStream());
			dos = new DataOutputStream(c_socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void run() {
		while(flag_exit){
			try {
				chat_re = dis.readUTF();
			} catch (IOException e) {
				flag_exit = false;
				if(!chat_re.contains("@serverexit")){
					chat_re = null;
				}
			}
			if(chat_re != null){
				if(chat_re.contains("@clientThread")){
					int local = chat_re.indexOf("@clientThread");
					setThreadID(Integer.parseInt(chat_re.substring(0, local)));
					try {
						dos.writeUTF(username + "@login" + getThreadID() + "@login");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					if(chat_re.contains("@userlist")){
						c_chatFrame.setDisUsers(chat_re);
					}else{
						if(chat_re.contains("@chat")){
							c_chatFrame.setDisMess(chat_re);
						}else{
							if(chat_re.contains("@serverexit")){
								c_chatFrame.closeClient();
							}else{
								if(chat_re.contains("@single")){
									try {
										c_chatFrame.setSingleFrame(chat_re);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
		}
	}
	public void transMess(String mess) {
		try {
			dos.writeUTF(username  + "@chat" + getThreadID() + "@chat"+ mess + "@chat");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void exitChat() {
		try {
			dos.writeUTF(username + "@exit" + getThreadID() + "@exit");
			flag_exit = false;
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void exitLogin() {
		System.exit(0);
	}
	public void exitClient() {
		flag_exit = false;
		System.exit(0);
	}
}