/*
 * Copyright (C) 2012 Kazuya (Kaz) Yokoyama <kazuya.yokoyama@gmail.com>
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

package mobisocial.bento.todo.ui;

import mobisocial.bento.todo.io.BentoManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import mobisocial.bento.todo.R;

public class BentoListFragment extends ListFragment {
    //private static final String TAG = "BentoListFragment";

    private BentoManager mManager = BentoManager.getInstance();
	private BentoListItemAdapter mListAdapter = null;
	private ListView mListView = null;
	private View mRootList = null;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
		mRootList = inflater.inflate(R.layout.fragment_bento_list, container, false);
        
        // ListView
		mListView = (ListView) mRootList.findViewById(android.R.id.list);
        mListView.setFastScrollEnabled(true);
        
        return mRootList;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);

        // Create adapter
        mListAdapter = new BentoListItemAdapter(
        		getActivity(), 
        		android.R.layout.simple_list_item_1,
        		mListView);
        setListAdapter(mListAdapter);
        
        // refresh
        refreshView();
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		BentoListItem item = mManager.getBentoListItem(position);
		mManager.setDbFeed(item.feedUri);
		
		// Intent
		Intent intent = new Intent(getActivity(), TodoListActivity.class);
		intent.putExtra(TodoListActivity.EXTRA_LAUNCHED_FROM_BENTO_LIST, true);
		startActivity(intent);
	}

	public void refreshView() {
		// Load Bento List
		BentoListAsyncTask task = new BentoListAsyncTask(mListAdapter, mListView, mRootList);
		task.execute();
    }
	
	private class BentoListAsyncTask extends AsyncTask<Void, Void, Boolean> {
		private BentoManager mManager = BentoManager.getInstance();
		private BentoListItemAdapter mListAdapter;
		private ListView mListView;
		private TextView mEmptyText;
		private ProgressBar mProgressBar;
		
		public BentoListAsyncTask(BentoListItemAdapter listAdapter, ListView listView, View listRoot) {
			mListAdapter = listAdapter;
			mListView = listView;
			
			mEmptyText = (TextView) listRoot.findViewById(R.id.empty_message);
			mProgressBar = (ProgressBar) listRoot.findViewById(R.id.progress);
		}

		@Override
		protected void onPreExecute() {
			mEmptyText.setVisibility(View.GONE);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			mManager.loadBentoList();
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mEmptyText.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			
			mListAdapter.notifyDataSetChanged();
			mListView.invalidateViews();
		}
	}
}
