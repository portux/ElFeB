package de.portux.elfeb.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.annotation.Size;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * A {@code Tag} may be added to an {@link Observation} to further classify it.
 * <p>
 * There are two main use-cases for tags:
 * <ul>
 *   <li>adding taxonomical ranks</li>
 *   <li>assigning orthogonal information, such as age or sex of birds</li>
 * </ul>
 * To relate tags to each other, a {@code Tag} may be assigned mParent. This relationship will than
 * be respected in filters and the like.
 *
 * @author Rico Bergmann
 */
@Entity(tableName = "tags")
public class Tag implements Serializable {

  /**
   * The minimum number of characters each tag must have.
   */
  private static final int MIN_CONTENT_LENGTH = 3;
  private static final String CONTENT_TOO_SHORT_MSG = "Tag mContent must be at least %d characters long";

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "tag")
  @Size(min = MIN_CONTENT_LENGTH)
  private String mContent;

  @Nullable
  @Ignore
  private Tag mParent;

  /**
   * Creates a new top-level tag.
   *
   * @param content the name of the tag. It must be at least {@link #MIN_CONTENT_LENGTH}
   *     characters long.
   * @return the tag
   */
  @NonNull
  public static Tag generateFor(@NonNull String content) {
    return new Tag(content, null);
  }

  public Tag(@NonNull String content) {
    this.mContent = content;
  }

  /**
   * Builds a new {@code Tag}.
   *
   * @param content the name of the tag. It must be at least {@link #MIN_CONTENT_LENGTH}
   *     characters long.
   * @param parent the {@code Tag} {@code this} should be a sub-tag of. May be {@code null} if
   *     {@code this} should be a top-level tag.
   */
  protected Tag(@NonNull String content, @Nullable Tag parent) {
    Objects.requireNonNull(content, "Tag mContent may not be null");
    if (content.length() < MIN_CONTENT_LENGTH) {
      String errMsg = String.format(Locale.ENGLISH, CONTENT_TOO_SHORT_MSG, MIN_CONTENT_LENGTH);
      throw new IllegalArgumentException(errMsg);
    }
    this.mContent = content;
    this.mParent = parent;
  }

  /**
   * The {@code Tag}'s name.
   *
   * @return the name. Will never be {@code null} and at least {@link #MIN_CONTENT_LENGTH}
   *     characters long.
   */
  @NonNull
  public String getContent() {
    return mContent;
  }

  /**
   * The {@code Tag} {@code this} is a sub-tag of.
   * <p>
   * If such a tag exists, each {@link Observation} which is tagged with {@code this} should be
   * treated as (implicitly) also tagged with {@code mParent}.
   *
   * @return the mParent. May be {@code null} if {@code this} is a top-level tag.
   */
  @Nullable
  public Tag getParent() {
    return mParent;
  }

  /**
   * Creates a new {@code Tag} with {@code this} as mParent.
   * @param content the name of the new tag
   * @return the resulting {@code Tag}
   */
  @NonNull
  public Tag generateSubTag(@NonNull String content) {
    return new Tag(content, this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Tag tag = (Tag) o;
    return Objects.equals(mContent, tag.mContent) &&
        Objects.equals(mParent, tag.mParent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mContent, mParent);
  }

  @Override
  public String toString() {
    return "Tag{" +
        "mContent='" + mContent + '\'' +
        ", mParent=" + mParent +
        '}';
  }
}
