package common.ServerReply;

import akka.actor.ActorRef;
import java.io.Serializable;

public abstract class ServerReply implements Serializable {

    // The sending server
    private final ActorRef server;

    // The reply itself
    private final Object reply;

    // Target actor to reply to
    private final ActorRef client;

    public ServerReply(ActorRef server, ActorRef client, Object reply){
        this.server = server;
        this.client = client;
        this.reply = reply;
    }

    public ActorRef getServer() {
        return server;
    }

    public Object getReply() {
        return reply;
    }

    public void reply(){
        client.tell(this, null);
    }

    public abstract void printMessage();
}
