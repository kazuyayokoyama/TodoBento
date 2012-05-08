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

import mobisocial.bento.todo.R;
import mobisocial.bento.todo.io.BentoManager;
import mobisocial.bento.todo.io.BentoManager.OnStateUpdatedListener;
import mobisocial.bento.todo.ui.TodoListFragment.OnBentoSelectedListener;
import mobisocial.bento.todo.util.InitialHelper;
import mobisocial.bento.todo.util.InitialHelper.OnInitCompleteListener;
import mobisocial.socialkit.musubi.Musubi;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;

public class TodoListActivity extends FragmentActivity implements OnBentoSelectedListener {
	public static final String EXTRA_LAUNCHED_FROM_BENTO_LIST = "launched_from_bento_list";
	
	//private static final String TAG = "TodoListActivity";

    private BentoManager mManager = BentoManager.getInstance();
    private TodoListFragment mTodoListFragment;
    private boolean mLaunchedFromBentoList = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLaunchedFromBentoList = getIntent().hasExtra(EXTRA_LAUNCHED_FROM_BENTO_LIST);

        if (!mLaunchedFromBentoList) {
			// create Musubi Instance
	        InitialHelper initHelper = new InitialHelper(this, mInitCompleteListener);
			Musubi musubi = initHelper.initMusubiInstance();
			if (musubi == null) {
				return;
			}
        }
        
        setContentView(R.layout.activity_todo_list);

		final ActionBar actionBar = getSupportActionBar();
		// set defaults for logo & home up
		actionBar.setDisplayHomeAsUpEnabled(true); // bad know-how for enabling home clickable on ICS.
		actionBar.setDisplayHomeAsUpEnabled(mLaunchedFromBentoList);
		actionBar.setDisplayUseLogoEnabled(false);
		if (mLaunchedFromBentoList) {
			actionBar.setTitle(mManager.getBentoListItem().bento.name.toString());
		}
		
		FragmentManager fm = getSupportFragmentManager();
		mTodoListFragment = (TodoListFragment) fm.findFragmentById(R.id.fragment_todo_list);
		mManager.addListener(mStateUpdatedListener);
		
		// loading
		if (!mLaunchedFromBentoList) {
			mTodoListFragment.setProgressBarVisible(true);
		}
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mManager.removeListener(mStateUpdatedListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_items_todo_list, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	if (mLaunchedFromBentoList) {
        		finish();
        		return true;
        	} else {
        		return mTodoListFragment.onOptionsItemSelected(item);
        	}
        default:
            return mTodoListFragment.onOptionsItemSelected(item);
        }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	// InitialHelper > OnInitCompleteListener
	private OnInitCompleteListener mInitCompleteListener = new OnInitCompleteListener() {
		@Override
		public void onInitCompleted() {
			onBentoSelected();
			mTodoListFragment.setProgressBarVisible(false);
		}
	};
	
	// TodoListFragment > OnBentoSelectedListener
	@Override
	public void onBentoSelected() {
		getSupportActionBar().setTitle(mManager.getBentoListItem().bento.name.toString());
		this.invalidateOptionsMenu();
		mTodoListFragment.refreshView();
	}
	
	// BentoManager > OnStateUpdatedListener
	private OnStateUpdatedListener mStateUpdatedListener = new OnStateUpdatedListener() {
		@Override
		public void onStateUpdated() {
			mTodoListFragment.refreshView();
		}
	};
}