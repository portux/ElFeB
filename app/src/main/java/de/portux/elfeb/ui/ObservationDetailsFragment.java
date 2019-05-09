package de.portux.elfeb.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import de.portux.elfeb.R;
import de.portux.elfeb.model.Observation;
import de.portux.elfeb.model.Tag;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ObservationDetailsFragment extends Fragment {

  private ObservationViewModel mObservationViewModel;
  private TagViewModel mTagViewModel;

  private TextView mLocationText;
  private EditText mEditSuspicion;
  private EditText mEditComment;
  private RecyclerView mRecyclerTags;

  private TagSelectionAdapter mTagsAdapter;

  private LiveData<Observation> mObservation;

  private final Observer<List<Tag>> updateTagsObserver = new Observer<List<Tag>>() {
    @Override
    public void onChanged(List<Tag> tags) {
      // at this point, we know that the observation is present
      Observation observation = mObservation.getValue();
      List<Tag> selectedTags = new ArrayList<>(tags.size());
      for (Tag tag : tags) {
        if (mTagsAdapter.isSelected(tag)) {
          selectedTags.add(tag);
        }
      }
      mObservationViewModel.updateTags(observation, selectedTags);
    }
  };

  /**
   * Returns a new instance of this fragment for the given section number.
   */
  public static ObservationDetailsFragment newInstance(LiveData<Observation> observation) {
    ObservationDetailsFragment fragment = new ObservationDetailsFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    fragment.mObservation = observation;
    fragment.mObservation.observe(fragment, fragment::initView);
    return fragment;
  }

  public ObservationDetailsFragment() {}

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_observation_details, container, false);

    mObservationViewModel = ViewModelProviders.of(this).get(ObservationViewModel.class);
    mTagViewModel = ViewModelProviders.of(this).get(TagViewModel.class);

    mLocationText = rootView.findViewById(R.id.text_observation_details_location);
    mEditSuspicion = rootView.findViewById(R.id.edit_observation_details_suspicion);
    mEditComment = rootView.findViewById(R.id.edit_observation_details_comment);
    mRecyclerTags = rootView.findViewById(R.id.recycler_observation_details_tags);

    final LayoutManager tagLayoutManager = new LinearLayoutManager(this.getContext());
    mRecyclerTags.setLayoutManager(tagLayoutManager);

    final Button createTagButton = rootView.findViewById(R.id.button_observation_details_create_tag);
    createTagButton.setOnClickListener(__ -> showCreateTagDialog());

    return rootView;
  }

  void updateObservation(Observation observation) {
    String currentSuspicion = observation.getSuspicion();
    observation.setSuspicion(mEditSuspicion.getText().toString());
    observation.setComment(mEditComment.getText().toString());

    if (!currentSuspicion.equals(observation.getSuspicion())) {
      mObservationViewModel.updateSuspicion(currentSuspicion, observation);
    }
    mObservationViewModel.insert(observation);
    mTagViewModel.getTags().observe(this, updateTagsObserver);
  }

  private void initView(Observation observation) {
    mEditSuspicion.setText(observation.getSuspicion());
    mEditComment.setText(observation.getComment());

    mTagsAdapter = new TagSelectionAdapter();
    mTagViewModel.getTags().observe(this, mTagsAdapter::setTags);
    mObservationViewModel.getTagsFor(observation).observe(this, mTagsAdapter::setPreselectedTags);
    mRecyclerTags.setAdapter(mTagsAdapter);

    if (observation.isLocationAttached()) {
      mLocationText.setText(getString(R.string.location_coords, observation.getLocationLatitude(), observation.getLocationLongitude()));
    }

    getArguments().putParcelable("obs", observation);
  }

  private void tagObservationWith(String content) {
    Tag tag = Tag.generateFor(content);
    mTagViewModel.insert(tag);
    mObservation.observe(this, observation -> mObservationViewModel.tagObservation(observation, tag));
  }

  private void showCreateTagDialog() {

    // FIXME disable add button if text is empty

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setTitle(R.string.action_add_tag);
    final EditText input = new EditText(getContext());
    builder.setView(input);
    builder.setPositiveButton(R.string.ok, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        tagObservationWith(input.getText().toString());
      }
    });
    builder.setNegativeButton(R.string.cancel, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });
    builder.show();
  }
}
