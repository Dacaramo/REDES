package com.dns.proyectodns.view;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPClient {
    
    
    public UDPClient () throws Exception {
        
        try{
        DatagramSocket clientSocket = new DatagramSocket();

        String url = "www.gugol.com"; //Recibimos como parámetro del comando (?)
        byte[] urlByteArray = url.getBytes();
        
        InetAddress dns_ip_address = InetAddress.getLocalHost(); //Qué dirección será la del DNS? Se recibe como parámetro del comando (no necesariamente ingresada por el usuario)?
        
        int portNumber = 9999; //TODO: Averiguar qué número de puerto debe usar el SERVIDOR
        
        DatagramPacket queryPacket = new DatagramPacket(urlByteArray, urlByteArray.length, dns_ip_address, portNumber);
        
        clientSocket.send(queryPacket); //Enviando el paquete a través del socket...
        //La siguiente línea simula la concurrencia entre los dos procesos:
        //UDPServer.main(args);
        
        int tamanoRespuesta = 1024; //TODO: Determinar de qué tamaño (en Bytes) debe ser el paquete que contiene la respuesta al query, teniendo en cuenta todos los campos de Registro tipo A.
        byte[] responseByteArray = new byte[tamanoRespuesta];
        DatagramPacket responsePacket = new DatagramPacket(responseByteArray, responseByteArray.length);
        
        //Las siguientes líneas son de prueba:
        String response = new String(responsePacket.getData());
        System.out.println(response);  
        }
        catch(Exception e){
            throw new SocketException();
        }
    }
    
    
}
