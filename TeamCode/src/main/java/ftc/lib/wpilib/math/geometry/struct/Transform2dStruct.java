// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.math.geometry.struct;

import edu.wpi.first.util.struct.Struct;
import ftc.lib.wpilib.math.geometry.Rotation2d;
import ftc.lib.wpilib.math.geometry.Transform2d;
import ftc.lib.wpilib.math.geometry.Translation2d;
import java.nio.ByteBuffer;

public class Transform2dStruct implements Struct<Transform2d> {
  @Override
  public Class<Transform2d> getTypeClass() {
    return Transform2d.class;
  }

  @Override
  public String getTypeName() {
    return "Transform2d";
  }

  @Override
  public int getSize() {
    return Translation2d.struct.getSize() + Rotation2d.struct.getSize();
  }

  @Override
  public String getSchema() {
    return "Translation2d translation;Rotation2d rotation";
  }

  @Override
  public Struct<?>[] getNested() {
    return new Struct<?>[] {Translation2d.struct, Rotation2d.struct};
  }

  @Override
  public Transform2d unpack(ByteBuffer bb) {
    Translation2d translation = Translation2d.struct.unpack(bb);
    Rotation2d rotation = Rotation2d.struct.unpack(bb);
    return new Transform2d(translation, rotation);
  }

  @Override
  public void pack(ByteBuffer bb, Transform2d value) {
    Translation2d.struct.pack(bb, value.getTranslation());
    Rotation2d.struct.pack(bb, value.getRotation());
  }

  @Override
  public boolean isImmutable() {
    return true;
  }
}
