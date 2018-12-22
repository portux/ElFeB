package de.portux.elfeb.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
interface TagDao {

  @Query("SELECT * FROM tags ORDER BY tag")
  LiveData<List<Tag>> getAllTags();

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  void insert(Tag... tags);

  @Query("DELETE FROM tags")
  void deleteAll();

}
