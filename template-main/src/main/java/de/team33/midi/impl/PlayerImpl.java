package de.team33.midi.impl;

import de.team33.messaging.Listener;
import de.team33.messaging.Register;
import de.team33.messaging.sync.Router;
import de.team33.messaging.util.ListenerUtil;
import de.team33.midi.Player;
import de.team33.midi.Sequence;
import de.team33.midi.Timing;
import net.team33.util.ClassUtil;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class PlayerImpl implements Player {
    private static final Preferences PREFS = Preferences.userRoot().node(ClassUtil.getPathString(PlayerImpl.class));
    private final SET_MODES msgSetModes = new SET_MODES();
    private final SET_POSITION msgSetPosition = new SET_POSITION();
    private final SET_TEMPO msgSetTempo = new SET_TEMPO();
    private final SET_STATE msgSetState = new SET_STATE();
    private final Router<Player.Message> router = new Router();
    private final Sequence sequence;
    private final Sequencer sequencer;
    private MidiDevice outputDevice;
    private Player.Mode[] modes;

    public PlayerImpl(Sequence sequence) throws MidiUnavailableException {
        this.router.addInitials(Arrays.asList(this.msgSetModes, this.msgSetPosition, this.msgSetState, this.msgSetTempo));
        this.sequencer = MidiSystem.getSequencer(false);
        this.sequence = sequence;
        this.sequence.getRegister(Sequence.SetParts.class).add(new SONG_CLIENT());
        this.router.getRegister(Player.SetState.class).add(new STARTER());
    }

    private static MidiDevice newOutputDevice() throws MidiUnavailableException {
        String preferedOutputDeviceName = PREFS.get("preferedOutputDeviceName", "");
        Preferences prefs = PREFS.node("DeviceInfo");
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        MidiDevice.Info preferedOutputDeviceInfo = null;

        for (int index = 0; index < infos.length; ++index) {
            MidiDevice.Info info = infos[index];
            Preferences node = prefs.node(String.format("%04d", index));
            node.put("name", info.getName());
            node.put("description", info.getDescription());
            node.put("vendor", info.getVendor());
            node.put("version", info.getVersion());
            if (preferedOutputDeviceName.equals(info.getName())) {
                preferedOutputDeviceInfo = info;
            }
        }

        Object result;
        if (preferedOutputDeviceInfo == null) {
            result = MidiSystem.getSynthesizer();
        } else {
            result = MidiSystem.getMidiDevice(preferedOutputDeviceInfo);
        }

        PREFS.put("preferedOutputDeviceName", ((MidiDevice) result).getDeviceInfo().getName());
        return (MidiDevice) result;
    }

    private void core_close(Set<MESSAGE> messages) {
        if (this.sequencer.isOpen()) {
            this.outputDevice.close();
            this.outputDevice = null;
            this.sequencer.close();
            messages.add(this.msgSetState);
        }

    }

    private void core_clrModes(Set<MESSAGE> messages) {
        this.modes = null;
        messages.add(this.msgSetModes);
    }

    private Player.Mode[] core_Modes() {
        if (this.modes == null) {
            javax.sound.midi.Sequence seq = this.sequencer.getSequence();
            if (seq != null) {
                int length = seq.getTracks().length;
                int nNormal = 0;
                int iNormal = -1;
                this.modes = new Player.Mode[length];

                for (int i = 0; i < length; ++i) {
                    if (this.sequencer.getTrackMute(i)) {
                        this.modes[i] = Mode.MUTE;
                    } else {
                        this.modes[i] = Mode.NORMAL;
                        ++nNormal;
                        iNormal = i;
                    }
                }

                if (nNormal == 1) {
                    this.modes[iNormal] = Mode.SOLO;
                }
            } else {
                this.modes = new Player.Mode[0];
            }
        }

        return this.modes;
    }

    private void core_open(Set<MESSAGE> messages) {
        if (!this.sequencer.isOpen()) {
            try {
                this.outputDevice = newOutputDevice();
                this.outputDevice.open();
                this.sequencer.getTransmitter().setReceiver(this.outputDevice.getReceiver());
                this.sequence.associate(this.sequencer);
                this.sequencer.open();
                messages.add(this.msgSetState);
                messages.add(this.msgSetTempo);
            } catch (Exception var3) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), var3);
            }
        }

    }

    private void core_setTickPosition(long ticks, Set<MESSAGE> messages) {
        if (!this.sequencer.isOpen()) {
            this.core_open(messages);
        }

        this.sequencer.setTickPosition(ticks);
        messages.add(this.msgSetPosition);
        messages.add(this.msgSetState);
    }

    private void core_setTrackMute(int i, boolean b, Set<MESSAGE> messages) {
        if (this.sequencer.getTrackMute(i) != b) {
            this.sequencer.setTrackMute(i, b);
            this.core_clrModes(messages);
        }

    }

    private void core_start(Set<MESSAGE> messages) {
        if (!this.sequencer.isOpen()) {
            this.core_open(messages);
        }

        if (!this.sequencer.isRunning()) {
            this.sequencer.start();
            messages.add(this.msgSetState);
        }

    }

    private void core_stop(Set<MESSAGE> messages) {
        if (this.sequencer.isRunning()) {
            this.sequencer.stop();
            messages.add(this.msgSetState);
        }

    }

    public Player.Mode getMode(int index) {
        return index >= 0 && index < this.core_Modes().length ? this.core_Modes()[index] : Mode.NORMAL;
    }

    public long getPosition() {
        return this.sequencer.getTickPosition();
    }

    public void setPosition(long ticks) {
        Set<MESSAGE> messages = new HashSet();
        this.core_setTickPosition(ticks, messages);
        ListenerUtil.pass(this.router, messages);
    }

    public <MSX extends Player.Message> Register<MSX> getRegister(Class<MSX> msgClass) {
        return this.router.getRegister(msgClass);
    }

    public Sequence getSequence() {
        return this.sequence;
    }

    public Player.State getState() {
        if (this.sequencer.isRunning()) {
            return State.RUN;
        } else if (this.sequencer.isOpen()) {
            return this.sequencer.getTickPosition() == 0L ? State.STOP : State.PAUSE;
        } else {
            return State.IDLE;
        }
    }

    public void setState(Player.State newState) {
        Set<MESSAGE> messages = new HashSet();
        Player.State currState = this.getState();
        if (newState == null) {
            newState = State.IDLE;
        }

        if (currState != newState) {
            if (currState == State.IDLE) {
                this.core_open(messages);
            }

            if (newState == State.RUN) {
                this.core_start(messages);
            } else if (newState == State.IDLE) {
                this.core_close(messages);
            } else {
                this.core_stop(messages);
                if (newState == State.STOP) {
                    this.core_setTickPosition(0L, messages);
                }
            }

            ListenerUtil.pass(this.router, messages);
        }
    }

    public int getTempo() {
        return Math.round(this.sequencer.getTempoInBPM());
    }

    public void setTempo(int tempo) {
        Set<MESSAGE> messages = new HashSet();
        messages.add(this.msgSetTempo);
        this.sequencer.setTempoInBPM((float) tempo);
        this.sequence.setTempo(tempo);
        ListenerUtil.pass(this.router, messages);
    }

    public Timing getTiming() {
        return this.sequence.getTiming();
    }

    public void setMode(int index, Player.Mode newMode) {
        Player.Mode oldMode = this.getMode(index);
        if (oldMode != newMode) {
            Set<MESSAGE> messages = new HashSet();
            int length;
            int i;
            if (oldMode != Mode.SOLO) {
                if (newMode == Mode.SOLO) {
                    length = this.sequence.getTracks().length;

                    for (i = 0; i < length; ++i) {
                        this.core_setTrackMute(i, i != index, messages);
                    }
                } else {
                    this.core_setTrackMute(index, newMode == Mode.MUTE, messages);
                }
            } else {
                length = this.sequence.getTracks().length;

                for (i = 0; i < length; ++i) {
                    this.core_setTrackMute(i, i == index && newMode == Mode.MUTE, messages);
                }
            }

            ListenerUtil.pass(this.router, messages);
        }

    }

    private class MESSAGE implements Player.Message {
        private MESSAGE() {
        }

        public Player getSender() {
            return PlayerImpl.this;
        }
    }

    private class SET_MODES extends MESSAGE implements Player.SetModes {
        private SET_MODES() {
            super();
        }
    }

    private class SET_POSITION extends MESSAGE implements Player.SetPosition {
        private SET_POSITION() {
            super();
        }
    }

    private class SET_STATE extends MESSAGE implements Player.SetState {
        private SET_STATE() {
            super();
        }
    }

    private class SET_TEMPO extends MESSAGE implements Player.SetTempo {
        private SET_TEMPO() {
            super();
        }
    }

    private class SONG_CLIENT implements Listener<Sequence.SetParts> {
        private SONG_CLIENT() {
        }

        public void pass(Sequence.SetParts message) {
            Set<MESSAGE> messages = new HashSet();
            boolean running = PlayerImpl.this.sequencer.isRunning();
            boolean open = PlayerImpl.this.sequencer.isOpen();
            long currPos = PlayerImpl.this.sequencer.getTickPosition();
            PlayerImpl.this.core_close(messages);
            if (open) {
                PlayerImpl.this.core_setTickPosition(currPos, messages);
                if (running) {
                    PlayerImpl.this.core_start(messages);
                }
            }

            ListenerUtil.pass(PlayerImpl.this.router, messages);
        }
    }

    private class STARTER implements Listener<Player.SetState> {
        private STARTER() {
        }

        public void pass(Player.SetState message) {
            Player.State s = ((Player) message.getSender()).getState();
            if (s == State.RUN) {
                ((Player) message.getSender()).getRegister(Player.SetState.class).remove(this);
                (new Timer()).schedule(PlayerImpl.this.new TASK(), 100L, 50L);
            }

        }
    }

    private class TASK extends TimerTask {
        private int m_Tempo;

        private TASK() {
            this.m_Tempo = 0;
        }

        public void run() {
            Set<MESSAGE> messages = new HashSet();
            messages.add(PlayerImpl.this.msgSetPosition);
            if (!PlayerImpl.this.sequencer.isRunning()) {
                this.cancel();
                PlayerImpl.this.router.getRegister(Player.SetState.class).add(PlayerImpl.this.new STARTER());
                messages.add(PlayerImpl.this.msgSetState);
            }

            int currTempo = PlayerImpl.this.getTempo();
            if (this.m_Tempo != currTempo) {
                this.m_Tempo = currTempo;
                messages.add(PlayerImpl.this.msgSetTempo);
            }

            ListenerUtil.pass(PlayerImpl.this.router, messages);
        }
    }
}
