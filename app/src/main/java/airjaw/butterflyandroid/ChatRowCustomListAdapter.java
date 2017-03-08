package airjaw.butterflyandroid;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import airjaw.butterflyandroid.app.AppController;

public class ChatRowCustomListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<ChatRow> chatRowItems;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public ChatRowCustomListAdapter(Activity activity, List<ChatRow> items) {
        this.activity = activity;
        this.chatRowItems = items;
    }

    @Override
    public int getCount() {
        return chatRowItems.size();
    }

    @Override
    public Object getItem(int position) {
        return chatRowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.activity_chat_listview, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        NetworkImageView thumbNail = (NetworkImageView) convertView.findViewById(R.id.chat_fb_pic_thumbnail);
        TextView chatTitle = (TextView) convertView.findViewById(R.id.chat_title);
        TextView chatLastMessage = (TextView) convertView.findViewById(R.id.chat_last_message);

        ChatRow c = chatRowItems.get(position);
        ChatsMeta chatMetaObj = c.getChatMetaObj();

        String lastMessage = chatMetaObj.getLastMessage();
        String lastSenderID = chatMetaObj.getLastSender();
        String withUserName = chatMetaObj.getWithUserName();

        // create the message to display here
        String cellLastMessageText = lastMessage;

        if (lastSenderID.equals("none")) {
            // don't need to change last message
        }
        else { // already matched
            if (chatMetaObj.isUnread()) {
                if (!lastSenderID.equals(Constants.userID)){
                    cellLastMessageText = withUserName + ": " + lastMessage;
                }
            }
            else {
                if (lastSenderID.equals(Constants.userID)) {
                    cellLastMessageText = "You: " + lastMessage;

                }
                else {
                    cellLastMessageText= withUserName + ": " + lastMessage;
                }
            }
        }

        thumbNail.setImageUrl(c.getProfilePicURL(), imageLoader);
        chatTitle.setText(withUserName);
        chatLastMessage.setText(cellLastMessageText);

        return convertView;
    }

}