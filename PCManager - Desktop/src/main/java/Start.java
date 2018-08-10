import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JOptionPane;

import com.google.gson.Gson;

public class Start {
	private static String clientID = "";
	private static ObjectOutputStream output;
	private static ObjectInputStream input;
	
	public static void main(String[] args) 
	{
		clientID = "0";
		try
		{
			ServerSocket server = new ServerSocket(2865, 1);
		    String dir = System.getenv("AppData");
		    dir += "\\.ZazDat\\PCManager";
			File dirFile = new File(dir);
			File idFile = new File(dir+"/id.txt");
			if (!idFile.exists())
			{
				try
				{
					dirFile.mkdir();
					idFile.createNewFile();
					InetAddress inetAddress = InetAddress.getLocalHost();
					String encryptedData = getCodeFromIP(inetAddress.getHostAddress());
					FileWriter fileWriter = new FileWriter(idFile);
					PrintWriter printWriter = new PrintWriter(fileWriter);
					printWriter.print(encryptedData);
					printWriter.close();
					fileWriter.close();
					JOptionPane.showMessageDialog(null, "Welcome to PCManager! To set up, please enter this code in the mobile app and click \"Ok\":\n"+encryptedData);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					return;
				}
			}
			Scanner fileScanner = new Scanner(idFile);
			clientID = fileScanner.next();
			String decryptedIP = getIPFromCode(clientID);
			if (!decryptedIP.equals(InetAddress.getLocalHost().getHostAddress()))
			{
				String encryptedData = getCodeFromIP(InetAddress.getLocalHost().getHostAddress());
				FileWriter fileWriter = new FileWriter(idFile);
				PrintWriter printWriter = new PrintWriter(fileWriter);
				printWriter.print(encryptedData);
				printWriter.close();
				fileWriter.close();
				JOptionPane.showMessageDialog(null, "Your local IP address has changed. To avoid this, please set-up a static IP address.\nTo reconnect, please select \"New Code\" in the app and enter:\n"+encryptedData);
			}
			while (true)
			{
				try
				{	
					System.out.println("Ready.");
					Socket connection = server.accept();
					output = new ObjectOutputStream(connection.getOutputStream());
					output.flush();	
					input = new ObjectInputStream(connection.getInputStream());
					try
					{
						output.writeObject("1337");
						output.flush();
						handleConnection(connection);
					}
					catch (IOException e)
					{
						//Fail silently
					}

					//Check if id file exists
					//If not, launch setup();
					//Else, wait for input
				}
				catch (IOException e)
				{
					System.out.println("Connection failed.");
					e.printStackTrace();
				}
			}
		}
		catch (IOException e)
		{
			System.out.println("ERROR: Port is already taken.");
		}
	}
	private static void handleConnection(Socket connection)
	{
		while (!connection.isClosed())
		{
			try 
			{
				String receivedInput = (String) input.readObject();
				CommandInterpreter(receivedInput);
			}
			catch (IOException | ClassNotFoundException e)
			{
				try 
				{
					connection.close();
				} 
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}
	private static String encrypt(String source)
	{
		return source; //TODO: Implement again, but more secure
	}
	private static String decrypt(String source)
	{
		return source; //TODO: Implement again, but more secure
	}
	private static String getCodeFromIP(String source)
	{
		return source; //TODO: Implement again (with better system)
		//The role of this will be to turn an IP address into a more user friendly string, which can then be entered onto the mobile device.
	}
	private static String getIPFromCode(String source)
	{
		return source;
	}

	private static void CommandInterpreter(String sentData)
	{
		Gson gson = new Gson();
		ResponseData responseData = gson.fromJson(decrypt(sentData), ResponseData.class); //Set object
		try
		{
			output.writeObject("-1");
			output.flush();
			manageCommand(responseData.cid); //If it is, execute the command
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void manageCommand(String commandCode) //Possibly make system specific variations. Windows by default.
	{
		try
		{
			switch (commandCode)
			{
				case "0": //Shutdown
					Runtime.getRuntime().exec("shutdown -t 0");
					break;
				case "1": //Standby
					Runtime.getRuntime().exec("rundll32.exe powrprof.dll,SetSuspendState 0,1,0");
					break;
				case "2": //Lock
					Runtime.getRuntime().exec("rundll32.exe user32.dll, LockWorkStation");
					break;
				case "3": //Reboot
					Runtime.getRuntime().exec("shutdown -r -t 0");
					break;
				case "#":
					//output.writeObject("#");
					//output.flush();
					break;
			}
		}
		catch (IOException e)
		{
			System.out.println("Could not locate Runtime");
			e.printStackTrace();
		}
	}
	private class ResponseData //JSON class
	{
		String id;
		String cid;
	}

}
