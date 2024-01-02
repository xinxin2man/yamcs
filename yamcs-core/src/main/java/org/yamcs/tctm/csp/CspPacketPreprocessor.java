package org.yamcs.tctm.csp;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32C;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractPacketPreprocessor;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.utils.TimeEncoding;

/**
 * Link preprocessor for CSP 1.x packets (CubeSat Protocol)
 */
public class CspPacketPreprocessor extends AbstractPacketPreprocessor {

    // Unless -1, filter incoming packets on their CSP destination ID
    private int cspId = -1;
    private AtomicInteger seqCount = new AtomicInteger();

    // Arbitrary max sequence count, only to distinguish archived packets
    private static final int MAX_SEQ_COUNT = 0x0000ffff;

    public CspPacketPreprocessor(String yamcsInstance) {
        this(yamcsInstance, YConfiguration.emptyConfig());
    }

    public CspPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
        cspId = config.getInt("cspId", -1);
    }

    @Override
    public TmPacket process(TmPacket packet) {
        byte[] bytes = packet.getPacket();

        if (bytes.length < 4) { // Expect at least the length of the CSP header
            eventProducer.sendWarning("SHORT_PACKET",
                    "Short packet received, length: " + bytes.length + "; minimum required length is 4 bytes");
            return null; // Drop packet
        }

        if (cspId != -1 && cspId != CspPacket.getDestination(bytes)) {
            return null; // Drop packet, it's not for us
        }

        var checksumIndicator = CspPacket.getCrcFlag(bytes);
        var corrupted = false;
        if (checksumIndicator) {
            int n = packet.length();
            var crc = new CRC32C();
            crc.update(bytes, 4, bytes.length - 4 - 4);
            int computedCheckword = (int) crc.getValue(); // uint32
            int packetCheckword = ByteArrayUtils.decodeInt(bytes, n - 4);
            if (packetCheckword != computedCheckword) {
                var message = "Corrupted packet received, computed checkword: " + computedCheckword
                        + "; packet checkword: " + packetCheckword;
                log.warn(message);
                eventProducer.sendWarning(ETYPE_CORRUPTED_PACKET, message);
                corrupted = true;
            }
        }

        packet.setGenerationTime(getGenerationTime(packet));
        packet.setSequenceCount(getSequenceCount(packet));
        packet.setInvalid(corrupted);

        return packet;
    }

    /**
     * Returns the generation time (= packet time).
     * <p>
     * Because no time information is available in CSP header, returns Yamcs-local time by default.
     */
    protected long getGenerationTime(TmPacket packet) {
        return TimeEncoding.getWallclockTime();
    }

    /**
     * Returns a sequence count for identifying a packet in addition to the generation time.
     * <p>
     * No sequence counter is available in CSP header, so the default implementation uses a local rotating counter.
     */
    protected int getSequenceCount(TmPacket packet) { // For extension
        return seqCount.accumulateAndGet(1, (value, inc) -> {
            var newValue = value + inc;
            if (newValue > MAX_SEQ_COUNT) {
                return 0;
            } else {
                return newValue;
            }
        });
    }
}
