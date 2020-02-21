package client;

import common.Group.GroupInvitation;
import common.ServerReply.ServerReply;
import common.ServerRequest.ClientToClientRequests.ClientToClientRequest;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ClientActor extends AbstractActor{
    private String actorName;
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    public ClientActor(String name){
        this.actorName = name;
    }

    public static Props props(String name) {
        return Props.create(ClientActor.class, name);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClientToClientRequest.class, msg -> {
                    msg.sendMessage(getSelf());
                })
                .match(ServerReply.class, ServerReply::printMessage)
                .match(GroupInvitation.class, msg ->{
                    System.out.println("You have been invited to " + msg.getGroupName() + ", Accept?");
                    System.out.println("to accept, type 'yes' + ENTER twice ");
                    msg.askForReply();
                })
                .matchAny(o -> logger.info("received unknown message: " + o.toString()))
                .build();
    }
}
