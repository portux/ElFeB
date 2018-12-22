package de.portux.elfeb.model;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

public class TagRepository {

  private final TagDao mTagDao;

  private LiveData<List<Tag>> mTags;

  public TagRepository(Application app) {
    FieldNotesDatabase db = FieldNotesDatabase.getDatabase(app);
    this.mTagDao = db.tagDao();
  }

  public LiveData<List<Tag>> getTags() {
    if (mTags == null) {
      mTags = mTagDao.getAllTags();
    }
    return mTags;
  }

  public void insert(Tag tag) {
    new insertAsyncTask(mTagDao).execute(tag);
  }

  private static class insertAsyncTask extends AsyncTask<Tag, Void, Void> {
    private TagDao mAsyncTaskDao;

    insertAsyncTask(TagDao asyncTaskDao) {
      mAsyncTaskDao = asyncTaskDao;
    }

    @Override
    protected Void doInBackground(Tag... tags) {
      mAsyncTaskDao.insert(tags);
      return null;
    }
  }

}
