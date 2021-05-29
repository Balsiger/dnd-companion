package net.ixitxachitls.companion.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.ui.Alert;
import net.ixitxachitls.companion.ui.Hints;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.IdRes;
import androidx.annotation.IntegerRes;

/**
 * A fragment showing the initial loading progress.
 */
public class LoadingFragment extends CompanionFragment {

  private final Map<Templates.Kind, Integer> counts = new HashMap<>();
  private final Map<Templates.Kind, TextWrapper<TextView>> counters = new HashMap<>();
  private final Map<Templates.Kind, ProgressBar> progress = new HashMap<>();

  public LoadingFragment() {
    super(Type.loading);
  }

  @Override
  public boolean goBack() {
    return false;
  }

  public void increment(Templates.Kind kind) {
    int count = counts.getOrDefault(kind, 0) + 1;
    counts.put(kind, count);
    if (counters.containsKey(kind)) {
      counters.get(kind).text(Integer.toString(count));
    }
    if (progress.containsKey(kind)) {
      progress.get(kind).setProgress(count);
    }
  }

  public void loggedIn() {
    if (isVisible()) {
      getView().findViewById(R.id.login_progress).setVisibility(View.GONE);
      getView().findViewById(R.id.login_label).setVisibility(View.GONE);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    super.onCreateView(inflater, container, state);

    LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_loading, container, false);

    TextWrapper.wrap(view, R.id.hint).text(Hints.nextHint());
    setupKind(view, Templates.Kind.worlds, R.id.worlds_current, R.id.worlds_total,
        R.id.worlds_progress, R.integer.app_entities_world);
    setupKind(view, Templates.Kind.monsters, R.id.monsters_current, R.id.monsters_total,
        R.id.monsters_progress, R.integer.app_entities_monster);
    setupKind(view, Templates.Kind.levels, R.id.levels_current, R.id.levels_total,
        R.id.levels_progress, R.integer.app_entities_level);
    setupKind(view, Templates.Kind.items, R.id.items_current, R.id.items_total,
        R.id.items_progress, R.integer.app_entities_item);
    setupKind(view, Templates.Kind.feats, R.id.feats_current, R.id.feats_total,
        R.id.feats_progress, R.integer.app_entities_feat);
    setupKind(view, Templates.Kind.miniatures, R.id.miniatures_current, R.id.miniatures_total,
        R.id.miniatures_progress, R.integer.app_entities_miniature);
    setupKind(view, Templates.Kind.skills, R.id.skills_current, R.id.skills_total,
        R.id.skills_progress, R.integer.app_entities_skill);
    setupKind(view, Templates.Kind.spells, R.id.spells_current, R.id.spells_total,
        R.id.spells_progress, R.integer.app_entities_spell);
    setupKind(view, Templates.Kind.qualities, R.id.qualities_current, R.id.qualities_total,
        R.id.qualities_progress, R.integer.app_entities_quality);
    setupKind(view, Templates.Kind.adventures, R.id.adventures_current, R.id.adventures_total,
        R.id.adventures_progress, R.integer.app_entities_adventure);
    setupKind(view, Templates.Kind.products, R.id.products_current, R.id.products_total,
        R.id.products_progress, R.integer.app_entities_product);

    return view;
  }

  private void setupKind(View view, Templates.Kind kind, @IdRes int currentId, @IdRes int totalId,
                         @IdRes int progressId, @IntegerRes int totalCountId) {
    TextWrapper.wrap(view, totalId).text(Integer.toString(getResources().getInteger(totalCountId)));
    counters.put(kind, TextWrapper.wrap(view, currentId));
    progress.put(kind, view.findViewById(progressId));
    progress.get(kind).setMax(getResources().getInteger(totalCountId));
  }

  @Override
  public void update() {
  }

  @Override
  public void onResume() {
    super.onResume();
  }
}
