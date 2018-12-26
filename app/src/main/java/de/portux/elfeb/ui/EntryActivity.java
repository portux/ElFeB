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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.portux.elfeb.R;
import de.portux.elfeb.model.Attachment;
import de.portux.elfeb.model.Observation;
import de.portux.elfeb.model.Tag;
import de.portux.elfeb.services.LocationService;
import de.portux.elfeb.services.StorageService;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntryActivity extends AppCompatActivity {

  public static final String CALLEE = "callee";
  public static final String ATTACH_IMMEDIATELY = "instant_action";
  public static final String ATTACH_IMAGE = "capture_image";
  public static final String ATTACH_AUDIO = "record_audio";

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

  private TagViewModel mTagViewModel;
  private ObservationViewModel mObservationViewModel;

  private Uri mImageAttachment;
  private Uri mAudioAttachment;

  private EditText mSuspicion;
  private EditText mDescription;

  private Button mSaveObservation;
  private RecyclerView mTagSelectionView;
  private TagSelectionAdapter mTagSelectionAdapter;
  private RecyclerView.LayoutManager mTagSelectionLayoutManager;
  private FloatingActionButton mCaptureImageButton;
  private FloatingActionButton mRecordAudioButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_entry);

    mCallee = getIntent();

    if (mCallee.getStringExtra(CALLEE) != null //
        && mCallee.getStringExtra(CALLEE).equals(OverviewActivity.TAG) //
        && getActionBar() != null) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    mSuspicion = findViewById(R.id.edit_suspicion);
    mDescription = findViewById(R.id.edit_comment);

    mTagSelectionView = findViewById(R.id.recycler_tag_selection);
    mTagSelectionView.setHasFixedSize(true);

    mTagSelectionLayoutManager = new LinearLayoutManager(this);
    mTagSelectionView.setLayoutManager(mTagSelectionLayoutManager);

    mTagSelectionAdapter = new TagSelectionAdapter();
    mTagViewModel = ViewModelProviders.of(this).get(TagViewModel.class);
    mTagViewModel.getTags().observe(this, new SelectionAdapterTagInitializer());
    mTagSelectionView.setAdapter(mTagSelectionAdapter);

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

    if (VERSION.SDK_INT >= 23) {
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
    if (!mImmediateActionsProcessed.get()) {
      String immediateAttachment = mCallee.getStringExtra(ATTACH_IMMEDIATELY);
      if (immediateAttachment != null && immediateAttachment.equals(ATTACH_IMAGE)) {
        doCaptureImage();
      } else if (immediateAttachment != null && immediateAttachment.equals(ATTACH_AUDIO)) {
        doRecordAudio();
      }
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
        if (resultCode != RESULT_OK) {
          mImageAttachment = null;
        } else {
          mCaptureImageButton.hide();
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
        }
        break;
    }

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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
      File imageFile = mImageStorageService.convertUriToFile(this, mImageAttachment);
      result.attach(Attachment.forImage(imageFile, result));
    }

    if (mAudioAttachment != null) {
      Log.d(TAG, "Attachment: " + mAudioAttachment);
      File audioFile = mAudioStorageService.convertUriToFile(this, mAudioAttachment);
      Log.d(TAG, "File: " + audioFile);
      result.attach(Attachment.forAudio(audioFile, result));
    }

    return result;
  }

  private void finishNewEntry() {
    Observation observation = buildObservation();
    mObservationViewModel.insert(observation);
    Intent res = new Intent();
    res.putExtra("obsDate", observation.getTime());
    res.putExtra("obsSuspicion", observation.getSuspicion());
    setResult(RESULT_OK, res);
    finish();
  }

  private void doCaptureImage() {
    if (VERSION.SDK_INT >= 23) {
      requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE}, RQ_CAPTURE_IMAGE_STORAGE_PERMISSION);
      return;
    }
    callCaptureImageApp();
  }

  private void callCaptureImageApp() {
    Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (captureImageIntent.resolveActivity(getPackageManager()) != null) {
      Uri imageFile = mImageStorageService.createImageFile();
      captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile);
      startActivityForResult(captureImageIntent, RQ_CAPTURE_IMAGE);
      mImageAttachment = imageFile;
    }
  }

  private void doRecordAudio() {
    if (VERSION.SDK_INT >= 23) {
      requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.RECORD_AUDIO},
          RQ_RECORD_AUDIO_PERMISSIONS);
      return;
    }
    callRecordAudioApp();
  }

  private void callRecordAudioApp() {
    Intent recordAudioIntent = new Intent(Media.RECORD_SOUND_ACTION);
    if (recordAudioIntent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(recordAudioIntent, RQ_RECORD_AUDIO);
    }
  }

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
  };

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
      mSaveObservation.setEnabled(!s.toString().isEmpty() || mImageAttachment != null || mAudioAttachment != null);
    }
  };

  private final OnClickListener captureImage = new OnClickListener() {
    @Override
    public void onClick(View v) {
      doCaptureImage();
    }
  };

  private final OnClickListener recordAudio = new OnClickListener() {
    @Override
    public void onClick(View v) {
      doRecordAudio();
    }
  };

  private class SelectionAdapterTagInitializer implements Observer<List<Tag>> {

    @Override
    public void onChanged(@Nullable List<Tag> tags) {
      mTagSelectionAdapter.setTags(tags);
    }
  }

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
