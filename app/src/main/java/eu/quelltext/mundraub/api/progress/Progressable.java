package eu.quelltext.mundraub.api.progress;

import eu.quelltext.mundraub.error.ErrorAware;

public abstract class Progressable extends ErrorAware {
    public abstract void setProgress(double progress);
    public abstract double getProgress();

    protected double inBounds(double progress) {
        if (progress > 1) {
            return 1;
        } else if (progress < 0) {
            return 0;
        }
        return progress;
    }

    public Progressable getFraction(double fraction) {
        return new Fraction(this, fraction);
    }

}
