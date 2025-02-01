package org.firstinspires.ftc.teamcode.EagleMatrixTiny;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class Driver {
	Pose2D startPosition;
	Pose2D targetPosition;
	double DISTANCE_THRESHOLD;
	private double xPower = 0;
	private double yPower = 0;
	private double headingPower = 0;
	DcMotor frontLeft;
	DcMotor frontRight;
	DcMotor rearLeft;
	DcMotor rearRight;

	public Driver(HardwareMap hardwareMap){
		DcMotor frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
		DcMotor frontRight = hardwareMap.get(DcMotor.class, "frontRight");
		DcMotor rearLeft = hardwareMap.get(DcMotor.class, "rearLeft");
		DcMotor rearRight = hardwareMap.get(DcMotor.class, "rearRight");
	}

	public void moveTo(Pose2D startPosition, Pose2D targetPosition, double DISTANCE_THRESHOLD){
		this.startPosition = startPosition;
		this.targetPosition = targetPosition;
		this.DISTANCE_THRESHOLD = DISTANCE_THRESHOLD;
	}

	public void update(){
		// Move X (Strafe)
		if (Math.abs(startPosition.getX(DistanceUnit.INCH) - targetPosition.getX(DistanceUnit.INCH)) <= DISTANCE_THRESHOLD) {
			// Reached X
			xPower = 0;
		} else if (startPosition.getX(DistanceUnit.INCH) > targetPosition.getX(DistanceUnit.INCH)){
			// Move back
			xPower = 1;
		} else if (startPosition.getX(DistanceUnit.INCH) < targetPosition.getX(DistanceUnit.INCH)) {
			// Move forward
			xPower = -1;
		}

		// Move Y (Back and Forth)
		if (Math.abs(startPosition.getY(DistanceUnit.INCH) - targetPosition.getY(DistanceUnit.INCH)) <= DISTANCE_THRESHOLD) {
			// Reached Y
			yPower = 0;
		} else if (startPosition.getY(DistanceUnit.INCH) > targetPosition.getY(DistanceUnit.INCH)){
			// Move back
			yPower = -1;
		} else if (startPosition.getY(DistanceUnit.INCH) < targetPosition.getY(DistanceUnit.INCH)) {
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
	}
}
