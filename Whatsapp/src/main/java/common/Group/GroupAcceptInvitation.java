package common.Group;

import java.io.Serializable;
import akka.actor.ActorRef;

public class GroupAcceptInvitation implements Serializable {
    private String addressee;
    private ActorRef addresseeActor;
    private String groupName;

    public GroupAcceptInvitation(String addressee, ActorRef addresseeActor, String groupName){
        this.addressee = addressee;
        this.addresseeActor = addresseeActor;
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void addUSer(Group group){
        group.addUser(addressee, addresseeActor);
    }
}