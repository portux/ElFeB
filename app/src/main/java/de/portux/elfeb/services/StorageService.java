package de.portux.elfeb.services;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import de.portux.elfeb.model.GPSPosition;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StorageService {

  public static class Images {
    private final Context mCtx;

    public Images(@NonNull Context ctx) {
      mCtx = ctx;
    }

    public File createImageFile() {
      return createImageFile(null);
    }

    public File createImageFile(GPSPosition currentPosition) {
      final Date now = new Date();
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now);
      String imageFileName = "IMG_" + timeStamp;

      File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
      try {
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
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
