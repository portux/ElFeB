package de.portux.elfeb.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.portux.elfeb.support.Assert;
import de.portux.elfeb.support.Constraint;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

public class FilterCriteria {

  private final Date from;
  private final Date to;
  private final Set<Tag> tags;

  @NonNull
  public static CriteriaBuilder construct() {
    return new CriteriaBuilder();
  }

  @Constraint
  public static void assertValidTimeSpan(Date from, Date to) {
    if (from == null || to == null) {
      return;
    }
    if (from.compareTo(to) > 0) {
      throw new IllegalArgumentException("Start date may not be after end date");
    }
  }

  private FilterCriteria(@Nullable Date from, @Nullable Date to, @NonNull Set<Tag> tags) {
    Assert.noNullElements(tags, "No tag may be null");
    assertValidTimeSpan(from, to);
    this.from = from;
    this.to = to;
    this.tags = tags;
  }

  @Nullable
  public Date getFrom() {
    return from;
  }

  @Nullable
  public Date getTo() {
    return to;
  }

  @NonNull
  public Set<Tag> getTags() {
    return tags;
  }

  public boolean isStartDateSpecified() {
    return from != null;
  }

  public boolean isEndDateSpecified() {
    return to != null;
  }

  public boolean containsTags() {
    return !tags.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FilterCriteria that = (FilterCriteria) o;
    return Objects.equals(from, that.from) &&
        Objects.equals(to, that.to) &&
        Objects.equals(tags, that.tags);
  }

  @Override
  public int hashCode() {

    return Objects.hash(from, to, tags);
  }

  @Override
  public String toString() {
    return "FilterCriteria{" +
        "from=" + from +
        ", to=" + to +
        ", tags=" + tags +
        '}';
  }

  public static class CriteriaBuilder {

    private Date from, to;
    private Set<Tag> tags;

    private CriteriaBuilder() {}

    @NonNull
    public CriteriaBuilder startingOn(@NonNull Date from) {
      Assert.notNull(from, "Start date may not be null");
      this.from = from;
      return this;
    }

    @NonNull
    public CriteriaBuilder endingOn(@NonNull Date to) {
      Assert.notNull(to, "End date may not be null");
      this.to = to;
      return this;
    }

    @NonNull
    public CriteriaBuilder withTags(@NonNull Set<Tag> tags) {
      Assert.noNullElements(tags, "Tags may not be null");
      this.tags = tags;
      return this;
    }

    @NonNull
    public FilterCriteria done() {
      return new FilterCriteria(from, to, tags);
    }

  }

}
