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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Validator;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import androidx.annotation.ColorRes;

/**
 * A text view with a label and description.
 */
public class LabelledTextView<T extends LabelledTextView, S extends TextView>
    extends LabelledView<T, S> {

  private TextWrapper<? extends TextView> text;
  private Optional<Validator> validator = Optional.empty();

  public LabelledTextView(Context context, AttributeSet attributes) {
    super(context, attributes);

    view(createTextView());
    text = TextWrapper.wrap(getView());
    text.get().setTextAppearance(R.style.LargeText);

    TypedArray styles =
        getContext().obtainStyledAttributes(attributes, R.styleable.LabelledTextView);
    TypedArray baseStyles =
        getContext().obtainStyledAttributes(attributes, new int [] { android.R.attr.inputType, });

    String defaultText = styles.getString(R.styleable.LabelledTextView_defaultText);
    if (defaultText != null) {
      text.text(defaultText);
    }
    text.textColorValue(styles.getColor(R.styleable.LabelledTextView_textColor,
        getContext().getResources().getColor(R.color.colorPrimary, null)));
    int lines = styles.getInt(R.styleable.LabelledTextView_minLines, 1);
    if (lines > 1) {
      text.get().setMinLines(lines);
      text.get().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
      text.get().setHorizontallyScrolling(false);
    }
    int type = baseStyles.getInt(0, 0);
    if (type != 0) {
      text.get().setInputType(type);
    }

    styles.recycle();
  }

  public String getText() {
    return text.getText();
  }

  @Override
  public T disabled() {
    return enabled(false);
  }

  @Override
  public T enabled() {
    return enabled(true);
  }

  @Override
  public T enabled(boolean enabled) {
    text.enabled(enabled);
    return super.enabled(enabled);
  }

  public T onClick(Wrapper.Action action) {
    text.onClick(action);

    return (T) this;
  }

  public T removeClick() {
    text.removeClick();

    return (T) this;
  }

  public T text(String text) {
    this.text.text(text);
    doValidate(text);

    return (T) this;
  }

  public T text(Spanned text) {
    this.text.text(text);
    doValidate(text.toString());

    return (T) this;
  }

  public T textColor(@ColorRes int color) {
    text.textColor(color);

    return (T) this;
  }

  public T type(int type) {
    text.get().setInputType(type);

    return (T) this;
  }

  public T validate(Validator validator) {
    this.validator = Optional.of(validator);

    return (T) this;
  }

  protected S createTextView() {
    return (S) new TextView(getContext());
  }

  protected List<String> doValidate(String value) {
    if (this.validator.isPresent()) {
      List<String> errors = this.validator.get().validate(value);
      error(errors);
      return errors;
    }

    return Collections.emptyList();
  }
}
