package server;


import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import com.typesafe.config.ConfigFactory;

class ServerMain {
    public static void main(String[] args) {
        ActorSystem serverActorSystem = ActorSystem.create("ServerActorSystem", ConfigFactory.load("server"));

        ActorRef serverActor = serverActorSystem.actorOf(ServerActor.props(), "ServerActor");

        System.out.println("Server Started");

        serverActorSystem.whenTerminated();
    }
}
