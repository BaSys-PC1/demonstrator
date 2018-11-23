/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.iui.basys.demonstrator.device.urrpc;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author mrk
 */

public class UrRpcClient {
    private static final Logger LOG = LoggerFactory.getLogger(UrRpcClient.class);
    private XmlRpcClient client;
 
    public UrRpcClient(String url) {
        client = new XmlRpcClient();
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL(url));
            client.setConfig(config);
        } catch (MalformedURLException ex) {
           LOG.error("Exception occurred: {}", ex.toString());
        }
    }
    
    public UrRpcClient(String host, int port) {
        this("http://" + host + ":" + port);
    }
    
    public Object getCurrentRoutine() {
        if(client!=null) {
            Object params [] = {};
            try {
                return client.execute("get_routine", params);
            } catch (XmlRpcException ex) {
                LOG.error("Exception occurred: {}", ex.toString());
                return null;
            }        
        }
        else
            return null;
    }
    
    public void setCurrentRoutine(int code) {
        if(client!=null) {
            Object params [] = {code};
            try {
                client.execute("set_routine", params);
            } catch (XmlRpcException ex) {
                LOG.error("Exception occurred: {}", ex.toString());
            }          
        }
        else 
            LOG.error("Client is null!");
    }
    
    public Object getCurrentStatus() {
        if(client!=null) {
            Object params [] = {};
            try {
                return client.execute("get_status", params);
            } catch (XmlRpcException ex) {
                LOG.error("Exception occurred: {}", ex.toString());
                return null;
            }            
        }
        else {
            LOG.error("Client is null!");
            return null;
        }
    }
}
