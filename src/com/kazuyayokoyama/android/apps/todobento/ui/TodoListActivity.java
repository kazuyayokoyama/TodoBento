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

package com.kazuyayokoyama.android.apps.todobento.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;

import com.kazuyayokoyama.android.apps.todobento.R;
import com.kazuyayokoyama.android.apps.todobento.io.BentoManager;
import com.kazuyayokoyama.android.apps.todobento.io.BentoManager.OnStateUpdatedListener;
import com.kazuyayokoyama.android.apps.todobento.ui.TodoListFragment.OnBentoSelectedListener;

public class TodoListActivity extends FragmentActivity implements OnBentoSelectedListener {
	//private static final String TAG = "TodoListActivity";

    private BentoManager mManager = BentoManager.getInstance();
    private TodoListFragment mTodoListFragment;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

		final ActionBar actionBar = getSupportActionBar();
		// set defaults for logo & home up
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setTitle(mManager.getBentoListItem().bento.name.toString());
		
		FragmentManager fm = getSupportFragmentManager();
		mTodoListFragment = (TodoListFragment) fm.findFragmentById(R.id.fragment_todo_list);
		mManager.addListener(mStateUpdatedListener);
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
            finish();
            return true;
        default:
            return mTodoListFragment.onOptionsItemSelected(item);
        }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
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