package fr.thomah.roger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Db {

    private Connection c;

    Db() {
        init();
    }

    private void init() {
        Statement stmt;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/roger",
                            "roger", "roger");

            stmt = c.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS MESSAGES " +
                    "(CLIENT_MSG_ID VARCHAR(36) PRIMARY KEY NOT NULL," +
                    " TS VARCHAR(20) NOT NULL," +
                    " USER_ID VARCHAR(9) NOT NULL," +
                    " TEXT TEXT NOT NULL);");
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }

    public List<Message> syncMessages(JsonArray messagesInSlack) {
        Statement stmt;
        List<Message> messagesInDb = new ArrayList<>();
        List<Message> messagesToAddInDb = new ArrayList<>();
        Message messageInDb, messageInSlackExported;
        try {

            // Get messages in Database
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM MESSAGES;" );
            while ( rs.next() ) {
                messageInDb = new Message();
                messageInDb.setClientMsgId(rs.getString("CLIENT_MSG_ID"));
                messageInDb.setTs(rs.getString("TS"));
                messageInDb.setUserId(rs.getString("USER_ID"));
                messageInDb.setText(rs.getString("TEXT"));
                messagesInDb.add(messageInDb);
            }
            rs.close();
            stmt.close();

            // Iterate through messages in SlackClient
            JsonObject messageInSlack;
            for (JsonElement messageInSlackTmp : messagesInSlack) {
                messageInSlack = messageInSlackTmp.getAsJsonObject();
                messageInSlackExported = new Message();
                JsonElement userJson = messageInSlack.get("user");
                JsonElement clientMsgIdJson = messageInSlack.get("client_msg_id");
                if(userJson != null && clientMsgIdJson != null) {
                    messageInSlackExported.setUserId(userJson.getAsString());
                    messageInSlackExported.setText(messageInSlack.get("text").getAsString());
                    messageInSlackExported.setClientMsgId(clientMsgIdJson.getAsString());
                    messageInSlackExported.setTs(messageInSlack.get("ts").getAsString());

                    // Test if message is already in Database
                    Message finalMessageInSlackExported = messageInSlackExported;
                    messageInDb = messagesInDb.stream()
                            .filter(message -> finalMessageInSlackExported.getClientMsgId().equals(message.getClientMsgId()))
                            .findAny()
                            .orElse(null);
                    if(messageInDb == null) {
                        messagesToAddInDb.add(messageInSlackExported);
                    }
                }
            }

            // Insert all messages non-existing in Database
            stmt = c.createStatement();
            String sql;
            for(Message m : messagesToAddInDb) {
                sql = "INSERT INTO MESSAGES (CLIENT_MSG_ID,TS,USER_ID,TEXT) "
                        + "VALUES ('" + m.getClientMsgId() + "', '"
                        + m.getTs() + "', '"
                        + m.getUserId() + "', '"
                        + m.getText().replaceAll("'", "") + "');";
                System.out.println(sql);
                stmt.executeUpdate(sql);
            }
            stmt.close();
            c.commit();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }
        return messagesToAddInDb;
    }

}
