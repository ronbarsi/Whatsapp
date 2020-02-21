package common.ServerRequest.GroupServerRequests;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import common.ServerRequest.ServerRequest;

public class GroupUserDisconnect extends ServerRequest {
    public GroupUserDisconnect(ActorRef sourceActor, String sourceName, ActorSelection server) {
        super(sourceActor, sourceName, server);
    }
}
