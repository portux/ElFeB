package de.portux.elfeb.model.support;

import androidx.room.TypeConverter;
import de.portux.elfeb.model.Attachment.AttachmentType;

public class AttachmentTypeTypeConverter {

  @TypeConverter
  public static AttachmentType attachmentTypeFromValue(String value) {
    return value == null ? null : AttachmentType.valueOf(value);
  }

  @TypeConverter
  public static String attachmentTypeToValue(AttachmentType type) {
    return type == null ? null : type.name();
  }

}
