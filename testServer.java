package skGniazda;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class testServer {

    public static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws Exception{


        ExecutorService threads = Executors.newFixedThreadPool(10);
        //Otwarcie gniazda z okreslonym portem
        DatagramSocket datagramSocket = new DatagramSocket(9000);

        //Serwer w petli przyjmuje pakiety przychodzace.
          while (true){

            DatagramPacket receivedPacket
                    = new DatagramPacket( new byte[BUFFER_SIZE], BUFFER_SIZE);

            datagramSocket.receive(receivedPacket);


            byte[] byteMessage = Arrays.copyOf(receivedPacket.getData(), receivedPacket.getLength());

    
            // port i host ktory wyslal nam zapytanie
            InetAddress address = receivedPacket.getAddress();
            int port = receivedPacket.getPort();
        

            threads.submit( () -> {
                try {
                    // Zamiana skopiowanych bajtów na String
                    String message = new String(byteMessage, 0, byteMessage.length, "utf8");

                    System.out.println("adres: " + address + " port: " + port);
                    System.out.println(message);

                    // odpowiedz (Twój oryginalny kod)
                    String[] czesci = message.split("\\$");
                    String odpowiedz = czesci[0] + "$" + czesci[1] + "$" + czesci[2] + "$" +  czesci[3] + "$" + "twoja wiadomość: " + czesci[4];
                    byte[] byteResponse = odpowiedz.getBytes("utf8");

                    DatagramPacket response = new DatagramPacket(byteResponse, byteResponse.length, address, port);

                    // Serwer wysyła odpowiedź korzystając z tego samego, głównego gniazda
                    datagramSocket.send(response);


                } catch (Exception e) {
                    System.err.println("Błąd przetwarzania: " + e.getMessage());
                }
            });
            
            
        }
    }
}

