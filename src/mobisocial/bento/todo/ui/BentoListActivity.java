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

import java.util.UUID;

import mobisocial.bento.todo.R;
import mobisocial.bento.todo.io.Bento;
import mobisocial.bento.todo.io.BentoManager;
import mobisocial.bento.todo.io.BentoManager.OnStateUpdatedListener;
import mobisocial.bento.todo.util.UIUtils;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.EditText;

public class BentoListActivity extends FragmentActivity {
	//private static final String TAG = "BentoListActivity";
    private static final int REQUEST_CREATE_FEED = 1;
    private static final String ACTION_CREATE_FEED = "musubi.intent.action.CREATE_FEED";

    private BentoManager mManager = BentoManager.getInstance();
    private BentoListFragment mBentoListFragment;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bento_list);

		final ActionBar actionBar = getSupportActionBar();
		// set defaults for logo & home up
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);
		
		FragmentManager fm = getSupportFragmentManager();
		mBentoListFragment = (BentoListFragment) fm.findFragmentById(R.id.fragment_bento_list);
		mManager.addListener(mStateUpdatedListener);
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mManager.removeListener(mStateUpdatedListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_items_bento_list, menu);
		
		super.onCreateOptionsMenu(menu);
		return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_add:
            	// If it's still working on Musubi, create Bento on current Feed
            	if (mManager.isFromMusubi()) {
            		goCreate();
            	} else {
            		goNewFeed();
            	}
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE_FEED) {
            if (resultCode == RESULT_OK) {
                Uri feedUri = data.getData();
                mManager.setFeedUri(feedUri);
                
                goCreate();
            }
        }
    };
	
	// BentoEventManager > OnStateUpdatedListener
	private OnStateUpdatedListener mStateUpdatedListener = new OnStateUpdatedListener() {
		@Override
		public void onStateUpdated() {
			mBentoListFragment.refreshView();
		}
	};
    
    private void goCreate() {
		// Show Add dialog
		LayoutInflater factory = LayoutInflater.from(this);
		final View inputView = factory.inflate(R.layout.dialog_create_bento, null);

		// EditText
		final EditText editTextBentoName = (EditText) inputView.findViewById(R.id.edittext_bento_name);

		AlertDialog.Builder createBentoDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.create_dialog_title)
				.setMessage(R.string.create_dialog_text)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(inputView)
				.setCancelable(true)
				.setPositiveButton(R.string.add_dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// fetch new Bento
						String bentoName = editTextBentoName.getText()
								.toString();

						// check string length
						if (bentoName.length() > 0) {
							Bento bento = new Bento();
							bento.name = bentoName;
							bento.uuid = UUID.randomUUID().toString();
							bento.numberOfTodo = 0;
							bento.creContactId = mManager.getLocalContactId();
							
							// create
							StringBuilder msg = new StringBuilder(
									getString(R.string.feed_msg_created, mManager.getLocalName()));
							String htmlMsg = UIUtils.getHtmlString(bentoName, msg.toString());
							
							mManager.createBento(bento, htmlMsg);

							// return to home
							Intent intent = new Intent();
							setResult(RESULT_OK, intent);
							finish();
						}
					}
				})
				.setNegativeButton(R.string.create_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// nothing to do (dismiss dialog)
							}
						});
		final AlertDialog dialog = createBentoDialog.create();
		editTextBentoName.setOnFocusChangeListener(new OnFocusChangeListener() {
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
    
    private void goNewFeed() {
        Intent create = new Intent(ACTION_CREATE_FEED);
        startActivityForResult(create, REQUEST_CREATE_FEED);
    }
}