package de.portux.elfeb.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.annotation.NonNull;
import java.util.Date;

@Entity(tableName = "observation_tags", //
    primaryKeys = {"observation_time", "observation_suspicion", "tag"}, //
    foreignKeys = { //
        @ForeignKey( //
            parentColumns = {"time", "suspicion"}, //
            childColumns = {"observation_time", "observation_suspicion"}, //
            entity = Observation.class, //
            onUpdate = ForeignKey.CASCADE, //
            onDelete = ForeignKey.CASCADE), //
        @ForeignKey(parentColumns = "tag", childColumns = "tag", entity = Tag.class)})
public class ObservationTag {

  @NonNull
  @ColumnInfo(name = "observation_time")
  public final Date observationTime;

  @NonNull
  @ColumnInfo(name = "observation_suspicion")
  public final String observationSuspicion;

  @NonNull
  @ColumnInfo(name = "tag", index = true)
  public final String tag;

  public static ObservationTag create(Observation observation, Tag tag) {
    return new ObservationTag(observation.getTime(), observation.getSuspicion(), tag.getContent());
  }

  public ObservationTag(@NonNull Date observationTime,
      @NonNull String observationSuspicion, @NonNull String tag) {
    this.observationTime = observationTime;
    this.observationSuspicion = observationSuspicion;
    this.tag = tag;
  }

  public String getTag() {
    return tag;
  }

}
