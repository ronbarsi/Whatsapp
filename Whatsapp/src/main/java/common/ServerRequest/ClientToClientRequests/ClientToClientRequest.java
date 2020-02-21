package common.ServerRequest.ClientToClientRequests;

import akka.actor.ActorSelection;
import common.ServerRequest.ServerRequest;
import akka.actor.ActorRef;

public abstract class ClientToClientRequest extends ServerRequest {
    /**
     * reply required both in the source actor and the target actor
     */
    private final String addresseeName;
    private  ActorRef addresseeActor;

    public ClientToClientRequest(ActorRef sourceActor, String sourceName, ActorSelection server, String addresseeName) {
        super(sourceActor, sourceName, server);
        this.addresseeName = addresseeName;
    }

    public String getAddresseeName() {
        return addresseeName;
    }

    public ActorRef getAddresseeActor(){
        return this.addresseeActor;
    }

    public void setAddresseeActor(ActorRef addresseeActor){
        this.addresseeActor = addresseeActor;
    }

    public abstract void sendMessage(ActorRef sender);
}