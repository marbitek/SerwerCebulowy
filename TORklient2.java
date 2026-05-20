package skGniazda;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class TORklient2 {


    public static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {

        Properties config = new Properties();
        
        // Czytamy plik z dysku, z tego samego folderu co plik .jar
        FileInputStream file = new FileInputStream("Config.properties");
        config.load(file);
            
            
        String IP_SERWER = config.getProperty("IP_SERWER");
        String IP_KLIENT = config.getProperty("IP_KLIENT");
        String IP_KLIENT2 = config.getProperty("IP_KLIENT2");
    	String IP_W1 = config.getProperty("IP_W1");
        String IP_W2 = config.getProperty("IP_W2");
        String IP_W3 = config.getProperty("IP_W3");
        //String KLIENT_PORT = config.getProperty("KLIENT_PORT");
        String KLIENT2_PORT = config.getProperty("KLIENT2_PORT");
        

    	//otwarcie gniazda
    	int port = Integer.parseInt(KLIENT2_PORT);
        DatagramSocket socket = new DatagramSocket(port); 
        
    	//inicjalizacja 
        String[] ip = {IP_W1, IP_W2, IP_W3};
        String ip_serwer = IP_SERWER;
    	String[] trasaIP = trasa(ip);
        //kaString[] trasaIP = ip;
        int liczbaW = liczbaWezlow(); //liczba węzłów, 2 lub 3
        //int liczbaW = 2;
    	int krok = 0; //0 bo jestesmy w kliencie
    	String message, paczka;
    	
    	//info dla uzytkownika
        System.out.println("Napisz STOP aby zakończyć");
        InetAddress serverAddress = InetAddress.getByName(trasaIP[0]);
        System.out.println("Wiadomość idzie do: " + serverAddress);
        System.out.println("Podaj wiadomość:");

      //wczytanie wiadomości z klawiatury
        Scanner sc = new Scanner(System.in);
        message = sc.nextLine();

        while(!message.equals("STOP")) {
        	
        	/*do serwera idzie wiadomosc:
        	 * [lista ip] $ [liczba wezlow] $ [krok] $ [ip serwera] $ [wiadomosc]
        	*/
        	paczka = Arrays.toString(trasaIP) + "$" + Integer.toString(liczbaW) +
        			"$" + Integer.toString(krok) + "$" + ip_serwer + "$" + message;

        	//zamieniamy na bity (czy cos)
        	byte[] stringContents = paczka.getBytes("utf8");

        	//wysłanie wiadomości
        	DatagramPacket sentPacket = new DatagramPacket(stringContents, stringContents.length);
            sentPacket.setAddress(serverAddress);
            sentPacket.setPort(Config.PORT);
            socket.send(sentPacket);
            
            //odbieramy potwierdzenie otrzymania wiadomosci z serwera
            DatagramPacket recievePacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
            socket.setSoTimeout(10000); //10 sekund czekamy na odp
            try{
                socket.receive(recievePacket);
                System.out.println("Serwer otrzymał wiadomość");
                //wyświetlamy odpowiedź
                String odpowiedz = new String(recievePacket.getData(), 0, recievePacket.getLength(), "utf8");
                String wiadomosc = odpowiedz.split("\\$")[4];
                System.out.println("Odpowiedź serwera: "+ wiadomosc);
            }catch (SocketTimeoutException ste){
                System.out.println("Serwer nie odpowiedzial, wiec albo dostal wiadomosc albo nie...");
            }
            
            System.out.println("Podaj następną wiadomość");
            message = sc.nextLine();
            
        }
        
        //gdy napiszemy STOP to się rozłączamy
        sc.close();
        socket.close();
        System.out.println("Do widzenia!");      
    }
    
    public static int liczbaWezlow() {
    	//losowa liczba 2-3
    	Random rand = new Random();
    	int liczbaW = rand.nextInt(2, 4);
    	return liczbaW;
    }
    
    public static String[] trasa(String[] ip) {
    	//losowa kolejnosc tablicy
    	Random rnd = new Random();
        for (int i = ip.length - 1; i > 0; i--)
        {
          int index = rnd.nextInt(i + 1);
          String b = ip[index];
          ip[index] = ip[i];
          ip[i] = b;
        }
    	return ip;
    }
}
