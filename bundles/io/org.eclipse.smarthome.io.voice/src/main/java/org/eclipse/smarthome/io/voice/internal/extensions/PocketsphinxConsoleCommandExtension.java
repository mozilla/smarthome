/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.voice.KSEvent;
import org.eclipse.smarthome.io.voice.KSListener;
import org.eclipse.smarthome.io.voice.KeywordSpottingErrorEvent;
import org.eclipse.smarthome.io.voice.KeywordSpottingEvent;
import org.eclipse.smarthome.io.voice.RecognitionStopEvent;

import edu.cmu.pocketsphinx.nar.NarSystem;

/**
 * Console command extension to interpret human language commands.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public class PocketsphinxConsoleCommandExtension extends AbstractConsoleCommandExtension {

    public PocketsphinxConsoleCommandExtension() {
        super("ws", "Test Wordspotting.");
    }

    @Override
    public List<String> getUsages() {
        return Collections.singletonList(buildCommandUsage("<command>", "interprets the human language command"));
    }

    /**
     * Utility STTListener for testing
     */
    class KSListenerUtility implements KSListener {
        /**
         * Boolean indicating if session is closed
         */
        private boolean isClosed = false;

        /**
         * Final transcript
         */
        private String wordspot;

        /**
         * Gets if the session is closed
         *
         * @return If the session is closed
         */
        public boolean isClosed() {
            return this.isClosed;
        }

        /**
         * Gets the transcript
         *
         * @return The transcript
         */
        public String getSpot() {
            return this.wordspot;
        }

        @Override
        public void ksEventReceived(KSEvent ksEvent) {
            if (ksEvent instanceof KeywordSpottingEvent) {
                // this.wordspot = wordspot ksEvent.
                System.out.println("dentro do listener - found - ");

                return;
            }
            if (ksEvent instanceof RecognitionStopEvent) {
                this.isClosed = true;
            }
            if (ksEvent instanceof KeywordSpottingErrorEvent) {
                KeywordSpottingErrorEvent e = (KeywordSpottingErrorEvent) ksEvent;
                System.out.println("dentro do listener - found - " + e.getMessage());
            }
        }
    }

    static {
        NarSystem.loadLibrary();
    }

    public final native String sayHello();

    @Override
    public void execute(String[] args, Console console) {

        System.out.println(this.sayHello());

        // testaudio();

        // StringBuilder sb = new StringBuilder(args[0]);
        // for (int i = 1; i < args.length; i++) {
        // sb.append(" ");
        // sb.append(args[i]);
        // }
        // String msg = sb.toString();
        // BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        // Collection<ServiceReference<VaaniOrchestrator>> refs = null;
        // try {
        // refs = context.getServiceReferences(VaaniOrchestrator.class, null);
        // } catch (InvalidSyntaxException e) {
        // // should never happen
        // }
        //
        // if (refs != null && refs.size() > 0) {
        // try {
        //
        // testaudio();
        // // FileAudioSource fsrc = new FileAudioSource("goforward.raw");
        // // KSListenerUtility listener = new KSListenerUtility();
        //
        // // VaaniOrchestrator vaaniorchestrator = context.getService(refs.iterator().next());
        // // vaaniorchestrator.a
        //
        // // ksservice.spot(listener, fsrc, null, msg);
        // } catch (Exception ie) {
        // console.println(ie.getMessage());
        // }
        // } else {
        // console.println("No pocketsphinx ws available - tried to interpret: " + msg);
        // }
    }

    public void testaudio() {

        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
        TargetDataLine microphone;
        AudioInputStream audioInputStream;
        SourceDataLine sourceDataLine;
        try {
            microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int numBytesRead;
            int CHUNK_SIZE = 1024;
            byte[] data = new byte[microphone.getBufferSize() / 5];
            microphone.start();

            int bytesRead = 0;

            try {
                while (bytesRead < 100000) { // Just so I can test if recording
                                             // my mic works...
                    numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                    bytesRead = bytesRead + numBytesRead;
                    System.out.println(bytesRead);
                    out.write(data, 0, numBytesRead);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte audioData[] = out.toByteArray();
            // Get an input stream on the byte array
            // containing the data
            InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
            audioInputStream = new AudioInputStream(byteArrayInputStream, format,
                    audioData.length / format.getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);
            sourceDataLine.start();
            int cnt = 0;
            byte tempBuffer[] = new byte[10000];
            try {
                while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        // Write data to the internal buffer of
                        // the data line where it will be
                        // delivered to the speaker.
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    } // end if
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Block and wait for internal buffer of the
            // data line to empty.
            sourceDataLine.drain();
            sourceDataLine.close();
            microphone.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Picks the best fit from a set of available languages (given by {@link Locale}s).
     * Matching happens in the following priority order:
     * 1st: system's default {@link Locale} (e.g. "de-DE"), if contained in {@link locales}
     * 2nd: first item in {@link locales} matching system default language (e.g. "de" matches "de-CH")
     * 3rd: first language in {@link locales}
     * 4th: English, if {@link locales} is null or empty
     *
     * @param locales set of supported {@link Locale}s to pick from
     * @return {@link Locale} of the best fitting language
     */
    private Locale pickLanguage(Set<Locale> locales) {
        if (locales == null || locales.size() == 0) {
            return Locale.ENGLISH;
        }
        Locale locale = Locale.getDefault();
        if (locales.contains(locale)) {
            return locale;
        }
        String language = locale.getLanguage();
        Locale first = null;
        for (Locale l : locales) {
            if (first == null) {
                first = l;
            }
            if (language.equals(l.getLanguage())) {
                return l;
            }
        }
        return first;
    }

}
//
/// **
// * Utility AudioSource for testing
// */
// class FileAudioSource implements AudioSource {
// /**
// * The name of the wrapped file
// */
// private final String fileName;
//
// /**
// * The name of the resource directory
// */
// private final String resourcePath = "src/test/resources/org/eclipse/smarthome/io/voice/internal";
//
// /**
// * Constructs an instance wrapping the passed file
// *
// * @param fileName The name of the wrapped file
// */
// public FileAudioSource(String fileName) {
// this.fileName = fileName;
// }
//
// /**
// * {@inheritDoc}
// */
// @Override
// public AudioFormat getFormat() {
// String container = "NONE";
// String codec = "RAW";
// Boolean bigEndian = new Boolean(false);
// Integer bitDepth = new Integer(16);
// Integer bitRate = new Integer(16000 * 2 * 8);
// Long frequency = new Long(16000);
//
// return new AudioFormat(container, codec, bigEndian, bitDepth, bitRate, frequency);
// }
//
// /**
// * {@inheritDoc}
// */
// @Override
// public InputStream getInputStream() throws AudioException {
// FileInputStream fileInputStream;
// try {
// String filePath =
// "/Users/anatal/projects/mozilla/smarthome/esh_setup/openhab2-master/git/smarthome/bundles/io/org.eclipse.smarthome.io.voice.test/src/test/resources/org/eclipse/smarthome/io/voice/internal/goforward.raw";
// fileInputStream = new FileInputStream(filePath);
// } catch (FileNotFoundException e) {
// throw new AudioException("Unable to file audio file", e);
// }
//
// return fileInputStream;
// }
// }
