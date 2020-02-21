package client;

import com.typesafe.config.ConfigFactory;

import common.ServerReply.StringReply;
import common.ServerRequest.ClientToClientRequests.FileMessageRequest;
import common.ServerRequest.ClientToClientRequests.TextMessageRequest;
import common.ServerRequest.ClientToServerRequests.LoginRequest;
import common.ServerRequest.ClientToServerRequests.LogoutRequest;
import common.ServerRequest.GroupServerRequests.GroupServerRequest;
import common.ServerRequest.ServerRequest;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import scala.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ClientMain {
    //  The ActorSystem is a root actor in actors structure.
//  An ActorSystem is a hierarchical group of actors which share common configuration
    private static final String serverAddress = "akka://ServerActorSystem@127.0.0.1:6666/user/ServerActor";

    private static final ActorSystem ActorSystemFrontEnd = ActorSystem.create("client", ConfigFactory.load("client"));

    private static ActorRef clientActor = ActorSystemFrontEnd.actorOf(ClientActor.props("client"), "client");
    private static final ActorSelection server = ActorSystemFrontEnd.actorSelection(serverAddress);
    private static String userName = "";
    private static ServerRequest request;
    private static boolean isConnected = false;

    public static void userHandler(String[] args_list){

        switch (args_list[1]) {
            case "connect":
                userName = args_list[2];
                clientActor = ActorSystemFrontEnd.actorOf(ClientActor.props(userName), userName);
                request = new LoginRequest(clientActor, args_list[2], server);
                final Timeout to = new Timeout(Duration.create(10, SECONDS));
                Future<Object> future = Patterns.ask(server, request, to);

                try {
                    StringReply result = (StringReply) Await.result(future, to.duration());
                    result.printMessage();
                    if (!((String)(result.getReply())).substring(0,12).equals("Server Error")){
                        isConnected = true;
                    }
                } catch (Exception e) {
                    System.out.println("Server is offline!");
                    clientActor.tell(akka.actor.PoisonPill.getInstance(), ActorRef.noSender());
                }
                break;

            case "disconnect":
                //clientActor = ActorSystemFrontEnd.actorOf(ClientActor.props(userName), userName);
                request = new LogoutRequest(clientActor, args_list[2], server);
                final Timeout disconnectTO = new Timeout(Duration.create(10, SECONDS));
                Future<Object> disconnectFuture = Patterns.ask(server, request, disconnectTO);
                try{
                    StringReply result = (StringReply) Await.result(disconnectFuture, disconnectTO.duration());
                    result.printMessage();
                    clientActor.tell(akka.actor.PoisonPill.getInstance(), ActorRef.noSender());
                    isConnected = false;
                    clientActor = null;
                } catch (Exception e){
                    System.out.println("Server is offline!");
                }
                break;

            case "text":
                String msg = String.join(" ", Arrays.copyOfRange(args_list, 3, args_list.length));
                new TextMessageRequest(clientActor, userName, server, args_list[2], msg).tellServer();
                break;

            case "file":
                String pwd = new File("").getAbsolutePath() + "\\";
                String filePath = pwd.concat(args_list[3]);
                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println(args_list[3] + " doesn't exist!");
                    break;
                } else {
                    new FileMessageRequest(clientActor, userName, server, args_list[2], getBytes(file)).tellServer();
                }
                break;

            default:
                System.out.println("Invalid Command arguments");
        }
    }

    public static void groupHandler(String[] args_list) {
        request = new GroupServerRequest(clientActor, userName, server, args_list);
        if (args_list[2].equals("file")) {
            String pwd =  new File("").getAbsolutePath() + "\\";
            String filePath = pwd.concat(args_list[4]);
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println(args_list[4] + " doesn't exist!");
                return;
            } else {
                try{
                    ((GroupServerRequest) request).setFileBytes(getBytes(file));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        request.tellServer();
    }

    private static boolean isConnected(String[] args){
        return !(!isConnected && args.length >=2 &&!(args[0].equals("/user") && args[1].equals("connect")));

    }

    private static byte[] getBytes(File file) {
        int len = (int) file.length();
        byte[] data = new byte[len];
        readData(data, file);
        return data;
    }

    private static void readData(byte[] data, File file) {
        FileInputStream stream;
        try {
            stream = new FileInputStream(file);
            stream.read(data);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ActorSystemFrontEnd.whenTerminated();
        String[] args_list;
        Scanner input = new Scanner(System.in);

        System.out.println("Welcome to Gad & Ron Chat app!");
        System.out.println("Have Fun!!\n");

        while (input.hasNextLine()) {
            args_list = input.nextLine().split("\\s+");

            // Connectivity check:
            if (!isConnected(args_list)){
                System.out.println("First, connect to the server by the connect command.");
                continue;
            }

            switch(args_list[0]){
                case "/user":
                    userHandler(args_list);
                    break;

                case "/group":
                    groupHandler(args_list);
                    break;
                case "yes":
                case "no":
                    break;
                default:
                    System.out.println("Invalid Command.");
            }
        }
    }
}
