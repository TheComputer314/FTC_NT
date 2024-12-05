// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.trobotix;

import com.outoftheboxrobotics.photoncore.Photon;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import edu.wpi.first.networktables.NetworkTableInstance;
import ftc.lib.wpilib.commands.CommandScheduler;
import ftc.lib.wpilib.commands.button.Trigger;

/**
 * A base op mode that contains shared code. As all code defined in an op mode is in the init stage,
 * and the active running is handled by the {@link CommandScheduler}, op mode classes only need to
 * override {@link BaseOpMode#startup()}.
 */
@Photon
public abstract class BaseOpMode extends LinearOpMode {
  private final Trigger enableTrigger = new Trigger(this::opModeIsActive);

  @Override
  public final void runOpMode() {
    var statePublisher = NetworkTableInstance.getDefault().getStringTopic("State").publish();
    statePublisher.set("Initializing");
    NetworkTableInstance.getDefault().flushLocal();
    startup();
    statePublisher.set("Waiting_For_Start");
    NetworkTableInstance.getDefault().flushLocal();
    waitForStart();
    EndableThread.startThreads();
    statePublisher.set("Running");
    while (opModeIsActive()) {
      CommandScheduler.getInstance().run();
      NetworkTableInstance.getDefault().flushLocal();
    }
    EndableThread.endThreads();
    statePublisher.close();
  }

  protected abstract void startup();

  protected final Trigger enableTrigger() {
    return enableTrigger;
  }

  private final CommandXboxController primaryController = new CommandXboxController(this, true);
  private final CommandXboxController secondaryController = new CommandXboxController(this, false);

  protected final CommandXboxController primaryController() {
    return primaryController;
  }

  protected final CommandXboxController secondaryController() {
    return secondaryController;
  }
}
