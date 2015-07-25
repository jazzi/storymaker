package org.storymaker.app.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.test.internal.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.storymaker.app.AccountsActivity;
import org.storymaker.app.ConnectAccountActivity;
import org.storymaker.app.HomeActivity;
import org.storymaker.app.ProjectsActivity;
import org.storymaker.app.R;
import org.storymaker.app.SceneEditorActivity;
import org.storymaker.app.SimplePreferences;
import org.storymaker.app.StoryInfoEditActivity;
import org.storymaker.app.server.LoginActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import io.scal.secureshareui.login.SoundCloudLoginActivity;
import scal.io.liger.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;

/**
 * Created by mnbogner on 7/24/15.
 */
public class BaseTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    public HomeActivity mHomeActivity;
    public MainActivity mMainActivity;

    public Instrumentation.ActivityMonitor mVideoActivityMonitor;
    public Instrumentation.ActivityMonitor mAudioActivityMonitor;
    public Instrumentation.ActivityMonitor mPhotoActivityMonitor;
    public Instrumentation.ActivityMonitor mPassThroughMonitor0;
    public Instrumentation.ActivityMonitor mPassThroughMonitor1;
    public Instrumentation.ActivityMonitor mPassThroughMonitor2;
    public Instrumentation.ActivityMonitor mPassThroughMonitor3;
    public Instrumentation.ActivityMonitor mPassThroughMonitor4;
    public Instrumentation.ActivityMonitor mPassThroughMonitor5;
    public Instrumentation.ActivityMonitor mPassThroughMonitor6;
    public Instrumentation.ActivityMonitor mPassThroughMonitor7;
    public Instrumentation.ActivityMonitor mPassThroughMonitor8;
    public Instrumentation.ActivityMonitor mPassThroughMonitor9;

    public String testDirectory;

    public BaseTest() {
        super(HomeActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        Log.d("AUTOMATION", "BEGIN SETUP");

        mHomeActivity = getActivity();

        String sampleVideoName = "TEST_SAMPLE.mp4";
        String sampleAudioName = "TEST_SAMPLE.mp3";
        String samplePhotoName = "TEST_SAMPLE.jpg";

        // create references to sample files for dummy responses
        // NOTE: can these be refactored into uri's like "content://media/external/video/media/1258"
        String packageName = mHomeActivity.getApplicationContext().getPackageName();
        File root = Environment.getExternalStorageDirectory();
        testDirectory = root.toString() + "/Android/data/" + packageName + "/files/";
        String sampleVideo = testDirectory + sampleVideoName;
        String sampleAudio = testDirectory + sampleAudioName;
        String samplePhoto = testDirectory + samplePhotoName;

        // copy test files from assets
        InputStream assetIn = null;
        OutputStream assetOut = null;

        AssetManager assetManager = getInstrumentation().getContext().getAssets();

        File sampleVideoFile = new File(sampleVideo);

        try {
            assetIn = assetManager.open(sampleVideoName);

            assetOut = new FileOutputStream(sampleVideoFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = assetIn.read(buffer)) != -1) {
                assetOut.write(buffer, 0, read);
            }
            assetIn.close();
            assetIn = null;
            assetOut.flush();
            assetOut.close();
            assetOut = null;
            Log.d("AUTOMATION", "COPYING SAMPLE FILE " + sampleVideoName + " FROM ASSETS TO " + testDirectory + " FINISHED");
        } catch (IOException ioe) {
            Log.e("AUTOMATION", "COPYING SAMPLE FILE " + sampleVideoName + " FROM ASSETS TO " + testDirectory + " FAILED");
            return;
        }

        // check for zero-byte files
        if (sampleVideoFile.exists() && (sampleVideoFile.length() == 0)) {
            Log.e("AUTOMATION", "COPYING SAMPLE FILE " + sampleVideoName + " FROM ASSETS TO " + testDirectory + " FAILED (FILE WAS ZERO BYTES)");
            sampleVideoFile.delete();
        }

        // seems necessary to create activity monitors to prevent activities from being caught by other monitors
        mPassThroughMonitor0 = new Instrumentation.ActivityMonitor(HomeActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor0);
        mPassThroughMonitor1 = new Instrumentation.ActivityMonitor(MainActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor1);
        mPassThroughMonitor2 = new Instrumentation.ActivityMonitor(AccountsActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor2);
        mPassThroughMonitor3 = new Instrumentation.ActivityMonitor(StoryInfoEditActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor3);
        mPassThroughMonitor4 = new Instrumentation.ActivityMonitor(SoundCloudLoginActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor4);
        mPassThroughMonitor5 = new Instrumentation.ActivityMonitor(SceneEditorActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor5);
        mPassThroughMonitor6 = new Instrumentation.ActivityMonitor(ConnectAccountActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor6);
        mPassThroughMonitor7 = new Instrumentation.ActivityMonitor(ProjectsActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor7);
        mPassThroughMonitor8 = new Instrumentation.ActivityMonitor(SimplePreferences.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor8);
        mPassThroughMonitor9 = new Instrumentation.ActivityMonitor(LoginActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor9);

        // create activity monitors to intercept media capture requests
        IntentFilter videoFilter = new IntentFilter(MediaStore.ACTION_VIDEO_CAPTURE);
        Intent videoIntent = new Intent();
        Uri videoUri = Uri.parse(sampleVideo);
        videoIntent.setData(videoUri);
        Instrumentation.ActivityResult videoResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, videoIntent);
        mVideoActivityMonitor = new Instrumentation.ActivityMonitor(videoFilter, videoResult, true);
        getInstrumentation().addMonitor(mVideoActivityMonitor);

        IntentFilter photoFilter = new IntentFilter(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent photoIntent = new Intent();
        Uri photoUri = Uri.parse(samplePhoto);
        photoIntent.setData(photoUri);
        Instrumentation.ActivityResult photoResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, photoIntent);
        mPhotoActivityMonitor = new Instrumentation.ActivityMonitor(photoFilter, photoResult, true);
        getInstrumentation().addMonitor(mPhotoActivityMonitor);

        IntentFilter audioFilter = new IntentFilter(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        Intent audioIntent = new Intent();
        Uri audioUri = Uri.parse(sampleAudio);
        audioIntent.setData(audioUri);
        Instrumentation.ActivityResult audioResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, audioIntent);
        mAudioActivityMonitor = new Instrumentation.ActivityMonitor(audioFilter, audioResult, true);
        getInstrumentation().addMonitor(mAudioActivityMonitor);

        // clear out files from previous tests
        cleanup(testDirectory);

        Log.d("AUTOMATION", "SETUP COMPLETE");
    }

    public void tearDown() throws Exception {
        super.tearDown();

        Log.d("AUTOMATION", "BEGIN TEARDOWN");

        Log.d("AUTOMATION", "TEARDOWN COMPLETE");
    }

    public void cleanup(String directory) {

        Log.d("AUTOMATION", "BEGIN CLEANUP");

        WildcardFileFilter oldFileFilter = new WildcardFileFilter("*instance*");
        for (File oldFile : FileUtils.listFiles(new File(directory), oldFileFilter, null)) {
            Log.d("AUTOMATION", "FOUND " + oldFile.getPath() + ", DELETING");
            FileUtils.deleteQuietly(oldFile);
        }

        Log.d("AUTOMATION", "CLEANUP COMPLETE");
    }

    public void doEula() {

        Log.d("AUTOMATION", "BEGIN EULA");

        onView(withId(R.id.btnTos)).perform(click());
        Log.d("AUTOMATION", "CLICKED TOS BUTTON");

        onView(withText("Accept")).perform(click());
        Log.d("AUTOMATION", "CLICKED ACCEPT BUTTON");

        onView(withId(R.id.btnNoThanks)).perform(click());
        Log.d("AUTOMATION", "CLICKED NO THANKS BUTTON");

        Log.d("AUTOMATION", "EULA COMPLETE");
    }

    public void stall(long milliseconds, String message) {
        try {
            Log.d("AUTOMATION", "SLEEP " + (milliseconds / 1000) + " (" + message + ")");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class ActivityGetter implements Runnable
    {
        public void run() {
            Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
            if (resumedActivities.iterator().hasNext()){
                Object currentActivity = resumedActivities.iterator().next();

                if (currentActivity instanceof MainActivity) {
                    Log.d("AUTOMATION", "GOT MAIN ACTIVITY");
                    mMainActivity = (MainActivity)currentActivity;
                } else {
                    Log.d("AUTOMATION", "NOT MAIN ACTIVITY");
                }
            }
        }
    }

    public class ActivityScroller implements Runnable
    {
        int position = 0;

        public ActivityScroller (int position) {
            this.position = position;
        }

        public void run() {
            mMainActivity.scroll(position);
        }
    }

    public class HomeActivityScroller implements Runnable
    {
        int position = 0;

        public HomeActivityScroller (int position) {
            this.position = position;
        }

        public void run() {
            mHomeActivity.scroll(position);
        }
    }
}
