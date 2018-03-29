/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Random;

/**
 * A view to select a dice result or roll randomly.
 */
public class DiceView extends LinearLayout {

  private static final Random RANDOM = new Random();

  private int modifier;
  private int dice;
  private SelectAction action;
  private final DiceAdapter adapter = new DiceAdapter();

  // UI elements.
  private GridView grid;

  private final TextWrapper<TextView> label;
  private final TextWrapper<TextView> modifierView;
  private final Wrapper<Button> random;

  public DiceView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.DiceView );

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_dice, this, false);
    view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));

    label = TextWrapper.wrap(view, R.id.label);
    label.text(array.getString(R.styleable.DiceView_modifier_label));
    modifierView = TextWrapper.wrap(view, R.id.modifier);
    random = Wrapper.<Button>wrap(view, R.id.random)
        .onClick(this::selectRandom)
        .description("Random", "Instead of selecting the number that you actually rolled on your "
            + "dice, you can also just click on this button to randomly select a value");
    grid = view.findViewById(R.id.numbers);
    grid.setAdapter(adapter);
    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        select(position + 1);
      }
    });

    addView(view);
  }

  private void selectRandom() {
    select(RANDOM.nextInt(dice) + 1);
  }

  private void select(int number) {
    if (action != null) {
      action.select(number + modifier);
    }
  }

  public void setSelectAction(SelectAction action) {
    this.action = action;
  }

  public void setLabel(String text) {
    label.text(text);
  }

  public void setModifier(int modifier) {
    this.modifier = modifier;
    this.modifierView.text(modifier >= 0 ? "+" + modifier : String.valueOf(modifier));
  }

  public void setDice(int dice) {
    this.dice = dice;

    grid.setAdapter(adapter);
  }

  private class DiceAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      return dice;
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView text;
      if (convertView == null) {
        text = new TextView(parent.getContext(), null, R.style.LargeText);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setPadding(20, 40, 20, 40);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setBackgroundColor(getResources().getColor(R.color.cell, null));
      } else {
        text = (TextView) convertView;
      }

      text.setText(String.valueOf(position + 1));

      return text;
    }
  }

  @FunctionalInterface
  public interface SelectAction {
    public void select(int selection);
  }
}
