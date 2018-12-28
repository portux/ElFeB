package de.portux.elfeb.ui;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import de.portux.elfeb.BuildConfig;
import de.portux.elfeb.R;
import de.portux.elfeb.model.Attachment;
import de.portux.elfeb.model.Observation;
import de.portux.elfeb.model.Tag;
import de.portux.elfeb.services.StorageService;
import java.io.File;
import java.util.Date;
import java.util.List;

public class ObservationDetailsActivity extends AppCompatActivity {

  /**
   * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
   * sections/tabs/pages.
   */
  public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case PAGE_DETAILS:
          mDetailsFragment = ObservationDetailsFragment.newInstance(mObservation);
          return mDetailsFragment;
        case PAGE_ATTACHMENTS:
          mAttachmentsFragment = ObservationAttachmentsFragment.newInstance(mObservation);
          return mAttachmentsFragment;
        default:
          throw new IllegalArgumentException("" + position);
      }
    }

    @Override
    public int getCount() {
      return 2;
    }
  }

  private static final String TAG = ObservationDetailsActivity.class.getSimpleName();
  private static final int PAGE_DETAILS = 0;
  private static final int PAGE_ATTACHMENTS = 1;
  private static final int RQ_CAPTURE_IMAGE = 111;
  private static final int RQ_RECORD_AUDIO = 222;
  private static final int RQ_CAPTURE_IMAGE_PERMISSIONS = 333;
  private static final int RQ_RECORD_AUDIO_PERMISSIONS = 444;

  /**
   * The {@link androidx.viewpager.widget.PagerAdapter} that will provide fragments for each of the
   * sections. We use a {@link FragmentPagerAdapter} derivative, which will keep every loaded
   * fragment in memory. If this becomes too memory intensive, it may be best to switch to a
   * androidx.fragment.app.FragmentStatePagerAdapter.
   */
  private SectionsPagerAdapter mSectionsPagerAdapter;

  /**
   * The {@link ViewPager} that will host the section contents.
   */
  private ViewPager mViewPager;

  private ObservationViewModel mObservationViewModel;
  private TagViewModel mTagViewModel;

  private StorageService.Images mImageStorageService;

  private FloatingActionButton mObservationDetermined;
  private FloatingActionButton mObservationUpdated;
  private FloatingActionButton mAddPicture;
  private FloatingActionButton mAddAudio;

  private ObservationDetailsFragment mDetailsFragment;
  private ObservationAttachmentsFragment mAttachmentsFragment;

  private MutableLiveData<Observation> mObservation = new MutableLiveData<>();

  private File mImageAttachment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_observation_details);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    // Create the adapter that will return a fragment for each of the three
    // primary sections of the activity.
    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    // Set up the ViewPager with the sections adapter.
    mViewPager = (ViewPager) findViewById(R.id.container);
    mViewPager.setAdapter(mSectionsPagerAdapter);

    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

    mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    mViewPager.addOnPageChangeListener(updateFloatingActionButtons);

    tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

    mObservationViewModel = ViewModelProviders.of(this).get(ObservationViewModel.class);
    mTagViewModel = ViewModelProviders.of(this).get(TagViewModel.class);


    mObservationViewModel.getAllObservations().observe(this, observations -> {
      Intent callee = getIntent();
      Date obsDate = (Date) callee.getSerializableExtra("obsDate");
      String obsSuspicion = callee.getStringExtra("obsSuspicion");

      for (Observation observation : observations) {
        if (observation.getTime().equals(obsDate) && observation.getSuspicion().equals(obsSuspicion)) {
          mObservation.setValue(observation);
          LiveData<List<Tag>> tags = mObservationViewModel.getTagsFor(observation);
          tags.observe(this, ts -> observation.tagIfNecessary(ts.toArray(new Tag[]{})));

          if (!observation.isDetermined()) {
            mObservationDetermined.show();
          }

          mObservationUpdated.show();
        }
      }
    });

    mObservationDetermined = findViewById(R.id.button_observation_determined);
    mObservationDetermined.setOnClickListener(markObservationDetermined);
    mObservationUpdated = findViewById(R.id.button_observation_updated);
    mObservationUpdated.setOnClickListener(updateObservation);
    mAddPicture = findViewById(R.id.button_observation_add_picture);
    mAddPicture.setOnClickListener(captureImage);
    mAddAudio = findViewById(R.id.button_observation_add_audio);
    mAddAudio.setOnClickListener(recordAudio);
    mObservationDetermined.hide();
    mObservationUpdated.hide();
    mAddPicture.hide();
    mAddAudio.hide();

    mImageStorageService = new StorageService.Images(this);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_observation_details, menu);

    mObservation.observe(this, observation -> {
      if (!observation.isLocationAttached()) {
        MenuItem item = menu.findItem(R.id.action_show_location);
        item.setEnabled(false);
        item.setVisible(false);
      }
    });

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_show_location) {
      final Observation observation = mObservation.getValue();
      if (observation != null) {
        // it is safe to get the coordinates here. The menu will only be shown if they are present.
        final double latitude = observation.getLocationLatitude();
        final double longitude = observation.getLocationLongitude();
        String position = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
        Intent showMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(position));

        if (showMapIntent.resolveActivity(getPackageManager()) != null) {
          startActivity(showMapIntent);
        } else {
          Toast noMapApp = Toast.makeText(this, R.string.no_maps_app, Toast.LENGTH_SHORT);
          noMapApp.show();
        }
      }

    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
      case RQ_CAPTURE_IMAGE:
        if (resultCode == RESULT_OK) {
          // at this point it is save to call getValue() as the capture image activity will only
          // be callable if the observation is present
          final Observation observation = mObservation.getValue();
          Attachment imageAttachment = Attachment.forImage(mImageAttachment, observation);
          mObservationViewModel.insertAttachment(imageAttachment);
          if (mAttachmentsFragment != null) {
            mAttachmentsFragment.displayImageAttachment(imageAttachment);
          }
        }
        mImageAttachment = null;
        break;
      case RQ_RECORD_AUDIO:
        if (resultCode == RESULT_OK) {
          Uri audioUri = data.getData();

          // at this point it is save to call getValue() as the capture image activity will only
          // be callable if the observation is present
          final Observation observation = mObservation.getValue();
          final StorageService.Audio audioStorageService = new StorageService.Audio(this);
          File audioFile = audioStorageService.convertUriToFile(this, audioUri);
          Attachment audioAttachment = Attachment.forAudio(audioFile, observation);
          mObservationViewModel.insertAttachment(audioAttachment);
          if (mAttachmentsFragment != null) {
            mAttachmentsFragment.displayAudioAttachment(audioAttachment);
          }
        }
        break;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    switch (requestCode) {
      case RQ_CAPTURE_IMAGE_PERMISSIONS:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          doCaptureImage();
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
          doRecordAudio();
        }
        break;
    }
  }

  private void updateObservation(Observation observation) {
    mDetailsFragment.updateObservation(observation);
    finish();
  }

  private void doCaptureImage() {
    Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (captureImageIntent.resolveActivity(getPackageManager()) != null) {
      File imageFile = mImageStorageService.createImageFile();
      Uri imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", imageFile);
      captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
      startActivityForResult(captureImageIntent, RQ_CAPTURE_IMAGE);
      mImageAttachment = imageFile;
    }
  }

  private void doRecordAudio() {
    Intent recordAudioIntent = new Intent(Media.RECORD_SOUND_ACTION);
    if (recordAudioIntent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(recordAudioIntent, RQ_RECORD_AUDIO);
    } else {
      Log.e(TAG, "No audio recorder found");
      mAddAudio.hide();
      Toast.makeText(this, R.string.no_recording_app, Toast.LENGTH_SHORT).show();
    }
  }

  private final OnPageChangeListener updateFloatingActionButtons = new OnPageChangeListener() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { ; }

    @Override
    public void onPageSelected(int position) {
      switch (position) {
        case PAGE_DETAILS:
          mObservationDetermined.show();
          mObservationUpdated.show();
          mAddPicture.hide();
          mAddAudio.hide();
          break;
        case PAGE_ATTACHMENTS:
          mObservationDetermined.hide();
          mObservationUpdated.hide();
          mAddPicture.show();
          mAddAudio.show();
      }
    }

    @Override
    public void onPageScrollStateChanged(int state) { ; }
  };

  private final Observer<Observation> markObservationDeterminedObserver = new Observer<Observation>() {
    @Override
    public void onChanged(Observation observation) {
      observation.markDetermined();
      ObservationDetailsActivity.this.updateObservation(observation);
      mObservation.removeObserver(this);
    }
  };

  private final FloatingActionButton.OnClickListener markObservationDetermined = new OnClickListener() {
    @Override
    public void onClick(View v) {
      mObservation.observe(ObservationDetailsActivity.this, markObservationDeterminedObserver);
    }
  };

  private final Observer<Observation> updateObservationObserver = new Observer<Observation>() {
    @Override
    public void onChanged(Observation observation) {
      ObservationDetailsActivity.this.updateObservation(observation);
      mObservation.removeObserver(this);
    }
  };

  private final FloatingActionButton.OnClickListener updateObservation = new OnClickListener() {
    @Override
    public void onClick(View v) {
      mObservation.observe(ObservationDetailsActivity.this, updateObservationObserver);
    }
  };

  private final Observer<Observation> captureImageObserver = new Observer<Observation>() {
    @Override
    public void onChanged(Observation observation) {
      mObservation.removeObserver(this);

      if (VERSION.SDK_INT >= 23) {
        requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE}, RQ_CAPTURE_IMAGE_PERMISSIONS);
        return;
      }
      doCaptureImage();
    }
  };

  private final FloatingActionButton.OnClickListener captureImage = new OnClickListener() {
    @Override
    public void onClick(View v) {
      mObservation.observe(ObservationDetailsActivity.this, captureImageObserver);
    }
  };

  private final Observer<Observation> recordAudioObserver = new Observer<Observation>() {
    @Override
    public void onChanged(Observation observation) {
      mObservation.removeObserver(this);

      if (VERSION.SDK_INT >= 23) {
        requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.RECORD_AUDIO}, RQ_RECORD_AUDIO_PERMISSIONS);
        return;
      }
      doRecordAudio();
    }
  };

  private final FloatingActionButton.OnClickListener recordAudio = new OnClickListener() {
    @Override
    public void onClick(View v) {
      mObservation.observe(ObservationDetailsActivity.this, recordAudioObserver);
    }
  };
}
