package common.ServerRequest.ClientToClientRequests;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import common.ServerReply.ServerReply;
import common.ServerReply.StringReply;

import java.sql.Timestamp;

public class TextMessageRequest extends ClientToClientRequest {
    private final String message;

    public TextMessageRequest(ActorRef sourceActor, String sourceName, ActorSelection server, String targetName, String message) {
        super(sourceActor, sourceName, server, targetName);
        this.message = message;
    }

    public void sendMessage(ActorRef sender) {
        /**
         * send message directly to the target client
         */
        String message = getMessage(this.message, "user", getSourceName());
        ServerReply reply = new StringReply(sender, getSourceActor(), message);
        this.getAddresseeActor().tell(reply, this.sourceActor);
    }

    public static String getMessage(String message, String type, String sourceName) {
        String time = new java.text.SimpleDateFormat("HH:mm").format(new Timestamp(System.currentTimeMillis()));
        return "[" + time + "][" + type + "][" + sourceName + "] " + message;
    }
}