package de.portux.elfeb.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import androidx.room.Ignore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.portux.elfeb.model.Attachment.AttachmentType;
import de.portux.elfeb.support.Assert;
import de.portux.elfeb.support.CollectionModificationException;
import de.portux.elfeb.support.ParcelableDate;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * An {@code Observation} represents a single entry in the {@link FieldNotes}.
 * <p>
 * {@code Observations} are noted for either known species in order to keep track of them, or for
 * unknown species in order to identify the actual species. The latter is the more likely use-case.
 * Therefore
 *
 * @author Rico Bergmann
 */
@Entity(tableName = "observations", primaryKeys = {"time", "suspicion"})
public class Observation implements Parcelable {

  public static final Creator<Observation> CREATOR = new Creator<Observation>() {
    @Override
    public Observation createFromParcel(Parcel in) {
      return new Observation(in);
    }

    @Override
    public Observation[] newArray(int size) {
      return new Observation[size];
    }
  };

  public static Observation noticeNew(String suspicion, String comment) {
    return new Observation(suspicion, comment);
  }

  @NonNull
  @ColumnInfo(name = "time")
  private Date mTime;

  @NonNull
  @ColumnInfo(name = "suspicion")
  private String mSuspicion;

  @Nullable
  @ColumnInfo(name = "comment")
  private String mComment;

  @ColumnInfo(name = "determined")
  private boolean mDetermined;

  @ColumnInfo(name = "images_attached")
  private boolean mImageAttached;

  @ColumnInfo(name = "recordings_attached")
  private boolean mRecordingAttached;

  @Nullable
  @ColumnInfo(name = "pos_latitude")
  private Double mLocationLatitude;

  @Nullable
  @ColumnInfo(name = "pos_longitude")
  private Double mLocationLongitude;

  @Ignore
  private Set<Attachment> mAttachments;

  @Ignore
  private Set<Tag> mTags;

  @Ignore
  private boolean mCompletelyInflated = false;

  protected Observation(Parcel in) {
    mTime = new Date(in.readLong());
    mSuspicion = in.readString();
    mComment = in.readString();
    mDetermined = in.readByte() != 0;
    mImageAttached = in.readByte() != 0;
    mRecordingAttached = in.readByte() != 0;
    if (in.readByte() == 0) {
      mLocationLatitude = null;
    } else {
      mLocationLatitude = in.readDouble();
    }
    if (in.readByte() == 0) {
      mLocationLongitude = null;
    } else {
      mLocationLongitude = in.readDouble();
    }
    mCompletelyInflated = in.readByte() != 0;
  }

  protected Observation(@NonNull String suspicion, @NonNull String comment) {
    Objects.requireNonNull(suspicion, "Suspicion may not be null");
    this.mTime = new Date();
    this.mSuspicion = suspicion;
    this.mComment = comment.isEmpty() ? null : comment;
    this.mLocationLatitude = null;
    this.mLocationLongitude = null;
    this.mAttachments = new HashSet<>();
    this.mTags = new HashSet<>();
    this.mDetermined = false;
    this.mImageAttached = false;
    this.mRecordingAttached = false;
  }

  protected Observation(@NonNull Date time, @NonNull String suspicion, @Nullable String comment,
      @Nullable Double latitude, @Nullable Double longitude, @NonNull Set<Attachment> attachments,
      @NonNull Set<Tag> tags, boolean determined, boolean imageAttached,
      boolean recordingAttached) {
    Objects.requireNonNull(suspicion, "Suspicion may not be null");
    this.mTime = time;
    this.mSuspicion = suspicion;
    this.mComment = comment != null && comment.isEmpty() ? null : comment;
    this.mAttachments = attachments;
    this.mTags = tags;
    this.mLocationLatitude = latitude;
    this.mLocationLongitude = longitude;

    for (Attachment attachment : attachments) {
      if (attachment.getType() == AttachmentType.IMAGE && !imageAttached) {
        throw new IllegalArgumentException("imageAttached must be true if images are attached");
      } else if (attachment.getType() == AttachmentType.AUDIO && !recordingAttached) {
        throw new IllegalArgumentException(
            "recordingAttached must be true if recordings are attached");
      }
    }

    this.mDetermined = determined;
    this.mImageAttached = imageAttached;
    this.mRecordingAttached = recordingAttached;
  }

  @NonNull
  public Date getTime() {
    return mTime;
  }

  @NonNull
  public String getSuspicion() {
    return mSuspicion;
  }

  @NonNull
  public String getComment() {
    return mComment == null ? "" : mComment;
  }

  @Nullable
  public Double getLocationLatitude() {
    return mLocationLatitude;
  }

  @Nullable
  public Double getLocationLongitude() {
    return mLocationLongitude;
  }

  @Nullable
  public GPSPosition getLocation() {
    if (mLocationLatitude == null || mLocationLongitude == null) {
      return null;
    }
    return GPSPosition.of(mLocationLatitude, mLocationLongitude);
  }

