// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.math.kinematics.struct;

import edu.wpi.first.util.struct.Struct;
import ftc.lib.wpilib.math.kinematics.MecanumDriveWheelPositions;
import java.nio.ByteBuffer;

public class MecanumDriveWheelPositionsStruct implements Struct<MecanumDriveWheelPositions> {
  @Override
  public Class<MecanumDriveWheelPositions> getTypeClass() {
    return MecanumDriveWheelPositions.class;
  }

  @Override
  public String getTypeName() {
    return "MecanumDriveWheelPositions";
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
  public MecanumDriveWheelPositions unpack(ByteBuffer bb) {
    double frontLeft = bb.getDouble();
    double frontRight = bb.getDouble();
    double rearLeft = bb.getDouble();
    double rearRight = bb.getDouble();
    return new MecanumDriveWheelPositions(frontLeft, frontRight, rearLeft, rearRight);
  }

  @Override
  public void pack(ByteBuffer bb, MecanumDriveWheelPositions value) {
    bb.putDouble(value.frontLeftMeters);
    bb.putDouble(value.frontRightMeters);
    bb.putDouble(value.rearLeftMeters);
    bb.putDouble(value.rearRightMeters);
  }
}
