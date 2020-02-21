package common.Group;

import akka.actor.ActorRef;
import java.io.Serializable;
import java.util.Scanner;

public class GroupInvitation implements Serializable {
    private ActorRef addresseeActor;
    private String groupName;
    private ActorRef server;
    private Group group;
    private String addressee;
    private boolean reply;


    public GroupInvitation(String addressee, ActorRef addresseeActor, String groupName, ActorRef server, Group group){
        this.addresseeActor = addresseeActor;
        this.groupName = groupName;
        this.server = server;
        this.group = group;
        this.addressee = addressee;
    }

    public void askForReply(){
            Scanner input = new Scanner(System.in);
            while (input.hasNextLine()) {
                String ans = input.nextLine();
                reply = ans.toLowerCase().equals("yes");
                invitationReply();
                break;
                }
            }

    public String getGroupName() {
        return groupName;
    }

    public void sendReply() {
        this.addresseeActor.tell(this, this.server);
    }

    private void invitationReply(){
        this.server.tell(this, null);
    }

    public void addUser(){
        if (reply){
            this.server.tell(new GroupAcceptInvitation(addressee, addresseeActor, getGroupName()), addresseeActor);
        }
    }
}
