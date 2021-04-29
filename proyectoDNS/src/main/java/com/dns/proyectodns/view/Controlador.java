/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dns.proyectodns.view;

import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author USUARIO
 */
public class Controlador {
    
    public static void  main(String[] args) throws Exception {
        
        try {
        UDPClient cliente = new UDPClient();
        UDPServer servidor = new UDPServer();
    } catch (SocketException ex) {
        Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
    }
}
     
   
}
