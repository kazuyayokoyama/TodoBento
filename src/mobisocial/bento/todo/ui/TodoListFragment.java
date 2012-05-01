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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import leoliang.tasks365.DraggableListView;
import leoliang.tasks365.DraggableListView.DropListener;
import mobisocial.bento.todo.R;
import mobisocial.bento.todo.io.BentoManager;
import mobisocial.bento.todo.util.BitmapHelper;
import mobisocial.bento.todo.util.ImageCache;
import mobisocial.bento.todo.util.JpgFileHelper;
import mobisocial.bento.todo.util.UIUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

public class TodoListFragment extends ListFragment {
    public interface OnBentoSelectedListener {
        public void onBentoSelected();
    }

	private static final Boolean DEBUG = true;
    private static final String TAG = "TodoListFragment";
	private static final int REQUEST_IMAGE_CAPTURE = 0;
	private static final int REQUEST_GALLERY = 1;
    
	private BentoManager mManager = BentoManager.getInstance();
	private TodoListItemAdapter mListAdapter = null;
	private DraggableListView mListView = null;
    private OnBentoSelectedListener mListener;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
        View root = inflater.inflate(R.layout.fragment_todo_list, container, false);
        
        // ListView
		mListView = (DraggableListView) root.findViewById(android.R.id.list);
        mListView.setFastScrollEnabled(true);
        mListView.setDropListener(new DropListener() {
            @Override
            public void drop(int from, int to) {
            	if (DEBUG) Log.d(TAG, "drop - from:" + from + " to:" + to);
                if (from == to) {
                    return;
                }

				// sort
				StringBuilder msg = new StringBuilder(
						getString(R.string.feed_msg_sorted, mManager.getLocalName()));
				String plainMsg = UIUtils.getPlainString(mManager.getBentoListItem().bento.name, msg.toString());
                mManager.sortTodoList(from, to);
                mManager.sortTodoCompleted(plainMsg);
                
                refreshView();
            }
        });
		
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
	public void onAttach(SupportActivity activity) {
		super.onAttach(activity);

        try {
        	mListener = (OnBentoSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnRsvpSelectedListener");
        }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_add) {
			goAdd();
			return true;
		} else if (item.getItemId() == R.id.menu_clear) {
			goClear();
			return true;
		} else if (item.getItemId() == R.id.menu_bento_list) {
			goBentoList();
			return true;
		} else if (item.getItemId() == R.id.menu_camera) {
			goCamera();
			return true;
		} else if (item.getItemId() == R.id.menu_gallery) {
			goGallery();
			return true;
		} 
		return super.onOptionsItemSelected(item);
	}

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		// Intent
		Intent intent = new Intent(getActivity(), TodoDetailActivity.class);
		intent.putExtra(TodoDetailFragment.EXTRA_TODO_UUID, mManager.getTodoListItem(position).uuid);
		intent.putExtra(HomeActivity.EXTRA_TODO, HomeActivity.EXTRA_TODO);

