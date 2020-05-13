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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.ProductTemplate;
import net.ixitxachitls.companion.ui.views.EntityImageView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;
import net.ixitxachitls.companion.util.Texts;

import java.util.Optional;

/**
 * A subpage or tab per product.
 */
public class ProductFragment extends NestedCompanionFragment {

  public static final String ARG_NAME = "name";

  private static final String PATH = "products/";

  protected Optional<ProductTemplate> product = Optional.empty();

  private EntityImageView image;
  private TextWrapper<TextView> subtitle;
  private TextWrapper<TextView> description;
  private Wrapper<CheckBox> use;
  private Wrapper<CheckBox> owned;
  private LabelledTextView world;
  private LabelledTextView producer;
  private LabelledTextView date;
  private LabelledTextView pages;
  private LabelledTextView author;
  private LabelledTextView editor;
  private LabelledTextView cover;
  private LabelledTextView cartographer;
  private LabelledTextView illustator;
  private LabelledTextView typographer;
  private LabelledTextView manager;
  private LabelledTextView isbn;
  private LabelledTextView system;
  private LabelledTextView audience;
  private LabelledTextView type;
  private LabelledTextView style;
  private LabelledTextView layout;
  private LabelledTextView price;
  private LabelledTextView content;
  private LabelledTextView series;

  public ProductFragment() {}

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {

    ViewGroup view =
        (ViewGroup) inflater.inflate(R.layout.fragment_product_page, container, false);


    image = view.findViewById(R.id.entity_image);
    image.setup(PATH, R.drawable.noun_book_1411063);

    subtitle = TextWrapper.wrap(view, R.id.subtitle);
    description = TextWrapper.wrap(view, R.id.description);
    use = Wrapper.wrap(view, R.id.use);
    owned = Wrapper.wrap(view, R.id.owned);
    world = view.findViewById(R.id.world);
    producer = view.findViewById(R.id.producer);
    date = view.findViewById(R.id.date);
    pages = view.findViewById(R.id.pages);
    author = view.findViewById(R.id.author);
    editor = view.findViewById(R.id.editor);
    cover = view.findViewById(R.id.cover);
    cartographer = view.findViewById(R.id.cartographer);
    illustator = view.findViewById(R.id.illustrator);
    typographer = view.findViewById(R.id.typographer);
    manager = view.findViewById(R.id.manager);
    isbn = view.findViewById(R.id.isbn);
    system = view.findViewById(R.id.system);
    audience = view.findViewById(R.id.audience);
    type = view.findViewById(R.id.type);
    style = view.findViewById(R.id.style);
    layout = view.findViewById(R.id.layout);
    price = view.findViewById(R.id.price);
    content = view.findViewById(R.id.content);
    series = view.findViewById(R.id.series);

    redraw();
    return view;
  }


  @Override
  public void onPause() {
    super.onPause();

    if (product.isPresent()) {
      try {
        me().setProduct(product.get().getName(), owned.get().isChecked(),
            Templates.get().hasData(product.get().getName())
                ?  Optional.of(use.get().isChecked()) : Optional.empty());
      } catch (NumberFormatException e) {
        Status.error("Cannot parse number for owned miniatures: " + e);
      }
    }
  }

  private void redraw() {
    product = Templates.get().getProductTemplates().get(getArguments().getString(ARG_NAME));

    if(product.isPresent()) {
      image.setName(product.get().getName(),
          product.get().getName() + ": " + product.get().getFormattedTitle());
      image.setNumber(Templates.get().getProductTemplates().getNumber(product.get()),
          Templates.get().getProductTemplates().getFilteredNumber(),
          Templates.get().getProductTemplates().getFilteredOwnedNumber(me()));

      subtitle.text(Texts.toSpanned(getContext(), product.get().getSubtitle()));
      description.text(Texts.toSpanned(getContext(), product.get().getDescription()));
      owned.get().setChecked(me().ownsProduct(product.get().getName()));
      use.get().setChecked(me().usesProduct(product.get().getName()));
      use.visible(Templates.get().hasData(product.get().getName()));
      world.text(Strings.COMMA_JOINER.join(product.get().getWorlds()));
      producer.text(product.get().getProducer());
      date.text(product.get().getFormattedDate());
      pages.text(String.valueOf(product.get().getPages()));
      author.text(product.get().getFormattedAuthors());
      editor.text(product.get().getFormattedEditors());
      cover.text(product.get().getFormattedCover());
      cartographer.text(product.get().getFormattedCartographers());
      illustator.text(product.get().getFormattedIllustrators());
      typographer.text(product.get().getFormattedTypographers());
      manager.text(product.get().getFormattedManagers());
      isbn.text(product.get().getFormattedIsbn());
      system.text(product.get().getFormattedSystem());
      audience.text(product.get().getFormattedAudience());
      type.text(product.get().getFormattedType());
      style.text(product.get().getFormattedStyle());
      layout.text(product.get().getFormattedLayout());
      price.text(product.get().getFormattedPrice());
      content.text(product.get().getFormattedContent());
      series.text(product.get().getFormattedSeries());
    } else {
      image.clearName();
      image.clearNumber();

      subtitle.text("");
      description.text("");
      use.gone();
      world.text("");
      producer.text("");
      date.text("");
      pages.text("");
      author.text("");
      editor.text("");
      cover.text("");
      cartographer.text("");
      illustator.text("");
      typographer.text("");
      manager.text("");
      isbn.text("");
      system.text("");
      audience.text("");
      type.text("");
      style.text("");
      layout.text("");
      price.text("");
      content.text("");
      series.text("");
    }
  }
}
