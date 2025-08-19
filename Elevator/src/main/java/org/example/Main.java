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

//DATA
class CallLiftData {
    Integer floorCalledFrom;
    Direction floorCalledDirection;
    CallLiftData(Integer floorCalledFrom, Direction floorCalledDirection) {
        this.floorCalledFrom = floorCalledFrom;
        this.floorCalledDirection = floorCalledDirection;
    }
}

class NextLiftMove {
    Integer floor;
    Direction direction;
    NextLiftMove(Integer floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
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
    NextLiftMove findNextMove(ElevatorService elevatorService);
}

class OneDirectionElevatorMovementStrategy implements ElevatorMovementStrategy {

    @Override
    public NextLiftMove findNextMove(ElevatorService elevatorService) {

        int currentFloor = elevatorService.getCurrentFloor();
        Direction currentDirection = elevatorService.getDirection();

        if(currentDirection == Direction.DOWN) {
            currentFloor--;
        } else {
            currentFloor++;
        }

        if(currentFloor == elevatorService.getMinFloor()) {
            currentDirection = Direction.UP;
        } else if(currentFloor == elevatorService.getMaxFloor()) {
            currentDirection = Direction.DOWN;
        }

        return new NextLiftMove(currentFloor, currentDirection);
    }
}

class NearestRequestStrategy implements ElevatorMovementStrategy {
    @Override
    public NextLiftMove findNextMove(ElevatorService elevator) {
        List<Integer> requests = new ArrayList<>(elevator.getGoToDataList());
        elevator.getCallLiftDataList().forEach(c -> requests.add(c.floorCalledFrom));

        if (requests.isEmpty()) return null;

        // pick the nearest request floor
        int curr = elevator.getCurrentFloor();
        int next = requests.stream()
                .min(Comparator.comparingInt(f -> Math.abs(f - curr)))
                .get();

        Direction dir = next > curr ? Direction.UP : Direction.DOWN;
        return new NextLiftMove(next, dir);
    }
}

//ELEVATOR SERVICE
class ElevatorService {
    private Integer currentFloor;
    private Direction direction;
    private final Integer minFloor;
    private final Integer maxFloor;
    private final List<CallLiftData> callLiftDataList;
    private final List<Integer> goToDataList;
    private Gate gate;
    private final ElevatorMovementStrategy elevatorMovementStrategy;
    private ElevatorState elevatorState;

    ElevatorService(Integer maxFloor, Integer minFloor, ElevatorMovementStrategy elevatorMovementStrategy) {
        this.currentFloor = 0;
        this.direction = Direction.UP;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.callLiftDataList = new ArrayList<>();
        this.goToDataList = new ArrayList<>();
        this.gate = Gate.CLOSE;
        this.elevatorMovementStrategy = elevatorMovementStrategy;
        this.elevatorState = ElevatorState.IDLE;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(Integer currentFloor) {
        this.currentFloor = currentFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getMaxFloor() {
        return maxFloor;
    }

    public Integer getMinFloor() {
        return minFloor;
    }

    public List<CallLiftData> getCallLiftDataList() {
        return callLiftDataList;
    }

    public List<Integer> getGoToDataList() {
        return goToDataList;
    }

    private void startMoving() {
        if (this.elevatorState == ElevatorState.MOVING) {
            // If already moving don't move again
            return;
        }
        while(!getCallLiftDataList().isEmpty() || !getGoToDataList().isEmpty()) {
            if(this.elevatorState == ElevatorState.DOOR_OPEN) {
                continue;
            }
            this.elevatorState = ElevatorState.MOVING;
            // get next move
            NextLiftMove nextLiftMove = elevatorMovementStrategy.findNextMove(this);

            setDirection(nextLiftMove.direction);
            setCurrentFloor(nextLiftMove.floor);

            // check if this is right to open for user in or out
            checkIfGateCanOpen();
        }
        this.elevatorState = ElevatorState.IDLE;
    }

    public void callLift(Integer floorCalledFrom, Direction fllorCalledDirection) {
        callLiftDataList.add(new CallLiftData(floorCalledFrom, fllorCalledDirection));
        startMoving();
    }

    public void goToFloor(Integer floorToGoTo) {
        goToDataList.add(floorToGoTo);
    }

    private Optional<CallLiftData> isCallLiftIsInSameFloorAndDirection(Integer currentFloor, Direction currentDirection) {
        return callLiftDataList
                .stream()
                .filter(callLift -> Objects.equals(callLift.floorCalledFrom, currentFloor) && callLift.floorCalledDirection == currentDirection)
                .findFirst();
    }

    private Optional<Integer> isGoToIsInSameFloor(Integer currentFloor) {
        return goToDataList
                .stream()
                .filter(floor -> Objects.equals(floor, currentFloor))
                .findFirst();
    }

    private void checkIfGateCanOpen() {
        Optional<CallLiftData> callLiftData = isCallLiftIsInSameFloorAndDirection(currentFloor, direction);
        if(callLiftData.isPresent()) {
            // remove from call list
            // open & close gate
            this.callLiftDataList.remove(callLiftData.get());
            this.openGate();
            this.goToFloor(5);

        }

        Optional<Integer> goToData = isGoToIsInSameFloor(currentFloor);

        if(goToData.isPresent()) {
            // remove from go to list
            // open & close gate
            this.goToDataList.remove(goToData.get());
            this.openGate();
        }
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

}

//ELEVATOR CONTROLLER
class ElevatorController {
    private List<ElevatorService> elevators;

    ElevatorController(List<ElevatorService> elevators) {
        this.elevators = elevators;
    }

    public void requestElevator(int floor, Direction dir) {
        // choose best elevator (nearest + same direction OR idle)
        // forward request
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}