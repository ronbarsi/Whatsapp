package server;


import common.Group.GroupInvitation;
import common.ServerReply.StringReply;
import common.ServerRequest.ClientToClientRequests.ClientToClientRequest;
import common.ServerRequest.GroupServerRequests.GroupUserDelete;
import common.ServerRequest.GroupServerRequests.GroupServerRequest;
import common.ServerRequest.GroupServerRequests.GroupUserDisconnect;
import common.ServerRequest.ClientToServerRequests.LoginRequest;
import common.ServerRequest.ClientToServerRequests.LogoutRequest;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import java.util.Map;


class ServerActor extends AbstractActor {
    /**
     * Server actor:
     *  1. Handles server requests
     *  2. Forwards groups requests to groups server
     */

    // logger
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    // Connections map: key = actor's name, value = actor's reference
    private Map<String, ActorRef> actorsMap;

    // Group actor server - will handle group server requests
    private ActorRef groupsServer;

    private ServerActor() {}

    public static Props props() {
        return Props.create(ServerActor.class);
    }

    @Override
    public void preStart() {
        this.actorsMap = new HashMap<>();
        this.groupsServer = getContext().actorOf(GroupServerActor.props(actorsMap), "groupServerActor");
    }

    private void handleByGroupsServer(Object msg, ActorRef sender){
        this.groupsServer.tell(msg, sender);
    }

    private static void serverReply(ActorRef server, ActorRef client, String msg){
        new StringReply(server, client, "Server: "+msg).reply();
    }

    private static void serverErrorReply(ActorRef server, ActorRef client, String msg){
        new StringReply(server, client, "Server Error: " + msg).reply();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LoginRequest.class, msg -> {
                    logger.info("Login Request: " + msg.getSourceName());
                    if (!actorsMap.containsKey(msg.getSourceName())) {
                        actorsMap.put(msg.getSourceName(), msg.getSourceActor());
                        serverReply(getSelf(), getSender(),"User connected: " + msg.getSourceName());
                    }
                    else {
                        serverErrorReply(getSelf(), getSender(),"User already exists: " +msg.getSourceName());
                    }
                })
                .match(LogoutRequest.class, msg -> {
                    logger.info("Logout Request: " + msg.getSourceName());
                    if (actorsMap.containsKey(msg.getSourceName())) {
                        // delete user from all groups
                        handleByGroupsServer(new GroupUserDisconnect(msg.getSourceActor(), msg.getSourceName(), null), getSelf());
                        // remove connection
                        actorsMap.remove(msg.getSourceName());
                        // reply
                        getSender().tell(new StringReply(getSelf(),
                                getSender(), "Logged out: " + msg.getSourceName()), getSelf());
                    }
                    else {
                        serverErrorReply(getSelf(), getSender(),"User doesn't exists: " +msg.getSourceName());
                    }
                })
                .match(ClientToClientRequest.class, msg -> {
                    logger.info("Client 2 Client request received ");
                    if (actorsMap.containsKey(msg.getAddresseeName())) {
                        msg.setAddresseeActor(actorsMap.get(msg.getAddresseeName()));
                        msg.getAddresseeActor().tell(msg, getSelf());
                    } else {
                        serverErrorReply(getSelf(), getSender(),"User doesn't exists: " +msg.getSourceName());
                    }
                })
                // group requests handled by groups server
                .match(GroupInvitation.class, msg->{
                    handleByGroupsServer(msg, getSender());
                })
                .match(GroupServerRequest.class, msg->{
                    handleByGroupsServer(msg, getSender());
                })
                .match(GroupUserDelete.class, msg -> {
                    handleByGroupsServer(msg, getSender());
                })
                .matchAny(msg -> {
                    logger.info("Unknown Message");
                    serverErrorReply(getSelf(), getSender(),"Unknown Message");
                })
                .build();
    }
}

