package dns;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DNS {
        
    public static void main(String[] args) throws IOException {

        try {
            //Listado de registros tipo A
            ArrayList<ArrayList<String>> registros = new ArrayList<>();
            
            File masterFile = new File("/Users/cristobalcastrilonbalcazar/REDES/DNS/src/files/masterFile.txt"); //Pasarle el path completo al constructor del file
            FileReader masterReader = new FileReader(masterFile);
            BufferedReader reader = new BufferedReader(masterReader);
            
            String fileLine;
            
            while((fileLine = reader.readLine()) != null){
                String[] col = fileLine.split(",");
                ArrayList<String> filaList = new ArrayList<>();
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

                System.out.print("Datagrama recibido del host: "+ peticion.getAddress());
                System.out.println("Desde el puerto remoto: "+ peticion.getPort());

                //queryResponseDataMap: HashMap de Campos compartidos entre Query y Response.
                HashMap<String, String> queryResponseDataMap = new HashMap<>();
                ArrayList<byte[]> queryData = extraerData(peticion.getData(), queryResponseDataMap);
                
                // Construimos el DatagramPacket para enviar la respuesta
                DatagramPacket respuesta = new DatagramPacket(peticion.getData(), peticion.getLength(), peticion.getAddress(), peticion.getPort());
                
                ArrayList<Byte> respuestaBytes = crearRespuesta(queryResponseDataMap, registros, queryData);
                // Enviamos la respuesta, que es un eco
                socketUDP.send(respuesta);
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    private static ArrayList<Byte> crearRespuesta(HashMap<String, String> queryResponseDataMap, ArrayList<ArrayList<String>> registros, ArrayList<byte[]> camposByte) {
        
        ArrayList<Byte> response = new ArrayList<>();
        String responseStr = "";
        
        String trID = queryResponseDataMap.get("ID");
        String QR = "1";
        String opCode = queryResponseDataMap.get("OPCODE");
        String qdCount = queryResponseDataMap.get("QDCOUNT");
        String qName = queryResponseDataMap.get("QNAME");
        
        ArrayList<ArrayList<String>> foundName = findInMasterFile(qName, registros);
        Integer anCount = foundName.size();
        String anCountStr = String.format("%16s", Integer.toBinaryString(anCount)).replace(' ', '0');
        
        String AA = "1";
        String TC = "0";
        String RD = "1";
        String RA = "0";
        String Z = "000";
        String RCODE = "0000";
        String NSCOUNT = "0000000000000000";
        
        responseStr += trID;
        System.out.println(responseStr);
        System.out.println(responseStr.length());
        
        responseStr += QR + opCode + AA + TC + RD;
        System.out.println(responseStr);
        System.out.println("OPCODE: " + opCode + opCode.length());
        System.out.println(responseStr.length());
        
        responseStr += RA + Z + RCODE;
        System.out.println(responseStr);
        System.out.println(responseStr.length());
        
        responseStr += qdCount;
        System.out.println(responseStr);
        System.out.println(responseStr.length());
        
        responseStr += anCountStr;
        System.out.println(responseStr);
        System.out.println(responseStr.length());
        
        //NSCOUNT + ARCOUNT (Idéntico a NSCOUNT)
        responseStr += NSCOUNT + NSCOUNT;
        
        for(int i = 0; i < responseStr.length(); i += 8){
            Integer octet = Integer.parseInt(responseStr.substring(i, i + 8), 2);
            byte octetByte = octet.byteValue();
            response.add(octetByte);
        }
        
        //Añadiendo QNAME, QTYPE y QCLASS del camposByte
        response.add(Byte.parseByte(camposByte.get(7).toString()));
        response.add(camposByte.get(8));
        response.add(camposByte.get(9));
    }
    
    private static ArrayList<byte[]> extraerData(byte[] data, HashMap<String, String> queryResponseDataMap) {
        //ArrayList where query data is stored
        ArrayList<byte[]> queryData = new ArrayList<>();
        
        byte[] trID = {0,0};
        int index = 0;
        String trIDStr = "";
        while(index < 2){
            trID[index] = data[index];
            trIDStr += toBits(data[index]);
            index++;
        }
        
        //QR, OpCode & Banderas (sin RA)
        byte[] thirdByte = {0};
        thirdByte[0] = data[index++];

        //Extrayendo OPCode (campo compartido entre Query y Respuesta)
        String thirdByteStr = toBits(thirdByte[0]);
        String opCode = thirdByteStr.substring(1, 5);

        //RA
        //Z (Reserved for Future Use) = 4 bits en 0
        //RCODE 0 = No Error
        byte[] razRcode = {0};
        razRcode[0] = data[index++];

        //QDCOUNT
        String qdCountStr = "";
        byte[] qdCount = {0,0};
        for(int i = 0; i < 2; i++){
            qdCount[i] = data[index];
            qdCountStr += toBits(data[index]);
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
        
        //Armando el ArrayList de byte[]...
        queryData.add(trID);
        queryData.add(thirdByte);
        queryData.add(razRcode);
        queryData.add(qdCount);
        queryData.add(anCount);
        queryData.add(nsCount);
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
            
            byte[] qNameByte = {0,0,0,0,0,0,0,0,0,0,0};
            int j = 0;
            while(data[index] != 0){
                
                qNameByte[j++] = data[index];
                q_name_lengthStr = toBits(data[index++]);
                labelLength = Integer.parseInt(q_name_lengthStr, 2);
                
                //Prueba
                //System.out.println(labelLength);
                
                byte[] byteName = new byte[labelLength];
                for(int k = 0; k < labelLength; k++){
                    byteName[k] = data[index];
                    qNameByte[j++] = byteName[k];
                    index++;
                }

                //Label copiado al nombre
                name += new String(byteName) + '.';
            }
            name = name.substring(0, name.length()-1);
            
            //PRUEBA
            //System.out.println(qNameByte);
            
            //QNAME index = 7
            queryData.add(qNameByte);
            
            //QTYPE index = 8
            byte[] qType = {0,0};
            qType[0] = data[index++];
            qType[1] = data[index++];
            queryData.add(qType);
            
            //QCLASS index 10
            byte[] qClass = {0,0};
            qClass[0] = data[index++];
            qClass[1] = data[index++];
            queryData.add(qClass);
            
            queryResponseDataMap.put("ID", trIDStr);
            queryResponseDataMap.put("OPCODE", opCode);
            queryResponseDataMap.put("QDCOUNT", qdCountStr);
            queryResponseDataMap.put("QNAME", name);

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
    
    private static ArrayList<ArrayList<String>> findInMasterFile(String name, ArrayList<ArrayList<String>> registros){
        ArrayList<ArrayList<String>> registrosNombre = new ArrayList<>();
        for(ArrayList<String> row : registros){
            if(row.get(0).compareTo(name) == 0){
                registrosNombre.add(row);
            }
        }
        if(!registrosNombre.isEmpty())
            return registrosNombre;
        return null;
    }
}
