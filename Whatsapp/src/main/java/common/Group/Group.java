package common.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import akka.actor.ActorRef;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import common.ServerReply.ServerReply;
import common.ServerReply.StringReply;
import common.ServerReply.FileReply;
import common.ServerRequest.ClientToClientRequests.TextMessageRequest;
import common.ServerRequest.GroupServerRequests.GroupUserDelete;


public class Group implements Serializable {
    private ActorRef server;
    private String groupAdminName;
    private ActorRef groupAdminActor;
    private String groupName;
    private Map<String, ActorRef> groupMembers;
    private List<String> adminGroupMembers;
    private Map<String, Thread> muted;

    public Group(String groupName, ActorRef adminActor, String adminName, ActorRef server) {
        this.server = server;
        this.groupAdminName = adminName;
        this.groupAdminActor = adminActor;
        this.groupName = groupName;
        this.adminGroupMembers = new ArrayList<>();
        this.groupMembers = new HashMap<>();
        this.groupMembers.put(adminName, adminActor);
        this.adminGroupMembers.add(adminName);
        this.muted = new HashMap<>();
    }

    public void leave(String memberName, ActorRef memberActor){
        if(!isMember(memberName)) {
            new StringReply(server, memberActor, notAMemberMsg(memberName)).reply();
            return;
        }
        broadcastGroup(memberActor, new StringReply(server, memberActor, leavingGroupMsg(memberName)));
        if(isAdmin(memberName)){
            broadcastGroup(memberActor, new StringReply(server, memberActor, coadminLeaveGroupMsg(memberName)));
            removeAdmin(memberName);
        }
        if(isFounder(memberName)){
            deleteGroup();
        }
        groupMembers.remove(memberName);
    }

    public void sendText(String memberName, String msg, ActorRef memberActor){
        if(isMember(memberName) && !isMuted(memberName)) {
            broadcastTextToGroup(msg, memberName);
        }
    }

    public void sendFile(String memberName, byte[] msg, ActorRef memberActor){
        if(!isMember(memberName)){
            new StringReply(server, memberActor, "You are not part of " + groupName + "!").reply();
        } else if(isMuted(memberName)){
            new StringReply(server, memberActor, "You are muted for <time> in " + groupName + "!").reply();
        } else {
            broadcastFileToGroup(memberActor, memberName, msg);
        }
    }

    public void invite(String memberName, String addressee, ActorRef addresseeActor, ActorRef memberActor) {
        if (!isAdmin(memberName)) {

            new StringReply(server, memberActor, notAnAdminMsg()).reply();
        } else if(isMember(addressee)){

            new StringReply(server, memberActor,addressee + " is already in " + groupName + "!").reply();
        } else {
            new GroupInvitation(addressee, addresseeActor, groupName, server, this).sendReply();
        }
    }

    public void addUser(String userName, ActorRef userActor){
        broadcastGroup(server, new StringReply(server, userActor, userName + " has been added to the group: " + groupName));
        groupMembers.put(userName, userActor);
        sendStringMsg("Welcome to " + groupName + "!", userName);
    }

    public void remove(String memberName, ActorRef memberActor, String addressee, ActorRef addresseeActor) {
        if (!isMember(addressee)) {
            new StringReply(server, memberActor, addressee + " does not exist!").reply();
        } else if (!isAdmin(memberName)) {
            new StringReply(server, memberActor, notAnAdminMsg()).reply();
        } else if (isFounder(addressee) && isFounder(memberName)) {
            this.leave(memberName, memberActor);
        } else {
            TextMessageRequest msg = new TextMessageRequest(memberActor, memberName, null, addressee, removedMsg(memberName));
            msg.setAddresseeActor(addresseeActor);
            memberActor.tell(msg, server);
            deleteMember(addressee);
        }
    }

    public void coadminAdd(String memberName, ActorRef memberActor, String addressee){
        if (!isMember(addressee)) {
            new StringReply(server, memberActor, addressee + " does not exist!").reply();
        } else if (!isAdmin(memberName)) {
            new StringReply(server, memberActor, notAnAdminMsg()).reply();
        } else {
            addAdmin(addressee);
            // todo adminGroupMembers.add(addressee);
            sendStringMsg("You have been promoted to co-admin in " + groupName + "!", addressee);
        }
    }

    public void coadminRemove(String memberName, ActorRef memberActor, String addressee) {
        if (!isMember(addressee)) {
            new StringReply(server, memberActor, addressee + " does not exist!").reply();
        } else if (!isAdmin(memberName)) {
            new StringReply(server, memberActor, notAnAdminMsg()).reply();
        } else {
            removeAdmin(addressee);
            // todo adminGroupMembers.remove(addressee);
            sendStringMsg("You have been demoted  to user in " + groupName + "!", addressee);
        }
    }

