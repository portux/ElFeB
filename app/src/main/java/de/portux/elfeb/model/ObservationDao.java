package de.portux.elfeb.model;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagedList.Callback;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.annotation.NonNull;
import de.portux.elfeb.support.Internal;
import de.portux.elfeb.support.Internal.Scope;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Dao
interface ObservationDao {

  String TAG = ObservationDao.class.getSimpleName();

  @Query("SELECT * FROM observations ORDER BY time")
  DataSource.Factory<Integer, Observation> getAllObservations();

  @Query("SELECT * FROM attachments WHERE observation_time = :observationTime AND observation_suspicion = :observationSuspicion")
  LiveData<List<Attachment>> getAttachmentsForObservation(Date observationTime,
      String observationSuspicion);

  @Query("SELECT * FROM attachments ORDER BY observation_time, observation_suspicion")
  LiveData<List<Attachment>> getAllAttachments();

  @Internal(scope = Scope.PRIVATE)
  @Query("SELECT * FROM attachments WHERE observation_time = :observationTime AND observation_suspicion = :observationSuspicion")
  List<Attachment> getRawAttachmentsForObservation(Date observationTime, String observationSuspicion);

  @Query("SELECT tag FROM observation_tags WHERE observation_time = :observationTime AND observation_suspicion = :observationSuspicion")
  LiveData<List<Tag>> getTagsForObservation(Date observationTime, String observationSuspicion);

  @Internal(scope = Scope.PRIVATE)
  @Query("SELECT * FROM observation_tags WHERE observation_time = :observationTime AND observation_suspicion = :observationSuspicion")
  List<ObservationTag> getObservationTagsForObservation(Date observationTime, String observationSuspicion);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertObservation(Observation observation);

  @Insert(onConflict = OnConflictStrategy.FAIL)
  void insertAttachments(Attachment... attachments);

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  void insertTags(Tag... tags);

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  void insertObservationTags(ObservationTag... observationTags);

  @Query("UPDATE observations SET suspicion = :newSuspicion WHERE time = :observationTime AND suspicion = :oldSuspicion")
  void updateObservationSuspicion(Date observationTime, String oldSuspicion, String newSuspicion);

  @Delete
  void deleteObservationTags(ObservationTag... observationTags);

  @Delete
  void deleteAttachments(Attachment... attachments);

  default void updateSuspicion(String oldSuspicion, Observation updatedObservation) {
    updateObservationSuspicion(updatedObservation.getTime(), oldSuspicion, updatedObservation.getSuspicion());
  }

  default void updateTags(Observation observation, ObservationTag... newTags) {
    ObservationTag[] obsTagsArr = {};
    deleteObservationTags(getObservationTagsForObservation(observation.getTime(), observation.getSuspicion()).toArray(obsTagsArr));
    insertObservationTags(newTags);
  }

  default void insertObservationWithTagsAndAttachments(Observation observation) {
    insertObservation(observation);
    insertTags(observation.getTags().toArray(new Tag[]{}));
    insertAttachments(observation.getAttachments().toArray(new Attachment[]{}));

    List<ObservationTag> observationTags = new ArrayList<>(observation.getTags().size());
    for (Tag tag : observation.getTags()) {
      observationTags.add(ObservationTag.create(observation, tag));
    }
    insertObservationTags(observationTags.toArray(new ObservationTag[]{}));
  }

  class ObservationInitializationCallback extends Callback {

    @NonNull
    private final ObservationDao mDao;

    @NonNull
    private final List<Observation> mObservations;

    private ObservationInitializationCallback(@NonNull ObservationDao dao,
        @NonNull List<Observation> observations) {
      this.mDao = dao;
      this.mObservations = observations;
    }

    @Override
    public void onChanged(int position, int count) {
      //inflateObservationsIfNecessary(position, count);
    }

    @Override
    public void onInserted(int position, int count) {
      //inflateObservationsIfNecessary(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {

    }

    /*private void inflateObservationsIfNecessary(int position, int count) {
      for (int currPos = position; currPos < position + count; ++currPos) {
        Observation obs = mObservations.get(currPos);
        if (!obs.isCompletelyInflated()) {
          obs.attachAttachments(
              new HashSet<>(mDao.getAttachmentsForObservation(obs.getTime(), obs.getSuspicion())));
          obs.attachTags(new HashSet<>(mDao.getTagsForObservation(obs.getTime(), obs.getSuspicion())));
        }
      }
    }*/
  }

}
