package com.richluick.nowyoudrink.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.richluick.nowyoudrink.R;
import com.richluick.nowyoudrink.adapters.MessageAdapter;
import com.richluick.nowyoudrink.ui.activities.GroupActivity;
import com.richluick.nowyoudrink.utils.ParseConstants;
import com.richluick.nowyoudrink.utils.Utilities;

import java.util.List;

public class GroupsFragment extends android.support.v4.app.ListFragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    protected List<ParseObject> mGroups;
    protected ParseRelation<ParseObject> mMemberOfGroupRelation;
    protected ParseUser mCurrentUser;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        Utilities.setContext(getActivity()); //set the utilities context to this

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(
                R.color.swipeRefresh1,
                R.color.swipeRefresh2,
                R.color.swipeRefresh3,
                R.color.swipeRefresh4);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mMemberOfGroupRelation = mCurrentUser.getRelation(ParseConstants.KEY_MEMBER_OF_GROUP_RELATION);

        getActivity().setProgressBarIndeterminateVisibility(true);

        //query all the groups that the user is a member of
        retrieveGroups();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Utilities.setContext(null); //set context to null to prevent leak
    }

    private void retrieveGroups() {
        ParseQuery<ParseObject> query = mMemberOfGroupRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_GROUP_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> groups, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);

                if(mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (e == null) {
                    mGroups = groups;

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
                    Utilities.getErrorAlertDialog();
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

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveGroups();
        }
    };
}
