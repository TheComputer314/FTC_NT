// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.trobotix;

import edu.wpi.first.networktables.*;
import edu.wpi.first.util.CircularBuffer;
import edu.wpi.first.util.struct.Struct;
import edu.wpi.first.util.struct.StructSerializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A class for easy publishing of data to NetworkTables. Inspired by DogLog.
 *
 * @noinspection resource
 */
public final class Telemetry {
  private Telemetry() {}

  public static void put(String key, boolean... value) {
    TelemetryThread.getInstance().add(new BooleanArrayQueuedEntry(key, value));
  }

  public static void put(String key, boolean value) {
    TelemetryThread.getInstance().add(new BooleanQueuedEntry(key, value));
  }

  public static void put(String key, double... value) {
    TelemetryThread.getInstance().add(new DoubleArrayQueuedEntry(key, value));
  }

  public static void put(String key, double value) {
    TelemetryThread.getInstance().add(new DoubleQueuedEntry(key, value));
  }

  public static void put(String key, float... value) {
    TelemetryThread.getInstance().add(new FloatArrayQueuedEntry(key, value));
  }

  public static void put(String key, float value) {
    TelemetryThread.getInstance().add(new FloatQueuedEntry(key, value));
  }

  public static void put(String key, long... value) {
    TelemetryThread.getInstance().add(new IntegerArrayQueuedEntry(key, value));
  }

  public static void put(String key, long value) {
    TelemetryThread.getInstance().add(new IntegerQueuedEntry(key, value));
  }

  public static void put(String key, int... value) {
    TelemetryThread.getInstance().add(new IntegerArrayQueuedEntry(key, value));
  }

  public static void put(String key, int value) {
    TelemetryThread.getInstance().add(new IntegerQueuedEntry(key, value));
  }

  public static void put(String key, String... value) {
    TelemetryThread.getInstance().add(new StringArrayQueuedEntry(key, value));
  }

  public static void put(String key, String value) {
    TelemetryThread.getInstance().add(new StringQueuedEntry(key, value));
  }

  @SafeVarargs
  public static <T extends StructSerializable> void put(String key, T... value) {
    TelemetryThread.getInstance().add(new StructArrayQueuedEntry<T>(key, value));
  }

  public static <T extends StructSerializable> void put(String key, T value) {
    TelemetryThread.getInstance().add(new StructQueuedEntry<T>(key, value));
  }

  private static class TelemetryThread extends Thread implements AutoCloseable {
    private static TelemetryThread instance;

    private static TelemetryThread getInstance() {
      if (instance == null) {
        instance = new TelemetryThread(256);
        instance.start();
      }
      return instance;
    }

    private final NetworkTable table = NetworkTableInstance.getDefault().getTable("Outputs");
    private final CircularBuffer<QueuedEntry> circularQueue;

    TelemetryThread(int size) {
      circularQueue = new CircularBuffer<>(size);
      setDaemon(true);
      setName("Telemetry Thread");
    }

    @Override
    public void run() {
      //noinspection InfiniteLoopStatement
      while (true) {
        synchronized (circularQueue) {
          new IntegerQueuedEntry("Telemetry Queue Size", circularQueue.size()).publish(table);
          for (int i = 0; i < circularQueue.size(); i++) {
            circularQueue.get(i).publish(table);
          }
          circularQueue.clear();
        }
        NetworkTableInstance.getDefault().flushLocal();
        try {
          //noinspection BusyWait
          Thread.sleep(100);
        } catch (Exception ignored) {
        }
        Thread.yield();
      }
    }

    void add(QueuedEntry entry) {
      circularQueue.addLast(entry);
    }

    @Override
    public void close() {
      // Close all publishers stored in the maps
      for (var publisher : BooleanArrayQueuedEntry.publishers.values()) {
        publisher.close();
      }
      BooleanArrayQueuedEntry.publishers.clear();
      for (var publisher : BooleanQueuedEntry.publishers.values()) {
        publisher.close();
      }
      BooleanQueuedEntry.publishers.clear();
      for (var publisher : DoubleArrayQueuedEntry.publishers.values()) {
        publisher.close();
      }
      DoubleArrayQueuedEntry.publishers.clear();
      for (var publisher : DoubleQueuedEntry.publishers.values()) {
        publisher.close();
      }
      DoubleQueuedEntry.publishers.clear();
      for (var publisher : FloatArrayQueuedEntry.publishers.values()) {
        publisher.close();
      }
      FloatArrayQueuedEntry.publishers.clear();
      for (var publisher : FloatQueuedEntry.publishers.values()) {
        publisher.close();
      }
      FloatQueuedEntry.publishers.clear();
      for (var publisher : IntegerArrayQueuedEntry.publishers.values()) {
        publisher.close();
      }
      IntegerArrayQueuedEntry.publishers.clear();
      for (var publisher : IntegerQueuedEntry.publishers.values()) {
        publisher.close();
      }
      IntegerQueuedEntry.publishers.clear();
      for (var publisher : StringArrayQueuedEntry.publishers.values()) {
        publisher.close();
      }
      StringArrayQueuedEntry.publishers.clear();
      for (var publisher : StringQueuedEntry.publishers.values()) {
        publisher.close();
      }
      StringQueuedEntry.publishers.clear();
      for (var publisher : StructArrayQueuedEntry.publishers.values()) {
        publisher.close();
      }
      StructArrayQueuedEntry.publishers.clear();
      for (var publisher : StructQueuedEntry.publishers.values()) {
        publisher.close();
      }
      StructQueuedEntry.publishers.clear();
    }
  }

