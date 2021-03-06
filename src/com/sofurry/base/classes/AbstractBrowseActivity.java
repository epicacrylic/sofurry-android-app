package com.sofurry.base.classes;

import com.sofurry.AppConstants;
import com.sofurry.activities.SettingsActivity;
import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.helpers.ProgressBarHelper;
import com.sofurry.model.NetworkList;
import com.sofurry.storage.NetworkListStorage;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

/**
 * Abstract activity to browse through list of submissions.
 * Automatically load thumbnails after page load
 * 
 * @author Night_Gryphon
 */
public abstract class AbstractBrowseActivity<T> extends Activity {
	/**
	 * primary data list that will be presented to user
	 */
	protected NetworkList<T> fList = null;
	
	/**
	 * UI element to present NetworkList items to user
	 */
	@SuppressWarnings("rawtypes")
	protected AdapterView fDataView = null;
	
	/**
	 * return UI element to present NetworkList items to user
	 */
	@SuppressWarnings("rawtypes")
	protected abstract AdapterView getDataView();
	
	/**
	 * Lock excess refreshDataView calls
	 */
	private boolean refreshLock = false;
	private boolean refreshRequested = false;
	private Handler refreshLockHandler = new Handler() {
        public void handleMessage(Message m) {
        	refreshLock = false;
        	if (refreshRequested)
        		refreshDataView();
        }
    };

	/**
	 * Refresh display to show changes in list
	 */
	protected synchronized void refreshDataView() {
		// block excess refresh calls.
		// this done as delayed message as we should not loose refresh requests.
		// We just join all excess requests in to one delayed call 
		if (refreshLock) {
			refreshRequested = true;
			return;
		}
		
		refreshLock = true;
		refreshRequested = false;
		
        Message m = new Message();
        refreshLockHandler.sendMessageDelayed(m, 2000);
        
        doRefreshDataView();
	}

	/**
	 * do actual job on refresh display changes in list
	 * Descendants should override this method instead of refreshDataView
	 */
    protected synchronized void doRefreshDataView() {
        
        // perform refresh
		if ((myAdapter != null) && (myAdapter instanceof BaseAdapter))
			((BaseAdapter) myAdapter).notifyDataSetChanged();

		if (fDataView != null) {
			if (fDataView instanceof AbsListView)
				((AbsListView) fDataView).invalidateViews();
			else
				fDataView.invalidate(); // used instead of invalidateViews
		}
	}

	/**
	 * adapter to use in AdapterView
	 */
	protected Adapter myAdapter = null;
	
	/**
	 * create new adapter to be used in DataView
	 * @param context
	 * @return
	 */
	protected abstract Adapter createAdapter();
	
	/**
	 * progress messages and dialogs
	 */
	private ProgressBarHelper pbh = new ProgressBarHelper(this, new ICanCancel() {
		public void cancel() {
			if (fList != null)
				fList.cancel();
		}
	});
	
	/**
	 * Create NetworkList that used in this activity
	 * @return
	 */
	protected abstract NetworkList<T> createBrowseList();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			setList(NetworkListStorage.get(savedInstanceState.getLong("ListID")));
			setTitle(savedInstanceState.getCharSequence("ActTitle"));
		} else {
		    Bundle extras = getIntent().getExtras();
		    if (extras != null) {
		    	setTitle(extras.getString("activityTitle"));
				setList(NetworkListStorage.get(extras.getInt("ListID")));
		    }
		}
	}



	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		
		// required descendant specific params should be loaded before this point so now we can request DataView and Adapter
		if (fDataView == null) 
			fDataView = getDataView();

		if (fList == null)
			setList(createBrowseList());

		if (fDataView != null) {
			// TODO set forward preload count to fList (set number of items till end of list that trigger next page load)

			fDataView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView parentView, View childView, int position, long id) {
					if ( (fList != null) && (fList.get(position) != null))
						onDataViewItemClick(position);
				}
			});
			fDataView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
					if ( (fList != null) && (fList.get(position) != null))
						return onDataViewItemLongClick(position);
					else
						return false;
				}
			});

			pluginAdapter();
			
			if (savedInstanceState != null) {
				fDataView.setSelection(savedInstanceState.getInt("", 0));
			} else {
			    Bundle extras = getIntent().getExtras();
			    if (extras != null) {
					fDataView.setSelection(extras.getInt("SelectedIndex", 0));
			    }
			}
		}
		
