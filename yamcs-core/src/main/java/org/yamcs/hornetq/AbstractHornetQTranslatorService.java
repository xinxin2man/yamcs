package org.yamcs.hornetq;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamcs.ConfigurationException;
import org.yamcs.yarch.Stream;
import org.yamcs.yarch.StreamSubscriber;
import org.yamcs.yarch.Tuple;
import org.yamcs.yarch.YarchDatabase;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.client.ClientMessage;

import org.yamcs.api.YamcsClient;
import org.yamcs.api.YamcsSession;

import com.google.common.util.concurrent.AbstractService;

/**
 * takes data from yarch streams and publishes it to hornetq address (reverse of HornetQTmProvider)
 * 
 * To avoid a ping-pong effect:
 *  - it creates a queue with a filter on hornet side
 *  - it remembers a thread local version of the tuple in transition on yarch side
 *
 */
public class AbstractHornetQTranslatorService extends AbstractService implements StreamSubscriber {
    final private TupleTranslator translator;
    YamcsSession yamcsSession;
    final private YamcsClient msgClient;
    List<Stream> streams = new ArrayList<Stream>();
    
    Logger log=LoggerFactory.getLogger(this.getClass().getName());
    ThreadLocal<Tuple> currentProcessingTuple=new ThreadLocal<Tuple>();
    String instance;
    
    
    public AbstractHornetQTranslatorService(String instance, List<String> streamNames, TupleTranslator translator) throws ConfigurationException {
        this.instance = instance;
        YarchDatabase db = YarchDatabase.getInstance(instance);
        
        for(String sn: streamNames) {
            Stream s = db.getStream(sn);
            if(s==null) throw new ConfigurationException("Cannot find stream '"+sn+"'");
            streams.add(s);
        }
        this.translator = translator;
        
        try {
            yamcsSession=YamcsSession.newBuilder().build();
            msgClient=yamcsSession.newClientBuilder().setDataProducer(true).build();
        } catch (Exception e) {
            throw new ConfigurationException("Cannot create hornetq client", e);
        }
    }

    
    @Override
    public void onTuple(Stream s, Tuple tuple) {
        if(tuple==currentProcessingTuple.get())return;
        try {
            SimpleString hornetAddress = new SimpleString(instance+s.getName());
            ClientMessage msg=translator.buildMessage(yamcsSession.session.createMessage(false), tuple);
            msg.putIntProperty(StreamAdapter.UNIQUEID_HDR_NAME, StreamAdapter.UNIQUEID);
            msgClient.dataProducer.send(hornetAddress, msg);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
        } catch (HornetQException e) {
            log.warn("Got exception when sending message:", e);
        }
    }

    @Override
    public void streamClosed(Stream istream) {
    }


    @Override
    public String toString() {
        return "HornetQTmService";
    }


    @Override
    protected void doStart() {
        for(Stream s:streams) {
            s.addSubscriber(this);
        }
        notifyStarted();
    }


    @Override
    protected void doStop() {
        try {
            msgClient.close();
            for(Stream s:streams) {
                s.removeSubscriber(this);
            }
        } catch (HornetQException e) {
            log.warn("Got exception when quiting:", e);
        }
        
    }
}
