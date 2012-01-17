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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.FeedObserver;
import mobisocial.socialkit.musubi.Musubi;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.kazuyayokoyama.android.todobento.R;
import com.kazuyayokoyama.android.todobento.io.TodoDataManager;
import com.kazuyayokoyama.android.todobento.ui.list.TodoListItem;
import com.kazuyayokoyama.android.todobento.ui.quickaction.ActionItem;
import com.kazuyayokoyama.android.todobento.ui.quickaction.QuickAction;
import com.kazuyayokoyama.android.todobento.util.BitmapHelper;
import com.kazuyayokoyama.android.todobento.util.ImageCache;
import com.kazuyayokoyama.android.todobento.util.JpgFileHelper;
import com.kazuyayokoyama.android.todobento.util.UIUtils;

public class HomeActivity extends BaseActivity {
	public static final String EXTRA_TODO = "com.kazuyayokoyama.android.todobento.extra.EXTRA_TODO";

	private static final String TAG = "HomeActivity";
	private static final int REQUEST_IMAGE_CAPTURE = 0;
	private static final int REQUEST_GALLERY = 1;

	private TodoDataManager mManager = TodoDataManager.getInstance();
	private TodoListFragment mTodoListFragment = null;
	private Musubi mMusubi = null;
	private QuickAction mQuickAction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_todolist);
		getActivityHelper().setupActionBar(null, 0);
		getActivityHelper().setActionBarColor(getResources().getColor(R.color.actionbar_text));

		FragmentManager fm = getSupportFragmentManager();
		mTodoListFragment = (TodoListFragment) fm.findFragmentById(R.id.fragment_todolist);

		// Quick Action
		ActionItem cameraItem = new ActionItem(REQUEST_IMAGE_CAPTURE,
				getResources().getString(R.string.description_camera),
				getResources().getDrawable(R.drawable.ic_menu_camera));
		ActionItem galleryItem = new ActionItem(REQUEST_GALLERY, 
				getResources().getString(R.string.description_gallery), 
				getResources().getDrawable(R.drawable.ic_menu_gallery));

		mQuickAction = new QuickAction(this);
		mQuickAction.addActionItem(cameraItem);
		mQuickAction.addActionItem(galleryItem);
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(QuickAction quickAction, int pos, int actionId) {
				if (actionId == REQUEST_IMAGE_CAPTURE) {
					goCamera();
				} else {
					goGallery();
				}
			}
		});
		mQuickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
			@Override
			public void onDismiss() {
			}
		});

		// Check if this activity launched from internal activity
		if (getIntent().hasExtra(EXTRA_TODO)) {
			// nothing to do for Musubi
			return;
		}

		// Check if this activity launched from Home Screen
		if (!Musubi.isMusubiIntent(getIntent())) {
			Toast.makeText(this, R.string.warning_launch, Toast.LENGTH_SHORT).show();
			finish();
			return;
		} else {
			// create Musubi Instance
			Intent intent = getIntent();
			mMusubi = Musubi.getInstance(this, intent);
			String selection = "type = ?";
			String[] selectionArgs = new String[] { TodoDataManager.TYPE_APP_STATE };
			mMusubi.getFeed().setSelection(selection, selectionArgs);
			mMusubi.getFeed().registerStateObserver(mStateObserver);

			mManager.init(mMusubi, (Uri) intent.getParcelableExtra(Musubi.EXTRA_FEED_URI));
		}
	}

	@Override
	protected void onDestroy() {
		ImageCache.clearCache();
		if (mMusubi != null) {
			mMusubi.getFeed().removeStateObserver(mStateObserver);
		}
		// TODO mManager.fin();
		super.onDestroy();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		//Log.d(TAG, "onPostCreate");
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupHomeActivity();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home_menu_items, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_add) {
			goAdd();
			return true;
		} else if (item.getItemId() == R.id.menu_picture) {
			goPicture();
			return true;
		} else if (item.getItemId() == R.id.menu_clear) {
			goClear();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void goAdd() {
		// Show Add dialog
		LayoutInflater factory = LayoutInflater.from(this);
		final View inputView = factory.inflate(R.layout.dialog_addtodo, null);

		// EditText
		final EditText editTextTodoTitle = (EditText) inputView.findViewById(R.id.edittext_todo_title);

		AlertDialog.Builder addTodoDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.add_dialog_title)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(inputView)
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// fetch new Todo item
						String todoTitle = editTextTodoTitle.getText()
								.toString();

						// check string length
						if (todoTitle.length() > 0) {
							TodoListItem item = new TodoListItem();
							item.uuid = UUID.randomUUID().toString();
							item.title = todoTitle;
							item.bDone = false;
							item.description = "";
							item.withImg = false;
							item.creDateMillis = System.currentTimeMillis();
							item.modDateMillis = System.currentTimeMillis();
							item.creContactId = mManager.getLocalContactId();
							item.modContactId = mManager.getLocalContactId();
							// add
							final CharSequence msg = getString(
									R.string.feed_msg_added, item.title);
							StringBuilder html = new StringBuilder(msg);
							mManager.addTodo(null, item, null, UIUtils.getUpdateString(
									HomeActivity.this, html.toString()));
							// refresh list view
							mTodoListFragment.refresh();
						}
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// nothing to do (dismiss dialog)
							}
						});
		final AlertDialog dialog = addTodoDialog.create();
		editTextTodoTitle.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					dialog.getWindow().setSoftInputMode(
									WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});
		dialog.show();
	}

	public void goPicture() {
		// TODO need to find out way to specify child by id
		if (getActivityHelper().getActionBarCompat() != null) {
			mQuickAction.show(getActivityHelper().getActionBarCompat().getChildAt(4));
		} else {
			mQuickAction.show(this.getWindow().getDecorView());
		}
		mQuickAction.setAnimStyle(QuickAction.ANIM_GROW_FROM_CENTER);
	}

	public void goCamera() {
		File tmpFile = JpgFileHelper.getTmpFile();
		Intent intent = new Intent();
		intent.setAction("android.media.action.IMAGE_CAPTURE");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	public void goGallery() {
		File tmpFile = JpgFileHelper.getTmpFile();
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
		startActivityForResult(intent, REQUEST_GALLERY);
	}

	public void goClear() {
		AlertDialog.Builder clearTodoDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.clear_dialog_title)
				.setMessage(R.string.clear_dialog_text)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// clear
						final CharSequence msg = getString(R.string.feed_msg_cleared);
						StringBuilder html = new StringBuilder(msg);
						mManager.clearTodoDone(
								null,
								UIUtils.getUpdateString(HomeActivity.this, html.toString()));
						ImageCache.clearCache();
						// refresh list view
						mTodoListFragment.refresh();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// nothing to do (dismiss dialog)
							}
						});
		clearTodoDialog.create().show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			try {
				// uncomment below if you want to get small image
				// Bitmap b = (Bitmap)data.getExtras().get("data");

				File tmpFile = JpgFileHelper.getTmpFile();
				if (tmpFile.exists() && tmpFile.length() > 0) {
					float degrees = 0;
					try {
						ExifInterface exif = new ExifInterface(
								tmpFile.getPath());
						switch (exif.getAttributeInt(
								ExifInterface.TAG_ORIENTATION,
								ExifInterface.ORIENTATION_NORMAL)) {
						case ExifInterface.ORIENTATION_ROTATE_90:
							degrees = 90;
							break;
						case ExifInterface.ORIENTATION_ROTATE_180:
							degrees = 180;
							break;
						case ExifInterface.ORIENTATION_ROTATE_270:
							degrees = 270;
							break;
						default:
							degrees = 0;
							break;
						}
						Log.e(TAG, exif
								.getAttribute(ExifInterface.TAG_ORIENTATION));
					} catch (IOException e) {
						e.printStackTrace();
					}

					Bitmap bitmap = BitmapHelper.getResizedBitmap(tmpFile,
							BitmapHelper.MAX_IMAGE_WIDTH,
							BitmapHelper.MAX_IMAGE_HEIGHT, degrees);

					TodoListItem item = new TodoListItem();
					item.uuid = UUID.randomUUID().toString();
					item.title = "";
					item.bDone = false;
					item.description = "";
					item.withImg = true;
					item.creDateMillis = System.currentTimeMillis();
					item.modDateMillis = System.currentTimeMillis();
					item.creContactId = mManager.getLocalContactId();
					item.modContactId = mManager.getLocalContactId();

					final CharSequence msg = getString(R.string.feed_msg_added_photo);
					StringBuilder html = new StringBuilder(msg);
					mManager.addTodo(
							null,
							item,
							bitmap,
							UIUtils.getUpdateString(HomeActivity.this, html.toString()));
					mTodoListFragment.refresh();

					tmpFile.delete();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ----------------------------------------------------------
	// Musubi
	// ----------------------------------------------------------
	private final FeedObserver mStateObserver = new FeedObserver() {
		@Override
		public void onUpdate(DbObj obj) {
			JSONObject newState = obj.getJson();
			if (newState == null || !newState.has(TodoDataManager.STATE))
				return;

			if (obj != null) {
				Log.d(TAG, "onUpdate(): " + newState.toString());

				mManager.setJSONObject(newState.optJSONObject(TodoDataManager.STATE));
				mTodoListFragment.refresh();
			}
		}
	};

}