  private static final PubSubOption PUB_SUB_OPTIONS = PubSubOption.sendAll(true);

  private interface QueuedEntry {
    void publish(NetworkTable table);
  }

  private record BooleanArrayQueuedEntry(String key, boolean... value) implements QueuedEntry {
    static final Map<String, BooleanArrayPublisher> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getBooleanArrayTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record BooleanQueuedEntry(String key, boolean value) implements QueuedEntry {
    static final Map<String, BooleanPublisher> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getBooleanTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record DoubleArrayQueuedEntry(String key, double... value) implements QueuedEntry {
    static final Map<String, DoubleArrayPublisher> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getDoubleArrayTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record DoubleQueuedEntry(String key, double value) implements QueuedEntry {
    static final Map<String, DoublePublisher> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getDoubleTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record FloatArrayQueuedEntry(String key, float... value) implements QueuedEntry {
    static final Map<String, FloatArrayPublisher> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getFloatArrayTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record FloatQueuedEntry(String key, float value) implements QueuedEntry {
    static final Map<String, FloatPublisher> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getFloatTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record IntegerArrayQueuedEntry(String key, long... value) implements QueuedEntry {
    static final Map<String, IntegerArrayPublisher> publishers = new HashMap<>();

    IntegerArrayQueuedEntry(String key, int... value) {
      this(key, new long[value.length]);
      for (int i = 0; i < value.length; i++) {
        this.value[i] = value[i];
      }
    }

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getIntegerArrayTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record IntegerQueuedEntry(String key, long value) implements QueuedEntry {
    static final Map<String, IntegerPublisher> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getIntegerTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record StringArrayQueuedEntry(String key, String... value) implements QueuedEntry {
    static final Map<String, StringArrayPublisher> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getStringArrayTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record StringQueuedEntry(String key, String value) implements QueuedEntry {
    static final Map<String, StringPublisher> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      publishers
          .computeIfAbsent(key, k -> table.getStringTopic(k).publish(PUB_SUB_OPTIONS))
          .set(value);
    }
  }

  private record StructArrayQueuedEntry<T extends StructSerializable>(String key, T... value)
      implements QueuedEntry {
    static final Map<String, StructArrayPublisher<?>> publishers = new HashMap<>();

    @SafeVarargs
    public StructArrayQueuedEntry {}

    @Override
    public void publish(NetworkTable table) {
      //noinspection unchecked
      ((StructArrayPublisher<T>)
              publishers.computeIfAbsent(
                  key,
                  k ->
                      table
                          .getStructArrayTopic(k, getStruct(value[0].getClass()))
                          .publish(PUB_SUB_OPTIONS)))
          .set(value);
    }
  }

  private record StructQueuedEntry<T extends StructSerializable>(String key, T value)
      implements QueuedEntry {
    static final Map<String, StructPublisher<?>> publishers = new HashMap<>();

    @Override
    public void publish(NetworkTable table) {
      //noinspection unchecked
      ((StructPublisher<T>)
              publishers.computeIfAbsent(
                  key,
                  k ->
                      table
                          .getStructTopic(k, getStruct(value.getClass()))
                          .publish(PUB_SUB_OPTIONS)))
          .set(value);
    }
  }

  private static final HashMap<
          Class<? extends StructSerializable>, Struct<? extends StructSerializable>>
      structCache = new HashMap<>();

  private static <T extends StructSerializable> Struct<T> getStruct(Class<T> classObj) {
    //noinspection unchecked
    return (Struct<T>)
        structCache.computeIfAbsent(
            classObj,
            c -> {
              try {
                var field = classObj.getField("struct");
                //noinspection unchecked
                return (Struct<T>) field.get(null);
              } catch (NoSuchFieldException e) {
                throw new IllegalStateException(
                    classObj.getSimpleName() + " doesn't have a struct field!");
              } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
              }
            });
  }
}
