# Whatsapp
Textual-based WhatsApp clone, implemented with Akka tools

```
Ben Gurion university
Advanced Topics in Functional and Reactive Programming
Semester A, 2019-2020 
```

## Assignment 2 - The Actor Model using Akka
```
Published: 25.11.2019
Due: 25.12.2019 23:59
https://www.cs.bgu.ac.il/~majeek/atd/201/assignments/2/
```

In this assignment we will be implementing a textual-based WhatsApp! clone. It will contain most of the features of the application. Make sure to imeplement it in Akka, preferebly using Java.

We will detail below the list of required features that must be implemented. Be sure to think in Actor Model mode, and not object-oriented mode. Make sure to incorporate the different behaviors needed for each Actor in the system.

The application will rely on two main features:

* 1-to-1 chat. In this feature a person using the application will send an addressed message to a specific destination.

* 1-to-many chat. In this feature a person using the application will
send a message addressed to the group itself. This message is broadcast
to all members found in the specific group.
