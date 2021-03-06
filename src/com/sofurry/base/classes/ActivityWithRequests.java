package com.sofurry.base.classes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.base.interfaces.ICanHandleFeedback;
import com.sofurry.helpers.ProgressBarHelper;
import com.sofurry.requests.RequestHandler;
import com.sofurry.storage.ItemStorage;
import com.sofurry.util.DialogFactory;
import com.sofurry.util.ErrorHandler;

/**
 * @author Rangarig
 *
 * The base for an Activity with request handler, and a convinient progressbar
 * handler
 */
public abstract class ActivityWithRequests
        extends Activity
        implements ICanHandleFeedback, ICanCancel {
    protected ProgressBarHelper pbh = new ProgressBarHelper(this, this);

    /**
     * The request handler to be used to handle the feedback from the
     * AjaxRequest
     */
    protected RequestHandler requesthandler = null;

    /**
     * When displaying an error dialog, this will be used as the message
     */
    protected String errorMessage_;

    /**
     * When displaying an error dialog, this will be used as the title
     */
    protected String errorTitle_;
    protected long   uniquestoragekey = System.nanoTime();

    public void cancel() {
        requesthandler.killThreads();
        finish();
    }

    /**
     * Clears all stored objects for this activity
     */
    protected void clearStorage() {
        ItemStorage.get().remove(uniquestoragekey);
    }

    // Goes back to the story list

    protected void closeList() {
        Bundle bundle  = new Bundle();
        Intent mIntent = new Intent();

        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            uniquestoragekey = System.nanoTime();
            requesthandler   = new RequestHandler(this);
        } else {
            uniquestoragekey = savedInstanceState.getLong("unique");
            requesthandler   = (RequestHandler) retrieveObject("handler");

            // Extra sanity check, because requesthandler can sometimes be null
            // TODO: Might want to figure out why this happens
            if ((requesthandler == null) || (requesthandler.isKilled())) {
                uniquestoragekey = System.nanoTime();
                requesthandler   = new RequestHandler(this);
            }

            // Set feedback callback handler
            requesthandler.setFeedbackReceive(this);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case AppConstants.DIALOG_ERROR_ID:
                dialog = DialogFactory.createErrorDialog(this, errorTitle_,
                                                         errorMessage_);
        }

        return dialog;
    }

//    public void onData(int id, JSONObject obj) throws Exception {
//        pbh.hideProgressDialog();
//
//        throw new Exception("JSONObject received, but no handler implemented.");
//    }

    public void onError(Exception e) {
        pbh.hideProgressDialog();
        ErrorHandler.showError(this, e);
    }

    public void onOther(int id, Object obj) throws Exception {
        throw new Exception("Unexpected object type "
                            + obj.getClass().getName() + " received.");
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case AppConstants.DIALOG_ERROR_ID:
                ((AlertDialog) dialog).setTitle(errorTitle_);
                ((AlertDialog) dialog).setMessage(errorMessage_);
        }
    }

//    public void onProgress(int id, ProgressSignal prg) throws Exception {
//        pbh.setProgress(prg);
//    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("unique", uniquestoragekey);
        storeObject("handler", requesthandler);
        super.onSaveInstanceState(outState);
    }

    public void refresh() throws Exception {
        // Intentionally left blank
    }

    /**
     * Retrieves an object from the global ItemStroage
     * @param key
     * The key of the object to retrieve
     * @return
     * The object that was stored, or null if the object does not exist
     */
    protected Object retrieveObject(String key) {
        return ItemStorage.get().retrieve(uniquestoragekey, key);
    }

    /**
     * Stores an item in the global ItemStorage for later retrieval
     * @param key
     * The key to store the item under
     * @param obj
     * The object to store
     */
    protected void storeObject(String key, Object obj) {
        ItemStorage.get().store(uniquestoragekey, key, obj);
    }


    /**
     * A quick shortcut to determine if the device is in landscape or portrait
     * mode
     *
     * @return True if the device is in landscape mode, false otherwise
     */
    public boolean isOrientationLandscape() {
        return (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
    }
}