  public boolean isLocationAttached() {
    return mLocationLatitude != null && mLocationLongitude != null;
  }

  public Set<Attachment> getAttachments() {
    return Collections.unmodifiableSet(mAttachments);
  }

  public boolean isDetermined() {
    return mDetermined;
  }

  public boolean isImageAttached() {
    return mImageAttached;
  }

  public boolean isRecordingAttached() {
    return mRecordingAttached;
  }

  public Set<Tag> getTags() {
    return Collections.unmodifiableSet(mTags);
  }

  public void setSuspicion(String suspicion) {
    this.mSuspicion = suspicion;
  }

  public void setComment(String comment) {
    this.mComment = comment;
  }

  protected void setLocationLatitude(Double latitude) {
    mLocationLatitude = latitude;
  }

  protected void setLocationLongitude(Double longitude) {
    mLocationLongitude = longitude;
  }

  protected void setDetermined(boolean determined) {
    this.mDetermined = determined;
  }

  protected void setImageAttached(boolean imageAttached) {
    this.mImageAttached = imageAttached;
  }

  protected void setRecordingAttached(boolean recordingAttached) {
    this.mRecordingAttached = recordingAttached;
  }

  protected void setAttachments(Set<Attachment> attachments) {
    this.mAttachments = attachments;

    for (Attachment attachment : attachments) {
      switch (attachment.getType()) {
        case IMAGE:
          mImageAttached = true;
          break;
        case AUDIO:
          mRecordingAttached = true;
          break;
      }
    }
  }

  protected void setTags(Set<Tag> mTags) {
    this.mTags = mTags;
  }

  public void attachLocation(GPSPosition location) {
    if (location == null) {
      mLocationLatitude = null;
      mLocationLongitude = null;
    } else {
      mLocationLatitude = location.latitude;
      mLocationLongitude = location.longitude;
    }
  }

  public void attach(Attachment attachment) {
    Objects.requireNonNull(attachment, "New attachment may not be null");
    final boolean result = mAttachments.add(attachment);
    if (!result) {
      throw new CollectionModificationException("Could not add attachment " + attachment);
    }
    switch (attachment.getType()) {
      case AUDIO:
        mRecordingAttached = true;
        break;
      case IMAGE:
        mImageAttached = true;
        break;
    }
  }

  public void tagWith(@Nonnull Tag... tags) {
    Objects.requireNonNull(tags, "New tags may not be null");
    for (Tag tag : tags) {
      Objects.requireNonNull(tag, "No tag may be null");
      boolean result = mTags.add(tag);
      if (!result) {
        throw new CollectionModificationException("Could not add tag " + tag);
      }
    }
  }

  public void tagIfNecessary(@NonNull Tag... tags) {
    Objects.requireNonNull(tags, "New tags may not be null");
    for (Tag tag : tags) {
      Objects.requireNonNull(tag, "No tag may be null");
      mTags.add(tag);
    }
  }

  public boolean hasTag(@Nonnull Tag tag) {
    Objects.requireNonNull(tag, "Tag to check may not be null");
    return mTags.contains(tag);
  }

  public void markDetermined() {
    setDetermined(true);
  }

  protected void setTime(@NonNull Date time) {
    this.mTime = time;
  }

  boolean isCompletelyInflated() {
    return this.mCompletelyInflated;
  }

  void attachAttachments(@NonNull Set<Attachment> attachments) {
    Assert.notNull(attachments, "Attachments may not be null");
    Assert.noNullElements(attachments, "No attachment may be null");
    this.mAttachments = attachments;
    this.mCompletelyInflated = true;
  }

  void attachTags(@NonNull Set<Tag> tags) {
    Assert.notNull(tags, "Tags may not be null");
    Assert.noNullElements(tags, "No tag may be null");
    this.mTags = tags;
    this.mCompletelyInflated = true;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(mTime.getTime());
    dest.writeString(mSuspicion);
    dest.writeString(mComment);
    dest.writeByte((byte) (mDetermined ? 1 : 0));
    dest.writeByte((byte) (mImageAttached ? 1 : 0));
    dest.writeByte((byte) (mRecordingAttached ? 1 : 0));
    if (mLocationLatitude == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeDouble(mLocationLatitude);
    }
    if (mLocationLongitude == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeDouble(mLocationLongitude);
    }
    dest.writeByte((byte) (mCompletelyInflated ? 1 : 0));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Observation that = (Observation) o;
    return Objects.equals(mTime, that.mTime) &&
        Objects.equals(mSuspicion, that.mSuspicion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mTime, mSuspicion);
  }

  @Override
  public String toString() {
    return "Observation{" +
        "mTime=" + mTime +
        ", mSuspicion='" + mSuspicion + '\'' +
        ", mComment='" + mComment + '\'' +
        ", mAttachments=" + mAttachments +
        ", mTags=" + mTags +
        '}';
  }
}
