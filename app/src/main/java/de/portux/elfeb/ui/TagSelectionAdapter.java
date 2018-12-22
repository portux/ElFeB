package de.portux.elfeb.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import de.portux.elfeb.R;
import de.portux.elfeb.model.FieldNotes;
import de.portux.elfeb.model.Observation;
import de.portux.elfeb.model.ObservationRepository;
import de.portux.elfeb.model.Tag;
import de.portux.elfeb.ui.TagSelectionAdapter.TagSelectionViewHolder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class TagSelectionAdapter extends RecyclerView.Adapter<TagSelectionViewHolder> {

  class TagSelectionViewHolder extends RecyclerView.ViewHolder {
    CheckBox mTagCheckBox;

    TagSelectionViewHolder(View v) {
      super(v);
      this.mTagCheckBox = v.findViewById(R.id.check_tag);
      this.mTagCheckBox.setOnCheckedChangeListener(tagSelectionListener);
    }
  }

  private final OnCheckedChangeListener tagSelectionListener = new OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      if (isChecked) {
        mCheckedTags.add(buttonView.getText());
      } else {
        mCheckedTags.remove(buttonView.getText());
      }
    }
  };

  private Set<CharSequence> mCheckedTags = new HashSet<>();
  private List<Tag> mTags;
  private List<Tag> mPreselectedTags;

  @NonNull
  @Override
  public TagSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()) //
        .inflate(R.layout.tag_selection_item, parent, false);
    TagSelectionViewHolder vh = new TagSelectionViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(@NonNull TagSelectionViewHolder holder, int position) {
    if (mTags != null) {
      Tag tag = mTags.get(position);
      holder.mTagCheckBox.setText(tag.getContent());

      if (mPreselectedTags != null && mPreselectedTags.contains(tag)) {
        holder.mTagCheckBox.setChecked(true);
      } else {
        holder.mTagCheckBox.setChecked(false);
      }

    } else {
      holder.mTagCheckBox.setText("");
    }
  }

  @Override
  public int getItemCount() {
    if (mTags != null) {
      return mTags.size();
    } else {
      return 0;
    }
  }

  void setTags(List<Tag> tags) {
    this.mTags = tags;
    notifyDataSetChanged();
  }

  void setPreselectedTags(List<Tag> tags) {
    this.mPreselectedTags = tags;
    notifyDataSetChanged();
  }

  List<Tag> getTags() {
    if (mTags != null) {
      return Collections.unmodifiableList(mTags);
    } else {
      return null;
    }
  }

  boolean isSelected(Tag tag) {
    return mCheckedTags.contains(tag.getContent());
  }

}
