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

package net.ixitxachitls.companion.ui.dialogs;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.FSCampaign;
import net.ixitxachitls.companion.data.values.Calendar;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * Fragment for editing a campaign date.
 */
public class DateDialog extends Dialog {

  private static final String ARG_ID = "id";

  private Optional<FSCampaign> campaign = Optional.empty();

  private GridView days;
  private DateAdapter adapter = new DateAdapter();
  private EditTextWrapper<EditText> year;
  private TextWrapper<TextView> month;
  private EditTextWrapper<EditText> hours;
  private EditTextWrapper<EditText> minutes;

  private int yearShown = 0;
  private int monthShown = 1;
  private int dayShown = 1;
  private int hoursShown = 0;
  private int minutesShown = 0;

  public DateDialog() {}

  public static DateDialog newInstance(String campaignId) {
    DateDialog dialog = new DateDialog();
    dialog.setArguments(arguments(R.layout.dialog_campaign_date,
        R.string.edit_campaign_date, R.color.campaign, campaignId));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, campaignId);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    campaign = application().fsCampaigns().getCampaign(getArguments().getString(ARG_ID));
    if (campaign.isPresent()) {
      from(campaign.get().getDate());
    }
  }

  @Override
  protected void createContent(View view) {
    year = EditTextWrapper.wrap(view, R.id.year);
    year.text(String.valueOf(yearShown)).onClick(this::editYear).onChange(this::editYear);
    Wrapper.wrap(view, R.id.year_minus).onClick(this::yearMinus);
    Wrapper.wrap(view, R.id.year_plus).onClick(this::yearPlus);
    month = TextWrapper.wrap(view, R.id.month);
    Wrapper.wrap(view, R.id.month_minus).onClick(this::monthMinus);
    Wrapper.wrap(view, R.id.month_plus).onClick(this::monthPlus);

    days = (GridView) view.findViewById(R.id.days);
    days.setAdapter(adapter);
    days.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectDay(position + 1);
      }
    });

    if (campaign.isPresent()) {
      hours = EditTextWrapper.wrap(view, R.id.hours)
          .text(String.valueOf(hoursShown)).onClick(this::editTime).onChange(this::editTime);
      hours.get().setFilters(new InputFilter[] {
          new MaxFilter(campaign.get().getCalendar().getHoursPerDay()) });
      hours.get().setSelectAllOnFocus(true);
      minutes = EditTextWrapper.wrap(view, R.id.minutes)
          .text(String.valueOf(minutesShown)).onClick(this::editTime).onChange(this::editTime);
      minutes.get().setFilters(new InputFilter[]
          {new MaxFilter(campaign.get().getCalendar().getMinutesPerHour())});
      Wrapper.wrap(view, R.id.plus_1).onClick(() -> addMinutes(1));
      Wrapper.wrap(view, R.id.plus_5).onClick(() -> addMinutes(5));
      Wrapper.wrap(view, R.id.plus_15).onClick(() -> addMinutes(15));
      Wrapper.wrap(view, R.id.plus_60).onClick(() -> addMinutes(60));
      Wrapper.wrap(view, R.id.night).onClick(this::night);
    }

  }

  private void from(CampaignDate date) {
    monthShown = date.getMonth();
    yearShown = date.getYear();
    dayShown = date.getDay();
    hoursShown = date.getHour();
    minutesShown = date.getMinute();
  }

  private void night() {
    if (campaign.isPresent()) {
      from(campaign.get().getCalendar().nextMorning(campaign.get().getDate()));
      update();
    }
  }

  public void addMinutes(int minutes) {
    if (campaign.isPresent()) {
      from(campaign.get().getCalendar().addMinutes(
          new CampaignDate(yearShown, monthShown, dayShown, hoursShown, minutesShown), minutes));
      update();
    }
  }

  private void editTime() {
    if (hours.getText().isEmpty() || minutes.getText().isEmpty()) {
      return;
    }

    try {
      hoursShown = Integer.parseInt(hours.getText());
      minutesShown = Integer.parseInt(minutes.getText());
    } catch(NumberFormatException e) {
      Status.log("Invalid time: " + hours.getText() + ":" + minutes.getText());
    }
  }

  private void selectDay(int day) {
    dayShown = day;
    update();
  }

  private void editYear() {
    try {
      yearShown = Integer.parseInt(year.getText());
    } catch(NumberFormatException e) {
      Status.log("Invalid year: " + year.getText());
    }
  }

  private void yearMinus() {
    yearShown--;
    update();
  }

  private void yearPlus() {
    yearShown++;
    update();
  }

  private void monthMinus() {
    if (campaign.isPresent()) {
      monthShown--;
      if (monthShown <= 0) {
        monthShown = campaign.get().getCalendar().getMonths().size();
        yearShown--;
      }
    }
    update();
  }

  private void monthPlus() {
    if (campaign.isPresent()) {
      monthShown++;
      if (monthShown > campaign.get().getCalendar().getMonths().size()) {
        monthShown = 1;
        yearShown++;
      }
    }
    update();
  }

  private String formatYear(int number) {
    Optional<Calendar.Year> calendarYear = Optional.empty();
    if (campaign.isPresent()) {
      calendarYear = campaign.get().getCalendar().getYear(number);
    }
    if (calendarYear.isPresent()) {
      return calendarYear.get().getName() + " (" + number + ")";
    } else {
      return String.valueOf(number);
    }
  }

  private String formatMonth(int number) {
    if (campaign.isPresent()) {
      return campaign.get().getCalendar().getMonth(number).getName();
    } else {
      return String.valueOf(number);
    }
  }

  private String formatTime(int number) {
    if (number < 10) {
      return "0" + number;
    }

    return String.valueOf(number);
  }

  @Override
  public void onDestroyView() {
    if (campaign.isPresent()) {
      campaign.get().setDate(new CampaignDate(yearShown, monthShown, dayShown, hoursShown,
          minutesShown));
      campaign.get().store();
    }

    super.onDestroyView();
  }

  protected void update() {
    if (campaign.isPresent()) {
      year.text(String.valueOf(yearShown));
      year.label(formatYear(yearShown));
      month.text(formatMonth(monthShown));

      // Setting hours or minutes will edit the values and overwrite our current values.
      int newHours = hoursShown;
      int newMinutes = minutesShown;
      hours.text(formatTime(newHours));
      minutes.text(formatTime(newMinutes));
      days.setAdapter(adapter);
    }
  }

  private class DateAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      if (campaign.isPresent()) {
        return campaign.get().getCalendar().getMonth(monthShown).getDays();
      } else {
        return 30;
      }
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
        text.setPadding(10, 20, 10, 20);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
      } else {
        text = (TextView) convertView;
      }

      int day = position + 1;
      if (day == dayShown) {
        text.setTypeface(null, Typeface.BOLD);
        text.setBackgroundColor(getResources().getColor(R.color.selected, null));
      } else {
        text.setTypeface(null, Typeface.NORMAL);
        text.setBackgroundColor(getResources().getColor(R.color.cell, null));
      }

      text.setText(String.valueOf(day));

      return text;
    }
  }

  private static class MaxFilter implements InputFilter {

    private int max;

    public MaxFilter(int max) {
      this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
                               int dend) {
      try {
        String value = dest.toString().substring(0, dstart)
            + source.toString().substring(start, end)
            + dest.toString().substring(dend, dest.toString().length());
        int input = Integer.parseInt(value);
        if (input < max) {
          return null;
        }
      } catch (NumberFormatException e) {
      }

      return "";
    }
  }
}
