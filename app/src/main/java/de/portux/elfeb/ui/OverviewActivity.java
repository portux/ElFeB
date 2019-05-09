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

public class OverviewActivity extends AppCompatActivity {

  public static final String TAG = OverviewActivity.class.getSimpleName();

  private ObservationViewModel mObservationViewModel;
  private FieldNotesListAdapter mFieldNotesAdapter;
  private FloatingActionButton mNewEntry;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_overview);

    RecyclerView fieldNotesList = findViewById(R.id.fieldnotes);
    mObservationViewModel = ViewModelProviders.of(this).get(ObservationViewModel.class);
    mFieldNotesAdapter = new FieldNotesListAdapter(this);
    mObservationViewModel.getAllObservations().observe(this, new FieldNotesAdapterObservationsInitializer());
    fieldNotesList.setAdapter(mFieldNotesAdapter);
    fieldNotesList.setLayoutManager(new LinearLayoutManager(this));

    mNewEntry = findViewById(R.id.button_add_entry);
    mNewEntry.setOnClickListener(createEntry);
  }


  private final View.OnClickListener  createEntry = view -> {
      Intent createEntry = new Intent(OverviewActivity.this, EntryActivity.class);
      createEntry.putExtra(EntryActivity.INTENT_EXTRA_CALLEE, TAG);
      startActivity(createEntry);
  };

  private class FieldNotesAdapterObservationsInitializer implements Observer<PagedList<Observation>> {
    @Override
    public void onChanged(PagedList<Observation> observations) {
      mFieldNotesAdapter.setObservations(observations);
    }
  }
}
