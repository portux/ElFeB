package de.portux.elfeb.support;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;
import java.io.File;
import java.util.Date;

/**
 * Collection of useful type converters for a number of default or util types.
 *
 * @author Rico Bergmann
 */
public class DefaultTypeConverters {

  @TypeConverter
  @Nullable
  public static Date dateFromTimestamp(@Nullable Long value) {
    return value == null ? null : new Date(value);
  }

  @TypeConverter
  @Nullable
  public static Long dateToTimestamp(@Nullable Date date) {
    return date == null ? null : date.getTime();
  }

  @TypeConverter
  @Nullable
  public static ParcelableDate parcelableDateFromTimestamp(@Nullable Long value) {
    return value == null ? null : new ParcelableDate(value);
  }

  @TypeConverter
  @Nullable
  public static Long parcelableDateToTimestamp(@Nullable ParcelableDate date) {
    return date == null ? null : date.getTime();
  }

  @TypeConverter
  @Nullable
  public static File fileFromPath(@Nullable String value) {
    return value == null ? null : new File(value);
  }

  @TypeConverter
  @Nullable
  public static String fileToPath(@Nullable File file) {
    return file == null ? null : file.getAbsolutePath();
  }
}
