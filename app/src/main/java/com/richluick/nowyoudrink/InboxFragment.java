package com.richluick.nowyoudrink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rich on 9/18/2014.
 */
public class InboxFragment extends android.support.v4.app.ListFragment {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected List<ParseObject> mMessages;
    protected List<ParseObject> mMessagesCopy;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected ParseRelation<ParseUser> mPendingRelation;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected String mSenderIdAnswered;
    protected String mGroupSenderIdAnswered;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setProgressBarIndeterminateVisibility(true);

        //check if request was answered and get the senders ID
        mSenderIdAnswered = getActivity().getIntent().getStringExtra(ParseConstants.KEY_SENDER_ID);
        mGroupSenderIdAnswered = getActivity().getIntent().getStringExtra(ParseConstants.KEY_GROUP_SENDER_ID);

        mCurrentUser = ParseUser.getCurrentUser();
        mPendingRelation = mCurrentUser.getRelation(ParseConstants.KEY_PENDING_RELATION);
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        retrieveMessages();
    }

    private void retrieveMessages() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser());
        query.orderByDescending(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);

                if (e == null) { //successfully found messages
                    mMessages = messages;
                    mMessagesCopy = new ArrayList<ParseObject>(messages); //avoid concurrent modification error
                    String[] usernames = new String[mMessages.size()];

                    int i = 0;
                    for (ParseObject message : mMessages) {
                        //Request Rejected: Remove the pending relation and delete the reject
                        //message before it appears
                        if (message.get(ParseConstants.KEY_MESSAGE_TYPE).equals(ParseConstants.TYPE_FRIEND_REQUEST_DENY)) {
                            removeRelation(message);
                            mMessagesCopy.remove(message);
                            message.deleteInBackground();
                        }
                        //Check if request was answered yet and if so delete it
                        else if (message.get(ParseConstants.KEY_MESSAGE_TYPE).equals(ParseConstants.TYPE_FRIEND_REQUEST)
                                && (message.get(ParseConstants.KEY_SENDER_ID)).equals(mSenderIdAnswered)) {

                            mMessagesCopy.remove(message); //remove message from query
                            DeleteMessageUtil.deleteMessage(message);
                        }
                        //Request Accepted, Friend Request not looked at, or drink request
                        else {
                            usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                            addRelation(message);
                            i++;
                        }
                    }

                    mMessages = mMessagesCopy;
                    listAdapter();
                }
            }
        });
    }

    private void listAdapter() {
        if (getListView().getAdapter() == null) {
            MessageAdapter adapter = new MessageAdapter(getListView().getContext(), mMessages);
            setListAdapter(adapter);
        }
        else {
            //refill the adapter
            ((MessageAdapter) getListView().getAdapter()).refill(mMessages);
        }
    }

    private void removeRelation(ParseObject message) {
        mPendingRelation.remove((ParseUser) message.get(ParseConstants.KEY_SENDER));
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void addRelation(ParseObject message) {
        //Add as pending user
        if (message.get(ParseConstants.KEY_MESSAGE_TYPE).equals(ParseConstants.TYPE_FRIEND_REQUEST)) {
            ParseUser user = (ParseUser) message.get(ParseConstants.KEY_SENDER);
            mPendingRelation.add(user);
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }
        //add as friend
        else if (message.get(ParseConstants.KEY_MESSAGE_TYPE).equals(ParseConstants.TYPE_FRIEND_REQUEST_CONFIRM)) {
            ParseUser user = (ParseUser) message.get(ParseConstants.KEY_SENDER);
            mFriendsRelation.add(user);
            mPendingRelation.remove(user);
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_MESSAGE_TYPE);
        String senderId = message.get(ParseConstants.KEY_SENDER_ID).toString();
        String messageId = message.getObjectId();
        String groupId = message.get(ParseConstants.KEY_GROUP_ID).toString();

        if(messageType.equals(ParseConstants.TYPE_FRIEND_REQUEST)) { //view the friend request
            Intent intent = new Intent(getActivity(), ViewFriendRequestActivity.class);
            intent.putExtra(ParseConstants.KEY_SENDER_ID, senderId);
            startActivity(intent);
        }
        else if (messageType.equals(ParseConstants.TYPE_FRIEND_REQUEST_CONFIRM)) {
            Intent intent = new Intent(getActivity(), FriendsProfileActivity.class);
            intent.putExtra(ParseConstants.KEY_ID, senderId);
            startActivity(intent);
            message.deleteInBackground();
        }
        else if (messageType.equals(ParseConstants.TYPE_GROUP_REQUEST)) {
            Intent intent = new Intent(getActivity(), GroupRequestActivity.class);
            intent.putExtra(ParseConstants.KEY_ID, messageId);
            startActivity(intent);
        }
        else { //view the drink request
            Intent intent = new Intent(getActivity(), GroupActivity.class);
            intent.putExtra(ParseConstants.KEY_GROUP_ID, groupId);
            startActivity(intent);
            message.deleteInBackground();
        }

    }


}
