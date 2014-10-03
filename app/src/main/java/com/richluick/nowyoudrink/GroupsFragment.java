package com.richluick.nowyoudrink;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

/**
 * Created by Rich on 9/18/2014.
 */
public class GroupsFragment extends android.support.v4.app.ListFragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    protected List<ParseObject> mGroups;
    protected ParseRelation<ParseObject> mMemberOfGroupRelation;
    protected ParseUser mCurrentUser;
    protected ParseObject mGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mMemberOfGroupRelation = mCurrentUser.getRelation(ParseConstants.KEY_MEMBER_OF_GROUP_RELATION);

        getActivity().setProgressBarIndeterminateVisibility(true);

        //query all the groups that the user is a member of
        ParseQuery<ParseObject> query = mMemberOfGroupRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_GROUP_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> groups, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    mGroups = groups;
                    String[] groupnames = new String[mGroups.size()];

                    //extrat usernames from all the groups
                    int i = 0;
                    for (ParseObject group : mGroups) {
                        String groupName = group.get(ParseConstants.KEY_GROUP_NAME).toString();
                        groupnames[i] = MainActivity.removeCharacters(groupName);
                        i++;
                    }

                    if (getListView().getAdapter() == null) {
                        MessageAdapter adapter = new MessageAdapter(getListView().getContext(), mGroups);
                        setListAdapter(adapter);
                    }
                    else {
                        //refill the adapter
                        ((MessageAdapter) getListView().getAdapter()).refill(mGroups);
                    }
                }
                else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject group = mGroups.get(position);
        String groupId = group.getObjectId();

        Intent intent = new Intent(getActivity(), GroupActivity.class);
        intent.putExtra(ParseConstants.KEY_GROUP_ID, groupId);
        startActivity(intent);
    }
}
