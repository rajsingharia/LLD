package org.example;

interface State {
    void setNextState(State nextState);
    State getNextState();
    void handle();
}

interface StateChangeStrategy {
    void changeColor(TrafficSignal trafficSignal) throws InterruptedException;
}

class EqualTimeStateChangeStrategy implements StateChangeStrategy {
    private final long WAIT_TIME = 1000L;

    @Override
    public void changeColor(TrafficSignal trafficSignal) throws InterruptedException {
        State currentState = trafficSignal.getState();
        while(true) {
            currentState.handle();
            Thread.sleep(this.WAIT_TIME);
            currentState = currentState.getNextState();

            if(currentState == null) {
                // restart cycle from beginning
                currentState = trafficSignal.getState();
            }
        }
    }
}

class DiffTimeStateChangeStrategy implements StateChangeStrategy {
    private final long RED_WAIT_TIME = 2000;
    private final long ORANGE_WAIT_TIME = 500L;
    private final long GREEN_WAIT_TIME = 1000L;

    @Override
    public void changeColor(TrafficSignal trafficSignal) throws InterruptedException {
        State currentState = trafficSignal.getState();

        while(true) {
            currentState.handle();
            handleWait(currentState);
            currentState = currentState.getNextState();

            if(currentState == null) {
                // restart cycle from beginning
                currentState = trafficSignal.getState();
            }

        }
    }

    private void handleWait(State currentState) throws InterruptedException {
        if(currentState instanceof RedLight) {
            Thread.sleep(RED_WAIT_TIME);
        } else if(currentState instanceof OrangeLight) {
            Thread.sleep(ORANGE_WAIT_TIME);
        } else if(currentState instanceof GreenLight) {
            Thread.sleep(GREEN_WAIT_TIME);
        } else {
            throw new RuntimeException("invalid state");
        }
    }

}

class RedLight implements State {
    private State nextState = null;
    @Override
    public void setNextState(State nextState) {
        this.nextState = nextState;
    }

    @Override
    public State getNextState() {
        return nextState;
    }
    @Override
    public void handle() {
        System.out.println(this.getClass().getSimpleName());
    }
}

class OrangeLight implements State {
    private State nextState = null;
    @Override
    public void setNextState(State nextState) {
        this.nextState = nextState;
    }
    @Override
    public State getNextState() {
        return nextState;
    }

    @Override
    public void handle() {
        System.out.println(this.getClass().getSimpleName());
    }
}

class GreenLight implements State {
    private State nextState = null;
    @Override
    public void setNextState(State nextState) {
        this.nextState = nextState;
    }
    @Override
    public State getNextState() {
        return nextState;
    }
    @Override
    public void handle() {
        System.out.println(this.getClass().getSimpleName());
    }
}

class TrafficSignal {
    private final State state;
    private final StateChangeStrategy stateChangeStrategy;

    TrafficSignal(State state) {
        this.state = state;
        this.stateChangeStrategy = new DiffTimeStateChangeStrategy();
    }

    public State getState() {
        return state;
    }

    public void start() {
        try {
            this.stateChangeStrategy.changeColor(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

public class Main {
    public static void main(String[] args) {
        State state = new RedLight();
        state.setNextState(new OrangeLight());
        state.getNextState().setNextState(new GreenLight());
        TrafficSignal trafficSignal = new TrafficSignal(state);
        trafficSignal.start();
    }
}