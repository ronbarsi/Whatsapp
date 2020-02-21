package common.ServerRequest.ClientToServerRequests;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class LogoutRequest extends ClientToServerRequest {
    public LogoutRequest(ActorRef sourceActor, String sourceName, ActorSelection server) {
        super(sourceActor, sourceName, server);
    }
}
