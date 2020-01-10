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

package net.ixitxachitls.companion.data.templates;

import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A template for a product (book, game, box).
 */
public class ProductTemplate extends StoredTemplate<Template.ProductTemplateProto> {

  public static final String TYPE = "product";
  private static final String []MONTH = {
      "January", "February", "March", "April", "May", "June", "July", "August", "September",
      "October", "November", "Dezember"
  };

  private final Template.ProductTemplateProto proto;

  public ProductTemplate(Template.ProductTemplateProto proto) {
    super(proto.getTemplate().getName());

    this.proto = proto;
  }

  @Override
  public Set<String> getProductIds() {
    return Collections.emptySet();
  }

  public String getId() {
    return proto.getTemplate().getId();
  }

  public String getTitle() {
    return proto.getTitle();
  }

  public String getLeader() {
    return proto.getLeader();
  }

  public String getFormattedTitle() {
    if (proto.getLeader().isEmpty()) {
      return proto.getTitle();
    }

    return proto.getLeader() + " " + proto.getTitle();
  }

  public String getSubtitle() {
    return proto.getSubtitle();
  }

  public String getDescription() {
    return proto.getTemplate().getDescription();
  }

  public List<String> getWorlds() {
    return proto.getTemplate().getWorldList();
  }

  public String getFormattedDate() {
    if (proto.getDate().getMonth() == 0) {
      return String.valueOf(proto.getDate().getYear());
    }

    return MONTH[proto.getDate().getMonth() - 1] + " " + proto.getDate().getYear();
  }

  public int getYear() {
    return proto.getDate().getYear();
  }

  public int getPages() {
    return proto.getPages();
  }

  public String getFormattedAuthors() {
    return proto.getAuthorList().stream().map(this::formatPerson).collect(Collectors.joining(", "));
  }

  public String getFormattedEditors() {
    return proto.getEditorList().stream().map(this::formatPerson).collect(Collectors.joining(", "));
  }

  public String getFormattedCover() {
    return proto.getCoverList().stream().map(this::formatPerson).collect(Collectors.joining(", "));
  }

  public String getFormattedCartographers() {
    return proto.getCartographerList().stream()
        .map(this::formatPerson)
        .collect(Collectors.joining(", "));
  }

  public String getFormattedIllustrators() {
    return proto.getIllustratorList().stream()
        .map(this::formatPerson)
        .collect(Collectors.joining(", "));
  }

  public String getFormattedTypographers() {
    return proto.getTypographerList().stream()
        .map(this::formatPerson)
        .collect(Collectors.joining(", "));
  }

  public String getFormattedManagers() {
    return proto.getManagerList().stream()
        .map(this::formatPerson)
        .collect(Collectors.joining(", "));
  }

  public String getFormattedIsbn() {
    if (!proto.getIsbn13().getGroup13().isEmpty()) {
      return proto.getIsbn13().getGroup13() + "-" + proto.getIsbn13().getGroup() + "-"
          + proto.getIsbn13().getPublisher() + "-" + proto.getIsbn13().getTitle() + "-"
          + proto.getIsbn13().getCheck();
    }

    if (!proto.getIsbn().getGroup().isEmpty()) {
      return proto.getIsbn().getGroup() + "-" + proto.getIsbn().getPublisher() + "-"
          + proto.getIsbn().getTitle() + "-" + proto.getIsbn().getCheck();
    }

    return "";
  }

  public String getFormattedSystem() {
    return Strings.toWords(proto.getSystem().toString());
  }

  public String getFormattedAudience() {
    return Strings.toWords(proto.getAudience().toString());
  }

  public String getFormattedType() {
    return Strings.toWords(proto.getType().toString());
  }

  public String getFormattedStyle() {
    return Strings.toWords(proto.getStyle().toString());
  }

  public String getFormattedLayout() {
    return Strings.toWords(proto.getLayout().toString());
  }

  public String getFormattedPrice() {
    if (proto.getPrice().getNumber() > 0) {
      return proto.getPrice().getCurrency()
          + proto.getPrice().getNumber() / proto.getPrice().getPrecision() + "."
          + Strings.pad(proto.getPrice().getNumber() % proto.getPrice().getPrecision(),
          String.valueOf(proto.getPrice().getPrecision()).length() - 1, false);
    }

    return "";
  }

  public String getFormattedContent() {
    return proto.getContentList().stream()
        .map(this::formatContent)
        .collect(Collectors.joining(", "));
  }

  public String getFormattedSeries() {
    List<String> parts = new ArrayList<>();
    parts.addAll(proto.getSeriesList());

    if (!proto.getVolume().isEmpty()) {
      if (proto.getNumber().isEmpty()) {
        parts.add(proto.getVolume());
      } else {
        parts.add(proto.getVolume() + " " + proto.getNumber());
      }
    }

    return Strings.COMMA_JOINER.join(parts);
  }

  public String formatPerson(Template.ProductTemplateProto.Person person) {
    if (person.getJob().isEmpty()) {
      return person.getName();
    }

    return person.getName() + " (" + person.getJob() + ")";
  }

  public String formatContent(Template.ProductTemplateProto.Content content) {
    return Strings.toWords(content.getPart().toString());
  }

  public String getProducer() {
    return proto.getProducer();
  }

  public List<String> getWorld() {
    return proto.getTemplate().getWorldList();
  }

  public static Template.ProductTemplateProto defaultProto() {
    return Template.ProductTemplateProto.getDefaultInstance();
  }

  public static ProductTemplate fromProto(Template.ProductTemplateProto proto) {
    return new ProductTemplate(proto);
  }
}
