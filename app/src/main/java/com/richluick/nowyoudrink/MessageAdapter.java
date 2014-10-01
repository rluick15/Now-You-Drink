package com.richluick.nowyoudrink;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

/**
 * Created by Rich on 9/5/2014.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mMessages;
    protected List<ParseObject> mGroups;

    public MessageAdapter(Context context, List<ParseObject> messages) {
        super(context, R.layout.message_item, messages);

        mContext = context;
        mMessages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.messageIcon);
            holder.textLabel = (TextView) convertView.findViewById(R.id.senderLabel);
            holder.timeLabel = (TextView) convertView.findViewById(R.id.timeLabel);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        ParseObject message = mMessages.get(position);

        if(message.getString(ParseConstants.KEY_MESSAGE_TYPE).equals(ParseConstants.TYPE_FRIEND_REQUEST)) {
            //holder.iconImageView.setImageResource(R.drawable.ic_picture);
            holder.textLabel.setText("You have a friend request!");
        }
        else if(message.getString(ParseConstants.KEY_MESSAGE_TYPE).equals(ParseConstants.TYPE_FRIEND_REQUEST_CONFIRM)) {
            holder.textLabel.setText(message.get(ParseConstants.KEY_SENDER_NAME)
                    + " has accepted your friend request!");
        }
        else if(message.getString(ParseConstants.KEY_MESSAGE_TYPE).equals(ParseConstants.TYPE_GROUP_REQUEST)) {
            holder.textLabel.setText("You have a group invite from "
                    + message.get(ParseConstants.KEY_SENDER_NAME) + "!");
        }
        else {
            //holder.iconImageView.setImageResource(R.drawable.ic_video);
            holder.textLabel.setText("Now You Drink!");
        }

        String convertedDate = formatDate(message);

        holder.timeLabel.setText(convertedDate);

        return convertView;
    }

    //Formats the date into time ago vs exact time
    private String formatDate(ParseObject message) {
        Date createdAt = message.getCreatedAt();
        long now = new Date().getTime();
        String convertedDate = DateUtils.getRelativeTimeSpanString(createdAt.getTime(),
            now,
            DateUtils.SECOND_IN_MILLIS).toString();
        return convertedDate;
    }

    private static class ViewHolder {
        ImageView iconImageView;
        TextView textLabel;
        TextView timeLabel;
    }

    public void refill(List<ParseObject> messages) {
        mMessages.clear();
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }

}