        getActivity().startActivity(intent);
	}

	public void refreshView() {
		Handler handler = new Handler();
		handler.post(new Runnable(){
			public void run(){
				mListAdapter.notifyDataSetChanged();
				mListView.invalidateViews();
			}
		});
    }

	private void goAdd() {
		// Show Add dialog
		LayoutInflater factory = LayoutInflater.from(getActivity());
		final View inputView = factory.inflate(R.layout.dialog_add_todo, null);

		// EditText
		final EditText editTextTodoTitle = (EditText) inputView.findViewById(R.id.edittext_todo_title);

		AlertDialog.Builder addTodoDialog = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.add_dialog_title)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(inputView)
				.setCancelable(true)
				.setPositiveButton(R.string.add_dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// fetch new Todo item
						String todoTitle = editTextTodoTitle.getText()
								.toString();

						// check string length
						if (todoTitle.length() > 0) {
							TodoListItem item = new TodoListItem();
							item.uuid = UUID.randomUUID().toString();
							item.title = todoTitle;
							item.description = "";
							item.hasImage = false;
							item.bDone = false;
							item.creDateMillis = System.currentTimeMillis();
							item.modDateMillis = System.currentTimeMillis();
							item.creContactId = mManager.getLocalContactId();
							item.modContactId = mManager.getLocalContactId();
							
							// add
							StringBuilder msg = new StringBuilder(
									getString(R.string.feed_msg_added, mManager.getLocalName(), item.title));
							String plainMsg = UIUtils.getPlainString(mManager.getBentoListItem().bento.name, msg.toString());
							
							mManager.addTodo(item, null, plainMsg);
							
							// refresh list view
							refreshView();
						}
					}
				})
				.setNegativeButton(R.string.add_dialog_cancel,
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

	private void goCamera() {
		File tmpFile = JpgFileHelper.getTmpFile();
		Intent intent = new Intent();
		intent.setAction("android.media.action.IMAGE_CAPTURE");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	private void goGallery() {
		File tmpFile = JpgFileHelper.getTmpFile();
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
		startActivityForResult(intent, REQUEST_GALLERY);
	}

	private void goClear() {
		AlertDialog.Builder clearTodoDialog = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.clear_dialog_title)
				.setMessage(R.string.clear_dialog_text)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setCancelable(true)
				.setPositiveButton(R.string.clear_dialog_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// clear
						StringBuilder msg = new StringBuilder(
								getString(R.string.feed_msg_cleared, mManager.getLocalName()));
						String plainMsg = UIUtils.getPlainString(mManager.getBentoListItem().bento.name, msg.toString());
						
						mManager.clearTodoDone(plainMsg);
						
						ImageCache.clearCache();
						
						// refresh list view
						refreshView();
					}
				})
				.setNegativeButton(R.string.clear_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// nothing to do (dismiss dialog)
							}
						});
		clearTodoDialog.create().show();
	}
	
	private void goBentoList() {

		// Load Bento List
		BentoListDialogAsyncTask task = new BentoListDialogAsyncTask(getActivity());
		task.execute();
	}
	
	class BentoListDialogAsyncTask extends AsyncTask<Void, Void, Boolean> {
		private BentoManager mManager = BentoManager.getInstance();
		private Context mContext;
		private AlertDialog mBentoListDialog = null;
		private ProgressDialog mProgressDialog = null;
		
		public BentoListDialogAsyncTask(Context context) {
			mContext = context;
		}

		@Override
		protected void onPreExecute() {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        final View listRoot = inflater.inflate(R.layout.fragment_bento_list, null, false);
	        
	        // Dialog
			AlertDialog.Builder bentoListDialogBuilder = new AlertDialog.Builder(mContext)
			.setTitle(R.string.bento_list_dialog_title)
			.setView(listRoot)
			.setCancelable(true)
			.setNegativeButton(getResources().getString(R.string.bento_list_dialog_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// nothing to do
						}
					});

	        // ListView
			final ListView listView = (ListView) listRoot.findViewById(android.R.id.list);
	        listView.setFastScrollEnabled(true);
	        
	        // Create adapter
	        final BentoListItemAdapter listAdapter = new BentoListItemAdapter(
	        		mContext, 
	        		android.R.layout.simple_list_item_1,
	        		listView);
			listView.setAdapter(listAdapter);
			
			mBentoListDialog = bentoListDialogBuilder.create();
			
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					BentoListItem item = mManager.getBentoListItem(position);
					mManager.setBentoObjUri(item.objUri);
					
					mBentoListDialog.dismiss();

	            	mListener.onBentoSelected();
				}
			});
			
			// disable empty
			LinearLayout emptyLayout = (LinearLayout) listRoot.findViewById(android.R.id.empty);
			emptyLayout.setVisibility(View.GONE);
			
			// show progress dialog
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setMessage(mContext.getString(R.string.bento_list_loading));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			mManager.loadBentoList();
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			try {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				// show dialog
				if (mBentoListDialog != null) {
					mBentoListDialog.show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
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
					} catch (IOException e) {
						e.printStackTrace();
					}

					Bitmap bitmap = BitmapHelper.getResizedBitmap(tmpFile,
							BitmapHelper.MAX_IMAGE_WIDTH,
							BitmapHelper.MAX_IMAGE_HEIGHT, degrees);

					TodoListItem item = new TodoListItem();
					item.uuid = UUID.randomUUID().toString();
					item.title = "";
					item.description = "";
					item.bDone = false;
					item.hasImage = true;
					item.creDateMillis = System.currentTimeMillis();
					item.modDateMillis = System.currentTimeMillis();
					item.creContactId = mManager.getLocalContactId();
					item.modContactId = mManager.getLocalContactId();

					StringBuilder msg = new StringBuilder(
							getString(R.string.feed_msg_added_photo, mManager.getLocalName()));
					String plainMsg = UIUtils.getPlainString(mManager.getBentoListItem().bento.name, msg.toString());
					
					mManager.addTodo(item, bitmap, plainMsg);
					
					refreshView();

					tmpFile.delete();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
