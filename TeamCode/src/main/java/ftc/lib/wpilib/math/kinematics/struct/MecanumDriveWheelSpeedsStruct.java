// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.math.kinematics.struct;

import edu.wpi.first.util.struct.Struct;
import ftc.lib.wpilib.math.kinematics.MecanumDriveWheelSpeeds;
import java.nio.ByteBuffer;

public class MecanumDriveWheelSpeedsStruct implements Struct<MecanumDriveWheelSpeeds> {
  @Override
  public Class<MecanumDriveWheelSpeeds> getTypeClass() {
    return MecanumDriveWheelSpeeds.class;
  }

  @Override
  public String getTypeName() {
    return "MecanumDriveWheelSpeeds";
  }

  @Override
  public int getSize() {
    return kSizeDouble * 4;
  }

  @Override
  public String getSchema() {
    return "double front_left;double front_right;double rear_left;double rear_right";
  }

  @Override
  public MecanumDriveWheelSpeeds unpack(ByteBuffer bb) {
    double frontLeft = bb.getDouble();
    double frontRight = bb.getDouble();
    double rearLeft = bb.getDouble();
    double rearRight = bb.getDouble();
    return new MecanumDriveWheelSpeeds(frontLeft, frontRight, rearLeft, rearRight);
  }

  @Override
  public void pack(ByteBuffer bb, MecanumDriveWheelSpeeds value) {
    bb.putDouble(value.frontLeftMetersPerSecond);
    bb.putDouble(value.frontRightMetersPerSecond);
    bb.putDouble(value.rearLeftMetersPerSecond);
    bb.putDouble(value.rearRightMetersPerSecond);
  }
}
