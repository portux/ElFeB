package de.portux.elfeb.support;

import androidx.room.TypeConverter;
import java.io.File;
import java.util.Date;

public class DefaultTypeConverters {

  @TypeConverter
  public static Date dateFromTimestamp(Long value) {
    return value == null ? null : new Date(value);
  }

  @TypeConverter
  public static Long dateToTimestamp(Date date) {
    return date == null ? null : date.getTime();
  }

  @TypeConverter
  public static File fileFromPath(String value) {
    return value == null ? null : new File(value);
  }

  @TypeConverter
  public static String fileToPath(File file) {
    return file == null ? null : file.getAbsolutePath();
  }

}
