package de.portux.elfeb.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.annotation.NonNull;
import androidx.room.Index;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * An {@code attachment} represents some additional resource or information that is associated with
 * a specific {@link Observation}.
 *
 * @author Rico Bergmann
 */
@Entity(tableName = "attachments", //
    primaryKeys = {"file_path"}, //
    foreignKeys = @ForeignKey( //
        childColumns = {"observation_time", "observation_suspicion"}, //
        parentColumns = {"time", "suspicion"}, //
        entity = Observation.class, //
        onUpdate = ForeignKey.CASCADE), //
    indices = { //
        @Index({"observation_time", "observation_suspicion"})
    }
)
public class Attachment implements Parcelable {

  public enum AttachmentType {
    IMAGE, AUDIO
  }

  public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
    @Override
    public Attachment createFromParcel(Parcel in) {
      final Date observationTime = new Date(in.readLong());
      final String observationSuspicion = in.readString();
      final File path = new File(in.readString());
      final AttachmentType attachmentType = AttachmentType.values()[in.readInt()];
      return new Attachment(observationTime, observationSuspicion, path, attachmentType);
    }

    @Override
    public Attachment[] newArray(int size) {
      return new Attachment[size];
    }
  };

  @NonNull
  @ColumnInfo(name = "observation_time")
  private final Date mObservationTime;

  @NonNull
  @ColumnInfo(name = "observation_suspicion")
  private final String mObservationSuspicion;

  @NonNull
  @ColumnInfo(name = "file_path")
  private final File mPath;

  @NonNull
  @ColumnInfo(name = "type")
  private final AttachmentType mType;

  @NonNull
  public static Attachment forImage(@NonNull File imageFile, @NonNull Observation observation) {
    return new Attachment(observation, imageFile, AttachmentType.IMAGE);
  }

  @NonNull
  public static Attachment forAudio(@NonNull File audioFile, @NonNull Observation observation) {
    return new Attachment(observation, audioFile, AttachmentType.AUDIO);
  }

  public Attachment(@NonNull Date mObservationTime, @NonNull String mObservationSuspicion,
      @NonNull File mPath, @NonNull AttachmentType mType) {
    this.mObservationTime = mObservationTime;
    this.mObservationSuspicion = mObservationSuspicion;
    this.mPath = mPath;
    this.mType = mType;
  }

  Attachment(@NonNull Observation observation, @NonNull File path,
      @NonNull AttachmentType type) {
    this.mObservationTime = observation.getTime();
    this.mObservationSuspicion = observation.getSuspicion();
    this.mPath = path;
    this.mType = type;
  }

  @NonNull
  public File getPath() {
    return mPath;
  }

  @NonNull
  public AttachmentType getType() {
    return mType;
  }

  @NonNull
  protected Date getObservationTime() {
    return mObservationTime;
  }

  @NonNull
  protected String getObservationSuspicion() {
    return mObservationSuspicion;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(mObservationSuspicion);
    dest.writeLong(mObservationTime.getTime());
    dest.writeString(mPath.getPath());
    dest.writeInt(mType.ordinal());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Attachment that = (Attachment) o;
    return Objects.equals(mObservationTime, that.mObservationTime) &&
        Objects.equals(mObservationSuspicion, that.mObservationSuspicion) &&
        Objects.equals(mPath, that.mPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mObservationTime, mObservationSuspicion, mPath);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + '{' +
        "mObservationTime=" + mObservationTime +
        ", mObservationSuspicion='" + mObservationSuspicion + '\'' +
        ", mPath=" + mPath +
        '}';
  }

}
