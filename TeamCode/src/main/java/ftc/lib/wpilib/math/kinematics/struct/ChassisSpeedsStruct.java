// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.math.kinematics.struct;

import edu.wpi.first.util.struct.Struct;
import ftc.lib.wpilib.math.kinematics.ChassisSpeeds;
import java.nio.ByteBuffer;

public class ChassisSpeedsStruct implements Struct<ChassisSpeeds> {
  @Override
  public Class<ChassisSpeeds> getTypeClass() {
    return ChassisSpeeds.class;
  }

  @Override
  public String getTypeName() {
    return "ChassisSpeeds";
  }

  @Override
  public int getSize() {
    return kSizeDouble * 3;
  }

  @Override
  public String getSchema() {
    return "double vx;double vy;double omega";
  }

  @Override
  public ChassisSpeeds unpack(ByteBuffer bb) {
    double vx = bb.getDouble();
    double vy = bb.getDouble();
    double omega = bb.getDouble();
    return new ChassisSpeeds(vx, vy, omega);
  }

  @Override
  public void pack(ByteBuffer bb, ChassisSpeeds value) {
    bb.putDouble(value.vxMetersPerSecond);
    bb.putDouble(value.vyMetersPerSecond);
    bb.putDouble(value.omegaRadiansPerSecond);
  }
}
