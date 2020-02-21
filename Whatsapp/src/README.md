# Assignment 2 - The Actor Model using Akka

### Written by
Gad Elbaz -  204362248

Ron Barsimantov -  203574082

##

## Documantation
The actors implementation can be found under "server" and "client" packages.
Actors hierarchy (and more information) is described in Actors.pdf file attached.
https://www.lucidchart.com/invitations/accept/bf0457af-1ed9-47c0-8a53-967b7d123cf6

The "common" code package, contains mostly objects that are sent as messages between the actors.
More details about the messages in Common.pdf file attached 
https://www.lucidchart.com/invitations/accept/f94d36fd-7c95-44a8-bc3c-43301585813f

## Generally:

### Server Side:

#### server\ServerMain.java: 
    - Starts server's actor.

#### server\ServerActor.java:
    - Handles server requests
    - Forward groups related requests to GroupServerActor
    - Manage Login and Logout requests

#### server\GroupServerActor.java:
    - Handles groups related server requests
    - Manage Group invitations, Group creation & destruction, Group admin's actions, and more...

##
### Client Side:

#### client\ClientMain.java: 
    - Starts client actor (connect to the server).
    - Handle User's commands: parses & sends suitable massages
    - Handles files & text messages

#### client\ClientMain2.java: 
    - A copy of ClientMain, bot configured to other port then ClientMain (so we will be able to run 2 clients on the same machine, just for convenient!)

#### client\ClientActor.java:
    - Handles client's messages
    - Handles server's replies

##
### Common code:

#### common\Group:
    - Group data structure which holds & manage all groups functionalitie
    - Group invitation & accepted invatation messages
    
#### common\ServerReply:
    - File replies from server 
    - Text messages replies from server 

#### common\ServerRequest:
    - Client2Client messages (text or files)
    - Client2Server messages (login or logout)
    - Group messages (Delete, Disconnect) 
