package de.portux.elfeb.model;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import java.util.List;

/**
 * The {@code field notes} contain a sequence of observations that were written down by the user.
 *
 * @author Rico Bergmann
 */
public interface FieldNotes {

  /**
   * Saves a new note.
   *
   * @param obs the note to save. May not be {@code null}.
   */
  void writeDown(Observation obs);

  /**
   * Provides all observations that have been noted so far.
   */
  LiveData<PagedList<Observation>> getObservations();

  /**
   * Provides all observations that match certain criteria.
   *
   * @param criteria the criteria. May not be {@code null}.
   */
  LiveData<PagedList<Observation>> getObservations(FilterCriteria criteria);

  LiveData<List<Tag>> getTagsFor(Observation observation);

  LiveData<List<Attachment>> getAttachmentsFor(Observation observation);

  LiveData<List<Attachment>> getAllAttachments();

  void tagObservation(Observation observation, Tag tag);

  void addAttachment(Attachment attachment);

  void removeAttachment(Attachment attachment);

  void updateSuspicion(String oldSuspicion, Observation updatedObservation);

  void updateTags(Observation observation, List<Tag> tags);
}
