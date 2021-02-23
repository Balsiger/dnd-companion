package net.ixitxachitls.companion.data.enums;

import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by user on 2021-02-23.
 */
public enum MetaMagic implements Enums.Named, Enums.Proto<Value.MetaMagic> {

  UNKNOWN("Unknown", "UNK", Value.MetaMagic.UNKNOWN_META_MAGIC),
  EMPOWERED("Empowered", "POW", Value.MetaMagic.EMPOWERED),
  ENLARGED("Enlarged", "LAR", Value.MetaMagic.ENLARGED),
  EXTENDED("Extended", "EXT", Value.MetaMagic.EXTENDED),
  HEIGHTENED("Heightened", "HIT", Value.MetaMagic.HEIGHTENED),
  MAXIMIZED("Maximized", "MAX", Value.MetaMagic.MAXIMIZED),
  QUICKENED("Quickened", "QIK", Value.MetaMagic.QUICKENED),
  SILENT("Silent", "SIL", Value.MetaMagic.SILENT),
  WIDENED("Widened", "WID", Value.MetaMagic.WIDENED);

  public static String PATTERN = Arrays.asList(values()).stream()
      .map(MetaMagic::getName).reduce((a, b) -> a + "|" + b).get()
      + "|" + Arrays.asList(values()).stream()
      .map(MetaMagic::getShortName).reduce((a, b) -> a + "|" + b).get();

  private final String name;
  private final String shortName;
  private final Value.MetaMagic proto;

  MetaMagic(String name, String shortName, Value.MetaMagic proto) {
    this.name = name;
    this.shortName = shortName;
    this.proto = proto;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getShortName() {
    return shortName;
  }

  @Override
  public Value.MetaMagic toProto() {
    return proto;
  }

  public static MetaMagic fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static MetaMagic fromProto(Value.MetaMagic proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
