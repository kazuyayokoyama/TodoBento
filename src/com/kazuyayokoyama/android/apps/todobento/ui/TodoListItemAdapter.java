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

import leoliang.tasks365.DraggableListView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.kazuyayokoyama.android.apps.todobento.R;
import com.kazuyayokoyama.android.apps.todobento.io.BentoManager;
import com.kazuyayokoyama.android.apps.todobento.util.BitmapHelper;
import com.kazuyayokoyama.android.apps.todobento.util.ImageCache;
import com.kazuyayokoyama.android.apps.todobento.util.UIUtils;

public class TodoListItemAdapter extends ArrayAdapter<TodoListItem> {
	//private static final String TAG = "TodoListItemAdapter";
	private static final int IMG_WIDTH = 160;
	private static final int IMG_HEIGHT = 160;

	private BentoManager mManager = BentoManager.getInstance();
	private LayoutInflater mInflater;
	private int mDraggingPosition = -1;
	private DraggableListView mDraggableListView = null;
	private Context mContext = null;

	public TodoListItemAdapter(Context context, int resourceId,
			DraggableListView listView) {
		super(context, resourceId);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDraggableListView = listView;
	}

	@Override
	public int getCount() {
		int count = mManager.getTodoListCount();
		return count;
	}

	@Override
	public TodoListItem getItem(int position) {
		return mManager.getTodoListItem(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			// Create view from Layout File
			convertView = mInflater.inflate(R.layout.item_todo_list, null);
			holder = new ViewHolder();
			holder.check = (CheckBox) convertView.findViewById(R.id.todo_checkbox);
			holder.title = (TextView) convertView.findViewById(R.id.todo_title);
			holder.description = (TextView) convertView.findViewById(R.id.todo_description);
			holder.imageView = (ImageView) convertView.findViewById(R.id.todo_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Fetch item
		final TodoListItem item = (TodoListItem) getItem(position);

		// Set CheckBox
		holder.check.setChecked(item.bDone);
		holder.check.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				// Log.d(TAG, "onCheckedChanged");
				item.bDone = holder.check.isChecked();
				item.modDateMillis = System.currentTimeMillis();
				item.modContactId = mManager.getLocalContactId();

				CharSequence baseMsg;
				Resources rsrc = mDraggableListView.getResources();
				if (item.bDone) {
					if (item.title.length() > 0) {
						baseMsg = rsrc.getString(R.string.feed_msg_done, mManager.getLocalName(), item.title);
					} else {
						baseMsg = rsrc.getString(R.string.feed_msg_done_notitle, mManager.getLocalName());
					}
				} else {
					if (item.title.length() > 0) {
						baseMsg = rsrc.getString(R.string.feed_msg_undone, mManager.getLocalName(), item.title);
					} else {
						baseMsg = rsrc.getString(R.string.feed_msg_undone_notitle, mManager.getLocalName());
					}
				}
				StringBuilder msg = new StringBuilder(baseMsg);
				String htmlMsg = UIUtils.getHtmlString(mManager.getBentoListItem().bento.name, msg.toString());
				
				mManager.updateTodo(item, htmlMsg);
				notifyDataSetChanged();
			}
		});

		// Set Title
		holder.title.setText(item.title);
		if (item.bDone) {
			holder.title.setTextColor(convertView.getResources().getColor(
					R.color.body_text_disabled));
		} else {
			holder.title.setTextColor(convertView.getResources().getColor(
					R.color.body_text_1));
		}

		// Set Description
		if (item.description != null && item.description.length() > 0) {
			holder.description.setText(item.description);
			holder.description.setVisibility(View.VISIBLE);
			if (item.bDone) {
				holder.description.setTextColor(
						convertView.getResources().getColor(R.color.body_text_disabled));
			} else {
				holder.description.setTextColor(
						convertView.getResources().getColor(R.color.body_text_2));
			}
		} else {
			// Log.d(TAG, "GONE");
			holder.description.setVisibility(View.GONE);
		}

		// Set Image
		if (item.hasImage) {
			try {
				holder.imageView.setTag(item.uuid);

				holder.imageView.setImageBitmap(
						BitmapHelper.getDummyBitmap(IMG_WIDTH, IMG_HEIGHT));
				//ImageGetTask task = new ImageGetTask(holder.imageView);
				//task.execute(item.uuid);
				
				Bitmap bitmap = ImageCache.getImage(item.uuid);
				if (bitmap == null) {
					holder.imageView.setImageBitmap(
							BitmapHelper.getDummyBitmap(IMG_WIDTH, IMG_HEIGHT));
					ImageGetTask task = new ImageGetTask(holder.imageView);
					task.execute(item.uuid);
				} else {
					holder.imageView.setImageBitmap(bitmap);
					holder.imageView.setVisibility(View.VISIBLE);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (item.bDone) {
				holder.imageView.setAlpha(128);
			} else {
				holder.imageView.setAlpha(255);
			}
		} else {
			holder.imageView.setVisibility(View.GONE);
		}

		// DragnDrop
		convertView.setVisibility(position == mDraggingPosition ? View.INVISIBLE : View.VISIBLE);

		return convertView;
	}

	public int getDraggingPosition() {
		return mDraggingPosition;
	}

	public void setDraggingPosition(int draggingPosition) {
		this.mDraggingPosition = draggingPosition;
	}

	static class ViewHolder {
		CheckBox check;
		TextView title;
		TextView description;
		ImageView imageView;
	}

	class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
		private ImageView image;
		private String tag;

		public ImageGetTask(ImageView imageView) {
			image = imageView;
			tag = image.getTag().toString();
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			synchronized (mContext) {
				try {
					Bitmap bitmap = mManager.getTodoBitmap(params[0], IMG_WIDTH, IMG_HEIGHT, 0);
					if (bitmap != null) {
						ImageCache.setImage(params[0], bitmap);
					}
					return bitmap;
				} catch (Exception e) {
					return null;
				}
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (tag.equals(image.getTag())) {
				if (result != null) {
					image.setImageBitmap(result);
				} else {
					image.setImageBitmap(BitmapHelper.getDummyBitmap(
							IMG_WIDTH, IMG_HEIGHT));
				}
				image.setVisibility(View.VISIBLE);
			}
		}
	}
}
