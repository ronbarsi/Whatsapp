package common.ServerRequest.GroupServerRequests;

import akka.actor.ActorRef;

/**
 * this class is not part of the api:
 *  We will send this request when user is disconnected abd we would like to remove it from groups.
 */

public class GroupUserDelete {
    private final String groupName;
    private ActorRef deleter;

    public GroupUserDelete(ActorRef deleter, String groupName) {
        this.groupName = groupName;
        this.deleter = deleter;
    }

    public String getGroupName() {
        return groupName;
    }
}
