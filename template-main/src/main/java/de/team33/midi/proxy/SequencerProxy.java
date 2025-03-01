package de.team33.midi.proxy;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@SuppressWarnings({"unused", "ClassWithTooManyMethods"})
public class SequencerProxy {

    private final Sequencer backing;
    private transient volatile SequenceProxy sequence;

    public SequencerProxy(final Sequencer backing) {
        this.backing = backing;
    }

    public final void setSequence(final SequenceProxy sequence) throws InvalidMidiDataException {
        this.sequence = sequence.setSequencer(backing);
    }

    public final void setSequence(final InputStream stream) throws IOException, InvalidMidiDataException {
        backing.setSequence(stream);
    }

    public final SequenceProxy getSequence() {
        return sequence;
    }

    public final void start() {
        backing.start();
    }

    public final void stop() {
        backing.stop();
    }

    public final boolean isRunning() {
        return backing.isRunning();
    }

    public final void startRecording() {
        backing.startRecording();
    }

    public final void stopRecording() {
        backing.stopRecording();
    }

    public final boolean isRecording() {
        return backing.isRecording();
    }

    public final void recordEnable(final TrackProxy track, final int channel) {
        track.recordEnable(backing, channel);
    }

    public final void recordDisable(final TrackProxy track) {
        track.recordDisable(backing);
    }

    public final float getTempoInBPM() {
        return backing.getTempoInBPM();
    }

    public final void setTempoInBPM(final float bpm) {
        backing.setTempoInBPM(bpm);
    }

    public final float getTempoInMPQ() {
        return backing.getTempoInMPQ();
    }

    public final void setTempoInMPQ(final float mpq) {
        backing.setTempoInMPQ(mpq);
    }

    public final void setTempoFactor(final float factor) {
        backing.setTempoFactor(factor);
    }

    public final float getTempoFactor() {
        return backing.getTempoFactor();
    }

    public final long getTickLength() {
        return backing.getTickLength();
    }

    public final long getTickPosition() {
        return backing.getTickPosition();
    }

    public final void setTickPosition(final long tick) {
        backing.setTickPosition(tick);
    }

    public final long getMicrosecondLength() {
        return backing.getMicrosecondLength();
    }

    public final long getMicrosecondPosition() {
        return backing.getMicrosecondPosition();
    }

    public final void setMicrosecondPosition(final long microseconds) {
        backing.setMicrosecondPosition(microseconds);
    }

    public final void setMasterSyncMode(final Sequencer.SyncMode sync) {
        backing.setMasterSyncMode(sync);
    }

    public final Sequencer.SyncMode getMasterSyncMode() {
        return backing.getMasterSyncMode();
    }

    public final Sequencer.SyncMode[] getMasterSyncModes() {
        return backing.getMasterSyncModes();
    }

    public final void setSlaveSyncMode(final Sequencer.SyncMode sync) {
        backing.setSlaveSyncMode(sync);
    }

    public final Sequencer.SyncMode getSlaveSyncMode() {
        return backing.getSlaveSyncMode();
    }

    public final Sequencer.SyncMode[] getSlaveSyncModes() {
        return backing.getSlaveSyncModes();
    }

    public final void setTrackMute(final int track, final boolean mute) {
        backing.setTrackMute(track, mute);
    }

    public final boolean getTrackMute(final int track) {
        return backing.getTrackMute(track);
    }

    public final void setTrackSolo(final int track, final boolean solo) {
        backing.setTrackSolo(track, solo);
    }

    public final boolean getTrackSolo(final int track) {
        return backing.getTrackSolo(track);
    }

    public final boolean addMetaEventListener(final MetaEventListener listener) {
        return backing.addMetaEventListener(listener);
    }

    public final void removeMetaEventListener(final MetaEventListener listener) {
        backing.removeMetaEventListener(listener);
    }

    public final int[] addControllerEventListener(final ControllerEventListener listener, final int[] controllers) {
        return backing.addControllerEventListener(listener, controllers);
    }

    public final int[] removeControllerEventListener(final ControllerEventListener listener, final int[] controllers) {
        return backing.removeControllerEventListener(listener, controllers);
    }

    public final void setLoopStartPoint(final long tick) {
        backing.setLoopStartPoint(tick);
    }

    public final long getLoopStartPoint() {
        return backing.getLoopStartPoint();
    }

    public final void setLoopEndPoint(final long tick) {
        backing.setLoopEndPoint(tick);
    }

    public final long getLoopEndPoint() {
        return backing.getLoopEndPoint();
    }

    public final void setLoopCount(final int count) {
        backing.setLoopCount(count);
    }

    public final int getLoopCount() {
        return backing.getLoopCount();
    }

    public final MidiDevice.Info getDeviceInfo() {
        return backing.getDeviceInfo();
    }

    public final void open() throws MidiUnavailableException {
        backing.open();
    }

    public final void close() {
        backing.close();
    }

    public final boolean isOpen() {
        return backing.isOpen();
    }

    public final int getMaxReceivers() {
        return backing.getMaxReceivers();
    }

    public final int getMaxTransmitters() {
        return backing.getMaxTransmitters();
    }

    public final Receiver getReceiver() throws MidiUnavailableException {
        return backing.getReceiver();
    }

    public final List<Receiver> getReceivers() {
        return backing.getReceivers();
    }

    public final Transmitter getTransmitter() throws MidiUnavailableException {
        return backing.getTransmitter();
    }

    public final List<Transmitter> getTransmitters() {
        return backing.getTransmitters();
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || (obj instanceof final SequencerProxy other && backing.equals(other.backing));
    }

    @Override
    public final int hashCode() {
        return backing.hashCode();
    }
}
