package common.ServerReply;

import akka.actor.ActorRef;

public class StringReply extends ServerReply {
    public StringReply(ActorRef server, ActorRef client, String reply) {
        super(server, client, reply);
    }

    public void printMessage() {
        System.out.println(getReply().toString());
    }
}
