 /*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.documents;

 import com.google.firebase.firestore.DocumentReference;
 import com.google.firebase.firestore.DocumentSnapshot;
 import com.google.firebase.firestore.FirebaseFirestoreException;
 import com.google.firebase.firestore.SetOptions;

 import net.ixitxachitls.companion.CompanionApplication;
 import net.ixitxachitls.companion.Status;
 import net.ixitxachitls.companion.data.CompanionContext;
 import net.ixitxachitls.companion.data.Templates;
 import net.ixitxachitls.companion.data.templates.MiniatureTemplate;

 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Optional;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.function.Function;
 import java.util.stream.Collectors;

 import androidx.annotation.CallSuper;

/**
 * The data for a single user.
 */
public class User extends Document<User> {

  private enum LoadingStatus {initial, loading, done,}
  protected static final String PATH = "users";
  private static final String MINIATURE_PATH = "/miniatures";
  private static final String PRODUCT_PATH = "/products";
  private static final Factory FACTORY = new Factory();
  private static final String FIELD_NICKNAME = "nickname";
  private static final String DEFAULT_NICKNAME = "";
  private static final String FIELD_PHOTO_URL = "photoUrl";
  private static final String FIELD_FEATURES = "features";
  private static final String FIELD_MINIATURE_OWNED = "owned";
  private static final String FIELD_MINIATURE_SPECIFIC_LOCATIONS = "specifc-locations";
  private static final String FIELD_MINIATURE_LOCATIONS = "locations";
  private static final String FIELD_MINIATURE_HIDDEN_SETS = "hidden-sets";
  private static final String FIELD_PRODUCTS = "products";
  private static final String FIELD_PRODUCTS_HIDDEN_PRODUCERS = "hidden-producers";
  private static final String FIELD_PRODUCTS_HIDDEN_WORLDS = "hidden-worlds";
  private static final String FIELD_PRODUCTS_HIDDEN_SYSTEMS = "hidden-systems";
  private static final String FIELD_PRODUCTS_HIDDEN_TYPES = "hidden-types";
  ;
  private final List<Callback> miniatureCallbacks = new ArrayList<>();
  private final List<Callback> productCallbacks = new ArrayList<>();
  private String nickname;
  private String photoUrl = "";
  private List<String> campaigns = new ArrayList<>();
  private List<String> features = new ArrayList<>();
  private Map<String, Long> miniatures = new HashMap<>();
  private Map<String, String> specificLocations = new HashMap<>();
  private LoadingStatus miniatureStatus = LoadingStatus.initial;
  private Map<String, Product> products = new HashMap<>();
  private LoadingStatus productStatus = LoadingStatus.initial;
  private SortedMap<String, MiniatureLocation> locations = new TreeMap<>();
  private List<MiniatureLocation> locationOrder = new ArrayList<>();
  private Set<String> hiddenSets = new HashSet<>();
  private Set<String> hiddenProducers = new HashSet<>();
  private Set<String> hiddenWorlds = new HashSet<>();
  private Set<String> hiddenSystems = new HashSet<>();
  private Set<String> hiddenTypes = new HashSet<>();

  public List<String> getCampaigns() {
    return campaigns;
  }

  public List<String> getFeatures() {
    return features;
  }

  public void setFeatures(List<String> features) {
    this.features = new ArrayList<>(features);
  }

  public List<String> getLocationNames() {
    List<String> values = new ArrayList<>();
    values.add("");
    values.addAll(locations.keySet());
    return values;
  }

