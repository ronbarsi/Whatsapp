package common.ServerRequest.GroupServerRequests;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import common.ServerRequest.ServerRequest;

public class GroupServerRequest extends ServerRequest {
    private String groupName;
    private final String [] args;
    private byte[] fileBytes;

    public GroupServerRequest(ActorRef sourceActor, String sourceName, ActorSelection server, String [] args) {
        super(sourceActor, sourceName, server);
        this.args = args;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String[] getArgs() {
        return args;
    }


    public void setFileBytes(byte[] fileBytes){
        this.fileBytes = fileBytes;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }
}