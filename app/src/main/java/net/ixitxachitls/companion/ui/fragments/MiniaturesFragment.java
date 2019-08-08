/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
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
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.MiniatureFilter;
import net.ixitxachitls.companion.data.templates.MiniatureTemplate;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.MiniatureConfigurationDialog;
import net.ixitxachitls.companion.ui.dialogs.MiniatureFilterDialog;
import net.ixitxachitls.companion.ui.dialogs.MiniatureLocationsDialog;
import net.ixitxachitls.companion.ui.views.ActionBarView;

import java.util.Optional;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * A fragment to display the miniatures.
 */
public class MiniaturesFragment extends CompanionFragment {

  private static final String LOADING_MINIATURES = "miniatures";

  private ViewPager pager;
  private PagerAdapter pagerAdapter;
  private ActionBarView.Action filterAction;
  private SeekBar seek;

  public MiniaturesFragment() {
    super(Type.miniatures);
  }

  @Override
  public boolean goBack() {
    CompanionFragments.get().show(Type.campaigns, Optional.empty());
    return true;
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    LinearLayout view = (LinearLayout)
        inflater.inflate(R.layout.fragment_miniatures, container, false);

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
    seek.setMax(Templates.get().getMiniatureTemplates().getFilteredNumber() - 1);
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

    clearActions();
    addAction(R.drawable.ic_arrow_back_black_24dp, "Back", "Go back to the campaigns overview")
        .onClick(this::back);
    filterAction = addAction(R.drawable.ic_filter_variant_black_24dp, "Filter",
        "Filter the displayed miniatures")
        .onClick(this::filter);
    addAction(R.drawable.ic_map_marker_black_24dp, "Locations",
        "Define or change the locations your miniatures are stored.")
        .onClick(this::editLocations);
    addAction(R.drawable.ic_settings_black_24dp, "Configuration",
        "Configure the miniatures displayed.")
        .onClick(this::config);

    startLoading(LOADING_MINIATURES);
    me().readMiniatures(this::miniaturesLoaded);

    return view;
  }

  private void back() {
    CompanionFragments.get().show(Type.campaigns, Optional.empty());
  }

  private void config() {
    MiniatureConfigurationDialog.newInstance().onSaved(o -> update()).display();
  }

  private void editLocations() {
    MiniatureLocationsDialog.newInstance().display();
  }

  private void filter() {
    MiniatureFilterDialog.newInstance(Templates.get().getMiniatureTemplates().getFilter(), true)
        .onSaved(this::filtered).display();
  }

  private void filtered(MiniatureFilter filter) {
    Templates.get().getMiniatureTemplates().filter(me(), filter);
    if (Templates.get().getMiniatureTemplates().isFiltered()) {
      filterAction.color(R.color.filtered);
    } else {
      filterAction.uncolor();
    }
    update();
    seek.setMax(Templates.get().getMiniatureTemplates().getFilteredNumber() - 1);
  }


  private void miniaturesLoaded() {
    finishLoading(LOADING_MINIATURES);
    update();
  }

  private void update() {
    pagerAdapter.notifyDataSetChanged();
  }

  public class PagerAdapter extends FragmentStatePagerAdapter {

    public PagerAdapter(FragmentManager manager) {
      super(manager);
    }

    @Override
    public int getCount() {
      return Templates.get().getMiniatureTemplates().getFilteredNumber();
    }

    @Override
    public int getItemPosition(Object miniature) {
      // When the miniatures change, it means that we applied or remove a filterAction. In this case,
      // saving a view for a miniature makes no sense.
      return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      Optional<MiniatureTemplate> miniature = Templates.get().getMiniatureTemplates().get(position);
      if (miniature.isPresent()) {
        return miniature.get().getName();
      } else {
        return "(not found)";
      }
    }

    @Override
    public Fragment getItem(int position) {
      Fragment fragment = new MiniatureFragment();
      Optional<MiniatureTemplate> template = Templates.get().getMiniatureTemplates().get(position);
      Bundle args = new Bundle();
      if (template.isPresent()) {
        args.putString(MiniatureFragment.ARG_NAME, template.get().getName());
      }
      fragment.setArguments(args);
      return fragment;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
      try{
        super.restoreState(state, loader);
      } catch (NullPointerException e){
        // This seems to happen when reloading the miniatures frame (cf.
        // https://stackoverflow.com/questions/18642890/fragmentstatepageradapter-with-childfragmentmanager-fragmentmanagerimpl-getfra
      }
    }
  }
}