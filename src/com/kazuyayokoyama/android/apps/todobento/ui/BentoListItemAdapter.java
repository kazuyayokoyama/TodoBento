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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kazuyayokoyama.android.apps.todobento.R;
import com.kazuyayokoyama.android.apps.todobento.io.BentoManager;

public class BentoListItemAdapter extends ArrayAdapter<BentoListItem> {
	//private static final String TAG = "BentoListItemAdapter";

	private LayoutInflater mInflater;
	private BentoManager mManager = BentoManager.getInstance();
	private Context mContext = null;

	public BentoListItemAdapter(Context context, int resourceId,
			ListView listView) {
		super(context, resourceId);
		
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	static class ViewHolder {
		TextView name;
		TextView numberOfTodos;
	}

	@Override
	public int getCount() {
		return mManager.getBentoListCount();
	}

	@Override
	public BentoListItem getItem(int position) {
		return mManager.getBentoListItem(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		// Fetch item
		final BentoListItem item = (BentoListItem) getItem(position);
		
		if (convertView == null) {
			// Create view from Layout File
			convertView = mInflater.inflate(R.layout.item_bento_list, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.numberOfTodos = (TextView) convertView.findViewById(R.id.number_of_todos);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Set Name
		holder.name.setText(item.bento.name);
		
		// Set Start Date and Time
		StringBuilder msg = new StringBuilder(
				mContext.getString(R.string.bento_list_number, item.bento.numberOfTodo));
		holder.numberOfTodos.setText(msg.toString());

		return convertView;
	}
}