    public void mute(String memberName, ActorRef memberActor, String addressee, long duration) {
        if (!isMember(addressee)) {
            new StringReply(server, memberActor, addressee + " does not exist!").reply();
        } else if (!isAdmin(memberName)) {
            new StringReply(server, memberActor, notAnAdminMsg()).reply();
        } else {
            muteMember(addressee, memberName, duration);
        }
    }

    public void unmute(String memberName, ActorRef memberActor, String addressee){
        if (!isMember(addressee)) {
            new StringReply(server, memberActor, addressee + " does not exist!").reply();
        } else if (!isAdmin(memberName)) {
            new StringReply(server, memberActor, notAnAdminMsg()).reply();
        } else if(!isMuted(addressee)){
            new StringReply(server, memberActor, addressee + " is not muted!").reply();
        } else {
            unmuteMember(memberName, addressee);
        }
    }

    public boolean userInGroup(String user){
        return groupMembers.containsKey(user);
    }

    private void broadcastTextToGroup(String msg, String memberName){
        for(String memberString : groupMembers.keySet()){
            sendStringMsg(generateMsg(msg, memberName), memberString);
        }
    }

    private void muteMember(String mutedName, String memberName, long duration){
        String msg = generateMsg(muteMsg(memberName, duration), memberName);
        Runnable unmute = () -> {
            try {
                sendStringMsg(msg, mutedName);
                Thread.sleep(duration * 1000);
            } catch (Exception e){
                muted.remove(mutedName);
            }

            if (muted.containsKey(mutedName)){
                muted.remove(mutedName);
                String unMutesMsg = generateMsg(unMuteMsg(), memberName);
                sendStringMsg(unMutesMsg, mutedName);
            }
        };
        // add to muted:
        muted.put(mutedName, new Thread(unmute));
        // start countdown:
        muted.get(mutedName).start();
    }

    private void unmuteMember(String memberName, String addressee){
        sendStringMsg("You have been unmuted in " + groupName + " by " + memberName + "!" , addressee);
        muted.get(addressee).interrupt();
    }

    private void deleteGroup(){
        broadcastGroup(server, new StringReply(server, groupAdminActor, adminLeaveGroupMsg(groupAdminName)));
        for(String memberName : groupMembers.keySet()) {
            deleteMember(memberName);
        }
        server.tell(new GroupUserDelete(groupAdminActor, groupName), server);
    }

    private ActorRef StringToActorRef(String memberString){
        return groupMembers.get(memberString);
    }

    private void sendStringMsg(String msg, String addressee){
        ActorRef addresseeActor = StringToActorRef(addressee);
        addresseeActor.tell(new StringReply(server, addresseeActor, msg),server);
    }

    private void broadcastFileToGroup(ActorRef memberActor, String memberName, byte[] msg){
        for (ActorRef member : groupMembers.values()) {
            member.tell(new FileReply(server, member, msg, memberName, true), memberActor);
        }
    }

    private void broadcastGroup(ActorRef senderActor, ServerReply msg){
        for (ActorRef member : this.groupMembers.values()) {
            member.tell(msg, senderActor);
        }
    }

    private boolean isFounder(String memberName){
        return groupAdminName.equals(memberName);
    }

    private boolean isAdmin(String memberName){
        return adminGroupMembers.contains(memberName);
    }

    private boolean isMember(String memberName){
        return groupMembers.containsKey(memberName);
    }

    private boolean isMuted(String memberName){
        return muted.containsKey(memberName);
    }

    private String getTime(){
        return new java.text.SimpleDateFormat("HH:mm").format(new Timestamp(System.currentTimeMillis()));
    }

    private String generateMsg(String msg, String memberName){
        return "[" + getTime() + "][" + groupName + "][" + memberName + "] " + msg;
    }

    private void deleteMember(String memberName){
        groupMembers.remove(memberName);
        removeAdmin(memberName);
        removeFromMuted(memberName);
    }

    private void addAdmin(String memberName){
        adminGroupMembers.add(memberName);
    }

    private void removeAdmin(String memberName){
        adminGroupMembers.remove(memberName);
    }

    private void removeFromMuted(String memberName){
        muted.remove(memberName);
    }

    private String leavingGroupMsg(String memberName){
        return memberName + " has left " + this.groupName + "!";
    }

    private String coadminLeaveGroupMsg(String memberName){
        return memberName + " is removed from co-admin list in " + this.groupName;
    }

    private String adminLeaveGroupMsg(String memberName){
        return memberName + " admin has closed " + this.groupName + "!";
    }

    private String notAMemberMsg(String name){
        return name + " is not in " + this.groupName + "!";
    }

    private String notAnAdminMsg(){
        return "You are neither an admin nor a co-admin of " + groupName + "!";
    }

    private String removedMsg(String memberName){
        return "You have been removed from " + groupName + " by " + memberName + "!";
    }

    private String muteMsg(String memberName, long duration){
        return "You have been muted for " + duration + " in " + groupName + " by " + memberName + "!";
    }

    private String unMuteMsg(){
        return "You have been unmuted! Muting time is up!";
    }

}
