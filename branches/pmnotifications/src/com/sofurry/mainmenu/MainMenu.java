/*
 * @(#)MainMenu.java   2011-02-09
 *
 * Copyright (c) 2011 Tristan Bendixen
 */



package com.sofurry.mainmenu;

//~--- imports ----------------------------------------------------------------

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;

import android.graphics.Typeface;

import android.os.Bundle;
import android.os.SystemClock;

import android.util.Log;

import android.view.View;

import android.widget.Button;

import com.sofurry.AccountActivity;
import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.base.classes.ActivityWithRequests;
import com.sofurry.chat.ChatActivity;
import com.sofurry.gallery.GalleryArt;
import com.sofurry.list.ListJournals;
import com.sofurry.list.ListMusic;
import com.sofurry.list.ListPM;
import com.sofurry.list.ListStories;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.services.BootVersionChecker;
import com.sofurry.services.OnAlarmReceiver;
import com.sofurry.services.OnBootReceiver;
import com.sofurry.util.Authentication;

import org.json.JSONObject;

import java.util.Date;


//~--- classes ----------------------------------------------------------------

/**
 * Main menu activity
 *
 * @author SoFurry
 */
public class MainMenu
        extends ActivityWithRequests {
    private Button buttonChat_;
    private Button buttonForums_;
    private Button buttonLogbook_;
    private Button buttonPMs_;
    private int    messageCount_ = -1;
    private long   lastCheck_    = -1;


    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param v
     */
    public void buttonClick(View v) {
        int    activityId = -1;
        int    id         = v.getId();
        Intent intent     = null;

        switch (id) {
            case R.id.pms:
                intent     = new Intent(this, ListPM.class);
                activityId = AppConstants.ACTIVITY_PMLIST;

                break;

            case R.id.chat:
                intent = new Intent(this, ChatActivity.class);

                break;

            case R.id.logbook:
                break;

            case R.id.stories:
                intent     = new Intent(this, ListStories.class);
                activityId = AppConstants.ACTIVITY_STORIESLIST;

                break;

            case R.id.art:
                intent     = new Intent(this, GalleryArt.class);
                activityId = AppConstants.ACTIVITY_GALLERYART;

                break;

            case R.id.music:
                intent     = new Intent(this, ListMusic.class);
                activityId = AppConstants.ACTIVITY_MUSICLIST;

                break;

            case R.id.journals:
                intent     = new Intent(this, ListJournals.class);
                activityId = AppConstants.ACTIVITY_JOURNALSLIST;

                break;

            case R.id.settings:
                intent     = new Intent(this, AccountActivity.class);
                activityId = AppConstants.ACTIVITY_SETTINGS;

                break;

            case R.id.forums:
                break;
        }

        // Check if we need to launch an activity
        if (intent != null) {
            // Will we be wanting the result?
            if (activityId > -1) {
                // Indeed we will
                startActivityForResult(intent, activityId);
            } else {
                // We don't care about the result, no
                startActivity(intent);
            }
        }
    }

    private void checkButtonDisabledState() {
        if ((Authentication.getUsername() == null) || (Authentication.getUsername().trim().length() <= 0)
                || (Authentication.getPassword() == null) || (Authentication.getPassword().trim().length() <= 0)) {
            buttonPMs_.setEnabled(false);
            buttonChat_.setEnabled(false);
        } else {
            buttonPMs_.setEnabled(true);
            buttonChat_.setEnabled(true);
        }
    }

    private void checkPmCount() {
        /*
         *  Limit how often it'll refresh itself, and only do so if information
         * has been filled out
         */
        if ((buttonPMs_.isEnabled()) && (new Date().getTime() > lastCheck_ + 300000)) {
            pbh.showProgressDialog("Fetching data...");
            getCheckParameters().execute(requesthandler);

            lastCheck_ = new Date().getTime();
        }
    }

    /**
     * Method description
     *
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check button state
        checkButtonDisabledState();

        // Check if we're coming from settings
        if (requestCode == AppConstants.ACTIVITY_SETTINGS) {
            // Check for PMs
            checkPmCount();
        }

        if (intent != null) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                // General error handling
                String errorMessage = extras.getString("errorMessage");

                if (errorMessage != null) {
                    new AlertDialog.Builder(MainMenu.this).setMessage(errorMessage).show();
                }

                switch (requestCode) {
                    case AppConstants.ACTIVITY_STORIESLIST:
                        break;

                    case AppConstants.ACTIVITY_PMLIST:
                        break;
                }
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve authentication info
        Authentication.loadAuthenticationInformation(this);

        // If the notification alarm service hasn't been scheduled, do so
        setupAlarmIfNeeded();

        // Set the content view
        setContentView(R.layout.mainmenu);

        // Find the buttons we'll be using
        buttonPMs_     = (Button) findViewById(R.id.pms);
        buttonChat_    = (Button) findViewById(R.id.chat);
        buttonLogbook_ = (Button) findViewById(R.id.logbook);
        buttonForums_  = (Button) findViewById(R.id.forums);

        // Disable forums and logbook buttons for now
        buttonForums_.setEnabled(false);
        buttonLogbook_.setEnabled(false);

        // Disable buttons as need be
        checkButtonDisabledState();

        // Retrieve information from the instance state object, if need be
        if (savedInstanceState != null) {
            messageCount_ = (Integer) retrieveObject("messageCount");

            updateButtons();
        } else {
            // Fetch the information from the server instead
            checkPmCount();
        }
    }

    /**
     * Method description
     *
     *
     * @param id
     * @param obj
     *
     * @throws Exception
     */
    @Override
    public void onData(int id, JSONObject obj) throws Exception {
        if (id == AppConstants.REQUEST_ID_FETCHDATA) {
            // Hide progress bar
            pbh.hideProgressDialog();

            // Fetch the number
            messageCount_ = obj.getInt("unreadpmcount");

            // Update display
            updateButtons();
        } else {
            super.onData(id, obj);
        }
    }

    /**
     * Method description
     *
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Check if we need to check for messages
        if (new Date().getTime() > lastCheck_ + 300000) {
            Log.i(AppConstants.TAG_STRING, "MainMenu: onResume check");
            checkPmCount();
        }
    }

    /**
     * Method description
     *
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        storeObject("messageCount", (Integer) messageCount_);
    }

    /**
     * Method description
     *
     */
    protected void setupAlarmIfNeeded() {
        Context            context       = getApplicationContext();
        BootVersionChecker bvc           = new BootVersionChecker();
        AlarmManager       manager       = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent             alarmIntent   = new Intent(context, OnAlarmReceiver.class);
        PendingIntent      pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        // Check if an alarm has been set for this version
        if (!bvc.hasLaunched(context)) {
            // It hasn't, so let's launch one.
        	// But first we'll be sure to cancel old ones, just in case.
        	manager.cancel(pendingIntent);
        	
        	// Then start schedule a new one
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                 SystemClock.elapsedRealtime() + AppConstants.ALARM_CHECK_DELAY_FIRST,
                                 AppConstants.ALARM_CHECK_DELAY_PERIOD,
                                 pendingIntent);

            // Tell the system about this newly set alarm
            bvc.setHasLaunched(context);
        }
    }

    /**
     * Method description
     *
     */
    protected void updateButtons() {
        // Alter the PM button based on the number of messages
        if (messageCount_ > 0) {
            buttonPMs_.setTypeface(Typeface.create((String) null, Typeface.BOLD));
        } else {
            buttonPMs_.setTypeface(Typeface.create((String) null, Typeface.NORMAL));
        }

        // Including the text
        buttonPMs_.setText("PMs (" + messageCount_ + ")");
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    protected AjaxRequest getCheckParameters() {
        AjaxRequest req = new AjaxRequest();

        // Set request parameters
        req.addParameter("f", "unreadpmcount");

        // Set request ID
        req.setRequestID(AppConstants.REQUEST_ID_FETCHDATA);

        // Return result
        return req;
    }
}