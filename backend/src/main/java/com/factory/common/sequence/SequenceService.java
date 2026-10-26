package com.factory.common.sequence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SequenceService {

    private final SystemSequenceRepository sequenceRepository;

    public SequenceService(SystemSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized String getNextPoNumber() {
        return generateFormattedSequence("PO_NUMBER", "PO-%06d");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized String getNextDcNumber() {
        return generateFormattedSequence("DC_NUMBER", "DC-%06d");
    }

    private String generateFormattedSequence(String sequenceName, String formatPattern) {
        SystemSequence seq = sequenceRepository.findByNameForUpdate(sequenceName)
                .orElseGet(() -> sequenceRepository.save(new SystemSequence(sequenceName, 0L)));

        long nextVal = seq.getCurrentValue() + 1;
        seq.setCurrentValue(nextVal);
        sequenceRepository.save(seq);

        return String.format(formatPattern, nextVal);
    }
}
