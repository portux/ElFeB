package de.portux.elfeb.ui;

import android.content.Intent;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.portux.elfeb.R;
import de.portux.elfeb.model.Observation;
import de.portux.elfeb.ui.FieldNotesListAdapter.ObservationViewHolder;
import java.text.SimpleDateFormat;

public class FieldNotesListAdapter extends PagedListAdapter<Observation, ObservationViewHolder> {

  static class ObservationDiffer extends DiffUtil.ItemCallback<Observation> {

    @Override
    public boolean areItemsTheSame(Observation oldItem, Observation newItem) {
      return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(Observation oldItem, Observation newItem) {
      return oldItem.equals(newItem);
    }
  }

  class ObservationViewHolder extends RecyclerView.ViewHolder {
    TextView observationSuspicionText;
    ImageView determinedImage;
    TextView observationTimeText;
    ImageView pictureAttachedImage;
    ImageView recordingAttachedImage;

    private Observation displayedObservation;

    private ObservationViewHolder(View itemView) {
      super(itemView);
      observationSuspicionText = itemView.findViewById(R.id.observation_suspicion);
      determinedImage = itemView.findViewById(R.id.image_determined);
      observationTimeText = itemView.findViewById(R.id.text_observation_time);
      pictureAttachedImage = itemView.findViewById(R.id.image_picture_attached);
      recordingAttachedImage = itemView.findViewById(R.id.image_recording_attached);
    }

    void updateDisplayedObservation(Observation observation) {
      observationSuspicionText.setTag(observation);
    }

  }

  private final LayoutInflater mInflater;
  private final SimpleDateFormat mDateFormatter;
  private final View.OnClickListener showObservation;

  private PagedList<Observation> mObservations;

  FieldNotesListAdapter(AppCompatActivity ctx) {
    super(new ObservationDiffer());
    this.mInflater = LayoutInflater.from(ctx);

    showObservation = view -> {
      TextView observationView = view.findViewById(R.id.observation_suspicion);
      Observation observation = (Observation) observationView.getTag();
      Intent showObservationIntent = new Intent(ctx, ObservationDetailsActivity.class);
      showObservationIntent.putExtra("obsDate", observation.getTime());
      showObservationIntent.putExtra("obsSuspicion", observation.getSuspicion());
      ctx.startActivity(showObservationIntent);
    };

    this.mDateFormatter = new SimpleDateFormat(ctx.getString(R.string.observation_date_format), ctx.getResources().getConfiguration().locale);
  }

  @NonNull
  @Override
  public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = mInflater.inflate(R.layout.fieldnotes_item, parent, false);
    itemView.setOnClickListener(showObservation);

    return new ObservationViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
    if (mObservations != null) {
      Observation current = mObservations.get(position);
      holder.observationSuspicionText.setText(current.getSuspicion());

      if (current.isDetermined()) {
        holder.determinedImage.setImageResource(R.drawable.ic_check_circle_black_24dp);
      } else {
        holder.determinedImage.setImageResource(R.drawable.ic_help_black_24dp);
      }

      holder.observationTimeText.setText(mDateFormatter.format(current.getTime()));

      if (current.isImageAttached()) {
        holder.pictureAttachedImage.setVisibility(View.VISIBLE);
      } else {
        holder.pictureAttachedImage.setVisibility(View.GONE);
      }

      if (current.isRecordingAttached()) {
        holder.recordingAttachedImage.setVisibility(View.VISIBLE);
      } else {
        holder.recordingAttachedImage.setVisibility(View.GONE);
      }

      holder.updateDisplayedObservation(current);
    } else {
      holder.observationSuspicionText.setText("");
    }
  }

  void setObservations(PagedList<Observation> observations) {
    this.mObservations = observations;
    notifyDataSetChanged();
  }

  @Override
  public int getItemCount() {
    if (mObservations != null) {
      return mObservations.size();
    } else {
      return 0;
    }
  }
}
