package de.portux.elfeb.services;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import de.portux.elfeb.model.GPSPosition;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StorageService {

  public static class Images {
    private final Context mCtx;

    public Images(@NonNull Context ctx) {
      mCtx = ctx;
    }

    public Uri createImageFile() {
      return createImageFile(null);
    }

    public Uri createImageFile(GPSPosition currentPosition) {
      final Date now = new Date();
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now);
      String imageFileName = "IMG_" + timeStamp;
      ContentValues imageValues = new ContentValues();
      imageValues.put(MediaStore.Images.Media.TITLE, imageFileName);
      imageValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
      imageValues.put(MediaStore.Images.Media.DATE_TAKEN, now.getTime());

      if (currentPosition != null) {
        imageValues.put(MediaStore.Images.Media.LATITUDE, currentPosition.latitude);
        imageValues.put(MediaStore.Images.Media.LONGITUDE, currentPosition.longitude);
      }

      mCtx.grantUriPermission("de.portux.elfeb", MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

      Uri imageUri = mCtx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageValues);
      return imageUri;
    }

    public File convertUriToFile(Context ctx, Uri theUri) {
      String[] proj = {MediaStore.Images.Media.DATA};
      try (Cursor contentCursor = ctx.getContentResolver().query(theUri, proj, null, null, null)) {
        int colIdx = contentCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        contentCursor.moveToFirst();
        return new File(contentCursor.getString(colIdx));
      }
    }

  }

  public static class Audio {
    private static final String TAG = Audio.class.getSimpleName();

    private final Context mCtx;

    public Audio(@NonNull Context ctx) {
      mCtx = ctx;
    }

    public File convertUriToFile(Context ctx, Uri theUri) {
      String[] proj = {MediaStore.Audio.Media.DATA};
      try (Cursor contentCursor = ctx.getContentResolver().query(theUri, proj, null, null, null)) {
        int colIdx = contentCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        contentCursor.moveToFirst();
        return new File(contentCursor.getString(colIdx));
      }
    }

  }

  private StorageService() {}

}
