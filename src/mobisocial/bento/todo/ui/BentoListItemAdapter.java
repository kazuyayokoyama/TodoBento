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

import java.util.ArrayList;

import mobisocial.bento.todo.R;
import mobisocial.bento.todo.io.BentoManager;
import mobisocial.bento.todo.util.UIUtils;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BentoListItemAdapter extends ArrayAdapter<BentoListItem> {
	private static final Boolean DEBUG = UIUtils.isDebugMode();
	private static final String TAG = "BentoListItemAdapter";

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
		
		if (isEnabled(position)) {
			if (convertView == null || convertView.getId() != R.layout.item_bento_list) {
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
			
			// Set Number of Todos
			holder.numberOfTodos.setText(mContext.getResources().getQuantityString(R.plurals.bento_list_number, item.bento.numberOfTodo, item.bento.numberOfTodo));
		} else {
			if (convertView == null || convertView.getId() != R.layout.item_bento_list_divider) {
				// Create view from Layout File
				convertView = mInflater.inflate(R.layout.item_bento_list_divider, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.feed_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
	
			// Set Name
			holder.name.setText(getFeedName(item.feedId));
		}
		
		return convertView;
	}
	
	@Override
    public boolean isEnabled(int position) {
        return getItem(position).enabled;
    }
	
	private String getFeedName(long feedId) {
		String feedName = "";
		ArrayList<String> members = mManager.getMemberNames(feedId);

		if (members.size() > 0) {
	        StringBuilder text = new StringBuilder(100);
			for (String memeber : members) {
	            text.append(memeber).append(", ");
			}
	        text.setLength(text.length() - 2);
	        
	        feedName = text.toString();
		}
		
		if (DEBUG) Log.d(TAG, "feedName: " + feedName);
		
		return feedName;
	}
}
