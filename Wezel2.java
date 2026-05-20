package skGniazda;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Wezel2 {

    public static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws Exception{

        //Otwarcie gniazda z okreslonym portem

        ExecutorService threads = Executors.newFixedThreadPool(10);

        DatagramSocket datagramSocket = new DatagramSocket(9000);

    
        //Serwer w petli przyjmuje pakiety przychodzace.
        while (true){

            DatagramPacket receivedPacket = new DatagramPacket( new byte[BUFFER_SIZE], BUFFER_SIZE);

            datagramSocket.setSoTimeout(0); //główne gniazdo czeka w nieskończonośc
            datagramSocket.receive(receivedPacket); //otrzymany pakiet
            
            // port i host ktory wyslal nam zapytanie
            InetAddress addressFrom = receivedPacket.getAddress();
            int portFrom = receivedPacket.getPort();

            byte[] byteMessage = Arrays.copyOf(receivedPacket.getData(), receivedPacket.getLength());
            //kopiuje odebrane bajty, żeby kolejny klient ich nie nadpisał zanim wątek nie obsłuży

            System.out.println("Odebrano od: " + addressFrom);


            threads.submit( () -> {

                try { 
                    //zmiana kroku
                    String pctContent = new String(byteMessage, 0, byteMessage.length, "utf8");
                    String[] pckgContentSpl = pctContent.split("\\$");
                    String ip_serwer = pckgContentSpl[3];
                    int step = Integer.parseInt(pckgContentSpl[2]);
                    int liczbaW = Integer.parseInt(pckgContentSpl[1]);
                    String rawIP = pckgContentSpl[0];
                    String[] rawIPSplit = rawIP.replace("[", "").replace("]", "").replace(" ", "").split(",");

                    String nextIP;
                    if (step + 1 < liczbaW) {
                        nextIP = rawIPSplit[step+1];
                    } else {
                        // Jeśli nie ma więcej węzłów, kierujemy do serwera
                        nextIP = ip_serwer;
                    }

                    pckgContentSpl[2] = Integer.toString(step+1);
                    String pckgToForward = String.join("$", pckgContentSpl);
                    byte[] byteMessageToForward = pckgToForward.getBytes("utf8");

    	
                    //wysłanie wiadomości dalej
                    InetAddress serverAddress = InetAddress.getByName(nextIP); //TU BĘDZIE DO ZMIANY

                    try (DatagramSocket tempSocket = new DatagramSocket()){
                        DatagramPacket sentPacket = new DatagramPacket(byteMessageToForward, byteMessageToForward.length, serverAddress, 9000);
                        tempSocket.send(sentPacket);
                        
                        //odbieramy potwierdzenie otrzymania wiadomosci z kolejnego wezła
                        DatagramPacket confirmation = new DatagramPacket(new byte[BUFFER_SIZE],BUFFER_SIZE);
                        tempSocket.setSoTimeout(10000); //1,5 sekundy czekamy na odp

                        try{
                            tempSocket.receive(confirmation);
                            System.out.println("Serwer otrzymał wiadomść");

                            //wyświetlamy odpowiedź
                            String odpowiedz = new String(confirmation.getData(), 0, confirmation.getLength(), "utf8");
                            String wiadomosc = odpowiedz.split("\\$")[4];
                            System.out.println("Odpowiedź serwera: "+ wiadomosc);

                            //przesyłam odpowedz do wcześniejszego wezla
                            DatagramPacket response = new DatagramPacket(confirmation.getData(), confirmation.getLength(), addressFrom, portFrom);
                            datagramSocket.send(response);
                    } catch (SocketTimeoutException ste){
                            System.out.println("Serwer nie odpowiedział, wiec albo dostał wiadomość albo nie...");
                    }  
                }
            } catch (Exception e) {
                e.printStackTrace();
            }  
        
        });
            
    }
}
}

// czyli trzeba zwiększyć liczbe obsługiwanych watków, zmienić timeout, 