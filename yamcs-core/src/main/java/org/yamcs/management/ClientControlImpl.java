package org.yamcs.management;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.yamcs.YProcessor;
import org.yamcs.YProcessorClient;
import org.yamcs.YProcessorException;

import org.yamcs.protobuf.YamcsManagement.ClientInfo;

public class ClientControlImpl extends StandardMBean implements ClientControl {
    ClientInfo clientInfo;
    YProcessorClient client;
    
    ClientControlImpl(String instance, int id, String username, String applicationName, String yprocName, YProcessorClient client) throws NotCompliantMBeanException {
        super(ClientControl.class);
        this.client=client;
        clientInfo=ClientInfo.newBuilder().setInstance(instance)
            .setApplicationName(applicationName)
            .setYProcessorName(yprocName).setUsername(username)
            .setId(id).build();
    }
    
    @Override
    public String getApplicationName() {
        return clientInfo.getApplicationName();
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    
    public YProcessorClient getClient(){
        return client;
    }

    public void switchYProcessor(YProcessor chan) throws YProcessorException {
        client.switchYProcessor(chan);
        
        clientInfo=ClientInfo.newBuilder().mergeFrom(clientInfo)
            .setInstance(chan.getInstance()).setYProcessorName(chan.getName())
            .build();
    }
}
