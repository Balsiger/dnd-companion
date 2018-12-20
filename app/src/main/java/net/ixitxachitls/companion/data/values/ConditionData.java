/*
 * Copyright (c) 2017-{2018} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
 *
 * The Roleplay Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Roleplay Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.values;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A condition a creature can have.
 */
public class ConditionData {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_DESCRIPTION = "description";
  private static final String FIELD_ADJUSTMENTS = "adjustments";
  private static final String FIELD_DURATION = "duration";
  private static final String FIELD_PREDEFINED = "predefined";
  private static final String FIELD_ENDS_BEFORE_TURN = "end_before_turn";
  private static final String FIELD_DM_ONLY = "dm_only";
  private static final String FIELD_DISMISSIBLE = "dismissible";
  private static final String FIELD_ICON = "icon";
  private static final String FIELD_COLOR = "color";
  private static final String FIELD_NO_SHOW = "no_show";

  private final String name;
  private final String description;
  private final List<Adjustment> adjustments = new ArrayList<>();
  private final Duration duration;
  private final boolean predefined;
  private final boolean endsBeforeTurn;
  private final boolean dmOnly;
  private final boolean noShow;
  private final boolean dismissible;
  @DrawableRes private final int icon;
  @ColorRes private final int color;

  public ConditionData(String name, String description, List<Adjustment> adjustments,
                       Duration duration, boolean predefined, boolean endsBeforeTurn,
                       boolean dmOnly, boolean noShow, boolean dismissible, @DrawableRes int icon,
                       @ColorRes int color) {
    this.name = name;
    this.description = description;
    this.adjustments.addAll(adjustments);
    this.duration = duration;
    this.predefined = predefined;
    this.endsBeforeTurn = endsBeforeTurn;
    this.dmOnly = dmOnly;
    this.noShow = noShow;
    this.dismissible = dismissible;
    this.icon = icon;
    this.color = color;
  }

  public List<Adjustment> getAdjustments() {
    return Collections.unmodifiableList(adjustments);
  }

  @ColorRes public int getColor() {
    return color;
  }

  public String getDescription() {
    return description;
  }

  public Duration getDuration() {
    return duration;
  }

  @DrawableRes public int getIcon() {
    return icon;
  }

  public String getName() {
    return name;
  }

  public String getSummary() {
    return Strings.COMMA_JOINER.join(adjustments.stream()
        .map(Adjustment::toString)
        .collect(Collectors.toList()));
  }

  public Spanned getSummarySpanned() {
    return adjustments.stream()
        .map(Adjustment::toSpanned)
        .reduce((a, b) -> new SpannableStringBuilder(a).append(", ").append(b)).get();
  }

  public boolean isDismissible() {
    return dismissible;
  }

  public boolean isPredefined() {
    return predefined;
  }

  public boolean dmOnly() {
    return dmOnly();
  }

