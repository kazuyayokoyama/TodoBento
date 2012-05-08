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

package mobisocial.bento.todo.util;

import mobisocial.bento.todo.R;
import mobisocial.bento.todo.io.BentoManager;
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

public class InitialHelper {
    public interface OnInitCompleteListener {
        public void onInitCompleted();
    }
    
	private Activity mActivity = null;
	private OnInitCompleteListener mListener;
	
	public InitialHelper(final Activity activity, OnInitCompleteListener listener) {
		mActivity = activity;
		mListener = listener;
	}
	
	public Musubi initMusubiInstance() {
		// Check if Musubi is installed
		boolean bInstalled = false;
		try {
			bInstalled = Musubi.isMusubiInstalled(mActivity);
		} catch (Exception e) {
			// be quiet
			bInstalled = false;
		}
		if (!bInstalled) {
			goMusubiOnMarket();
			return null;
		}
		
		// Intent
		Intent intent = mActivity.getIntent();
		// create Musubi Instance
		Musubi musubi = Musubi.forIntent(mActivity, intent);
		// Check if this activity launched from apps feed
		if (musubi == null) {
			// go to market
			goMarket();
			mActivity.finish();
			return null;
		}
		
		// From Musubi
		BentoManager manager = BentoManager.getInstance();
		manager.init(musubi);
		manager.setFromMusubi(Musubi.isMusubiIntent(intent));
		
		// get version code
		int versionCode = 0;
		try {
			PackageInfo packageInfo = mActivity.getPackageManager().getPackageInfo(
					"mobisocial.bento.todo", PackageManager.GET_META_DATA);
			versionCode = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
		    e.printStackTrace();
		}
		
		new TodoListAsyncTask(musubi, mActivity, versionCode).execute();
		return musubi;
	}
	
	public void goMusubiOnMarket() {
		AlertDialog.Builder marketDialog = new AlertDialog.Builder(mActivity)
				.setTitle(R.string.market_dialog_title)
				.setMessage(R.string.market_dialog_text)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setCancelable(true)
				.setPositiveButton(mActivity.getResources().getString(R.string.market_dialog_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Go to Android Market
						mActivity.startActivity(Musubi.getMarketIntent());
						mActivity.finish();
					}
				})
				.setNegativeButton(mActivity.getResources().getString(R.string.market_dialog_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								mActivity.finish();
							}
						});
		marketDialog.create().show();
	}

	public void goMarket() {
		// Go to Market
		Uri uri = Uri.parse("market://details?id=mobisocial.bento.todo");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		mActivity.startActivity(intent);
		mActivity.finish();
	}

	private class TodoListAsyncTask extends AsyncTask<Void, Void, Boolean> {
		private BentoManager mManager = BentoManager.getInstance();
		private Musubi mMusubi;
		private Context mContext;
		private int mVersionCode;
		private ProgressDialog mProgressDialog = null;
		
		public TodoListAsyncTask(Musubi musubi, Context context, int versionCode) {
			mMusubi = musubi;
			mContext = context;
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
			mManager.setMusubi(mMusubi, mVersionCode);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			try {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				
				// callback
				mListener.onInitCompleted();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
