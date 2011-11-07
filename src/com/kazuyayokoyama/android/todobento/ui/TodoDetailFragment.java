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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kazuyayokoyama.android.todobento.R;
import com.kazuyayokoyama.android.todobento.io.TodoDataManager;
import com.kazuyayokoyama.android.todobento.ui.list.TodoListItem;
import com.kazuyayokoyama.android.todobento.util.UIUtils;

public class TodoDetailFragment extends Fragment {
	public static final String EXTRA_TODO_CHILD_UUID = "com.kazuyayokoyama.android.todobento.extra.EXTRA_TODO_CHILD_UUID";

	private static final String TAG = "TodoDetailFragment";
	private static final int MAX_IMG_WIDTH = 320;
	private static final int MAX_IMG_HEIGHT = 320;

	private TodoDataManager mManager = TodoDataManager.getInstance();
	private String mChildUUID = null;
	private TodoListItem mTodoItem = null;

	private ViewGroup mRootView;
	private EditText mTitle;
	private EditText mDescription;
	private ImageView mImageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// extract UUID from intent
		final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
		mChildUUID = intent.getStringExtra(EXTRA_TODO_CHILD_UUID);

		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mChildUUID == null) {
			return;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_todo_detail, null);

		mTitle = (EditText) mRootView.findViewById(R.id.todo_title);
		mDescription = (EditText) mRootView.findViewById(R.id.todo_description);
		mDescription.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					Log.d(TAG, "onEditorAction");
					// clear focus
					v.clearFocus();

					// close keyboard
					InputMethodManager mgr = (InputMethodManager) getActivity()
							.getSystemService(
									Context.INPUT_METHOD_SERVICE);
					mgr.hideSoftInputFromWindow(
							mDescription.getWindowToken(), 0);

					return true;
				}
				return false;
			}
		});
		mImageView = (ImageView) mRootView.findViewById(R.id.todo_image);

		// retrieve To-do data
		if (mChildUUID != null) {
			mTodoItem = mManager.getItem(mChildUUID);
			if (mTodoItem != null) {
				mTitle.setText(mTodoItem.title);
				mDescription.setText(mTodoItem.description);
				if (mTodoItem.withImg) {
					mImageView.setImageBitmap(mManager.getBitmap(
							mTodoItem.uuid, MAX_IMG_WIDTH, MAX_IMG_HEIGHT, 0));
					mImageView.setVisibility(View.VISIBLE);
				} else {
					mImageView.setVisibility(View.GONE);
				}
			}
		}

		return mRootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.todo_detail_menu_items, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:

			if (mChildUUID != null && mTodoItem != null) {
				String title = mTitle.getText().toString();
				String description = mDescription.getText().toString();

				// update
				mTodoItem.title = title;
				mTodoItem.description = description;
				mTodoItem.modDateMillis = System.currentTimeMillis();
				mTodoItem.modContactId = mManager.getLocalContactId();

				StringBuilder html = new StringBuilder(getResources()
						.getString(R.string.feed_msg_updated));
				mManager.updateTodo(mChildUUID, mTodoItem,
						UIUtils.getUpdateString(getActivity(), html.toString()));
			}

			// go back
			getActivity().finish();

			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
