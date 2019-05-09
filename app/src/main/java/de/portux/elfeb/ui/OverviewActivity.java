package de.portux.elfeb.ui;

import android.content.Intent;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import de.portux.elfeb.R;
import de.portux.elfeb.model.Observation;

/**
 * The {@code OverviewActivity} displays a list of all observations and provides an action to create
 * new entries.
 * <p>
 * As there may be quite a lot entries in the fieldnotes, a {@link PagedList} will be used and
 * observations will be loaded on demand.
 *
 * @author Rico Bergmann
 */
public class OverviewActivity extends AppCompatActivity {

  /**
   * TAG to identify this Activity.
   */
  public static final String TAG = OverviewActivity.class.getSimpleName();

  private FieldNotesListAdapter mFieldNotesAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_overview);

    final ObservationViewModel observationViewModel = ViewModelProviders.of(this)
        .get(ObservationViewModel.class);

    // init the observation's list
    RecyclerView fieldNotesList = findViewById(R.id.fieldnotes);
    mFieldNotesAdapter = new FieldNotesListAdapter(this);
    observationViewModel.getAllObservations()
        .observe(this, new FieldNotesAdapterObservationsInitializer());
    fieldNotesList.setAdapter(mFieldNotesAdapter);
    fieldNotesList.setLayoutManager(new LinearLayoutManager(this));

    // init the new observation action
    FloatingActionButton newEntryButton = findViewById(R.id.button_add_entry);
    newEntryButton.setOnClickListener(createEntry);
  }

  /**
   * Action to start the observation entry activity.
   *
   * @see EntryActivity
   */
  private final View.OnClickListener createEntry = view -> {
    Intent createEntry = new Intent(OverviewActivity.this, EntryActivity.class);
    createEntry.putExtra(EntryActivity.INTENT_EXTRA_CALLEE, TAG);
    startActivity(createEntry);
  };

  /**
   * Observer to propagate the observations that were loaded by the {@link ObservationViewModel} to
   * the field notes recycler.
   *
   * @see FieldNotesListAdapter
   */
  private class FieldNotesAdapterObservationsInitializer implements
      Observer<PagedList<Observation>> {

    @Override
    public void onChanged(PagedList<Observation> observations) {
      mFieldNotesAdapter.setObservations(observations);
    }
  }
}
