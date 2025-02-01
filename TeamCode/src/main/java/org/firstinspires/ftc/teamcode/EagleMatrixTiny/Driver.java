package org.firstinspires.ftc.teamcode.EagleMatrixTiny;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Subsystems.GoBildaPinpointDriver;

public class Driver {
	Pose2D currentPosition;
	Pose2D targetPosition;
	double DISTANCE_THRESHOLD;
	private double xPower = 0;
	private double yPower = 0;
	private double headingPower = 0;
	DcMotor frontLeft;
	DcMotor frontRight;
	DcMotor rearLeft;
	DcMotor rearRight;
	GoBildaPinpointDriver odo;
	Telemetry telemetry;

	public Driver(HardwareMap hardwareMap, Telemetry telemetry){
		this.frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
		this.frontRight = hardwareMap.get(DcMotor.class, "frontRight");
		this.rearLeft = hardwareMap.get(DcMotor.class, "rearLeft");
		this.rearRight = hardwareMap.get(DcMotor.class, "rearRight");
		this.odo = hardwareMap.get(GoBildaPinpointDriver.class, "odo");
		this.telemetry = telemetry;


		this.odo.setOffsets(-173.0, -156); //measured in mm
		this.odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
		this.odo.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.REVERSED, GoBildaPinpointDriver.EncoderDirection.FORWARD);
		this.odo.resetPosAndIMU();

		frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
		rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
	}

	public void moveTo(Pose2D targetPosition, double DISTANCE_THRESHOLD){
		this.targetPosition = targetPosition;
		this.DISTANCE_THRESHOLD = DISTANCE_THRESHOLD;
	}

	public boolean update(){
		odo.update();
		currentPosition = odo.getPosition();

		telemetry.addData("CURRENT POSITION X", currentPosition.getX(DistanceUnit.INCH));
		telemetry.addData("CURRENT POSITION Y", currentPosition.getY(DistanceUnit.INCH));
		telemetry.addData("CURRENT POSITION HEADING", currentPosition.getHeading(AngleUnit.DEGREES));

		// Move X (Strafe)
		if (Math.abs(currentPosition.getX(DistanceUnit.INCH) - targetPosition.getX(DistanceUnit.INCH)) <= DISTANCE_THRESHOLD) {
			// Reached X
			telemetry.addLine("Reached required X position");
			xPower = 0;
		} else if (currentPosition.getX(DistanceUnit.INCH) > targetPosition.getX(DistanceUnit.INCH)){
			// Move back
			telemetry.addLine("Need to move back to reach required X position");
			xPower = -1;
		} else if (currentPosition.getX(DistanceUnit.INCH) < targetPosition.getX(DistanceUnit.INCH)) {
			// Move forward
			telemetry.addLine("Need to move forward to reach required X position");
			xPower = 1;
		}

		// Move Y (Back and Forth)
		if (Math.abs(currentPosition.getY(DistanceUnit.INCH) - targetPosition.getY(DistanceUnit.INCH)) <= DISTANCE_THRESHOLD) {
			// Reached Y
			yPower = 0;
		} else if (currentPosition.getY(DistanceUnit.INCH) > targetPosition.getY(DistanceUnit.INCH)){
			// Move back
			yPower = -1;
		} else if (currentPosition.getY(DistanceUnit.INCH) < targetPosition.getY(DistanceUnit.INCH)) {
			// Move forward
			yPower = 1;
		}

		double y = yPower; // Remember, Y stick value is reversed
		double x = xPower; // Counteract imperfect strafing
		double rx = headingPower;

		// Denominator is the largest motor power (absolute value) or 1
		// This ensures all the powers maintain the same ratio,
		// but only if at least one is out of the range [-1, 1]
		double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
		double frontLeftPower = (y + x + rx) / denominator;
		double backLeftPower = (y - x + rx) / denominator;
		double frontRightPower = (y - x - rx) / denominator;
		double backRightPower = (y + x - rx) / denominator;

		frontLeft.setPower(frontLeftPower);
		rearLeft.setPower(backLeftPower);
		frontRight.setPower(frontRightPower);
		rearRight.setPower(backRightPower);

		// Reached position or not
		return xPower == 0 && yPower == 0;
	}
}
