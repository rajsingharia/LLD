package org.example;

// User -> press (up/down button) -> list comes -> press floor -> lift goes to floor (diff alg)
// ElevatorService
// - currentFloor
// - currentDirection
// - callLift(floor, direction) -> stores (floor called from, direction)
// - reached - gateOpen
// - goToFloor(floor) -> stores (floor to goto)
// - strategy (to find the best path - change currentFloor and direction)
// - emergencyButton (diff strategy)

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

//DATA
class CallLiftData {
    Integer floorCalledFrom;
    Direction floorCalledDirection;
    CallLiftData(Integer floorCalledFrom, Direction floorCalledDirection) {
        this.floorCalledFrom = floorCalledFrom;
        this.floorCalledDirection = floorCalledDirection;
    }
}

//ENUM
enum Direction {
    UP, DOWN
}

enum Gate {
    OPEN, CLOSE
}

enum ElevatorState {
    IDLE, MOVING, DOOR_OPEN
}

//STRATEGY
interface ElevatorMovementStrategy {

    CallLiftData findNextMove(List<CallLiftData> callLiftDataList, Integer currentFloor, Direction direction);
}

class NearestRequestStrategy implements ElevatorMovementStrategy {

    @Override
    public CallLiftData findNextMove(List<CallLiftData> callLiftDataList, Integer currentFloor, Direction direction) {
        int minDifferent = Integer.MAX_VALUE;
        CallLiftData nextLiftData = null;
        for (CallLiftData callLiftData : callLiftDataList) {
            int currDifference = Math.abs(currentFloor - callLiftData.floorCalledFrom);
            if (currDifference < minDifferent) {
                minDifferent = (currDifference);
                nextLiftData = (callLiftData);
            }
        }
        return nextLiftData;
    }
}

//ELEVATOR SERVICE
class ElevatorService implements Runnable {
    private final Integer elevatorId;
    private Integer currentFloor;
    private Direction direction;
    private final List<CallLiftData> callLiftDataList;
    private Gate gate;
    private final ElevatorMovementStrategy elevatorMovementStrategy;
    private ElevatorState elevatorState;
    private boolean isRunning;

    ElevatorService(int id, ElevatorMovementStrategy elevatorMovementStrategy) {
        this.elevatorId = id;
        this.isRunning = true;
        this.currentFloor = 0;
        this.direction = Direction.UP;
        this.callLiftDataList = new ArrayList<>();
        this.gate = Gate.CLOSE;
        this.elevatorMovementStrategy = elevatorMovementStrategy;
        this.elevatorState = ElevatorState.IDLE;
    }

    public Integer getElevatorId() {
        return elevatorId;
    }

    private void goToFloor(Integer targetFloor) {
        this.elevatorState = ElevatorState.MOVING;
        while(!Objects.equals(this.currentFloor, targetFloor)) {
            if(this.currentFloor < targetFloor) {
                this.direction = Direction.UP;
                this.currentFloor++;
            } else {
                this.direction = Direction.DOWN;
                this.currentFloor--;
            }
        }
        this.elevatorState = ElevatorState.IDLE;
    }

    public void requestElevator(Integer floorCalledFrom, Direction fllorCalledDirection) {
        // Validate request
        callLiftDataList.add(new CallLiftData(floorCalledFrom, fllorCalledDirection));
    }

    private void openGate() {
        this.gate = Gate.OPEN;
        this.elevatorState = ElevatorState.DOOR_OPEN;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // user is out
        this.gate = Gate.CLOSE;
    }

    private void emergency() {
        this.isRunning = false;
    }

    @Override
    public void run() {
        while (this.isRunning) {

            if(this.callLiftDataList.isEmpty()) {
                // Lift is idle
                continue;
            }

            CallLiftData nextMove = elevatorMovementStrategy.findNextMove(this.callLiftDataList, this.currentFloor, this.direction);
            this.callLiftDataList.remove(nextMove);
            goToFloor(nextMove.floorCalledFrom);
            openGate();
        }
    }
}


//ELEVATOR CONTROLLER
class ElevatorController {
    private final ExecutorService executorService;
    private final List<ElevatorService> elevatorServiceList;

    ElevatorController(List<ElevatorService> elevatorServiceList) {
        this.elevatorServiceList = elevatorServiceList;
        executorService = Executors.newFixedThreadPool(elevatorServiceList.size());
        elevatorServiceList.forEach(elevatorService -> executorService.submit(elevatorService));
        executorService.shutdown();
    }

    public void requestElevator(int elevatorId, int floor, Direction dir) {
        Optional<ElevatorService> elevatorServiceFromElevatorId = elevatorServiceList
                .stream()
                .filter(elevatorService -> elevatorService.getElevatorId() == elevatorId)
                .findFirst();

        elevatorServiceFromElevatorId.ifPresent(elevatorService -> elevatorService.requestElevator(floor, dir));

    }
}

public class Main {
    public static void main(String[] args) {

        ElevatorService elevatorService = new ElevatorService(1, new NearestRequestStrategy());
        ElevatorService elevatorService1 = new ElevatorService(2, new NearestRequestStrategy());

        List<ElevatorService> elevatorServiceList = new ArrayList<>();
        elevatorServiceList.add(elevatorService);
        elevatorServiceList.add(elevatorService1);

        ElevatorController elevatorController = new ElevatorController(elevatorServiceList);


        elevatorController.requestElevator(1, 5, Direction.DOWN);
        elevatorController.requestElevator(1, 2, Direction.UP);

    }
}