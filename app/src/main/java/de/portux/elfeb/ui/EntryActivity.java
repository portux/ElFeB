package de.portux.elfeb.ui;

import android.Manifest.permission;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.portux.elfeb.BuildConfig;
import de.portux.elfeb.R;
import de.portux.elfeb.model.Attachment;
import de.portux.elfeb.model.GPSPosition;
import de.portux.elfeb.model.Observation;
import de.portux.elfeb.model.Tag;
import de.portux.elfeb.services.LocationService;
import de.portux.elfeb.services.StorageService;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code EntryActivity} enables the creation of new observations and adds them to the
 * fieldnotes.
 * <p>
 * To each observation up to one image or audio attachment may be added. Furthermore the observation
 * may be tagged with an arbitrary number of existing tags.
 * <p>
 * This activity may be supplied with a number of options through the calling intent. These options
 * are:
 * <ul>
 * <li>{@link #INTENT_EXTRA_CALLEE} - to indicate where the intent came from - we may try to set up
 * Up-Navigation if it was created from another Activity within this App. Values should mostly be
 * the {@code TAG}s of the calling components.</li>
 * <li>{@link #INTENT_EXTRA_ATTACH_IMMEDIATELY} - to instantly hand over control to the services
 * which take care of  creating the attachments. Possible values are {@link #ATTACH_IMAGE} or {@link
 * #ATTACH_AUDIO}.
 * </li>
 * </ul>
 * <p>
 * When an {@link Observation} was created successfully its timestamp (identified through {@link
 * #INTENT_EXTRA_OBS_DATE}) and suspicion (identified through {@link #INTENT_EXTRA_OBS_SUSPICION})
 * will be added to the resulting intent.
 *
 * @author Rico Bergmann
 * @see Observation
 */
public class EntryActivity extends AppCompatActivity {

  /**
   * Intent extra to identify the component that started {@code this} Activity.
   */
  public static final String INTENT_EXTRA_CALLEE = "callee";

  /**
   * Intent extra to specify an Attachment type that should be added to the new {@link Observation}
   * immediately.
   * <p>
   * Possible values are {@link #ATTACH_IMAGE} or {@link #ATTACH_AUDIO} (or {@code null} to indicate
   * the absence of such a command).
   *
   * @see Attachment
   */
  public static final String INTENT_EXTRA_ATTACH_IMMEDIATELY = "instant_action";

  /**
   * Indicates that when {@code this} Activity is started it should prepare to capture an image of
   * the new observation immediately. This is useful if the image has to be taken quickly and the
   * extra click can therefore be omitted.
   */
  public static final String ATTACH_IMAGE = "capture_image";

  /**
   * Indicates that when {@code this} Activity is started it should prepare to record an audio
   * sample of the new observation immediately. This is useful if the audio sample has to be taken
   * quickly and the extra click can therefore be omitted.
   */
  public static final String ATTACH_AUDIO = "record_audio";

  /**
   * Intent extra to specify the date of the observation that has just been created. This will be
   * present in the resulting intent handed over to the callee.
   */
  public static final String INTENT_EXTRA_OBS_DATE = "obsDate";

  /**
   * Intent extra to specify the suspicion of the observation that has just been created. This will
   * be present in the resulting intent handed over to the callee.
   */
  public static final String INTENT_EXTRA_OBS_SUSPICION = "obsSuspicion";

  private static final String TAG = EntryActivity.class.getSimpleName();
  private static final int RQ_CAPTURE_IMAGE = 111;
  private static final int RQ_RECORD_AUDIO = 222;
  private static final int RQ_CAPTURE_IMAGE_STORAGE_PERMISSION = 333;
  private static final int RQ_RECORD_AUDIO_PERMISSIONS = 444;
  private static final int RQ_LOCATION_PERMISSION = 555;

  private Intent mCallee;
  private AtomicBoolean mImmediateActionsProcessed = new AtomicBoolean(false);
  private LocationService mLocationService;
  private boolean mLocationServiceBound = false;

  private StorageService.Images mImageStorageService;
  private StorageService.Audio mAudioStorageService;

  private ObservationViewModel mObservationViewModel;

  private File mImageAttachment;
  private Uri mAudioAttachment;

  private EditText mSuspicion;
  private EditText mDescription;

  private Button mSaveObservation;
  private TagSelectionAdapter mTagSelectionAdapter;
  private FloatingActionButton mCaptureImageButton;
  private FloatingActionButton mRecordAudioButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_entry);

    mCallee = getIntent();

    // set-up Up-Navigation is necessary
    if (mCallee.getStringExtra(INTENT_EXTRA_CALLEE) != null //
        && mCallee.getStringExtra(INTENT_EXTRA_CALLEE).equals(OverviewActivity.TAG) //
        && getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    } else if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    if (savedInstanceState != null) {
      mImageAttachment = (File) savedInstanceState.getSerializable("mImageAttachment");
    }

    // set-up all controls

    mSuspicion = findViewById(R.id.edit_suspicion);
    mDescription = findViewById(R.id.edit_comment);

    final RecyclerView tagSelectionView = findViewById(R.id.recycler_tag_selection);
    tagSelectionView.setHasFixedSize(true);

    final RecyclerView.LayoutManager tagSelectionLayoutManager = new LinearLayoutManager(this);
    tagSelectionView.setLayoutManager(tagSelectionLayoutManager);

    mTagSelectionAdapter = new TagSelectionAdapter();
    final TagViewModel tagViewModel = ViewModelProviders.of(this).get(TagViewModel.class);
    tagViewModel.getTags().observe(this, new SelectionAdapterTagInitializer());
    tagSelectionView.setAdapter(mTagSelectionAdapter);

    mSaveObservation = findViewById(R.id.button_save_observation);
    mSaveObservation.setOnClickListener(new SaveNewObservationClickListener());

    mObservationViewModel = ViewModelProviders.of(this).get(ObservationViewModel.class);

    mImageStorageService = new StorageService.Images(this);
    mAudioStorageService = new StorageService.Audio(this);

    mCaptureImageButton = findViewById(R.id.button_add_photo);
    mRecordAudioButton = findViewById(R.id.button_add_audio);

    mCaptureImageButton.setOnClickListener(captureImage);
    mRecordAudioButton.setOnClickListener(recordAudio);

    mSaveObservation.setEnabled(false);
    mSuspicion.addTextChangedListener(disableSaveObservationIfNoSuspicion);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // we will try to connect to the location service to provide us with GPS data

    if (VERSION.SDK_INT >= 23
        && checkSelfPermission(permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{permission.ACCESS_FINE_LOCATION}, RQ_LOCATION_PERMISSION);
      return;
    }
    bindToLocationService();
  }

  private void bindToLocationService() {
    Intent locationService = new Intent(this, LocationService.class);
    bindService(locationService, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");

    // if necessary we will start the 'take photo' or 'record audio' immediate actions here

    if (!mImmediateActionsProcessed.get()) {
      String immediateAttachment = mCallee.getStringExtra(INTENT_EXTRA_ATTACH_IMMEDIATELY);
      if (immediateAttachment != null && immediateAttachment.equals(ATTACH_IMAGE)) {
        doCaptureImage();
      } else if (immediateAttachment != null && immediateAttachment.equals(ATTACH_AUDIO)) {
        doRecordAudio();
      }

      // no matter if we did execute an action, we at least processed whether there was one
      mImmediateActionsProcessed.set(true);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    unbindService(mLocationServiceConnection);
    mLocationServiceBound = false;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
      case RQ_CAPTURE_IMAGE:
        // the mImageAttachment member was set before calling the camera App. Thus we only need
        // to clean up if necessary.
        if (resultCode != RESULT_OK) {
          mImageAttachment = null;
          Log.e(TAG, "Error while taking image");
        } else {
          mCaptureImageButton.hide();

          if (data != null && data.getData() != null) {
            final Uri actualUri = data.getData();
            final Uri expectedUri = StorageService.resolveUriFor(mImageAttachment, this);
            if (!actualUri.equals(expectedUri)) {
              String logMsg = String
                  .format("Expected URI %s does not match actual URI %s", expectedUri.getPath(),
                      actualUri.getPath());
              Log.d(TAG, logMsg);
              mImageAttachment = new File(actualUri.getPath());
            }
          }

          sendImageToGalleries();

          // if an image was attached we will also allow observations with no suspicion
          mSaveObservation.setEnabled(true);
        }
        break;
      case RQ_RECORD_AUDIO:
        if (resultCode != RESULT_OK || data == null) {
          mAudioAttachment = null;
        } else {
          mAudioAttachment = data.getData();
          mSaveObservation.setEnabled(true);
          mRecordAudioButton.hide();

          // if a recording was attached we will also allow observations with no suspicion
          mSaveObservation.setEnabled(true);
        }
        break;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // the workflow is the same for all permission requests: if the request was successful we will
    // continue with the action that was interrupted by the request. Otherwise the request will just
    // be dropped

    switch (requestCode) {
      case RQ_LOCATION_PERMISSION:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          bindToLocationService();
        }
        break;
      case RQ_CAPTURE_IMAGE_STORAGE_PERMISSION:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          callCaptureImageApp();
        }
        break;
      case RQ_RECORD_AUDIO_PERMISSIONS:
        boolean allGranted = true;
        for (int grantResult : grantResults) {
          if (grantResult != PackageManager.PERMISSION_GRANTED) {
            allGranted = false;
          }
        }
        if (allGranted) {
          callRecordAudioApp();
        }
        break;
    }

  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putSerializable("mImageAttachment", mImageAttachment);
    super.onSaveInstanceState(outState);
  }

  /**
   * Initiates the connection to the {@link LocationService}.
   */
  private void bindToLocationService() {
    Intent locationService = new Intent(this, LocationService.class);
    bindService(locationService, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
  }

  /**
   * Sends an intent to notify all apps about the current image file.
   */
  private void sendImageToGalleries() {
    if (mImageAttachment != null) {
      Log.d(TAG, "Sending image to galleries: " + mImageAttachment);
      Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
      Uri imageUri = StorageService.resolveUriFor(mImageAttachment, this);
      mediaScanIntent.setData(imageUri);
      sendBroadcast(mediaScanIntent);
    }
  }

  /**
   * Extracts the current values of the inputs and creates the corresponding {@link Observation}
   * instance.
   */
  private Observation buildObservation() {
    Observation result = Observation
        .noticeNew(mSuspicion.getText().toString(), mDescription.getText().toString());

    if (mLocationServiceBound && mLocationService.isLocated()) {
      result.attachLocation(mLocationService.getCurrentLocationAsGPSPosition());
    }

    for (Tag tag : mTagSelectionAdapter.getTags()) {
      if (mTagSelectionAdapter.isSelected(tag)) {
        result.tagWith(tag);
      }
    }

    if (mImageAttachment != null) {
      result.attach(Attachment.forImage(mImageAttachment, result));
    }

    if (mAudioAttachment != null) {
      Log.d(TAG, "Attachment: " + mAudioAttachment);
      File audioFile = mAudioStorageService.convertUriToFile(this, mAudioAttachment);
      Log.d(TAG, "File: " + audioFile);
      result.attach(Attachment.forAudio(audioFile, result));
    }

    return result;
  }

  /**
   * Creates the observation instance and finishes the activity with the correct result.
   */
  private void finishNewEntry() {
    Observation observation = buildObservation();
    mObservationViewModel.insert(observation);
    Intent res = new Intent();
    res.putExtra(INTENT_EXTRA_OBS_DATE, observation.getTime());
    res.putExtra(INTENT_EXTRA_OBS_SUSPICION, observation.getSuspicion());
    setResult(RESULT_OK, res);
    finish();
  }

  /**
   * Safely calls the camera App to take a photo of the observation. If the necessary permissions
   * have not been granted, they will be requested.
   *
   * @see #callCaptureImageApp()
   */
  private void doCaptureImage() {
    if (VERSION.SDK_INT >= 23
        && checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE},
          RQ_CAPTURE_IMAGE_STORAGE_PERMISSION);
      return;
    }
    callCaptureImageApp();
  }

  /**
   * Actually calls the camera App to take a photo of the observation, assuming that all permissions
   * have been granted.
   */
  private void callCaptureImageApp() {
    Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (captureImageIntent.resolveActivity(getPackageManager()) != null) {
      GPSPosition currentPosition = mLocationServiceBound //
          ? mLocationService.getCurrentLocationAsGPSPosition() //
          : null;
      File imageFile = mImageStorageService.createImageFile(currentPosition);
      Uri imageUri = StorageService.resolveUriFor(imageFile, this);
      captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
      startActivityForResult(captureImageIntent, RQ_CAPTURE_IMAGE);
      mImageAttachment = imageFile;
    }
  }

  /**
   * Safely calls the recorder App to add an audio attachment to the observation. If the necessary
   * permissions have not been granted, they will be requested.
   *
   * @see #callRecordAudioApp()
   */
  private void doRecordAudio() {
    if (VERSION.SDK_INT >= 23 && (checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED
        || checkSelfPermission(permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
      requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.RECORD_AUDIO},
          RQ_RECORD_AUDIO_PERMISSIONS);
      return;
    }
    callRecordAudioApp();
  }

  /**
   * Actually calls the recorder App, assuming that all permission have been granted.
   */
  private void callRecordAudioApp() {
    Intent recordAudioIntent = new Intent(Media.RECORD_SOUND_ACTION);
    if (recordAudioIntent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(recordAudioIntent, RQ_RECORD_AUDIO);
    } else {
      // many audio recorders do not set-up their intent filters correctly, thus the intent
      // may easily not be resolvable
      Log.e(TAG, "No audio recorder found");
      mRecordAudioButton.hide();
      Toast.makeText(this, R.string.no_recording_app, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Connection to the {@link LocationService} which takes care of the whole connection management.
   */
  private final ServiceConnection mLocationServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      Log.d(TAG, "LocationService connected");
      LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) service;
      mLocationService = binder.getService();
      mLocationServiceBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      Log.d(TAG, "LocationService disconnected");
      mLocationServiceBound = false;
    }

    @Override
    public String toString() {
      return "LocationServiceConntection: " + super.toString();
    }
  };

  /**
   * Listener to disable the 'save observation' button if the suspicion is empty and no attachments
   * have been provided.
   */
  private final TextWatcher disableSaveObservationIfNoSuspicion = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      // pass
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      // pass
    }

    @Override
    public void afterTextChanged(Editable s) {
      mSaveObservation.setEnabled(
          !s.toString().isEmpty() || mImageAttachment != null || mAudioAttachment != null);
    }
  };

  /**
   * Listener to run the image capture logic if the capture button has been clicked.
   */
  private final OnClickListener captureImage = __ -> doCaptureImage();

  /**
   * Listener to run the audio recording logic if the record button has been clicked.
   */
  private final OnClickListener recordAudio = __ -> doRecordAudio();

  /**
   * Observer to propagate the tags that were loaded by the {@link TagViewModel} to the tag
   * selection recycler.
   *
   * @see TagSelectionAdapter
   */
  private class SelectionAdapterTagInitializer implements Observer<List<Tag>> {

    @Override
    public void onChanged(@Nullable List<Tag> tags) {
      mTagSelectionAdapter.setTags(tags);
    }
  }

  /**
   * Listener to run the save observation logic when the corresponding button has been clicked.
   */
  private class SaveNewObservationClickListener implements OnClickListener {

    @Override
    public void onClick(View view) {
      if (!mLocationServiceBound || !mLocationService.isLocated()) {
        AlertDialog.Builder confirmSkipLocalization = new AlertDialog.Builder(
            EntryActivity.this);
        confirmSkipLocalization.setMessage(R.string.location_not_yet_found);
        confirmSkipLocalization.setPositiveButton(R.string.wait,
            (dialog, __) -> dialog.dismiss()
        );
        confirmSkipLocalization.setNegativeButton(R.string.continue_anyway,
            (__, ___) -> EntryActivity.this.finishNewEntry()
        );
        confirmSkipLocalization.show();
      } else {
        finishNewEntry();
      }
    }
  }
}
