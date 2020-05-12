function [residuals] = EuclideanDistanceResidual_Tango_GoogleFLP_test(sensorMeasurements, X)

% unpack sensor measurements
TangoPolarVIODistance = sensorMeasurements.TangoPolarVIODistance;
TangoPolarVIOAngle = sensorMeasurements.TangoPolarVIOAngle;
TangoGoogleFLPIndex = sensorMeasurements.TangoGoogleFLPIndex;
TangoGoogleFLPLocation = sensorMeasurements.TangoGoogleFLPLocation;
TangoGoogleFLPAccuracy = sensorMeasurements.TangoGoogleFLPAccuracy;


% Tango VIO drift correction model
[startLocation, rotation, scale, bias] = unpackDriftCorrectionModelParameters(X);
TangoVIOLocation = DriftCorrectedTangoVIOAbsoluteAngleModel(startLocation, rotation, scale, bias, TangoPolarVIODistance, TangoPolarVIOAngle);


% (1) residuals for Google FLP location
TangoEstimatedLocation = TangoVIOLocation(:,TangoGoogleFLPIndex);
TangoLocationNormError = vecnorm(TangoEstimatedLocation - TangoGoogleFLPLocation);
residualGoogleFLP = max((TangoLocationNormError - TangoGoogleFLPAccuracy), 0);


% (2) residuals for scale/bias changes in Tango VIO drift correction model
scaleRegularization = (scale - 1);
biasRegularization = (bias - 0);


% (3) final residuals for nonlinear optimization
%residuals = [residualGoogleFLP, scaleRegularization, biasDifference].';
residuals = [10*residualGoogleFLP, scaleRegularization, biasRegularization].';


end
