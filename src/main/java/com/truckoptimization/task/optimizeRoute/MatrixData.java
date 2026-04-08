package com.truckoptimization.task.optimizeRoute;

public class MatrixData {

    private final long[][] distanceMatrix;
    private final long[][] travelTimeMatrix;

    public MatrixData(long[][] distanceMatrix, long[][] travelTimeMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.travelTimeMatrix = travelTimeMatrix;
    }

    public long[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    public long[][] getTravelTimeMatrix() {
        return travelTimeMatrix;
    }
}