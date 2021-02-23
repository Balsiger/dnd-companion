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

package net.ixitxachitls.companion.data.templates;

import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.data.Entry;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An entry that cannot be changed once created.
 */
public abstract class StoredTemplate<P extends MessageLite> extends Entry<P> {
  private final Template.TemplateProto template;

  public StoredTemplate(Template.TemplateProto template, String name) {
    super(name);

    this.template = template;
  }

  protected static Set<String> extractProductIds(Template.TemplateProto template) {
    return template.getReferenceList().stream()
        .map(Value.ReferenceProto::getName)
        .collect(Collectors.toSet());
  }

  public List<String> getReferences() {
    return template.getReferenceList().stream()
        .map(r -> formatReference(r))
        .collect(Collectors.toList());
  }

  public String getIncomplete() {
    return template.getIncomplete();
  }

  public String getDescription() {
    return template.getDescription();
  }

  protected static String formatReference(Value.ReferenceProto reference) {
    Optional<ProductTemplate> product =
        Templates.get().getProductTemplates().get(reference.getName());
    String name;
    if (product.isPresent()) {
      name = product.get().getTitle() + " (" + reference.getName() + ")";
    } else {
      name = reference.getName();
    }

    String pages = "";
    if (reference.getPagesCount() > 1) {
      pages = " pages " + Strings.SPACE_JOINER.join(reference.getPagesList().stream()
          .map(p -> formatRange(p))
          .collect(Collectors.toList()));
    } else if (reference.getPagesCount() > 0) {
      pages = " page " + formatRange(reference.getPages(0));
    }

    return name + pages;
  }

  protected static String formatRange(Value.RangeProto range) {
    if (range.getHigh() == range.getLow()) {
      return String.valueOf(range.getLow());
    }

    return range.getLow() + "_" + range.getHigh();
  }
}