  public List<MiniatureLocation> getLocations() {
    return locationOrder;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public List<String> getOwnedValues() {
    SortedSet<Long> owned = new TreeSet<>(miniatures.values());
    owned.add(0L);
    return new ArrayList<>(owned.stream().map(String::valueOf).collect(Collectors.toList()));
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public void setHiddenSets(List<String> sets) {
    hiddenSets.clear();
    hiddenSets.addAll(sets);

    storeMiniatures();
    Templates.get().getMiniatureTemplates().updateSets(this, hiddenSets);
  }

  public static boolean isUser(String id) {
    return id.matches(PATH + "/[^/]*/");
  }

  public boolean amDM(String id) {
    // Campaigns start with the user id.
    if (isA(id, Campaigns.PATH) && id.startsWith(getId())) {
      return true;
    }

    // Characters an anything below it need an indirection to the campaign.
    if (isA(id, Characters.PATH)) {
      Optional<Character> character =
          CompanionApplication.get().context().characters().get(
              id.replaceAll("(/" + Characters.PATH + "/.*?)/.*$", "$1"));
      return character.isPresent() && character.get().amDM();
    }

    return false;
  }

  public boolean amPlayer(String id) {
    return id.startsWith(getId());
  }

  public void deleteLocation(String name) {
    locations.remove(name);
    for (Iterator<MiniatureLocation> i = locationOrder.iterator(); i.hasNext(); ) {
      if (i.next().getName().equals(name)) {
        i.remove();
      }
    }
    storeMiniatures();
  }

  public Optional<MiniatureLocation> getLocation(String location) {
    return Optional.ofNullable(locations.get(location));
  }

  public int getMiniatureCount(String name) {
    return miniatures.getOrDefault(name, 0L).intValue();
  }

  public String getMiniatureSpecificLocation(String name) {
    return specificLocations.getOrDefault(name, "");
  }

  public boolean hasProducerHidden(String name) {
    return hiddenProducers.contains(name);
  }

  public boolean hasSetHidden(String name) {
    return hiddenSets.contains(name);
  }

  public boolean hasSystemHidden(String name) {
    return hiddenSystems.contains(name);
  }

  public boolean hasTypeHidden(String name) {
    return hiddenTypes.contains(name);
  }

  public boolean hasWorldHidden(String name) {
    return hiddenWorlds.contains(name);
  }

  public String locationFor(MiniatureTemplate template) {
    String specific = getMiniatureSpecificLocation(template.getName());
    if (!specific.isEmpty()) {
      return specific;
    }

    for (MiniatureLocation location : locationOrder) {
      if (location.matches(this, template).isPresent()) {
        return location.getName();
      }
    }

    return "";
  }

  public void moveLocation(MiniatureLocation location, int move) {
    int i = locationOrder.indexOf(location);
    if (i >= 0 && i + move >= 0 && i + move <= locationOrder.size()) {
      locationOrder.remove(i);
      locationOrder.add(i + move, location);
      storeMiniatures();
    }
  }

  @Override
  public void onUpdate(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
    Status.log("update of user document: " + snapshot.getMetadata());
    super.onUpdate(snapshot, e);
  }

  public boolean owns(String id) {
    return id.startsWith(getId());
  }

  public boolean ownsProduct(String id) {
    Product product = products.get(id.toLowerCase());
    return product == null ? false : product.isOwned();
  }

  public void readMiniatures(Callback callback) {
    if (miniatureStatus == LoadingStatus.done) {
      callback.done();
      return;
    }

    miniatureCallbacks.add(callback);
    if (miniatureStatus == LoadingStatus.loading) {
      return;
    }

    miniatureStatus = LoadingStatus.loading;
    db.document(getId() + MINIATURE_PATH + MINIATURE_PATH).get().addOnCompleteListener(task -> {
      miniatureStatus = LoadingStatus.done;
      if (task.isSuccessful() ) {
        Data data = Data.fromSnapshot(task.getResult());
        miniatures.putAll(data.getMap(FIELD_MINIATURE_OWNED, 0L));
        specificLocations.putAll(data.getMap(FIELD_MINIATURE_SPECIFIC_LOCATIONS, ""));

        for (MiniatureLocation location :
            data.getNestedList(FIELD_MINIATURE_LOCATIONS)
                .stream()
                .map(MiniatureLocation::read)
                .collect(Collectors.toList())) {
          if (!locations.containsKey(location.getName())) {
            locations.put(location.getName(), location);
            locationOrder.add(location);
          } else {
            Status.toast("Location " + location.getName() + " already read, ignored.");
          }
        }

        hiddenSets.addAll(data.getList(FIELD_MINIATURE_HIDDEN_SETS, Collections.emptyList()));

        verifyMiniatures();
      }

      miniatureCallbacks.forEach(Callback::done);
    });
  }

  public void readProducts(Callback callback) {
    if (productStatus == LoadingStatus.done) {
      callback.done();
      return;
    }

    productCallbacks.add(callback);
    if (productStatus == LoadingStatus.loading) {
      return;
    }

    productStatus = LoadingStatus.loading;
    DocumentReference document = db.document(getId() + PRODUCT_PATH + PRODUCT_PATH);
    document.get().addOnCompleteListener(task -> {
      productStatus = LoadingStatus.done;
      if (task.isSuccessful() ) {
        Data data = Data.fromSnapshot(task.getResult());
        products.putAll(
            data.getNestedList(FIELD_PRODUCTS).stream()
                .map(Product::read)
                .collect(Collectors.toMap(Product::getId, Function.identity(),
                    (a, b) -> {
                      Status.error("Duplicate key found, ignored: " + a);
                      return a;
                    })));
        hiddenProducers.addAll(data.getList(FIELD_PRODUCTS_HIDDEN_PRODUCERS,
            Collections.emptyList()));
        hiddenWorlds.addAll(data.getList(FIELD_PRODUCTS_HIDDEN_WORLDS, Collections.emptyList()));
        hiddenSystems.addAll(data.getList(FIELD_PRODUCTS_HIDDEN_SYSTEMS, Collections.emptyList()));
        hiddenTypes.addAll(data.getList(FIELD_PRODUCTS_HIDDEN_TYPES, Collections.emptyList()));
      }

      productCallbacks.forEach(Callback::done);
    });
  }

  public void replaceLocation(String originalName, MiniatureLocation location) {
    locations.remove(originalName);
    locations.put(location.getName(), location);

    if (!replaceLocationOrder(originalName, location)) {
      locationOrder.add(location);
    }

    storeMiniatures();
   }

  public void setHiddenProducts(List<String> producers, List<String> worlds, List<String> systems,
                                List<String> types) {
    hiddenProducers.clear();
    hiddenProducers.addAll(producers);
    hiddenWorlds.clear();
    hiddenWorlds.addAll(worlds);
    hiddenSystems.clear();
    hiddenSystems.addAll(systems);
    hiddenTypes.clear();
    hiddenTypes.addAll(types);

    storeProducts();
    Templates.get().getProductTemplates().updateHidden(this, hiddenProducers, hiddenWorlds,
        hiddenSystems, hiddenTypes);
  }

  public void setMiniatureCount(String miniature, long count) {
    if (miniatures.getOrDefault(miniature, 0L) != count) {
      if (count == 0) {
        miniatures.remove(miniature);
      } else{
        miniatures.put(miniature, count);
      }
      storeMiniatures();
    }
  }

  public void setMiniatureSpecificLocation(MiniatureTemplate miniature, String location) {
    if (!location.equals(locationFor(miniature))) {
      if (location.isEmpty()) {
        specificLocations.remove(miniature.getName());
      } else {
        specificLocations.put(miniature.getName(), location);
      }

      storeMiniatures();
    }
  }

  public void setProduct(String id, boolean owned, Optional<Boolean> used) {
    Product product = products.get(id.toLowerCase());
    if (product == null && (owned || used.isPresent()) ||
        (product != null && (product.owned != owned ||
            (used.isPresent() && product.used != used.get())))) {
      product = new Product(id.toLowerCase(), owned, used.isPresent() ? used.get() : true);
      products.put(id.toLowerCase(), product);
      storeProducts();
    }
  }

  @Override
  public String toString() {
    return getNickname();
  }

  public boolean usesProduct(String id) {
    Product product = products.get(id.toLowerCase());
    return product == null ? true : product.isUsed();
  }

  @Override
  public Data write() {
    return Data.empty()
        .set(FIELD_NICKNAME, nickname)
        .set(FIELD_PHOTO_URL, photoUrl)
        .set(FIELD_FEATURES, features);
  }

  public Data writeMiniatures() {
    return Data.empty()
        .set(FIELD_MINIATURE_OWNED, miniatures)
        .set(FIELD_MINIATURE_SPECIFIC_LOCATIONS, specificLocations)
        .setNested(FIELD_MINIATURE_LOCATIONS, locationOrder)
        .set(FIELD_MINIATURE_HIDDEN_SETS, new ArrayList<>(hiddenSets));
  }

  public Data writeProducts() {
    return Data.empty()
        .setNested(FIELD_PRODUCTS, products.values())
        .set(FIELD_PRODUCTS_HIDDEN_PRODUCERS, new ArrayList<>(hiddenProducers))
        .set(FIELD_PRODUCTS_HIDDEN_WORLDS, new ArrayList<>(hiddenWorlds))
        .set(FIELD_PRODUCTS_HIDDEN_SYSTEMS, new ArrayList<>(hiddenSystems))
        .set(FIELD_PRODUCTS_HIDDEN_TYPES, new ArrayList<>(hiddenTypes));
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();
    nickname = data.get(FIELD_NICKNAME, DEFAULT_NICKNAME);
    photoUrl = data.get(FIELD_PHOTO_URL, "");
    features = data.getList(FIELD_FEATURES, new ArrayList<>());
  }

  private void readCampaigns() {
    context.invites().listenCampaigns(campaigns -> {
      this.campaigns = campaigns;
      CompanionApplication.get().update("user read campaigns");
    });
  }

  private boolean replaceLocationOrder(String name, MiniatureLocation location) {
    for (int i = 0; i < locationOrder.size(); i++) {
      if (locationOrder.get(i).getName().equals(name)) {
        locationOrder.set(i, location);
        return true;
      }
    }

    return false;
  }

  private void storeMiniatures() {
    db.collection(getId() + MINIATURE_PATH).document(MINIATURE_PATH)
        .set(writeMiniatures().asMap())
        .addOnFailureListener(e -> Status.exception("Saving miniature data failed!", e));
  }

  private void storeProducts() {
    db.collection(getId() + PRODUCT_PATH).document(PRODUCT_PATH)
        .set(writeProducts().asMap(), SetOptions.merge())
        .addOnFailureListener(e -> Status.exception("Saving products data failed!", e));
  }

  private void verifyMiniatures() {
    boolean toasted = false;
    for (String miniature : miniatures.keySet()) {
      if (!Templates.get().getMiniatureTemplates().get(miniature).isPresent()) {
        Templates.get().getMiniatureTemplates().addDummy(miniature);
        if (!toasted) {
          Status.error("Cannot find owned miniature " + miniature);
          // Only show the first missing to avoid toast spamming!
          toasted = true;
        }
      }
    }
  }

  protected static User getOrCreate(CompanionContext context, String id) {
    User user = Document.getOrCreate(FACTORY, context, PATH + "/" + id);
    user.whenReady(() -> {
      user.readCampaigns();
      CompanionApplication.get().update("user loaded");
    });

    return user;
  }

  private static class Factory implements DocumentFactory<User> {
    @Override
    public User create() {
      return new User();
    }
  }

  private static class Product extends NestedDocument {

    private static final String FIELD_ID = "id";
    private static final String FIELD_OWNED = "owned";
    private static final String FIELD_USED = "used";

    private final String id;
    private final boolean owned;
    private final boolean used;

    public Product(String id, boolean owned, boolean used) {
      this.id = id;
      this.owned = owned;
      this.used = used;
    }

    public String getId() {
      return id;
    }

    public boolean isOwned() {
      return owned;
    }

    public boolean isUsed() {
      return used;
    }

    @Override
    public Data write() {
      return Data.empty()
          .set(FIELD_ID, id)
          .set(FIELD_OWNED, owned)
          .set(FIELD_USED, used);
    }

    @CallSuper
    protected static Product read(Data data) {
      return new Product(data.get(FIELD_ID, ""), data.get(FIELD_OWNED, false),
          data.get(FIELD_USED, true));
    }
  }
}
