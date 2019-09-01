package eu.quelltext.mundraub;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.common.Settings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class SynchronizedSetTest {

    private final String name = "testing";
    private final List<String> emptySet = new ArrayList<>();

    @Test
    public void createEmptySyncSet(){
        Settings.SynchronizedStringSet set = new Settings.SynchronizedStringSet(name, emptySet);
        assertEquals(set.toString(), "");
    }

    @Test
    public void testContain(){
        Settings.SynchronizedStringSet set = new Settings.SynchronizedStringSet(name, emptySet);
        set.add("Zero");
        assertTrue(set.contains("Zero"));
    }

    @Test
    public void addToSyncTest(){
        Settings.SynchronizedStringSet set = new Settings.SynchronizedStringSet(name, emptySet);
        set.add("Zero");
        set.add("One");
        set.add("Two");
        assertEquals(set.toString(), "Zero,One,Two");
    }

    @Test
    public void addAndRemoveToSyncTest(){
        Settings.SynchronizedStringSet set = new Settings.SynchronizedStringSet(name, emptySet);
        set.add("Zero");
        set.add("One");
        set.add("Two");

        set.remove("One");
        assertEquals(set.toString(), "Zero,Two");
    }

    @Test
    public void addAndSetUnchecked(){
        Settings.SynchronizedStringSet set = new Settings.SynchronizedStringSet(name, emptySet);
        set.add("Zero");
        set.add("One");
        set.add("Two");

        set.setChecked("Zero", false);
        set.setChecked("Two", false);
        assertEquals(set.toString(), "One");
    }
}
