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

package mobisocial.bento.todo.io;

import java.util.ArrayList;

import mobisocial.bento.todo.ui.BentoListItem;
import mobisocial.bento.todo.ui.TodoListItem;
import mobisocial.bento.todo.util.BitmapHelper;
import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.FeedObserver;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.musubi.multiplayer.FeedRenderable;
import mobisocial.socialkit.obj.MemObj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;


public class BentoManager {
    public interface OnStateUpdatedListener {
        public void onStateUpdated();
    }

	public static final String TYPE_APP_STATE = "appstate";
	public static final String TYPE_APP = "app";

	// root > state
	public static final String STATE = "state";
	// root > state > bento
	public static final String VERSION_CODE = "version_code";
	public static final String BENTO = "bento";
	public static final String BENTO_UUID = "uuid";
	public static final String BENTO_NAME = "name";
	public static final String BENTO_CRE_CONTACT_ID = "cre_contact_id";
	// root > state > bento > todo array
	public static final String TODO_LIST = "list";
	public static final String TODO_UUID = "uuid";
	public static final String TODO_TITLE = "title";
	public static final String TODO_DESCRIPTION = "description";
	public static final String TODO_HAS_IMG = "has_image";
	public static final String TODO_DONE = "done";
	public static final String TODO_CRE_DATE = "cre_date";
	public static final String TODO_MOD_DATE = "mod_date";
	public static final String TODO_CRE_CONTACT_ID = "cre_contact_id";
	public static final String TODO_MOD_CONTACT_ID = "mod_contact_id";
	// root > state > todo_image
	public static final String TODO_IMAGE = "todo_image";
	public static final String TODO_IMAGE_UUID = "todo_image_uuid";
	public static final String B64JPGTHUMB = "b64jpgthumb";
	
	private static final String TAG = "TodoDataManager";
	private static BentoManager sInstance = null;
	private Musubi mMusubi = null;
	private DbFeed mDbFeed = null;
	private Uri mBaseUri = null;
	private String mLocalContactId = null;
	private String mLocalName = null;
    private Integer mLastInt = 0;
    private int mVersionCode = 0;

	private ArrayList<BentoListItem> mBentoList = new ArrayList<BentoListItem>();
	private BentoListItem mBento = new BentoListItem();
    private ArrayList<OnStateUpdatedListener> mListenerList = new ArrayList<OnStateUpdatedListener>();

	// ----------------------------------------------------------
	// Instance
	// ----------------------------------------------------------
	private BentoManager() {
		mBento = null;
	}

	public static BentoManager getInstance() {
		if (sInstance == null) {
			sInstance = new BentoManager();
		}

		return sInstance;
	}

	public void init(Musubi musubi, Uri baseUri, int versionCode) {
		mMusubi = musubi;
		mBaseUri = baseUri;
		mVersionCode = versionCode;
		mLocalContactId = mMusubi.userForLocalDevice(mBaseUri).getId();
		mLocalName = mMusubi.userForLocalDevice(mBaseUri).getName();
		setDbFeed(mMusubi.getObj().getSubfeed());
	}

	public void fin() {
        mDbFeed.removeStateObserver(mStateObserver);
		mBentoList = null;
		mBento = null;

		if (sInstance != null) {
			sInstance = null;
		}
	}

	// ----------------------------------------------------------
	// Get / Retrieve
	// ----------------------------------------------------------
	synchronized public boolean hasBento() {
		return (mBento != null);
	}
	
	synchronized public BentoListItem getBentoListItem() {
		return mBento;
	}
	
	synchronized public TodoListItem getTodoListItem(int position) {
		return mBento.bento.todoList.get(position);
	}
	
	synchronized public TodoListItem getTodoListItem(String uuid) {
		for (int i = 0; i < mBento.bento.todoList.size(); i++) {
			TodoListItem item = mBento.bento.todoList.get(i);
			if (item.uuid.equals(uuid)) {
				return item;
			}
		}
		
		return null;
	}

	synchronized public int getTodoListCount() {
		return mBento.bento.todoList.size();
	}
	
	synchronized public Bitmap getTodoBitmap(String todoUuid,
			int targetWidth, int targetHeight, float degrees) {
		return getTodoBitmap(mDbFeed, todoUuid, targetWidth, targetHeight, degrees);
	}
	
