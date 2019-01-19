package de.portux.elfeb.support;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;

public class ParcelableDate extends Date implements Parcelable {

  public static final Creator<ParcelableDate> CREATOR = new Creator<ParcelableDate>() {
    @Override
    public ParcelableDate createFromParcel(Parcel in) {
      final long time = in.readLong();
      return new ParcelableDate(time);

    }

    @Override
    public ParcelableDate[] newArray(int size) {
      return new ParcelableDate[size];
    }
  };

  public static ParcelableDate of(Date date) {
    return new ParcelableDate(date.getTime());
  }

  public ParcelableDate() {
    super();
  }

  public ParcelableDate(long date) {
    super(date);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(getTime());
  }
}
