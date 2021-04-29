/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dns.proyectodns.view;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author cristobalcastrillonbalcazar
 */
public class UDPServer {
    public UDPServer() throws Exception {
        
        try{
        int serverPortNumber = 53; //Número de Puerto Estándar para DNS: 53
        
        DatagramSocket serverSocket = new DatagramSocket(serverPortNumber);
        
        int tamanoQuery = 1024; //TODO: Determinar de qué tamaño debe ser el paquete que contiene el query, teniendo en cuenta qué se envía (tal vez solo una URL).
        byte[] query = new byte[tamanoQuery];
        
        DatagramPacket serverReceivedPacket = new DatagramPacket(query, query.length);
        serverSocket.receive(serverReceivedPacket);
        
        String url = new String(serverReceivedPacket.getData());
        //Aquí van las instrucciones para buscar en BBDD la equivalencia de la URL en dirección IP (y demás campos del registro tipo A)...
        
        InetAddress client_ip_address = serverReceivedPacket.getAddress(); //Dirección IP del cliente.
        int clientPortNumber = serverReceivedPacket.getPort(); //Número de puerto sobre el que escucha el cliente.
        //La siguientes líneas simulan un resultado...
        //El resultado se da a partir del Master File:
        //  Si no se encuentra el par URL - IPv4 en el Master File, se hace un query a otro DNS.
        String ipResuelto = "192.158.1.38";
        byte[] ipResByteArray = ipResuelto.getBytes();
        
        DatagramPacket queryResponse = new DatagramPacket(ipResByteArray, ipResByteArray.length, client_ip_address, clientPortNumber);
        serverSocket.send(queryResponse);
        } 
        catch(Exception e){
            throw new SocketException();
        }
    }
}
