/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.FilteredTemplatesStore;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.views.ActionBarView;

import java.util.Optional;

import androidx.annotation.CallSuper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * A class for rendering a list of templates.
 */
public abstract class TemplatesFragment extends CompanionFragment {

  protected ViewPager pager;
  protected PagerAdapter pagerAdapter;
  protected ActionBarView.Action filterAction;
  protected SeekBar seek;

  protected TemplatesFragment(Type type) {
    super(type);
  }

  @Override
  public boolean goBack() {
    CompanionFragments.get().show(Type.campaigns, Optional.empty());
    return true;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    LinearLayout view = (LinearLayout)
        inflater.inflate(R.layout.fragment_templates, container, false);

    pager = view.findViewById(R.id.pager);
    pagerAdapter = new PagerAdapter(getChildFragmentManager());
    pager.setAdapter(pagerAdapter);
    pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
        seek.setProgress(position);
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }
    });

    seek = view.findViewById(R.id.seek);
    seek.setMax(getTemplatesCount() - 1);
    seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          pager.setCurrentItem(progress);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });

    setupActions();
    loadEntities();
    return view;
  }

  protected abstract void config();
  protected abstract void filter();
  protected abstract String getTitle(int position);
  protected abstract Fragment getTemplateFragment(int position);
  protected abstract int getTemplatesCount();
  protected abstract FilteredTemplatesStore<?, ?> getTemplates();

  @CallSuper
  protected void setupActions() {
    clearActions();
    addAction(R.drawable.ic_arrow_back_black_24dp, "Back", "Go back to the campaigns overview")
        .onClick(this::goBack);
    filterAction = addAction(R.drawable.ic_filter_variant_black_24dp, "Filter",
        "Filter the displayed miniatures")
        .onClick(this::filter);
    addAction(R.drawable.ic_settings_black_24dp, "Configuration",
        "Configure the miniatures displayed.")
        .onClick(this::config);
  }

  @CallSuper
  protected void loadEntities() {
    pager.setVisibility(View.GONE);
  }

  @CallSuper
  protected void loadedEntities() {
    pager.setVisibility(View.VISIBLE);
  }

  protected void update() {
    if (getTemplates().isFiltered()) {
      filterAction.color(R.color.filtered);
    } else {
      filterAction.uncolor();
    }

    pagerAdapter.notifyDataSetChanged();
  }

  public class PagerAdapter extends FragmentStatePagerAdapter {

    public PagerAdapter(FragmentManager manager) {
      super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public int getCount() {
      return getTemplatesCount();
    }

    @Override
    public int getItemPosition(Object template) {
      // When the miniatures change, it means that we applied or remove a filterAction. In this case,
      // saving a view for a miniature makes no sense.
      return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return getTitle(position);
    }

    @Override
    public Fragment getItem(int position) {
      return getTemplateFragment(position);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
      try{
        super.restoreState(state, loader);
      } catch (NullPointerException|IllegalStateException e){
        // This seems to happen when reloading the miniatures frame (cf.
        // https://stackoverflow.com/questions/18642890/fragmentstatepageradapter-with-childfragmentmanager-fragmentmanagerimpl-getfra
      }
    }
  }
}
