/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package jdk.jfr.internal.consumer;

import java.io.IOException;
import java.nio.file.Path;
import java.security.AccessControlContext;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.internal.consumer.Dispatcher;
import jdk.jfr.internal.consumer.FileAccess;
import jdk.jfr.internal.consumer.RecordingInput;

/**
 * Implementation of an event stream that operates against a recording file.
 *
 */
public final class EventFileStream extends AbstractEventStream {
    private final static Comparator<? super RecordedEvent> EVENT_COMPARATOR = JdkJfrConsumer.instance().eventComparator();

    private final RecordingInput input;

    private ChunkParser currentParser;
    private RecordedEvent[] cacheSorted;

    public EventFileStream(AccessControlContext acc, Path path) throws IOException {
        super(acc, null);
        Objects.requireNonNull(path);
        this.input = new RecordingInput(path.toFile(), FileAccess.UNPRIVILIGED);
    }

    @Override
    public void start() {
        start(0);
    }

    @Override
    public void startAsync() {
        startAsync(0);
    }

    @Override
    public void close() {
        setClosed(true);
        dispatcher().runCloseActions();
        try {
            input.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    protected void process() throws IOException {
        Dispatcher disp = dispatcher();
        long start = 0;
        long end = Long.MAX_VALUE;
        if (disp.startTime != null) {
            start = disp.startNanos;
        }
        if (disp.endTime != null) {
            end = disp.endNanos;
        }

        currentParser = new ChunkParser(input, disp.parserConfiguration);
        while (!isClosed()) {
            if (currentParser.getStartNanos() > end) {
                close();
                return;
            }
            disp = dispatcher();
            disp.parserConfiguration.filterStart = start;
            disp.parserConfiguration.filterEnd = end;
            currentParser.updateConfiguration(disp.parserConfiguration, true);
            currentParser.setFlushOperation(getFlushOperation());
            if (disp.parserConfiguration.isOrdered()) {
                processOrdered(disp);
            } else {
                processUnordered(disp);
            }
            if (isClosed() || currentParser.isLastChunk()) {
                return;
            }
            currentParser = currentParser.nextChunkParser();
        }
    }

    private void processOrdered(Dispatcher c) throws IOException {
        if (cacheSorted == null) {
            cacheSorted = new RecordedEvent[10_000];
        }
        RecordedEvent event;
        int index = 0;
        while (true) {
            event = currentParser.readEvent();
            if (event == null) {
                Arrays.sort(cacheSorted, 0, index, EVENT_COMPARATOR);
                for (int i = 0; i < index; i++) {
                    c.dispatch(cacheSorted[i]);
                }
                return;
            }
            if (index == cacheSorted.length) {
                RecordedEvent[] tmp = cacheSorted;
                cacheSorted = new RecordedEvent[2 * tmp.length];
                System.arraycopy(tmp, 0, cacheSorted, 0, tmp.length);
            }
            cacheSorted[index++] = event;
        }
    }

    private void processUnordered(Dispatcher c) throws IOException {
        while (!isClosed()) {
            RecordedEvent event = currentParser.readEvent();
            if (event == null) {
                return;
            }
            c.dispatch(event);
        }
    }
}