package de.portux.elfeb.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import de.portux.elfeb.R;
import de.portux.elfeb.model.Attachment;
import de.portux.elfeb.model.Observation;
import java.util.ArrayList;
import java.util.List;

public class ObservationAttachmentsFragment extends Fragment {

  private static final String TAG = ObservationAttachmentsFragment.class.getSimpleName();

  public static class ImageAttachmentViewHolder extends RecyclerView.ViewHolder {
    TextView mImageNameText;
    ImageView mObservationImage;
    ImageButton mDeleteImageButton;

    public ImageAttachmentViewHolder(@NonNull View itemView) {
      super(itemView);
      mImageNameText = itemView.findViewById(R.id.text_observation_image_name);
      mObservationImage = itemView.findViewById(R.id.image_observation_image);
      mDeleteImageButton = itemView.findViewById(R.id.button_observation_image_delete);
    }
  }

  public static class ImageAttachmentsAdapter extends
      RecyclerView.Adapter<ImageAttachmentViewHolder> {

    private ObservationAttachmentsFragment mFragment;
    private ObservationViewModel mViewModel;
    private Observation mObservation;
    private List<Attachment> mImages;

    private final OnClickListener openImageListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        Attachment image = (Attachment) v.getTag(R.id.tag_image);
        Intent showImageIntent = new Intent(Intent.ACTION_VIEW);
        showImageIntent.setDataAndType(Uri.fromFile(image.getPath()), "image/*");
        if (showImageIntent.resolveActivity(mFragment.getActivity().getPackageManager()) != null) {
          mFragment.startActivity(showImageIntent);
        }
      }
    };

    private final OnClickListener imageDeletionListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        Attachment image = (Attachment) v.getTag(R.id.tag_image);
        mViewModel.removeAttachmentFrom(mObservation, image);
        mImages.remove(image);
        notifyDataSetChanged();
      }
    };

    public ImageAttachmentsAdapter(ObservationAttachmentsFragment fragment,
        Observation mObservation) {
      this.mFragment = fragment;
      this.mViewModel = ViewModelProviders.of(fragment).get(ObservationViewModel.class);
      this.mObservation = mObservation;
    }

    @NonNull
    @Override
    public ImageAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()) //
          .inflate(R.layout.observation_image, parent, false);
      ImageAttachmentViewHolder vh = new ImageAttachmentViewHolder(v);
      vh.mObservationImage.setOnClickListener(openImageListener);
      vh.mDeleteImageButton.setOnClickListener(imageDeletionListener);
      return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAttachmentViewHolder holder, int position) {
      if (mImages != null) {
        Attachment image = mImages.get(position);
        String fileName = image.getPath().getName();
        Resources res = mFragment.getResources();
        holder.mDeleteImageButton.setTag(R.id.tag_image, image);
        holder.mImageNameText.setText(res.getString(R.string.observation_image_title, fileName));
        holder.mObservationImage.setTag(R.id.tag_image, image);
        holder.mObservationImage.setImageURI(Uri.fromFile(image.getPath()));
      }
    }

    @Override
    public int getItemCount() {
      if (mImages != null) {
        return mImages.size();
      } else {
        return 0;
      }
    }

    void setImages(List<Attachment> images) {
      mImages = images;
      notifyDataSetChanged();
    }

    void addImage(Attachment image) {
      mImages.add(image);
      notifyDataSetChanged();
    }
  }

  public static class AudioAttachmentViewHolder extends RecyclerView.ViewHolder {
    TextView mRecordingTitleText;
    ImageButton mPlayRecordingButton;
    ImageButton mDeleteRecordingButton;

    public AudioAttachmentViewHolder(@NonNull View itemView) {
      super(itemView);
      mRecordingTitleText = itemView.findViewById(R.id.text_observation_audio_name);
      mPlayRecordingButton = itemView.findViewById(R.id.button_observation_audio_play);
      mDeleteRecordingButton = itemView.findViewById(R.id.button_observation_audio_delete);
    }
  }

  public static class AudioAttachmentsAdapter extends
      RecyclerView.Adapter<AudioAttachmentViewHolder> {
    private ObservationAttachmentsFragment mFragment;
    private ObservationViewModel mObservationViewModel;
    private Observation mObservation;
    private List<Attachment> mRecordings;

    private final OnClickListener playRecording = new OnClickListener() {
      @Override
      public void onClick(View v) {
        Attachment recording = (Attachment) v.getTag(R.id.tag_recording);
        Intent playRecordingIntent = new Intent(Intent.ACTION_VIEW);
        playRecordingIntent.setDataAndType(Uri.fromFile(recording.getPath()), "audio/*");
        if (playRecordingIntent.resolveActivity(mFragment.getActivity().getPackageManager()) != null) {
          mFragment.startActivity(playRecordingIntent);
        }
      }
    };

    private final OnClickListener deleteRecording = new OnClickListener() {
      @Override
      public void onClick(View v) {
        Attachment recording = (Attachment) v.getTag(R.id.tag_recording);
        mObservationViewModel.removeAttachmentFrom(mObservation, recording);
        mRecordings.remove(recording);
        notifyDataSetChanged();
      }
    };

    public AudioAttachmentsAdapter(ObservationAttachmentsFragment fragment,
        Observation observation) {
      mFragment = fragment;
      mObservationViewModel = ViewModelProviders.of(fragment).get(ObservationViewModel.class);
      mObservation = observation;
    }

    @NonNull
    @Override
    public AudioAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()) //
          .inflate(R.layout.observation_audio, parent, false);
      AudioAttachmentViewHolder vh = new AudioAttachmentViewHolder(v);
      vh.mPlayRecordingButton.setOnClickListener(playRecording);
      vh.mDeleteRecordingButton.setOnClickListener(deleteRecording);
      return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull AudioAttachmentViewHolder holder, int position) {
      if (mRecordings != null) {
        Attachment recording = mRecordings.get(position);
        Resources res = mFragment.getResources();
        String fileName = recording.getPath().getName();
        holder.mPlayRecordingButton.setTag(R.id.tag_recording, recording);
        holder.mDeleteRecordingButton.setTag(R.id.tag_recording, recording);
        holder.mRecordingTitleText.setText(res.getString(R.string.observation_audio_title, fileName));
      }
    }

    @Override
    public int getItemCount() {
      if (mRecordings != null) {
        return mRecordings.size();
      } else {
        return 0;
      }
    }

    void setRecordings(List<Attachment> recordings) {
      mRecordings = recordings;
      notifyDataSetChanged();
    }

    void addRecording(Attachment recording) {
      mRecordings.add(recording);
      notifyDataSetChanged();
    }
  }

  public static ObservationAttachmentsFragment newInstance(LiveData<Observation> observation) {
    ObservationAttachmentsFragment fragment = new ObservationAttachmentsFragment();
    fragment.mObservation = observation;
    fragment.mObservation.observe(fragment, fragment::initView);
    return fragment;
  }

  private ObservationViewModel mObservationViewModel;

  private RecyclerView mImagesRecycler;
  private RecyclerView mAudioRecordingsRecycler;
  private ImageAttachmentsAdapter mImagesAdapter;
  private AudioAttachmentsAdapter mAudioAdapter;

  private LiveData<Observation> mObservation;

  public ObservationAttachmentsFragment() {
    ;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_observation_attachments, container, false);

    mObservationViewModel = ViewModelProviders.of(this).get(ObservationViewModel.class);

    mImagesRecycler = rootView.findViewById(R.id.recycler_observation_images);
    mAudioRecordingsRecycler = rootView.findViewById(R.id.recycler_observation_audio);

    final LayoutManager imagesRecyclerLayoutManager = new LinearLayoutManager(this.getContext());
    final LayoutManager recordingsRecyclerLayoutManager = new LinearLayoutManager(this.getContext());
    mImagesRecycler.setLayoutManager(imagesRecyclerLayoutManager);
    mAudioRecordingsRecycler.setLayoutManager(recordingsRecyclerLayoutManager);

    return rootView;
  }

  public void displayImageAttachment(Attachment picture) {
    mObservation.observe(this, __ -> mImagesAdapter.addImage(picture));
  }

  public void displayAudioAttachment(Attachment recording) {
    mObservation.observe(this, __ -> mAudioAdapter.addRecording(recording));
  }

  private void initView(Observation observation) {

    mObservationViewModel.getAttachmentsFor(observation).observe(this, attachments -> {
      List<Attachment> imageAttachments = new ArrayList<>((int) (attachments.size() * 0.75));
      List<Attachment> audioAttachments = new ArrayList<>((int) (attachments.size() * 0.25));

      for (Attachment attachment : attachments) {
        switch (attachment.getType()) {
          case IMAGE:
            imageAttachments.add(attachment);
            break;
          case AUDIO:
            audioAttachments.add(attachment);
            break;
        }
      }

      Log.d(TAG, "Found attachments: " + imageAttachments);

      mImagesAdapter = new ImageAttachmentsAdapter(this, observation);
      mImagesAdapter.setImages(imageAttachments);
      mImagesRecycler.setAdapter(mImagesAdapter);
      mAudioAdapter = new AudioAttachmentsAdapter(this, observation);
      mAudioAdapter.setRecordings(audioAttachments);
      mAudioRecordingsRecycler.setAdapter(mAudioAdapter);
    });
  }

}
