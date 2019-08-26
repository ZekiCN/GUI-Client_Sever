package com.server;

import java.io.IOException;



public class BroadCast extends Thread {

	private ClientThread clientThread;
	private final ServerThread serverThread;
	private String str;
	private boolean flag_exit = false;
	public BroadCast(ServerThread serverThread){
		this.serverThread = serverThread;
	}
	
	@Override
	public void run() {
		boolean flag = true;
		while(flag_exit){
			synchronized (serverThread.messages) {
				if(serverThread.messages.isEmpty()){
					continue;
				}else{
					str = (String)serverThread.messages.firstElement();
					serverThread.messages.removeElement(str);
					if(str.contains("@clientThread")){
						flag = false;
					}
				}
			}
			synchronized (serverThread.clients) {
				for(int i=0; i < serverThread.clients.size(); i++)
                {
                    clientThread = serverThread.clients.elementAt(i);
                    if(flag){
						//向纪录的每一个客户端发送数据信息
						if(str.contains("@exit")){
							serverThread.clients.remove(i);
							clientThread.closeCentered(clientThread);
							try {
								clientThread.dos.writeUTF(str);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if(str.contains("@chat") || str.contains("@userlist") || str.contains("@severest")){
							try {
								clientThread.dos.writeUTF(str);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if(str.contains("@single")){
							String[] info = str.split("@single");
							int id_thread = Integer.parseInt(info[2]);
							for(int j = 0; j < serverThread.clients.size(); j++){
								if(id_thread == serverThread.clients.get(j).getId()){
									try {
										serverThread.clients.get(j).dos.writeUTF(str);
									} catch (IOException e) {
										e.printStackTrace();
									}
									i = serverThread.clients.size();
									break;
								}
							}
						}
					}else{
                    	String value = serverThread.users.get((int)clientThread.getId());
                    	if(value.equals("@login@")){
                    		flag = true;
							//向纪录的每一个客户端发送数据信息
							try {
								clientThread.dos.writeUTF(str);
							} catch (IOException e) {
								e.printStackTrace();
							}
							if(str.contains("@exit")){
								serverThread.clients.remove(i);
								clientThread.closeCentered(clientThread);
							}
							break;
                    	}
                    }
                }
			}
			if(str.contains("@severest")){
				serverThread.users.clear();
				flag_exit = false;
			}
		}
	}

	public void setFlag_exit(boolean b) {
		flag_exit = b;
	}

	public void stopBroadCase() {
		flag_exit  = false;
	}
}
