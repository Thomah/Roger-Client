package fr.thomah.roger;

public class Message {
    private String clientMsgId;
    private String userId;
    private String text;
    private String ts;

    String getClientMsgId() {
        return clientMsgId;
    }

    void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    String getTs() {
        return ts;
    }

    void setTs(String ts) {
        this.ts = ts;
    }

    String getUserId() {
        return userId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "clientMsgId='" + clientMsgId + '\'' +
                ", ts=" + ts +
                ", userId='" + userId + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
