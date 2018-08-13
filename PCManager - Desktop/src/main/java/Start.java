import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JOptionPane;

import com.google.gson.Gson;

public class Start
{
	private static File chFile = new File(System.getenv("AppData") + "\\.ZazDat\\PCManager\\connection-hash.txt");

	private static short port = 2865;
	private static ObjectOutputStream output;
	private static ObjectInputStream input;

	private static final byte SHUTDOWN = 0;
	private static final byte STANDBY = 1;
	private static final byte LOCK = 2;
	private static final byte REBOOT = 3;

	private static final byte CONNECTION_ESTABLISHED = -1;
	private static final byte CONNECTION_LOST = -2;
	private static final byte CONNECTION_ONGOING = -3;
	private static final byte COMMAND_EXECUTED_SUCCESSFULLY = -4;
	private static final byte COMMAND_NOT_FOUND = -5;
	private static final byte COMMAND_NOT_SUPPORTED_ON_OS = -6;


	public static void main(String[] args) 
	{
		if (args.length > 0)
		{
			try
			{
				port = Short.parseShort(args[0]);
				if (port < 0)
				{
					throw new NumberFormatException();
				}
			}
			catch (NumberFormatException e)
			{
				System.err.println("The passed argument is not a valid port.");
			}

		}
		try
		{
			ServerSocket server = new ServerSocket(port, 1);
			if (!chFile.exists())
			{
				firstTimeSetup();
			}

			String savedIP = getSavedIP();
			if (!savedIP.equals(InetAddress.getLocalHost().getHostAddress()))
			{
				manageIPChange();
			}

			runServer(server);
		}
		catch (IOException e)
		{
			System.err.println("ERROR: Default port is already taken. You can pass a new port as a command line argument.");
		}
	}

	private static void runServer(ServerSocket server)
	{
		while (true)
		{
			try
			{
				Socket connection = server.accept();
				output = new ObjectOutputStream(connection.getOutputStream());
				output.flush();
				input = new ObjectInputStream(connection.getInputStream());
				try
				{
					output.writeByte(CONNECTION_ESTABLISHED);
					output.flush();
					handleConnection(connection);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			catch (IOException e)
			{
				System.out.println("Connection failed.");
				e.printStackTrace();
			}
		}
	}
	private static void handleConnection(Socket connection)
	{
		while (!connection.isClosed())
		{
			try
			{
				String receivedInput = (String) input.readObject();
				manageCommand(decrypt(receivedInput));
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
	private static void manageCommand(String request) //Possibly make system specific variations. Windows by default.
	{
		try
		{
			Gson gson = new Gson();
			ResponseData responseData = gson.fromJson(request, ResponseData.class); //Set object

			switch (Integer.parseInt(responseData.cid))
			{
				case SHUTDOWN: //Shutdown
					Runtime.getRuntime().exec("shutdown -t 0");
					output.writeByte(COMMAND_EXECUTED_SUCCESSFULLY);
					output.flush();
					break;
				case STANDBY: //Standby
					Runtime.getRuntime().exec("rundll32.exe powrprof.dll,SetSuspendState 0,1,0");
					output.writeByte(COMMAND_EXECUTED_SUCCESSFULLY);
					output.flush();
					break;
				case LOCK: //Lock
					Runtime.getRuntime().exec("rundll32.exe user32.dll, LockWorkStation");
					output.writeByte(COMMAND_EXECUTED_SUCCESSFULLY);
					output.flush();
					break;
				case REBOOT: //Reboot
					Runtime.getRuntime().exec("shutdown -r -t 0");
					output.writeByte(COMMAND_EXECUTED_SUCCESSFULLY);
					output.flush();
					break;
				default:
					output.writeByte(COMMAND_NOT_FOUND);
					output.flush();
					break;

			}
		}
		catch (IOException e)
		{
			System.out.println("Could not locate Runtime");
			e.printStackTrace();
		}
	}

	private static String getSavedIP() throws FileNotFoundException
	{
		Scanner fileScanner = new Scanner(chFile);
		String storedIP = fileScanner.next();
		return storedIP;
	}
	private static String updateSavedIP() throws IOException
	{
		String IP = InetAddress.getLocalHost().getHostAddress()+":"+port;
		PrintWriter printWriter = new PrintWriter(chFile);
		printWriter.print(IP);
		printWriter.close();
		return IP;
	}

	private static void firstTimeSetup()
	{
		try
		{
			chFile.getParentFile().mkdirs();
			chFile.createNewFile();
			String IP = updateSavedIP();
			JOptionPane.showMessageDialog(null, "Welcome to PCManager! To set up, please enter this code in the mobile app and click \"Ok\":\n"+IP);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	private static void manageIPChange() throws IOException
	{
		String IP = updateSavedIP();
		JOptionPane.showMessageDialog(null, "Your local IP address has changed. To avoid this, please set-up a static IP address.\nTo reconnect, please select \"New Code\" in the app and enter:\n"+IP);
	}


	private static String encrypt(String source)
	{
		return source; //TODO: Implement again, but more secure
	}
	private static String decrypt(String source)
	{
		return source; //TODO: Implement again, but more secure
	}
	

	private class ResponseData //JSON class
	{
		String cid; //Command ID.

		public ResponseData(String cid)
		{
			this.cid = cid;
		}
	}

}
