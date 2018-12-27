package de.portux.elfeb.model;

import android.app.Application;
import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import android.os.AsyncTask;
import de.portux.elfeb.model.Attachment.AttachmentType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ObservationRepository implements FieldNotes {

  private ObservationDao mObservationDao;
  private LiveData<PagedList<Observation>> mObservations;
  private Map<Observation, LiveData<List<Tag>>> mObservationTags;
  private Map<Observation, LiveData<List<Attachment>>> mObservationAttachments;

  public ObservationRepository(Application app) {
    FieldNotesDatabase db = FieldNotesDatabase.getDatabase(app);
    this.mObservationDao = db.observationDao();
    final DataSource.Factory<Integer, Observation> observationsFactory = mObservationDao.getAllObservations();
    this.mObservations = new LivePagedListBuilder<>(observationsFactory, 15).build();
    this.mObservationTags = new HashMap<>();
    this.mObservationAttachments = new HashMap<>();
  }

  @Override
  public void writeDown(Observation obs) {
    new insertAsyncTask(mObservationDao).execute(obs);
  }

  @Override
  public LiveData<PagedList<Observation>> getObservations() {
    return mObservations;
  }

  public LiveData<List<Tag>> getTagsFor(Observation observation) {
    if (!mObservationTags.containsKey(observation)) {
      LiveData<List<Tag>> tags = mObservationDao.getTagsForObservation(observation.getTime(), observation.getSuspicion());
      mObservationTags.put(observation, tags);
      return tags;
    }
    return mObservationTags.get(observation);
  }

  @Override
  public LiveData<List<Attachment>> getAllAttachments() {
    return mObservationDao.getAllAttachments();
  }

  @Override
  public LiveData<List<Attachment>> getAttachmentsFor(Observation observation) {
    if (!mObservationAttachments.containsKey(observation)) {
      LiveData<List<Attachment>> attachments = mObservationDao.getAttachmentsForObservation(observation.getTime(), observation.getSuspicion());
      mObservationAttachments.put(observation, attachments);
      return attachments;
    }
    return mObservationAttachments.get(observation);
  }

  @Override
  public void tagObservation(Observation observation, Tag tag) {
    ObservationTag observationTag = ObservationTag.create(observation, tag);
    new tagObservationAsyncTask(mObservationDao).execute(observationTag);
  }

  @Override
  public void addAttachment(Attachment attachment) {
    new addAttachmentAsyncTask(mObservationDao).execute(attachment);
    switch (attachment.getType()) {
      case IMAGE:
        new updateImagesAttachedAsyncTask(mObservationDao, attachment.getObservationTime(), attachment.getObservationSuspicion()).execute(true);
        break;
      case AUDIO:
        new updateRecordingsAttachedAsyncTask(mObservationDao, attachment.getObservationTime(), attachment.getObservationSuspicion()).execute(true);
        break;
    }
  }

  @Override
  public void removeAttachment(Attachment attachment) {
    new deleteAttachmentsAndUpdateFlagsAsyncTask(mObservationDao).execute(attachment);
  }

  @Override
  public void updateSuspicion(String oldSuspicion, Observation updatedObservation) {
    new updateSuspicionAsyncTask(mObservationDao, oldSuspicion).execute(updatedObservation);
  }

  @Override
  public void updateTags(Observation observation, List<Tag> tags) {
    List<ObservationTag> observationTags = new ArrayList<>(tags.size());
    for (Tag tag : tags) {
      observationTags.add(ObservationTag.create(observation, tag));
    }
    new updateTagsAsyncTask(mObservationDao, observation).execute(observationTags.toArray(new ObservationTag[]{}));
  }

  @Override
  public LiveData<PagedList<Observation>> getObservations(FilterCriteria criteria) {
    throw new UnsupportedOperationException();
  }

  private static class insertAsyncTask extends AsyncTask<Observation, Void, Void> {
    private ObservationDao mAsyncTaskDao;

    insertAsyncTask(ObservationDao dao) {
      this.mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final Observation... observations) {
      mAsyncTaskDao.insertObservationWithTagsAndAttachments(observations[0]);
      return null;
    }
  }

  private static class tagObservationAsyncTask extends AsyncTask<ObservationTag, Void, Void> {
    private ObservationDao mAsyncTaskDao;

    tagObservationAsyncTask(ObservationDao asyncTaskDao) {
      this.mAsyncTaskDao = asyncTaskDao;
    }

    @Override
    protected Void doInBackground(ObservationTag... observationTags) {
      mAsyncTaskDao.insertObservationTags(observationTags);
      return null;
    }
  }

  private static class addAttachmentAsyncTask extends AsyncTask<Attachment, Void, Void> {
    private ObservationDao mAsyncTaskDao;

    addAttachmentAsyncTask(ObservationDao asyncTaskDao) {
      this.mAsyncTaskDao = asyncTaskDao;
    }

    @Override
    protected Void doInBackground(Attachment... attachments) {
      mAsyncTaskDao.insertAttachments(attachments);
      return null;
    }
  }

  private static class deleteAttachmentsAndUpdateFlagsAsyncTask extends  AsyncTask<Attachment, Void, Void> {
    private ObservationDao mAsyncTaskDao;

    deleteAttachmentsAndUpdateFlagsAsyncTask(ObservationDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(Attachment... attachments) {
      Map<Pair<Date, String>, Integer> remainingImageAttachments = new HashMap<>();
      Map<Pair<Date, String>, Integer> remainingAudioAttachments = new HashMap<>();

      // calculate how many attachments of each type are currently linked to the observations
      for (Attachment attachment : attachments) {
        Pair<Date, String> currentKey = Pair.create(attachment.getObservationTime(), attachment.getObservationSuspicion());

        switch (attachment.getType()) {
          case IMAGE:
            Integer currentImageEntry = remainingImageAttachments.get(currentKey);
            int currentImageCount = currentImageEntry != null //
                ? currentImageEntry //
                : mAsyncTaskDao.countAttachmentsForObservation(attachment.getObservationTime(), attachment.getObservationSuspicion(), AttachmentType.IMAGE.name());
            remainingImageAttachments.put(currentKey, --currentImageCount);
            break;
          case AUDIO:
            Integer currentAudioEntry = remainingAudioAttachments.get(currentKey);
            int currentAudioCount = currentAudioEntry != null //
                ? currentAudioEntry //
                : mAsyncTaskDao.countAttachmentsForObservation(attachment.getObservationTime(), attachment.getObservationSuspicion(), AttachmentType.AUDIO.name());
            remainingAudioAttachments.put(currentKey, --currentAudioCount);
            break;
        }
      }

      // delete the given attachments
      mAsyncTaskDao.deleteAttachments(attachments);

      // update the imagesAttached and recordingsAttached flags in the corresponding observation
      for (Entry<Pair<Date, String>, Integer> attachmentEntries : remainingImageAttachments.entrySet()) {
        if (attachmentEntries.getValue() == 0) {
          mAsyncTaskDao.updateObservationImagesAttached(attachmentEntries.getKey().first, attachmentEntries.getKey().second, false);
        }
      }
      for (Entry<Pair<Date, String>, Integer> attachmentEntries : remainingAudioAttachments.entrySet()) {
        if (attachmentEntries.getValue() == 0) {
          mAsyncTaskDao.updateObservationRecordingsAttached(attachmentEntries.getKey().first, attachmentEntries.getKey().second, false);
        }
      }
      return null;
    }
  }

  private static class updateSuspicionAsyncTask extends AsyncTask<Observation, Void, Void> {
    private ObservationDao mAsyncTaskDao;
    private String mOldSuspicion;

    updateSuspicionAsyncTask(ObservationDao dao, String oldSuspicion) {
      this.mAsyncTaskDao = dao;
      this.mOldSuspicion = oldSuspicion;
    }

    @Override
    protected Void doInBackground(Observation... observations) {
      Observation observation = observations[0];
      mAsyncTaskDao.updateSuspicion(mOldSuspicion, observation);
      return null;
    }
  }

  private static class updateTagsAsyncTask extends AsyncTask<ObservationTag, Void, Void> {
    private ObservationDao mAsyncTaskDao;
    private Observation mObservation;

    updateTagsAsyncTask(ObservationDao asyncTaskDao, Observation observation) {
      mAsyncTaskDao = asyncTaskDao;
      mObservation = observation;
    }

    @Override
    protected Void doInBackground(ObservationTag... observationTags) {
      mAsyncTaskDao.updateTags(mObservation, observationTags);
      return null;
    }
  }

  private static class updateImagesAttachedAsyncTask extends AsyncTask<Boolean, Void, Void> {
    private ObservationDao mAsyncTaskDao;
    private Date mObservationTime;
    private String mObservationSuspicion;

    updateImagesAttachedAsyncTask(ObservationDao dao, Date observationTime, String observationSuspicion) {
      this.mAsyncTaskDao = dao;
      this.mObservationTime = observationTime;
      this.mObservationSuspicion = observationSuspicion;
    }

    @Override
    protected Void doInBackground(Boolean... booleans) {
      mAsyncTaskDao.updateObservationImagesAttached(mObservationTime, mObservationSuspicion, booleans[0]);
      return null;
    }
  }

  private static class updateRecordingsAttachedAsyncTask extends AsyncTask<Boolean, Void, Void> {
    private ObservationDao mAsyncTaskDao;
    private Date mObservationTime;
    private String mObservationSuspicion;

    updateRecordingsAttachedAsyncTask(ObservationDao dao, Date observationTime, String observationSuspicion) {
      this.mAsyncTaskDao = dao;
      this.mObservationTime = observationTime;
      this.mObservationSuspicion = observationSuspicion;
    }

    @Override
    protected Void doInBackground(Boolean... booleans) {
      mAsyncTaskDao.updateObservationRecordingsAttached(mObservationTime, mObservationSuspicion,
          booleans[0]);
      return null;
    }
  }

}
