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

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.ProductFilter;
import net.ixitxachitls.companion.data.templates.ProductTemplate;
import net.ixitxachitls.companion.ui.dialogs.ProductConfigurationDialog;
import net.ixitxachitls.companion.ui.dialogs.ProductFilterDialog;

import java.util.Optional;

import androidx.fragment.app.Fragment;

/**
 * A fragment for listing products.
 */
public class ProductsFragment extends TemplatesFragment {

  private static final String LOADING_PRODUCTS = "products";

  public ProductsFragment() {
    super(Type.products);
  }

  @Override
  protected void loadEntities() {
    super.loadEntities();
    startLoading(LOADING_PRODUCTS);
    me().readProducts(this::loadedEntities);
  }

  @Override
  protected void config() {
    ProductConfigurationDialog.newInstance().onSaved(o -> update()).display();
  }

  @Override
  protected void filter() {
    ProductFilterDialog.newInstance(Templates.get().getProductTemplates().getFilter())
        .onSaved(this::filtered).display();
  }

  private void filtered(ProductFilter filter) {
    Templates.get().getProductTemplates().filter(me(), filter);
    if (Templates.get().getProductTemplates().isFiltered()) {
      filterAction.color(R.color.filtered);
    } else {
      filterAction.uncolor();
    }
    update();
    seek.setMax(Templates.get().getProductTemplates().getFilteredNumber() - 1);
  }

  @Override
  protected String getTitle(int position) {
    return null;
  }

  @Override
  protected Fragment getTemplateFragment(int position) {
    Fragment fragment = new ProductFragment();
    Optional<ProductTemplate> template = Templates.get().getProductTemplates().get(position);
    Bundle args = new Bundle();
    if (template.isPresent()) {
      args.putString(ProductFragment.ARG_NAME, template.get().getName());
    }
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected int getTemplatesCount() {
    return Templates.get().getProductTemplates().getFilteredNumber();
  }

  @Override
  protected void loadedEntities() {
    super.loadedEntities();
    finishLoading(LOADING_PRODUCTS);
    update();
  }
}
