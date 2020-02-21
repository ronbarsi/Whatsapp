package common.ServerRequest.ClientToServerRequests;

import akka.actor.ActorSelection;
import common.ServerRequest.ServerRequest;
import akka.actor.ActorRef;

abstract class ClientToServerRequest extends ServerRequest {
    ClientToServerRequest(ActorRef sourceActor, String sourceName, ActorSelection server) {
        super(sourceActor, sourceName, server);
    }
}
