package de.portux.elfeb.model;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;
import de.portux.elfeb.model.support.AttachmentTypeTypeConverter;
import de.portux.elfeb.support.DefaultTypeConverters;

@Database(version = 1, entities = {Observation.class, Tag.class, Attachment.class,
    ObservationTag.class})
@TypeConverters({DefaultTypeConverters.class, AttachmentTypeTypeConverter.class})
abstract class FieldNotesDatabase extends RoomDatabase {

  private static final String TAG = FieldNotesDatabase.class.getSimpleName();
  private static volatile FieldNotesDatabase INSTANCE;

  static FieldNotesDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      synchronized (FieldNotesDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room //
              .databaseBuilder( //
                  context.getApplicationContext(), //
                  FieldNotesDatabase.class, //
                  "field_notes_database") //
              .addCallback(sRoomDatabaseCallback) //
              .build();
        }
      }
    }
    return INSTANCE;
  }

  abstract public ObservationDao observationDao();

  abstract public TagDao tagDao();

  private static RoomDatabase.Callback sRoomDatabaseCallback =
      new RoomDatabase.Callback() {

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
          super.onOpen(db);
          new PopulateDbAsync(INSTANCE).execute();
        }

      };

  private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

    private final TagDao mDao;

    PopulateDbAsync(FieldNotesDatabase db) {
      mDao = db.tagDao();
    }

    @Override
    protected Void doInBackground(Void... voids) {
      /*mDao.deleteAll();
      mDao.insert(Tag.generateFor("Vogel"), Tag.generateFor("Pflanze"));

      Log.d(TAG, "doInBackground: done");
*/
      return null;
    }
  }

}