  public boolean endsBeforeTurn() {
    return endsBeforeTurn;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, duration, predefined, endsBeforeTurn, dmOnly,
        dismissible, icon);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConditionData other = (ConditionData) o;
    return predefined == other.predefined
        && endsBeforeTurn == other.endsBeforeTurn
        && icon == other.icon
        && Objects.equals(name, other.name)
        && Objects.equals(description, other.description)
        && Objects.equals(adjustments, other.adjustments)
        && Objects.equals(duration, other.duration)
        && Objects.equals(dmOnly, other.dmOnly)
        && Objects.equals(dismissible, other.dismissible);
  }

  @Override
  public String toString() {
    return name;
  }

  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_NAME, name);
    data.put(FIELD_DESCRIPTION, description);
    data.put(FIELD_ADJUSTMENTS,
        adjustments.stream().map(Adjustment::write).collect(Collectors.toList()));
    data.put(FIELD_DURATION, duration.write());
    data.put(FIELD_PREDEFINED, predefined);
    data.put(FIELD_ENDS_BEFORE_TURN, endsBeforeTurn);
    data.put(FIELD_DM_ONLY, dmOnly);
    data.put(FIELD_NO_SHOW, noShow);
    data.put(FIELD_DISMISSIBLE, dismissible);
    data.put(FIELD_ICON, icon);
    data.put(FIELD_COLOR, color);

    return data;
  }

  public static Builder newBuilder(String name) {
    return new Builder(name);
  }

  public static Builder newBuilder(String name, ConditionData condition) {
    return new Builder(name, condition);
  }

  public static ConditionData read(@Nullable Map<String, Object> data) {
    if (data == null) {
      throw new IllegalArgumentException("Data cannot be null");
    }

    String name = Values.get(data, FIELD_NAME, "...");
    String description = Values.get(data, FIELD_DESCRIPTION, "");
    List<Adjustment> adjustments = Values.getRawList(data, FIELD_ADJUSTMENTS).stream()
        .map(m -> Adjustment.read(m))
        .collect(Collectors.toList());
    Duration duration = Duration.read((Map<String, Object>) data.get(FIELD_DURATION));
    boolean predefined = Values.get(data, FIELD_PREDEFINED, false);
    boolean endBeforeTurn = Values.get(data, FIELD_ENDS_BEFORE_TURN, false);
    boolean dmOnly = Values.get(data, FIELD_DM_ONLY, false);
    boolean noShow = Values.get(data, FIELD_NO_SHOW, false);
    boolean dismissable = Values.get(data, FIELD_DISMISSIBLE, true);
    @DrawableRes int icon = (int) Values.get(data, FIELD_ICON, 0);
    @ColorRes int color = (int) Values.get(data, FIELD_COLOR, 0);

    return new ConditionData(name, description, adjustments, duration, predefined,
        endBeforeTurn, dmOnly, noShow, dismissable, icon, color);
  }

  public static class Builder {
    private String name;
    private String description;
    private List<Adjustment> adjustments = new ArrayList<>();
    private Duration duration = Duration.NULL;
    private boolean predefined;
    private boolean endsBeforeTurn;
    private boolean dmOnly;
    private boolean noShow = false;
    private boolean dismissable = true;
    @DrawableRes private int icon = R.drawable.icons8_hospital_24;
    @ColorRes private int color = 0;

    public Builder(String name) {
      this(name, Conditions.get(name));
    }

    public Builder(String name, ConditionData existing) {
      this(name, Optional.of(existing));
    }

    private Builder(String name, Optional<ConditionData> existing) {
      this.name = name;
      if (existing.isPresent()) {
        this.description = existing.get().description;
        this.adjustments.addAll(existing.get().adjustments);
        this.duration = existing.get().duration;
        this.predefined = existing.get().predefined;
        this.endsBeforeTurn = existing.get().endsBeforeTurn;
        this.dmOnly = existing.get().dmOnly;
        this.noShow = existing.get().noShow;
        this.dismissable = existing.get().dismissible;
        this.icon = existing.get().icon;
        this.color = existing.get().color;
      }
    }

    public Builder adjustment(Adjustment adjustment) {
      this.adjustments.add(adjustment);
      return this;
    }

    public Builder adjustment(String adjustment) {
      return adjustment(new GenericAdjustment(adjustment));
    }

    public Builder adjustments(List<Adjustment> adjustments) {
      this.adjustments.clear();
      this.adjustments.addAll(adjustments);
      return this;
    }

    public Builder adjustments(ConditionData condition) {
      return adjustments(condition.getAdjustments());
    }

    public ConditionData build() {
      return new ConditionData(name, description, adjustments, duration, predefined,
          endsBeforeTurn, dmOnly, noShow, dismissable, icon, color);
    }

    public Builder color(@ColorRes int color) {
      this.color = color;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder dmOnly() {
      this.dmOnly = true;
      return this;
    }

    public Builder duration(Duration duration) {
      this.duration = duration;
      return this;
    }

    public Builder endsBeforeTurn() {
      this.endsBeforeTurn = true;
      return this;
    }

    public Builder endsBeforeTurn(boolean value) {
      this.endsBeforeTurn = value;
      return this;
    }

    public Builder icon(@DrawableRes int icon) {
      this.icon = icon;
      return this;
    }

    public Builder noShow() {
      this.noShow = true;
      return this;
    }

    public Builder notDismissable() {
      this.dismissable = false;
      return this;
    }

    public Builder predefined() {
      this.predefined = true;
      return this;
    }

    public Builder predefined(boolean predefined) {
      this.predefined = predefined;
      return this;
    }
  }
}
