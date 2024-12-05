// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.math.geometry.struct;

import edu.wpi.first.util.struct.Struct;
import ftc.lib.wpilib.math.geometry.Translation2d;
import java.nio.ByteBuffer;

public class Translation2dStruct implements Struct<Translation2d> {
  @Override
  public Class<Translation2d> getTypeClass() {
    return Translation2d.class;
  }

  @Override
  public String getTypeName() {
    return "Translation2d";
  }

  @Override
  public int getSize() {
    return kSizeDouble * 2;
  }

  @Override
  public String getSchema() {
    return "double x;double y";
  }

  @Override
  public Translation2d unpack(ByteBuffer bb) {
    double x = bb.getDouble();
    double y = bb.getDouble();
    return new Translation2d(x, y);
  }

  @Override
  public void pack(ByteBuffer bb, Translation2d value) {
    bb.putDouble(value.getX());
    bb.putDouble(value.getY());
  }

  @Override
  public boolean isImmutable() {
    return true;
  }
}