	synchronized public Bitmap getTodoBitmap(DbFeed dbFeed, String todoUuid,
			int targetWidth, int targetHeight, float degrees) {
		Bitmap bitmap = null;

		Cursor c = dbFeed.query();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			Obj object = mMusubi.objForCursor(c);
			if (object != null && object.getJson() != null && object.getJson().has(TODO_IMAGE)) {
				JSONObject diff = object.getJson().optJSONObject(TODO_IMAGE);
				if (todoUuid.equals(diff.optString(TODO_IMAGE_UUID))) {
					byte[] byteArray = Base64.decode(object.getJson().optString(B64JPGTHUMB), Base64.DEFAULT);
					bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
					break;
				}
			}
			c.moveToNext();
		}
		c.close();

		if (bitmap != null) {
			bitmap = BitmapHelper.getResizedBitmap(bitmap, targetWidth, targetHeight, degrees);
		}

		return bitmap;
	}

	synchronized public String getLocalContactId() {
		return mLocalContactId;
	}

	synchronized public String getLocalName() {
		return mLocalName;
	}

	// Bento List
	synchronized public void loadBentoList() {
        String[] projection = null;
        String selection = "type = ? AND feed_name = ?";
        String[] selectionArgs = new String[] { TYPE_APP, mMusubi.getObj().getFeedName() };
        String sortOrder = null;
        
		Cursor c = mMusubi.getAppFeed().query(projection, selection, selectionArgs, sortOrder);

		if (c != null && c.moveToFirst()) {
			mBentoList = new ArrayList<BentoListItem>();
			
			for (int i = 0; i < c.getCount(); i++) {
				BentoListItem item = new BentoListItem();
				item.bento = new Bento();
				
				DbObj dbObj = mMusubi.objForCursor(c);
				DbFeed dbFeed = dbObj.getSubfeed();
				Obj latestObj = dbFeed.getLatestObj();
				if (latestObj != null && latestObj.getJson() != null && latestObj.getJson().has(STATE)) {
					JSONObject stateObj = latestObj.getJson().optJSONObject(STATE);
					if (fetchBentoObj(dbFeed, stateObj, item.bento)) {
						item.feedUri = dbFeed.getUri();

						// count number of todo
						ArrayList<TodoListItem> todoList = new ArrayList<TodoListItem>();
						if (fetchTodoListObj(dbFeed, stateObj, todoList)) {
							item.bento.numberOfTodo = todoList.size();
							mBentoList.add(0, item);
						}
					}
				}
				c.moveToNext();
			}
		}
		c.close();
	}
	
	synchronized public BentoListItem getBentoListItem(int position) {
		return mBentoList.get(position);
	}

	synchronized public int getBentoListCount() {
		return mBentoList.size();
	}

	// ----------------------------------------------------------
	// Update
	// ----------------------------------------------------------
	synchronized public void createBento(Bento bento, String htmlMsg) {
		mBento = new BentoListItem();
		mBento.bento = bento;
		pushUpdate(htmlMsg);
	}
	
	synchronized public void addTodo(TodoListItem item, Bitmap image, String htmlMsg) {
		mBento.bento.todoList.add(0, item);
		
		if (image == null) {
			pushUpdate(htmlMsg);
		} else {
			String data = Base64.encodeToString(BitmapHelper.bitmapToBytes(image), Base64.DEFAULT);
			pushUpdate(htmlMsg, item.uuid, data);
		}
	}

	synchronized public void removeTodo(TodoListItem item, String htmlMsg) {
		// not supported yet
	}

	synchronized public void updateTodo(TodoListItem updateItem, String htmlMsg) {
		for (int i=0; i<mBento.bento.todoList.size(); i++) {
			TodoListItem item = mBento.bento.todoList.get(i);
			if (item.uuid.equals(updateItem.uuid)) {
				mBento.bento.todoList.remove(i);
				mBento.bento.todoList.add(i, updateItem);
				break;
			}
		}

		pushUpdate(htmlMsg);
	}

	synchronized public void sortTodoList(int positionFrom, int positionTo) {
		TodoListItem item = mBento.bento.todoList.get(positionFrom);
		
		if (positionFrom < positionTo) {
			mBento.bento.todoList.add(positionTo, item);
			mBento.bento.todoList.remove(positionFrom);
		} else if (positionFrom > positionTo) {
			mBento.bento.todoList.remove(positionFrom);
			mBento.bento.todoList.add(positionTo, item);
		}
	}

	synchronized public void sortTodoCompleted(String htmlMsg) {
		pushUpdate(htmlMsg);
	}

	synchronized public void clearTodoDone(String htmlMsg) {
		int beforeCount = mBento.bento.todoList.size();
		if (beforeCount == 0) {
			return;
		}
		
		// remove backward
		for (int i = (beforeCount - 1); i >= 0; i--) {
			TodoListItem item = mBento.bento.todoList.get(i);
			if (item.bDone) {
				mBento.bento.todoList.remove(i);
			}
		}
		
		// if updated
		if (beforeCount > mBento.bento.todoList.size()) {
			pushUpdate(htmlMsg);
		}
	}

	// ----------------------------------------------------------
	// Listener
	// ----------------------------------------------------------
	public void addListener(OnStateUpdatedListener listener){
		mListenerList.add(listener);
    }
	
	public void removeListener(OnStateUpdatedListener listener){
		mListenerList.remove(listener);
    }

	// ----------------------------------------------------------
	// Musubi
	// ----------------------------------------------------------
	public void pushUpdate(String htmlMsg) {
		pushUpdate(htmlMsg, null, null);
	}

	public void pushUpdate(String htmlMsg, String todoUuid, String data) {
		try {
			JSONObject rootObj = new JSONObject();
			rootObj.put(STATE, getStateObj());
			
			JSONObject out = new JSONObject(rootObj.toString());

			if (todoUuid != null && data != null) {
				JSONObject todoImageObj = new JSONObject();
				todoImageObj.put(TODO_IMAGE_UUID, todoUuid);
				out.put(TODO_IMAGE, todoImageObj);
				out.put(B64JPGTHUMB, data);
			}
			
			FeedRenderable renderable = FeedRenderable.fromHtml(htmlMsg);
			renderable.withJson(out);
			mDbFeed.postObj(new MemObj(TYPE_APP_STATE, out, null, ++mLastInt));
		} catch (JSONException e) {
			Log.e(TAG, "Failed to post JSON", e);
		}
	}

	public void setDbFeed(Uri feedUri) {
		setDbFeed(mMusubi.getFeed(feedUri));
	}
	
	public void setDbFeed(DbFeed dbFeed) {
		// previous feed
		if (mDbFeed != null) {
			mDbFeed.removeStateObserver(mStateObserver);
		}
        
        // new feed
		mDbFeed = dbFeed;

        String[] projection = null;
        String selection = "type = ?";
        String[] selectionArgs = new String[] { TYPE_APP_STATE };
        String sortOrder = DbObj.COL_KEY_INT + " desc";
        mDbFeed.setQueryArgs(projection, selection, selectionArgs, sortOrder);
        
        mDbFeed.registerStateObserver(mStateObserver);

		// json
		JSONObject stateObj = null;
		Obj obj = mDbFeed.getLatestObj();
		if (obj != null && obj.getJson() != null && obj.getJson().has(STATE)) {
			stateObj = obj.getJson().optJSONObject(STATE);
		}

		if (stateObj == null) {
			mBento = null;
			mLastInt = 0;
		} else {
			setNewStateObj(stateObj);
			mLastInt = (obj.getInt() == null) ? 0 : obj.getInt();
		}
	}
	
	private final FeedObserver mStateObserver = new FeedObserver() {
		@Override
		public void onUpdate(DbObj obj) {

			mLastInt = (obj.getInt() == null) ? 0 : obj.getInt();

			JSONObject stateObj = null;
			if (obj != null && obj.getJson() != null && obj.getJson().has(STATE)) {
				stateObj = obj.getJson().optJSONObject(STATE);

				try {
					// TODO : just in case
					if (! isValidBento(stateObj.getJSONObject(BENTO).optString(BENTO_UUID))) {

						Handler handler = new Handler();
						handler.post(new Runnable(){
							public void run(){
								for (OnStateUpdatedListener listener : mListenerList) {
									listener.onStateUpdated();
								}
							}
						});
						
						return;
					}
				} catch (JSONException e) {
					Log.e(TAG, "Failed to get JSON", e);
					return;
				}
				
				setNewStateObj(stateObj);

				Handler handler = new Handler();
				handler.post(new Runnable(){
					public void run(){
						for (OnStateUpdatedListener listener : mListenerList) {
							listener.onStateUpdated();
						}
					}
				});
			} else {
				return;
			}
		}
		
	};

	// ----------------------------------------------------------
	// Private
	// ----------------------------------------------------------
	private void setNewStateObj(JSONObject stateObj) {
		mBento = new BentoListItem();
		setNewBentoObj(stateObj);
		setNewTodoListObj(stateObj);
	}
	
	private void setNewBentoObj(JSONObject stateObj) {
		fetchBentoObj(mDbFeed, stateObj, mBento.bento);
	}
	
	private boolean fetchBentoObj(DbFeed dbFeed, JSONObject stateObj, Bento bento) {
		boolean ret = false;
		try {
			JSONObject bentoObj = stateObj.getJSONObject(BENTO);
			bento.uuid = bentoObj.optString(BENTO_UUID);
			bento.name = bentoObj.optString(BENTO_NAME);
			bento.creContactId = bentoObj.optString(BENTO_CRE_CONTACT_ID);
			
			ret = true;
		} catch (JSONException e) {
			Log.e(TAG, "Failed to get JSON", e);
		}
		
		return ret;
	}
	
	private void setNewTodoListObj(JSONObject stateObj) {
		mBento.bento.todoList = new ArrayList<TodoListItem>();
		fetchTodoListObj(mDbFeed, stateObj, mBento.bento.todoList);
	}

	private boolean fetchTodoListObj(DbFeed dbFeed, JSONObject stateObj, ArrayList<TodoListItem> todoList) {
		boolean ret = false;
		try {
			JSONArray todoListArray = stateObj.optJSONArray(TODO_LIST);
			
			if (todoListArray != null) {
				
				for (int i=0; i<todoListArray.length(); i++) {
					JSONObject todoObj = todoListArray.getJSONObject(i);
					TodoListItem item = new TodoListItem();
					item.uuid = todoObj.optString(TODO_UUID);
					item.title = todoObj.optString(TODO_TITLE);
					item.description = todoObj.optString(TODO_DESCRIPTION);
					item.hasImage = todoObj.optBoolean(TODO_HAS_IMG);
					item.bDone = todoObj.optBoolean(TODO_DONE);
					item.creDateMillis = todoObj.optLong(TODO_CRE_DATE);
					item.modDateMillis = todoObj.optLong(TODO_MOD_DATE);
					item.creContactId = todoObj.optString(TODO_CRE_CONTACT_ID);
					item.modContactId = todoObj.optString(TODO_MOD_CONTACT_ID);
					
					todoList.add(item);
				}
				ret = true;
			}
			
		} catch (JSONException e) {
			Log.e(TAG, "Failed to get JSON", e);
		}
		
		return ret;
	}
	
	private JSONObject getBentoObj() {
		
		JSONObject bentoObj = new JSONObject();
		try {
			bentoObj.put(BENTO_UUID, mBento.bento.uuid);
			bentoObj.put(BENTO_NAME, mBento.bento.name);
			bentoObj.put(BENTO_CRE_CONTACT_ID, mBento.bento.creContactId);

		} catch (JSONException e) {
			Log.e(TAG, "Failed to put JSON", e);
		}
		
		return bentoObj;
	}
	
	private JSONArray getTodoListArray() {
		JSONArray todoListArray = new JSONArray();

		try {

			for (int i = 0; i < mBento.bento.todoList.size(); i++) {
				TodoListItem item = mBento.bento.todoList.get(i);
				
				JSONObject todoObj = new JSONObject();
				todoObj.put(TODO_UUID, item.uuid);
				todoObj.put(TODO_TITLE, item.title);
				todoObj.put(TODO_DESCRIPTION, item.description);
				todoObj.put(TODO_HAS_IMG, item.hasImage);
				todoObj.put(TODO_DONE, item.bDone);
				todoObj.put(TODO_CRE_DATE, item.creDateMillis);
				todoObj.put(TODO_MOD_DATE, item.modDateMillis);
				todoObj.put(TODO_CRE_CONTACT_ID, item.creContactId);
				todoObj.put(TODO_MOD_CONTACT_ID, item.modContactId);
				
				todoListArray.put(todoObj);
			}
						
		} catch (JSONException e) {
			Log.e(TAG, "Failed to put JSON", e);
		}
		
		return todoListArray;
	}

	private JSONObject getStateObj() {
		JSONObject stateObj = new JSONObject();
		try {
			stateObj.put(VERSION_CODE, mVersionCode);
			JSONObject bentoObj = getBentoObj();
			stateObj.put(BENTO, bentoObj);
			JSONArray todoListArray = getTodoListArray();
			stateObj.put(TODO_LIST, todoListArray);
		} catch (JSONException e) {
			Log.e(TAG, "Failed to put JSON", e);
		}
		return stateObj;
	}
	
	private boolean isValidBento(String uuid) {
		return (mBento != null && mBento.bento.uuid != null && uuid != null && mBento.bento.uuid.equals(uuid));
	}
}
