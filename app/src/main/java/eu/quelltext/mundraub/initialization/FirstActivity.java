package eu.quelltext.mundraub.initialization;

import eu.quelltext.mundraub.error.MundraubBaseActivity;

public class FirstActivity extends MundraubBaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        // TODO remove and ask when needed.
        Permissions.of(this).checkAllPermissions();
    }

}
