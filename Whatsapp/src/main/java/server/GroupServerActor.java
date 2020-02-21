package server;

import common.Group.Group;
import common.Group.GroupAcceptInvitation;
import common.Group.GroupInvitation;
import common.ServerReply.ServerReply;
import common.ServerReply.StringReply;
import common.ServerRequest.GroupServerRequests.GroupUserDelete;
import common.ServerRequest.GroupServerRequests.GroupServerRequest;
import common.ServerRequest.GroupServerRequests.GroupUserDisconnect;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class GroupServerActor extends AbstractActor  {
    /**
     * Groups actor:
     *  1. Handles groups requests
     */

    // logger
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    // Connections map: key = actor's name, value = actor's reference
    private Map<String, ActorRef> actorsMap;

    // Groups map: key = group's name, value = representative ChatGroup object
    private final Map<String, Group> groupsMap;

    private GroupServerActor(Map<String, ActorRef> actorsMap) {
        this.actorsMap = actorsMap;
        this.groupsMap = new HashMap<>();
    }

    public static Props props(Map<String, ActorRef> actorsMap) {
        return Props.create(GroupServerActor.class, actorsMap);
    }

    private static void GroupErrorMessage(ActorRef groupServer, ActorRef client, String msg){
        new StringReply(groupServer, client, msg).reply();
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(GroupUserDisconnect.class, msg->{
                    logger.info("Leaving groups: " + msg.getSourceName());
                    leaveGroups(msg);
                })
                .match(GroupInvitation.class, msg->{

                    logger.info("Inviting to groups: " + msg.getGroupName());
                    msg.addUser();
                })
                .match(GroupAcceptInvitation.class, msg->{
                    logger.info("Accept invite: " + msg.getGroupName());
                    msg.addUSer(groupsMap.get(msg.getGroupName()));
                })
                .match(GroupServerRequest.class,  msg->{
                    logger.info("Group request: " + Arrays.toString(msg.getArgs()));
                    if (!actorsMap.containsKey(msg.getSourceName())){
                        GroupErrorMessage(getSelf(), msg.getSourceActor(), "User is not logged in");
                        return;
                    }
                    String[] args = msg.getArgs();
                    if (args.length < 3){
                        GroupErrorMessage(getSelf(), getSender(), "Group: Invalid arguments: " + Arrays.toString(args));
                        return;
                    }
                    handleGroupRequest(msg, args);
                })
                .match(GroupUserDelete.class, msg -> {
                    logger.info("Delete Group request: " + msg.getGroupName());
                    groupsMap.remove(msg.getGroupName());
                })
                .matchAny(msg -> {
                    logger.info("Unknown Message");
                    GroupErrorMessage(getSelf(), getSender(), "Unknown Message");
                })
                .build();
    }

    private void leaveGroups(GroupUserDisconnect msg) {
        String userName = msg.getSourceName();
        for(Group group : this.groupsMap.values()){
            if(group.userInGroup(userName))
                group.leave(userName, msg.getSourceActor());
        }
    }

    private void handleGroupRequest(GroupServerRequest msg, String[] args) {
        ServerReply reply;
        String command = args[1];
        String groupName;

        switch (command) {
            case "create":
                groupName = args[2];
                if (!groupsMap.containsKey(groupName)) {
                    groupsMap.put(groupName, new Group(groupName, msg.getSourceActor(), msg.getSourceName(), getSelf()));
                    reply = new StringReply(getSelf(), getSender(), "Group created: " + groupName);
                } else {
                    reply = new StringReply(getSelf(), getSender(), "Group already exists: " + groupName);
                }
                msg.setGroupName(groupName);
                reply.reply();
                break;

            case "leave":
                groupName = args[2];
                if (groupsMap.containsKey(groupName)) {
                    Group group = groupsMap.get(groupName);
                    msg.setGroupName(groupName);
                    group.leave(msg.getSourceName(), getSender());
                }
                else {
                    reply = new StringReply(getSelf(), getSender(), "Group doesn't exists: " + groupName);
                    reply.reply();
                }
                break;

            case "send":
                groupName = args[3];
                if (groupsMap.containsKey(groupName)) {
                    msg.setGroupName(groupName);
                    handleGroupSend(msg, args);
                }
                else{
                    reply = new StringReply(getSelf(), getSender(), "Group doesn't exists: " + groupName);
                    reply.reply();
                }
                break;

            case "coadmin":
            case "user":
                groupName = args[3];
                if (groupsMap.containsKey(groupName)) {
                    msg.setGroupName(groupName);
                    handleGroupUserCoadmin(msg, args);
                }
                else{
                    reply = new StringReply(getSelf(), getSender(), "Group doesn't exists: " + groupName);
                    reply.reply();
                }
                break;
            default:
                GroupErrorMessage(getSelf(), getSender(), "Unknown Message");
        }
    }

    private void handleGroupSend(GroupServerRequest msg, String[] args) {
        if (args.length < 5){
            GroupErrorMessage(getSelf(), getSender(), "Send: Invalid arguments: " + Arrays.toString(args));
            return;
        }

        String type = args[2];
        String groupName = args[3];

        String invokerName = msg.getSourceName();
        ActorRef invokerActor = msg.getSourceActor();
        Group group = groupsMap.get(groupName);

        msg.setGroupName(groupName);

        if (type.equals("text")){
            group.sendText(invokerName, String.join(" ", Arrays.copyOfRange(args, 4, args.length)), invokerActor);
            return;
        }

        if (type.equals("file")){
            group.sendFile(invokerName, msg.getFileBytes(), invokerActor);
            return;
        }
        GroupErrorMessage(getSelf(), getSender(), "Unknown Message");

    }

    private void handleGroupUserCoadmin(GroupServerRequest msg, String[] args) {
        if (args.length < 5){
            GroupErrorMessage(getSelf(), getSender(), "User & Coadmin: Invalid arguments: " + Arrays.toString(args));
            return;
        }
        ServerReply reply;

        String userType = args[1];
        String action = args[2];
        String groupName = args[3];
        String targetUserName = args[4];

        String invokerName = msg.getSourceName();
        ActorRef invokerActor = msg.getSourceActor();
        Group group = groupsMap.get(groupName);

        msg.setGroupName(groupName);

        if (!actorsMap.containsKey(targetUserName)) {
            reply = new StringReply(getSelf(), getSender(), "Target user doesn't exists: " + targetUserName);
            reply.reply();
            return;
        }
        ActorRef targetActor = actorsMap.get(targetUserName);
        switch(userType){
            case "user":
                switch (action) {
                    case "invite":
                        group.invite(invokerName, targetUserName, targetActor,invokerActor);
                        break;
                    case "remove":
                        group.remove(invokerName,invokerActor, targetUserName, targetActor);
                        break;
                    case "mute":
                        if(args.length >= 6){
                            long timeInSeconds = Long.parseLong(args[5]);
                            group.mute(invokerName,invokerActor, targetUserName, timeInSeconds);
                        }
                        else{
                            GroupErrorMessage(getSelf(), getSender(), "Mute: Invalid arguments: " + Arrays.toString(args));
                        }
                        break;
                    case "unmute":
                        group.unmute(invokerName,invokerActor, targetUserName);
                        break;
                    default:
                        GroupErrorMessage(getSelf(), getSender(), "Unknown Message");
                }
                break;
            case "coadmin":
                switch (action) {
                    case "remove":
                        group.coadminRemove(invokerName, invokerActor, targetUserName);
                        break;
                    case "add":
                        group.coadminAdd(invokerName, invokerActor, targetUserName);
                        break;
                    default:
                        GroupErrorMessage(getSelf(), getSender(), "Unknown Message");
                }
                break;
            default:
                GroupErrorMessage(getSelf(), getSender(), "Unknown Message");
        }
    }
}
