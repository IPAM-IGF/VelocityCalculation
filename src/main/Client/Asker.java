package main.Client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import display.panels.Case;
import display.panels.GlandPanel;

import tools.server.CustomObjectInputStream;
import tools.server.ServerResponse;


/**
 * Enverra les requetes au serveur
 * 
 * @author jérémy DEVERDUN
 *
 */
public class Asker extends Thread{
	
	// Infos sur le serveur
	private static int port = 4444;
	public static String ip = "10.7.20.89";

	// Remote controller
	public static RemoteController REMOTE_CONTROLLER = null ;

	// Infos à mettre à jours
	private JProgressBar statusArea;
	private boolean DEBUG = false;
	// Attributs
	private Socket kkSocket = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private int BUFFER_SIZE = 65536;
	private Worker worker;
	private boolean displayClient = false;
	private Client clientWindow;
	
	private Object localObject = null;
	
	public Asker(JProgressBar l, Client c, String r, boolean b){
		super();
		setStatusArea(l);
		setClientWindow(c);
		if(l!=null) l.setStringPainted(true);
		displayClient = b;
		localObject = null;
		doAsk(r);
		
	}
	public Asker(JProgressBar l, Client c, String r, boolean b,
			Object obj) {
		super();
		setStatusArea(l);
		setClientWindow(c);
		if(l!=null) l.setStringPainted(true);
		localObject = obj;
		displayClient = b;
		doAsk(r);
	}
	public void run(){
		ServerResponse obj;
		if(statusArea != null) statusArea.setString("Attempting to connect");
		else if(DEBUG) System.out.println("Attempting to connect");
		try {
            kkSocket = new Socket(ip, port);
            if(statusArea != null) statusArea.setString("Connected");
            else if(DEBUG) System.out.println("Connected");
            in = new CustomObjectInputStream((kkSocket.getInputStream()));
			out = new ObjectOutputStream(kkSocket.getOutputStream());
			if(statusArea != null) statusArea.setString("Sending request");
			else if(DEBUG) System.out.println("Sending request");
			out.writeObject(worker.clone());
			if(statusArea != null) statusArea.setString("Retrieving informations");
			else if(DEBUG) System.out.println("Retrieving informations");
			obj = (ServerResponse)in.readObject();
			processObject(obj);
			if(statusArea != null) statusArea.setString("Done");
			else if(DEBUG) System.out.println("Done");
			
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+ip+".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: taranis.");
            System.exit(1);
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
				out.close();
				kkSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(displayClient) clientWindow.createAndShowGUI();
	}

	private void processObject(ServerResponse obj) {
		switch(obj.getRequest()){
		case Worker.GET_GLAND_PANEL:
			clientWindow.setGlandPanel((GlandPanel) obj.getReturnObject());break;
		case Worker.UPDATED_CASES:
			clientWindow.getGlandPanel().updateCasesColor((HashMap<String, Case>) obj.getReturnObject());break;
		case Worker.GIVE_USER_CONTROL:
			Client.isPaused = true;
			if(REMOTE_CONTROLLER == null)
				REMOTE_CONTROLLER = new RemoteController((byte[]) obj.getReturnObject(), clientWindow);
			else
				REMOTE_CONTROLLER.updateScreen((byte[]) obj.getReturnObject());
			break;
		default:
			Client.isPaused = false;
			break;
			
		}
	}
	
	public void doAsk(String request){
		worker = new Worker();
		worker.setRequest(request);
		if (localObject != null) worker.setSendObject(localObject);	
		this.start();
	}
	public JProgressBar getStatusArea() {
		return statusArea;
	}


	public void setStatusArea(JProgressBar statusArea) {
		this.statusArea = statusArea;
	}
	public int getBUFFER_SIZE() {
		return BUFFER_SIZE;
	}
	public void setBUFFER_SIZE(int bUFFER_SIZE) {
		BUFFER_SIZE = bUFFER_SIZE;
	}
	public Worker getWorker() {
		return worker;
	}
	public void setWorker(Worker worker) {
		this.worker = worker;
	}
	public Client getClientWindow() {
		return clientWindow;
	}
	public void setClientWindow(Client clientWindow) {
		this.clientWindow = clientWindow;
	}
}
