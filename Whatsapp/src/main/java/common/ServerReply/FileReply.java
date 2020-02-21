package common.ServerReply;

import akka.actor.ActorRef;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

public class FileReply extends ServerReply {

    private final String senderName;
    private final boolean isGroup;

    public FileReply(ActorRef server, ActorRef client, byte[] reply, String sender_name, boolean isGroup) {
        super(server, client, reply);
        this.senderName = sender_name;
        this.isGroup = isGroup;
    }

    private String saveFile() {
        /**
         * Save the file and return its path
         */
        if (this.getReply() != null){

            long now = new Date().getTime();
            String fileName = Long.toString(now);

            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                fos.write((byte[]) this.getReply());
            } catch (IOException e) {
                System.out.println("Couldn't write file");
            }

            File f = new File(fileName);
            String absPath = f.getAbsolutePath();
            return absPath;
        }
        else{
            return "Error receiving the file";
        }
    }

    private String getMessage(String filePath, String sourceName) {
        String type = getType();
        String time = new java.text.SimpleDateFormat("HH:mm").format(new Timestamp(System.currentTimeMillis()));
        return "[" + time + "][" + type + "][" + sourceName + "] File received: " + filePath;
    }

    private String getSenderName() {
        return senderName;
    }

    private String getType(){
        if (isGroup){
            return "group";
        }
        return "user";
    }

    @Override
    public void printMessage() {
        String absPath = saveFile();
        String senderName = getSenderName();
        String message = getMessage(absPath, senderName);
        System.out.println(message);
    }
}