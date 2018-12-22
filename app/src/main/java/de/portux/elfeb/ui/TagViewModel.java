package de.portux.elfeb.ui;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import de.portux.elfeb.model.Tag;
import de.portux.elfeb.model.TagRepository;
import java.util.List;

public class TagViewModel extends AndroidViewModel {

  private final TagRepository mTagRepo;
  private final LiveData<List<Tag>> mTags;

  public TagViewModel(Application app) {
    super(app);
    this.mTagRepo = new TagRepository(app);
    this.mTags = mTagRepo.getTags();
  }

  public LiveData<List<Tag>> getTags() {
    return mTags;
  }

  public void insert(Tag tag) {
    mTagRepo.insert(tag);
  }

}
