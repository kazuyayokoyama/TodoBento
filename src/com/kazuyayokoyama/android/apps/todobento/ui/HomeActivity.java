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

import mobisocial.socialkit.musubi.Musubi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.kazuyayokoyama.android.apps.todobento.R;
import com.kazuyayokoyama.android.apps.todobento.io.BentoManager;

public class HomeActivity extends FragmentActivity {
	public static final String EXTRA_TODO = "com.kazuyayokoyama.android.apps.todobento.extra.EXTRA_TODO";

	//private static final String TAG = "HomeActivity";
	private static final int REQUEST_TODO_LIST = 0;
	private static final int REQUEST_BENTO_LIST = 1;

	private BentoManager mManager = BentoManager.getInstance();
	private Musubi mMusubi = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Check if this activity launched from Home Screen
		if (!Musubi.isMusubiIntent(getIntent())) {
			boolean bInstalled = false;
			try {
				bInstalled = Musubi.isMusubiInstalled(getApplication());
			} catch (Exception e) {
				// be quiet
				bInstalled = false;
			}
			// Check if Musubi is installed
			if (bInstalled) {
				goMusubi();
			} else {
				goMarket();
			}
		} else {
	
			// Check if this activity launched from internal activity
			if (getIntent().hasExtra(EXTRA_TODO)) {
				// nothing to do for Musubi
				return;
			}
			
			// create Musubi Instance
			Intent intent = getIntent();
			mMusubi = Musubi.getInstance(this);
			
			// get version code
			int versionCode = 0;
			try {
				PackageInfo packageInfo = getPackageManager().getPackageInfo(
						"com.kazuyayokoyama.android.apps.todobento", PackageManager.GET_META_DATA);
				versionCode = packageInfo.versionCode;
			} catch (NameNotFoundException e) {
			    e.printStackTrace();
			}
			
			new TodoListAsyncTask(this, (Uri) intent.getParcelableExtra(Musubi.EXTRA_FEED_URI), versionCode).execute();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TODO_LIST) {
			mManager.fin();
			finish();
		} else if (requestCode == REQUEST_BENTO_LIST) {
			if (resultCode == Activity.RESULT_OK) {
				goTodoList();
			} else {
				mManager.fin();
				finish();
			}
		}
	}
    
    private void goBentoList() {
		// Intent
		Intent intent = new Intent(this, BentoListActivity.class);
		startActivityForResult(intent, REQUEST_BENTO_LIST);
    }
    
    private void goTodoList() {
		// Intent
		Intent intent = new Intent(this, TodoListActivity.class);
		startActivityForResult(intent, REQUEST_TODO_LIST);
    }
    
	private void goMarket() {
		AlertDialog.Builder marketDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.market_dialog_title)
				.setMessage(R.string.market_dialog_text)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setCancelable(true)
				.setPositiveButton(getResources().getString(R.string.market_dialog_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Go to Android Market
						startActivity(Musubi.getMarketIntent());
						finish();
					}
				})
				.setNegativeButton(getResources().getString(R.string.market_dialog_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								finish();
							}
						});
		marketDialog.create().show();
	}
    
	private void goMusubi() {
		AlertDialog.Builder musubiDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.musubi_dialog_title)
				.setMessage(R.string.musubi_dialog_text)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setCancelable(true)
				.setPositiveButton(getResources().getString(R.string.musubi_dialog_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							// Launching Musubi
			                Intent intent = new Intent(Intent.ACTION_MAIN);
			                intent.setClassName("edu.stanford.mobisocial.dungbeetle", "edu.stanford.mobisocial.dungbeetle.ui.FeedListActivity"); 
			                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			                startActivity(intent);
			                finish();
						} catch (Exception e) {
							goMarket();
						}
					}
				})
				.setNegativeButton(getResources().getString(R.string.musubi_dialog_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								finish();
							}
						});
		musubiDialog.create().show();
	}

	private class TodoListAsyncTask extends AsyncTask<Void, Void, Boolean> {
		private BentoManager mManager = BentoManager.getInstance();
		private Context mContext;
		private Uri mUri;
		private int mVersionCode;
		private ProgressDialog mProgressDialog = null;
		
		public TodoListAsyncTask(Context context, Uri uri, int versionCode) {
			mContext = context;
			mUri = uri;
			mVersionCode = versionCode;
		}

		@Override
		protected void onPreExecute() {
			// show progress dialog
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setMessage(mContext.getString(R.string.todo_list_loading));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			mManager.init(mMusubi, mUri, mVersionCode);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			try {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				
				// Has Bento
				if (mManager.hasBento()) {
					goTodoList();
				} else {
					goBentoList();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}