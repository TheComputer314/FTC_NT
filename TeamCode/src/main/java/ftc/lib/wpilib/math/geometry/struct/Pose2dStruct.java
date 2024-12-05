// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.math.geometry.struct;

import edu.wpi.first.util.struct.Struct;
import ftc.lib.wpilib.math.geometry.Pose2d;
import ftc.lib.wpilib.math.geometry.Rotation2d;
import ftc.lib.wpilib.math.geometry.Translation2d;
import java.nio.ByteBuffer;

public class Pose2dStruct implements Struct<Pose2d> {
  @Override
  public Class<Pose2d> getTypeClass() {
    return Pose2d.class;
  }

  @Override
  public String getTypeName() {
    return "Pose2d";
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
  public Pose2d unpack(ByteBuffer bb) {
    Translation2d translation = Translation2d.struct.unpack(bb);
    Rotation2d rotation = Rotation2d.struct.unpack(bb);
    return new Pose2d(translation, rotation);
  }

  @Override
  public void pack(ByteBuffer bb, Pose2d value) {
    Translation2d.struct.pack(bb, value.getTranslation());
    Rotation2d.struct.pack(bb, value.getRotation());
  }

  @Override
  public boolean isImmutable() {
    return true;
  }
}