/*		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("startThumbLoader", false))
				LoadThumbnails();
		}/**/
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("ListID", fList.getListId());
		outState.putCharSequence("ActTitle", getTitle());

		if (fDataView != null)
			outState.putInt("SelectedIndex", fDataView.getFirstVisiblePosition());
		
/*		if (thumbLoader != null) {
//			StopLoadThumbnails();
			outState.putBoolean("startThumbLoader", true);
		}/**/
		
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		try {
			if (isFinishing()) {
//				StopLoadThumbnails();
				fList.finalize();
				fList = null;
			} else {
				// TODO should we clean callback for fList and ThumbDownloader here?
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null) {
			int jumptoitem = data.getIntExtra("JumpTo", -1);
			if ((jumptoitem >= 0) && (fDataView != null)) {
//				fDataView.requestFocusFromTouch();
//				if ( (jumptoitem < fDataView.getFirstVisiblePosition()) || (jumptoitem > fDataView.getLastVisiblePosition()))
					fDataView.setSelection(jumptoitem);
			}
		}
	}

	/**
	 * Called when item is selected in list/gallery
	 * @param aItemIndex
	 */
	protected void onDataViewItemClick(int aItemIndex) {
	}

	/**
	 * Called when item is long clicked list/gallery
	 * @param aItemIndex
	 */
	protected boolean onDataViewItemLongClick(int aItemIndex) {
		return false;
	}
	
	/**
	 * Plug in NetworkList to browse in this activity and call refreshDataView
	 * @param aList
	 */
	public void setList(NetworkList<T> aList) {
		//detach current list and unplug adapter
		if (fList != null) {
			try {
				if (fDataView != null)
					fDataView.setAdapter(null);
				
				myAdapter = null; // is this enough to destroy object?
//				if (myAdapter != null)
				
				fList.setStatusListener(null); // clear status callbacks before detach list
				fList.finalize();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			fList = null;
		}
		
		// attach new list
		fList = aList;
		
		if (fList != null) {
			setListCallback();
			pluginAdapter();
		}
	}

	/**
	 * Set callback procs to fList
	 */
	protected void setListCallback() {
		if (fList != null) {
			fList.setStatusListener(new IJobStatusCallback() {
				
				public void onSuccess(Object job) {
					doRefreshDataView(); // force refresh when page loaded
					onLoadFinish();
				}
				
				public void onStart(Object job) {
					onLoadStart();
				}
				
				// Called by SFSubmissionList on thumb loading progress
				public void onProgress(Object job, int progress, int total, String msg) {
					refreshDataView();
				}
				
				public void onError(Object job, String msg) {
					onLoadError(msg);
				}
			});
		}
	}
	
	
	
	public void onLoadStart() {
		showProgressDialog("Loading page...", ! fList.isFirstPage());
	}
	
	public void onLoadFinish() {
		hideProgressDialog();
	}
	
	public void onLoadError(String msg) {
		hideProgressDialog();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//		if (fList != null)
//			fList.setStatusListener(null); // clear callback so another activity can use list
	}

	@Override
	protected void onResume() {
		super.onResume();
		setListCallback(); // restore callbacks to our activity
		refreshDataView();
	}

	/**
	 * Assign adapter to fDataView and reset list position
	 */
	public void pluginAdapter() {
		// Plug in adapter
		if ((fDataView != null) && (myAdapter == null)) {
			myAdapter = createAdapter(); 
			fDataView.setAdapter(myAdapter);
			fDataView.setSelection(0);
			
			doRefreshDataView(); // force refresh, do not trigger lock to allow fList to refresh immidiately on load
		}
	}
	
	/**
	 * Shows the progress Dialog
	 * @param msg
	 */
	public void showProgressDialog(String msg, Boolean toast) {
		if (toast)
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		else
			pbh.showProgressDialog(msg);
	}
	
	/**
	 * Hides the progress Dialog
	 */
	public void hideProgressDialog() {
		pbh.hideProgressDialog();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, AppConstants.MENU_SETTINGS, 10, "Settings").setIcon(android.R.drawable.ic_menu_preferences);

        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
			case AppConstants.MENU_SETTINGS:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
				
            default:
                return super.onOptionsItemSelected(item);
        }
	}
	
	
}
