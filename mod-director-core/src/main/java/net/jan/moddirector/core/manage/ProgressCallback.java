package net.jan.moddirector.core.manage;

public interface ProgressCallback {
    void setSteps(int steps);
    void reportProgress(long current, long max);
    void message(String message);
    void step();
    void done();
    void title(String newTitle);
    void indeterminate(boolean isIndeterminate);
}
