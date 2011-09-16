package com.sofurry.activities;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.BaseAdapter;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.adapters.SubmissionListAdapter;
import com.sofurry.base.classes.AbstractContentList;
import com.sofurry.model.Submission;
import com.sofurry.model.Submission.SUBMISSION_TYPE;
import com.sofurry.requests.AjaxRequest;

public class ListMusicActivity extends AbstractContentList<Submission> {

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		return GalleryArtActivity.createBrowse(page,source,man.getViewSearch(),AppConstants.CONTENTTYPE_MUSIC,AppConstants.ENTRIESPERPAGE_GALLERY);
	}
	

	@Override
	public void parseResponse(JSONObject obj) throws Exception {
		GalleryArtActivity.jsonToResultlist(obj, man, SUBMISSION_TYPE.MUSIC);
		// Start downloading the thumbnails
		man.startThumbnailDownloader();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		Submission s = getDataItem(selectedIndex);
		Log.i(AppConstants.TAG_STRING, "ListMusic: Viewing music ID: " + s.getId());
		Intent i = new Intent(this, ViewMusicActivity.class);
		s.feedIntent(i);
		//i.putExtra("pageID", s.getId());
		//i.putExtra("name", s.getName());

//		i.putExtra("username", s.getAuthorName());
//		i.putExtra("name", s.getName());
		startActivity(i);
	}

	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionListAdapter(context, R.layout.listitemtwolineicon, man.getResultList());
	}

	public void resetViewSourceExtra(int newViewSource) {
	}


	@Override
	public void finish() {
		
		super.finish();
	}

	
	

}