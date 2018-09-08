package eu.quelltext.mundraub.api.progress;

public class Fraction extends Progressable {
    private final double maxFraction;
    private final Progressable progressable;
    private double fraction = 0;

    Fraction(Progressable progressable, double fraction) {
        super();
        this.maxFraction = fraction;
        this.progressable = progressable;
    }

    public void setProgress(double fraction) {
        fraction = inBounds(fraction);
        double oldFraction = this.fraction;
        this.fraction = fraction;
        progressable.setProgress(progressable.getProgress() + (fraction - oldFraction) * maxFraction);
    }

    @Override
    public double getProgress() {
        return this.fraction;
    }
}
