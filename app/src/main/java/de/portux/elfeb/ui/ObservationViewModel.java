package de.portux.elfeb.ui;

import android.app.Application;
import android.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import com.google.common.base.Optional;
import de.portux.elfeb.model.Attachment;
import de.portux.elfeb.model.FieldNotes;
import de.portux.elfeb.model.Observation;
import de.portux.elfeb.model.ObservationRepository;
import de.portux.elfeb.model.Tag;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Guava") // The warnings are in fact a false positive
public class ObservationViewModel extends AndroidViewModel {

  private final FieldNotes mFieldNotes;
  private final LiveData<PagedList<Observation>> mAllObservations;
  private final Map<Pair<Date, String>, Observation> mObservationKeyMap = new HashMap<>();

  public ObservationViewModel(Application app) {
    super(app);
    this.mFieldNotes = new ObservationRepository(app);
    this.mAllObservations = mFieldNotes.getObservations();
    this.mAllObservations.observeForever(new ObservationKeyMapInitializer());
  }

  LiveData<PagedList<Observation>> getAllObservations() {
    return mAllObservations;
  }

  Optional<Observation> getObservation(Date time, String suspicion) {
    return getObservation(Pair.create(time, suspicion));
  }

  Optional<Observation> getObservation(Pair<Date, String> obsKey) {
    initObservationKeyMap();
    Observation obs = mObservationKeyMap.get(obsKey);
    if (obs != null) {
      return Optional.of(obs);
    } else {
      return Optional.absent();
    }
  }

  LiveData<List<Tag>> getTagsFor(Observation observation) {
    return mFieldNotes.getTagsFor(observation);
  }

  LiveData<List<Attachment>> getAttachmentsFor(Observation observation) {
    return mFieldNotes.getAttachmentsFor(observation);
  }

  void removeAttachmentFrom(Observation observation, Attachment attachment) {
    mFieldNotes.removeAttachment(attachment);
  }

  void insert(Observation observation) {
    mFieldNotes.writeDown(observation);
  }

  void insertAttachment(Attachment attachment) {
    mFieldNotes.addAttachment(attachment);
  }

  void updateSuspicion(String oldSuspicion, Observation updatedObservation) {
    mFieldNotes.updateSuspicion(oldSuspicion, updatedObservation);
  }

  void updateTags(Observation observation, List<Tag> tags) {
    mFieldNotes.updateTags(observation, tags);
  }

  void tagObservation(Observation observation, Tag tag) {
    mFieldNotes.tagObservation(observation, tag);
  }

  private void initObservationKeyMap() {
    PagedList<Observation> loadedObservations = mAllObservations.getValue();
    if (loadedObservations != null //
        && mObservationKeyMap.size() != loadedObservations.getLoadedCount()) {
      mObservationKeyMap.clear();

      for (Observation observation : loadedObservations.snapshot()) {
        mObservationKeyMap.put(Pair.create(observation.getTime(), observation.getSuspicion()), observation);
      }

    }
  }

  private class ObservationKeyMapInitializer implements Observer<PagedList<Observation>> {
    @Override
    public void onChanged(PagedList<Observation> observations) {
      for (Observation obs : observations.snapshot()) {
        mObservationKeyMap.put(Pair.create(obs.getTime(), obs.getSuspicion()), obs);
      }
      observations.addWeakCallback(null, new ObservationKeyMapUpdater());
    }
  }

  private class ObservationKeyMapUpdater extends PagedList.Callback {
    @Override
    public void onChanged(int position, int count) { ; }

    @Override
    public void onInserted(int position, int count) {
      for (Observation obs : mAllObservations.getValue().subList(position, position + count)) {
        mObservationKeyMap.put(Pair.create(obs.getTime(), obs.getSuspicion()), obs);
      }
    }

    @Override
    public void onRemoved(int position, int count) { ; }
  }

}
