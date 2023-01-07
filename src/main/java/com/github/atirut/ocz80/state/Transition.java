package com.github.atirut.ocz80.state;

import java.util.Objects;
import li.cil.oc.api.machine.ExecutionResult;

public class Transition {
    public State nexState;
    public ExecutionResult executionResult;

    public Transition(State nextState, ExecutionResult executionResult) {
        Objects.requireNonNull(executionResult);
        if (executionResult instanceof ExecutionResult.Sleep
                || executionResult instanceof ExecutionResult.SynchronizedCall) {
            Objects.requireNonNull(nextState);
        } else if (executionResult instanceof ExecutionResult.Shutdown
                || executionResult instanceof ExecutionResult.Error) {
            if (nextState != null) {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }

        this.nexState = nextState;
        this.executionResult = executionResult;
    }
}
