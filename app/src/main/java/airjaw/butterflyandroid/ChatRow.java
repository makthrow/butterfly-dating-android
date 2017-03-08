package airjaw.butterflyandroid;

import android.widget.ImageView;

public class ChatRow {
    private ChatsMeta chatMetaObj;
    private String profilePicURL;

    public ChatRow() {
    }

    public ChatRow(ChatsMeta chatMetaObj, String profilePicURL) {
        this.chatMetaObj = chatMetaObj;
        this.profilePicURL = profilePicURL;
    }

    public ChatsMeta getChatMetaObj() {
        return chatMetaObj;
    }

    public void setChatMetaObj(ChatsMeta chatMetaObj) {
        this.chatMetaObj = chatMetaObj;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }
}