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

/**
 * The {@code adapter} sets up the connection to the observations {@link RecyclerView} in the {@link
 * OverviewActivity}.
 * <p>
 * As there may be quite many observations, they will not be displayed all at once but a pager will
 * be used instead. It will then load the observations on demand.
 *
 * @author Rico Bergmann
 */
class FieldNotesListAdapter extends PagedListAdapter<Observation, ObservationViewHolder> {

  /**
   * The {@code differ} takes care of comparing two observations.
   *
   * @see Observation#equals(Object)
   */
  static class ObservationDiffer extends DiffUtil.ItemCallback<Observation> {

    @Override
    public boolean areItemsTheSame(@NonNull Observation oldItem, @NonNull Observation newItem) {
      return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull Observation oldItem, @NonNull Observation newItem) {
      // observations are equal if they share the same date and suspicion
      // however these fields are also displayed in the fieldnotes so we call equals() again
      return oldItem.equals(newItem);
    }
  }

  /**
   * The {@code ViewHolder} for {@link Observation} instances to quickly reference the fields in the
   * fieldnotes list items.
   */
  class ObservationViewHolder extends RecyclerView.ViewHolder {

    TextView observationSuspicionText;
    ImageView determinedImage;
    TextView observationTimeText;
    ImageView pictureAttachedImage;
    ImageView recordingAttachedImage;

    /**
     * Default constructor.
     *
     * @param itemView the item in the {@link RecyclerView}
     */
    private ObservationViewHolder(@NonNull View itemView) {
      super(itemView);
      observationSuspicionText = itemView.findViewById(R.id.observation_suspicion);
      determinedImage = itemView.findViewById(R.id.image_determined);
      observationTimeText = itemView.findViewById(R.id.text_observation_time);
      pictureAttachedImage = itemView.findViewById(R.id.image_picture_attached);
      recordingAttachedImage = itemView.findViewById(R.id.image_recording_attached);
    }

    /**
     * Updates the item to be linked to the current observation.
     */
    void updateDisplayedObservation(@NonNull Observation observation) {
      observationSuspicionText.setTag(observation);
    }
  }

  private final LayoutInflater mInflater;
  private final SimpleDateFormat mDateFormatter;
  private final View.OnClickListener showObservation;

  private PagedList<Observation> mObservations;

  /**
   * Default constructor.
   *
   * @param ctx context for which the adapter should be created
   */
  FieldNotesListAdapter(@NonNull AppCompatActivity ctx) {
    super(new ObservationDiffer());
    this.mInflater = LayoutInflater.from(ctx);

    showObservation = view -> {
      TextView observationView = view.findViewById(R.id.observation_suspicion);
      Observation observation = (Observation) observationView.getTag();
      Intent showObservationIntent = new Intent(ctx, ObservationDetailsActivity.class);
      showObservationIntent
          .putExtra(ObservationDetailsActivity.INTENT_EXTRA_OBS_DATE, observation.getTime());
      showObservationIntent.putExtra(ObservationDetailsActivity.INTENT_EXTRA_OBS_SUSPICION,
          observation.getSuspicion());
      ctx.startActivity(showObservationIntent);
    };

    this.mDateFormatter = new SimpleDateFormat(ctx.getString(R.string.observation_date_format),
        ctx.getResources().getConfiguration().locale);
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

      if (current == null) {
        holder.observationSuspicionText.setText(R.string.unknown);
        holder.observationTimeText.setText(R.string.unknown);
        holder.determinedImage.setImageResource(R.drawable.ic_help_black_24dp);
        holder.recordingAttachedImage.setVisibility(View.GONE);
        holder.pictureAttachedImage.setVisibility(View.GONE);
        return;
      }

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
      holder.observationSuspicionText.setText(R.string.unknown);
    }
  }

  /**
   * Updates the observations displayed by {@code this} adapter.
   */
  void setObservations(@NonNull PagedList<Observation> observations) {
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
