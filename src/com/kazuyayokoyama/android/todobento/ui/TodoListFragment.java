/*
 * Copyright (C) 2011 Kazuya Yokoyama <kazuya.yokoyama@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kazuyayokoyama.android.todobento.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kazuyayokoyama.android.todobento.R;
import com.kazuyayokoyama.android.todobento.io.TodoDataManager;
import com.kazuyayokoyama.android.todobento.ui.list.TodoListItemAdapter;
import com.kazuyayokoyama.android.todobento.ui.widget.SortableListView;
import com.kazuyayokoyama.android.todobento.util.UIUtils;

public class TodoListFragment extends ListFragment {
    private static final String TAG = "TodoListFragment";
    
	private TodoDataManager mManager = TodoDataManager.getInstance();
	private TodoListItemAdapter mListAdapter = null;
	private SortableListView mListView = null;
	private boolean mSorted = false;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
        View root = inflater.inflate(R.layout.fragment_todolist, container, false);
        
        // ListView
		mListView = (SortableListView) root.findViewById(android.R.id.list);
        mListView.setDragListener(new DragListener());
        mListView.setSortable(true);
        mListView.setFastScrollEnabled(true);
		
        return root;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
        // Create adapter
        mListAdapter = new TodoListItemAdapter(
        		getActivity(), 
        		android.R.layout.simple_list_item_1,
        		mListView);
        setListAdapter(mListAdapter);
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		// Intent
		Intent intent = new Intent(getActivity(), TodoDetailActivity.class);
		final String uuid = mManager.getItem("0", position).uuid;
		intent.putExtra(TodoDetailFragment.EXTRA_TODO_CHILD_UUID, uuid);
		intent.putExtra(HomeActivity.EXTRA_TODO, HomeActivity.EXTRA_TODO);

        ((BaseActivity) getActivity()).openActivityOrFragment(intent);
	}

	public void refresh() {
		Handler handler = new Handler();
		handler.post(new Runnable(){
			public void run(){
				Log.e(TAG, "refresh");
				mListAdapter.notifyDataSetChanged();
				mListView.invalidateViews();
			}
		});
    }
	
	private void sortCompleted() {
		// Musubi
		final CharSequence msg = getString(R.string.feed_msg_sorted);
        StringBuilder html = new StringBuilder(msg);
        mManager.pushUpdate(UIUtils.getUpdateString(mListView.getContext(), html.toString()));
	}
	
	class DragListener extends SortableListView.SimpleDragListener {
        @Override
        public int onStartDrag(int position) {
        	mSorted = false;
            mListAdapter.setDraggingPosition(position);
            refresh();
            return position;
        }
        
        @Override
        public int onDuringDrag(int positionFrom, int positionTo) {
        	Log.d(TAG, "From:" + positionFrom + " To:" + positionTo);
            if (positionFrom < 0 || positionTo < 0
                    || positionFrom == positionTo) {
                return positionFrom;
            }
            
            // sort
            mManager.sort(null, positionFrom, positionTo);
            mSorted = true;

            mListAdapter.setDraggingPosition(positionTo);
            refresh();
            return positionTo;
        }
        
        @Override
        public boolean onStopDrag(int positionFrom, int positionTo) {
            mListAdapter.setDraggingPosition(-1);
            refresh();

            if (mSorted) {
            	sortCompleted();
            }
            mSorted = false;
	        
            return super.onStopDrag(positionFrom, positionTo);
        }
    }
}
