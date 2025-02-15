package org.firstinspires.ftc.teamcode.EagleMatrix.NEW;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.controller.PIDController;
import com.arcrobotics.ftclib.controller.PIDFController;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Utilities.Robot;
import org.firstinspires.ftc.teamcode.EagleMatrix.NEW.botPIDConstants.PIDF_Constants;
import org.firstinspires.ftc.teamcode.EagleMatrix.NEW.botPIDConstants.Ticks2Deg;

@Config
public class botPID {
    //HARDWARE
    public final Robot robot;
    public final botPIDConstants constants;

    //PID CONTROLLERS
    public final PIDFController armController;
    public final PIDController liftController;
    public final PIDController driveXController;
    public final PIDController driveYController;
    public final PIDController driveHeadingController;

    // TARGETS
    public static double Arm_target;
    public static double Lift_target;
    public static double X_target;
    public static double Y_target;
    public static double Heading_target;

    public botPID(Robot robot, botPIDConstants constants) {
        this.robot = robot;
        this.constants = new botPIDConstants();
        armController = new PIDFController(PIDF_Constants.Arm_p, PIDF_Constants.Arm_i, PIDF_Constants.Arm_d, PIDF_Constants.Arm_f);
        liftController = new PIDController(PIDF_Constants.Lift_p, PIDF_Constants.Lift_i, PIDF_Constants.Lift_d);
        driveXController = new PIDController(PIDF_Constants.Xp, PIDF_Constants.Xi, PIDF_Constants.Xd);
        driveYController = new PIDController(PIDF_Constants.Yp, PIDF_Constants.Yi, PIDF_Constants.Yd);
        driveHeadingController = new PIDController(PIDF_Constants.Heading_p, PIDF_Constants.Heading_i, PIDF_Constants.Heading_d);
    }

    public void setArmTarget(double target){
        Arm_target = target;
    }
    public void setLiftTarget(double target){
        Lift_target = target;
    }
    public void setXTarget(double target){
        X_target = target;
    }
    public void setYTarget(double target){
        Y_target = target;
    }
    public void setHeadingTarget(double target){
        Heading_target = target;
    }
    public void setDriveTarget(double x_target, double y_target, double heading_target){
        X_target = x_target;
        Y_target = y_target;
        Heading_target = heading_target;
    }

    public void runArm(){
        armController.setPIDF(PIDF_Constants.Arm_p, PIDF_Constants.Arm_i, PIDF_Constants.Arm_d, PIDF_Constants.Arm_f);

        double armPosition = robot.lift.getShoulderPosition();

        double armPID = armController.calculate(armPosition, Arm_target);

        double Arm_ff = Math.cos(Math.toRadians(Arm_target / Ticks2Deg.ArmTicksInDegree)) * PIDF_Constants.Arm_f;

        double Arm_power = armPID + Arm_ff;

        robot.lift.getShoulder().setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.lift.shoulderMove(Arm_power);
    }
    public void runLift(){
        liftController.setPID(PIDF_Constants.Lift_p, PIDF_Constants.Lift_i, PIDF_Constants.Lift_d);

        double liftPosition = robot.lift.getLiftPosition();

        double liftPID = liftController.calculate(liftPosition, Lift_target);

        double Lift_ff = Math.cos(Math.toRadians(Lift_target / Ticks2Deg.LiftTicksInDegree)) * PIDF_Constants.Lift_f;

        double Lift_power = liftPID + Lift_ff;

        robot.lift.liftMove(Lift_power);
    }
    public void runDrive(){
        driveXController.setPID(PIDF_Constants.Xp, PIDF_Constants.Xi, PIDF_Constants.Xd);
        driveYController.setPID(PIDF_Constants.Yp, PIDF_Constants.Yi, PIDF_Constants.Yd);
        driveHeadingController.setPID(PIDF_Constants.Heading_p, PIDF_Constants.Heading_i, PIDF_Constants.Heading_d);

        double xPosition = robot.odometry.getPosition().getX(DistanceUnit.INCH);
        double yPosition = robot.odometry.getPosition().getY(DistanceUnit.INCH);

        // TODO: Fix normalization issue with heading
        // Fix with a mod equation that ensures the robot never crosses a 0 (from Craig)
        // another idea is set variable so whenever angle crosses 0, add 1 (if positive), and minus 1 (if negative)
        // if all else fails, add 360 to target and initial values

        //TODO: see if solution (ala ChatGPT) works.
        double heading = robot.odometry.getPosition().getHeading(AngleUnit.DEGREES);
        double delta = heading - Heading_target;
        double unnormalizedHeading = (delta % 360 + 360) % 360;

        // Ensuring smooth transitions without abrupt -180 flips
        if (unnormalizedHeading > 180) {
            unnormalizedHeading -= 360; // Prefers a shorter route in the negative direction
        }

        double xPID = driveXController.calculate(xPosition, X_target);
        double yPID = driveYController.calculate(yPosition, Y_target);
        double headingPID = driveHeadingController.calculate(unnormalizedHeading, Heading_target);

        double Xff = Math.cos(Math.toRadians(X_target / Ticks2Deg.DriveTicksInDegree)) * PIDF_Constants.Xf;
        double Yff = Math.cos(Math.toRadians(Y_target / Ticks2Deg.DriveTicksInDegree)) * PIDF_Constants.Yf;
        double Heading_ff = Math.cos(Math.toRadians(Heading_target / Ticks2Deg.DriveTicksInDegree)) * PIDF_Constants.Heading_f;

        double X_power = xPID + Xff;
        double Y_power = yPID + Yff;
        double Heading_power = headingPID + Heading_ff;

        robot.drive.driveMecanumFieldCentric(Y_power, X_power, -Heading_power, heading);
    }
    public double getArmTarget(){
        return Arm_target;
    }
    public double getLiftTarget(){
        return Lift_target;
    }
    public double getXTarget(){
        return X_target;
    }
    public double getYTarget(){
        return Y_target;
    }
    public double getHeadingTarget(){
        return Heading_target;
    }
    public double getArmPosition(){
        return robot.lift.getShoulderPosition();
    }
    public double getLiftPosition(){
        return robot.lift.getLiftPosition();
    }
    public Pose2D getOdoPosition(){
        return robot.odometry.getPosition();
    }
}