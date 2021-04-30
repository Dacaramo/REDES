package dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 *
 * @author USUARIO
 */
public class DNS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        try {
            DatagramSocket socketUDP = new DatagramSocket(53);
            byte[] bufer = new byte[1000];

            while (true) {
                // Construimos el DatagramPacket para recibir peticiones
                DatagramPacket peticion = new DatagramPacket(bufer, bufer.length);

                // Leemos una petición del DatagramSocket
                socketUDP.receive(peticion);

                System.out.print("Datagrama recibido del host: "
                        + peticion.getAddress());
                System.out.println(" desde el puerto remoto: "
                        + peticion.getPort());

                // Construimos el DatagramPacket para enviar la respuesta
                
                DatagramPacket respuesta = new DatagramPacket(peticion.getData(), peticion.getLength(), peticion.getAddress(), peticion.getPort());
                
                byte[] b = CrearRespuesta(peticion.getData());
                // Enviamos la respuesta, que es un eco
                socketUDP.send(respuesta);
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    private static byte[] CrearRespuesta(byte[] data) {
        //TransactionID
        byte[] TrID = {0,0};
        for (int i = 0; i<2; i++){
            TrID[i] = data[i];
        }
        
        System.out.println("TransactionID");
        for (int i = 0; i<2; i++){
            System.out.println(TrID[i]);
        }
        
        //banderas
        
        return TrID;
    }

}

//nslookup www.google.com 127.0.0.1
