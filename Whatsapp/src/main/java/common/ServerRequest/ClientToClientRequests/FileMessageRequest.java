package common.ServerRequest.ClientToClientRequests;


import common.ServerReply.FileReply;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class FileMessageRequest extends ClientToClientRequest {

    private final byte[] file;

    public FileMessageRequest(ActorRef sourceActor, String sourceName, ActorSelection server, String targetName, byte[] file) {
        super(sourceActor, sourceName, server, targetName);
        this.file = file;
    }

    public void sendMessage(ActorRef sender){
        FileReply fileReply = new FileReply(getSourceActor(), getAddresseeActor(), this.file, getSourceName(), false);
        this.getAddresseeActor().tell(fileReply, getSourceActor());
    }

}
