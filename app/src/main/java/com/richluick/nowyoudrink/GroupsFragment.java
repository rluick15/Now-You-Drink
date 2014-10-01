package com.richluick.nowyoudrink;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Rich on 9/18/2014.
 */
public class GroupsFragment extends android.support.v4.app.ListFragment {

    protected List<ParseObject> mGroups;
    protected ParseRelation<ParseUser> mMemberRelation;
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
        getActivity().setProgressBarIndeterminateVisibility(true);

        mCurrentUser = ParseUser.getCurrentUser();
        //mMemberRelation = mGroup.getRelation(ParseConstants.KEY_MEMBER_RELATION);

//        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_GROUPS);
//        query.whereEqualTo(ParseConstants.KEY_MEMBER_RELATION, ParseUser.getCurrentUser());
//        query.orderByDescending(ParseConstants.KEY_CREATED_AT);
//        query.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> groups, ParseException e) {
//                mGroups = groups;
//                String[] groupnames = new String[mGroups.size()];
//
//                int i = 0;
//                for (ParseObject group : mGroups) {
//                    groupnames[i] = group.getString(ParseConstants.KEY_GROUP_NAME);
//                    i++;
//                }
//
//                if (getListView().getAdapter() == null) {
//                    MessageAdapter adapter = new MessageAdapter(getListView().getContext(), mGroups);
//                    setListAdapter(adapter);
//                }
//                else {
//                    //refill the adapter
//                    ((MessageAdapter) getListView().getAdapter()).refill(mGroups);
//                }
//            }
//        });
    }
}
