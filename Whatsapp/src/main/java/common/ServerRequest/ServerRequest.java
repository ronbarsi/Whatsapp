package common.ServerRequest;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import java.io.Serializable;

public abstract class ServerRequest implements Serializable {

    protected final ActorRef sourceActor;
    private final String sourceName;
    private final ActorSelection server;

    protected ServerRequest(ActorRef sourceActor, String sourceName, ActorSelection server){
        this.sourceActor = sourceActor;
        this.sourceName = sourceName;
        this.server = server;
    }

    public String getSourceName() {
        return sourceName;
    }

    public ActorRef getSourceActor() {
        return sourceActor;
    }

    public ActorSelection getServer() {
        return server;
    }

    public void tellServer() {
        this.server.tell(this, sourceActor);
    }


}
