package eu.quelltext.mundraub.error;

/*  Instances of this class are error aware.
    Before they are created, it is made sure,
 */
public class ErrorAware implements Logger.Loggable {

    public final Logger.Log log;

    public ErrorAware() {
        log = Logger.newFor(this);
    }

    @Override
    public String getTag() {
        return this.getClass().getSimpleName();
    }
}
