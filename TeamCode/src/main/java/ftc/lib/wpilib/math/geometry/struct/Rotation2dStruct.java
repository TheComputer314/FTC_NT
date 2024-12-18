// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.math.geometry.struct;

import edu.wpi.first.util.struct.Struct;
import ftc.lib.wpilib.math.geometry.Rotation2d;
import java.nio.ByteBuffer;

public class Rotation2dStruct implements Struct<Rotation2d> {
  @Override
  public Class<Rotation2d> getTypeClass() {
    return Rotation2d.class;
  }

  @Override
  public String getTypeName() {
    return "Rotation2d";
  }

  @Override
  public int getSize() {
    return kSizeDouble;
  }

  @Override
  public String getSchema() {
    return "double value";
  }

  @Override
  public Rotation2d unpack(ByteBuffer bb) {
    double value = bb.getDouble();
    return new Rotation2d(value);
  }

  @Override
  public void pack(ByteBuffer bb, Rotation2d value) {
    bb.putDouble(value.getRadians());
  }

  @Override
  public boolean isImmutable() {
    return true;
  }
}
