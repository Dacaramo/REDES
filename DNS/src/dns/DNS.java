package dns;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

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
            //Listado de registros tipo A
            ArrayList<ArrayList<String>> registros = new ArrayList<ArrayList<String>>();
            
            File masterFile = new File("masterFile.txt");
            FileReader masterReader = new FileReader(masterFile);
            BufferedReader reader = new BufferedReader(masterReader);
            
            String fileLine;
            
            while((fileLine = reader.readLine()) != null){
                String[] col = fileLine.split(",");
                ArrayList<String> filaList = new ArrayList<String>();
                filaList.add(col[0]);
                filaList.add(col[1]);
                filaList.add(col[2]);
                filaList.add(col[3]);
                
                registros.add(filaList);
            }
            
            reader.close();
            
            //Socket al puerto DNS (#53)
            DatagramSocket socketUDP = new DatagramSocket(53);
            
            //Messages carried by UDP are restricted to 512 bytes (not counting the IP or UDP headers)
            byte[] buffer = new byte[512];
            
            while (true) {
                // Construimos el DatagramPacket para recibir peticiones
                //NOTA: El Datagram es un Datagrama UDP (tamaño de UDP Datagram)
                DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);

                // Leemos una petición del DatagramSocket
                socketUDP.receive(peticion);

                System.out.print("Datagrama recibido del host: "
                        + peticion.getAddress());
                System.out.println(" desde el puerto remoto: "
                        + peticion.getPort());

                // Construimos el DatagramPacket para enviar la respuesta
                
                extraerData(peticion.getData());
                
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
        byte[] TrID = {0,0};
        System.out.println("TransactionID");
        int index = 0;
        //TransactionID
        while(index < 2){
            TrID[index] = data[index];
            System.out.println(TrID[index]);
            index++;
        }
        
        //QR, OpCode & Banderas (sin RA)
        
        //RA
        //Z (Reserved for Future Use) = 4 bits en 0
        //RCODE 0 = No Error
        // 1, 2, 3, 4, 5...
        byte razRcode;
        
        
        //QDCOUNT
        //ANCOUNT
        //NSCOUNT
        //ARCOUNT
        return TrID;
    }
    
    private static void extraerData(byte[] data) {
        byte[] TrID = {0,0};
        System.out.println("TransactionID");
        int index = 0;
        //TransactionID
        while(index < 2){
            TrID[index] = data[index];
            System.out.println(TrID[index]);
            index++;
        }
        
        //QR, OpCode & Banderas (sin RA)
        byte thirdByte = data[index++];
        //RA
        //Z (Reserved for Future Use) = 4 bits en 0
        //RCODE 0 = No Error
        byte razRcode = data[index++];
        
        System.out.println(razRcode);
        
        //QDCOUNT
        //TODO: Convertir a entero
        byte[] qdCount = {0,0};
        for(int i = 0; i < 2; i++){
            qdCount[i] = data[index];
            index++;
        }
        
        //ANCOUNT
        byte[] anCount = {0,0};
        for(int i = 0; i < 2; i++){
            anCount[i] = data[index];
            index++;
        }
        
        //NSCOUNT
        byte[] nsCount = {0,0};
        for(int i = 0; i < 2; i++){
            nsCount[i] = data[index];
            index++;
        }
        
        //ARCOUNT
        byte[] arCount = {0,0};
        for(int i = 0; i < 2; i++){
            arCount[i] = data[index];
            index++;
        }
        
        //Question section
        //qdCountInt: qdCount en entero
        //OJO: Solo se está tomando el segundo Byte (i.e. QDCOUNT máximo = 2^8)
        int qdCountInt = qdCount[1];
        for(int i = 0; i < qdCountInt; i++){
            //QNAME
            //QTYPE
            //QCLASS
        }
    }

}

//nslookup www.google.com 127.0.0.1
