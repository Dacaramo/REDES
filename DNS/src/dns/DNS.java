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
            
            //Pasarle el path completo al constructor del file
            File masterFile = new File("/Users/cristobalcastrilonbalcazar/REDES/DNS/src/files/masterFile.txt");
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
                
                //byte[] b = CrearRespuesta(peticion.getData());
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
        byte[] trID = {0,0};
        System.out.println("TransactionID");
        int index = 0;
        //TransactionID
        while(index < 2){
            trID[index] = data[index];
            System.out.println(trID[index]);
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
        return trID;
    }
    
    private static ArrayList<byte[]> extraerData(byte[] data) {
        //ArrayList where query data is stored
        ArrayList<byte[]> queryData = new ArrayList<byte[]>();
        ArrayList<String> queryDataStr = new ArrayList<String>();
        
        //TransactionID
        System.out.println("Transaction ID:");
        
        byte[] trID = {0,0};
        int index = 0;
        String trIDStr = "";
        while(index < 2){
            trID[index] = data[index];
            trIDStr += toBits(data[index]);
            index++;
        }
        
        queryData.add(trID);
        queryDataStr.add(trIDStr);
        
        System.out.println(trIDStr);
        
        //QR, OpCode & Banderas (sin RA)
        byte[] thirdByte = {0};
        thirdByte[0] = data[index++];
        queryData.add(thirdByte);
        
        //RA
        //Z (Reserved for Future Use) = 4 bits en 0
        //RCODE 0 = No Error
        byte[] razRcode = {0};
        razRcode[0] = data[index++];
        queryData.add(razRcode);
        
        //QDCOUNT
        String qdCountStr = "";
        byte[] qdCount = {0,0};
        for(int i = 0; i < 2; i++){
            qdCount[i] = data[index];
            qdCountStr += toBits(data[index]);
            index++;
        }
        queryData.add(qdCount);
        queryDataStr.add(qdCountStr);
        
        //ANCOUNT
        byte[] anCount = {0,0};
        for(int i = 0; i < 2; i++){
            anCount[i] = data[index];
            index++;
        }
        queryData.add(anCount);
        
        //NSCOUNT
        byte[] nsCount = {0,0};
        for(int i = 0; i < 2; i++){
            nsCount[i] = data[index];
            index++;
        }
        queryData.add(nsCount);
        
        //ARCOUNT
        byte[] arCount = {0,0};
        for(int i = 0; i < 2; i++){
            arCount[i] = data[index];
            index++;
        }
        queryData.add(arCount);
        
        //Question section
        //qdCountInt: qdCount en entero
        int qdCountInt = Integer.parseInt(qdCountStr, 2);
        for(int i = 0; i < qdCountInt; i++){
            //QNAME
            
            //Label: cada una de los strings (sin los puntos) que contiene un URL
            //Ejemplo: www.google.com (www, google, com)
            //int j = 0;
            String q_name_lengthStr = ""; 
            int labelLength = 0;
            
            //String donde se va a almacenar el nombre de dominio
            String name = "";
            
            while(data[index] != 0){
                
                //La siguiente línea es de prueba...
                System.out.println(index);
                
                q_name_lengthStr = toBits(data[index++]);
                labelLength = Integer.parseInt(q_name_lengthStr, 2);
                
                byte[] byteName = new byte[labelLength];
                for(int k = 0; k < labelLength; k++){
                    byteName[k] = data[index];
                    index++;
                }
                
                //La siguiente línea es de prueba...
                System.out.println(new String(byteName));
                
                //index++;
                //Label copiado al nombre
                name += new String(byteName) + '.';
            }
            name = name.substring(0, name.length()-1);
            //La siguiente línea es de prueba...
            System.out.println(name);
            
            //QTYPE
            //QCLASS
        }
        
        return queryData;
    }
     
    private static String toBits(final byte val) {
        final StringBuilder result = new StringBuilder();

        for (int i=0; i<8; i++) {
                result.append((int)(val >> (8-(i+1)) & 0x0001));
        }

        return result.toString();
    }
    
    private static ArrayList<String> findInMasterFile(String name, ArrayList<ArrayList<String>> registros){
        for(ArrayList<String> row : registros){
            if(row.get(0).compareTo(name) == 0){
                return row;
            }
        }
        return null;
    }
}