package com.tvos.launcher;

/** Connection feedback follows observations, never the return value of a request alone. */
final class ConnectionAttempt {
    enum State { IDLE, REQUESTED, CONNECTED, UNCONFIRMED }
    private String target;
    private long deadline;
    private State state = State.IDLE;
    void begin(String ssid, long elapsed) { target = ssid; deadline = elapsed + 45_000; state = State.REQUESTED; }
    void authenticationFailed() { if (state == State.REQUESTED) state = State.UNCONFIRMED; }
    State observe(String ssid, boolean hasAddress, boolean completed, long elapsed) {
        if (target != null && target.equals(ssid) && !target.isEmpty() && hasAddress && completed) state = State.CONNECTED;
        else if (state == State.REQUESTED && elapsed >= deadline) state = State.UNCONFIRMED;
        return state;
    }
    State state() { return state; }
    String target() { return target; }
}
