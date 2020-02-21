package common.ServerRequest.ClientToServerRequests;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class LoginRequest extends ClientToServerRequest {
    public LoginRequest(ActorRef sourceActor, String sourceName, ActorSelection server) {
        super(sourceActor, sourceName, server);
    }
}
