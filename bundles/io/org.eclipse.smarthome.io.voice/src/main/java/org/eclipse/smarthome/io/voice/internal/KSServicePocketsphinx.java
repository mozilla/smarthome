/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;
import org.eclipse.smarthome.io.voice.KSException;
import org.eclipse.smarthome.io.voice.KSListener;
import org.eclipse.smarthome.io.voice.KSService;
import org.eclipse.smarthome.io.voice.KSServiceHandle;
import org.eclipse.smarthome.io.voice.KeywordSpottingErrorEvent;
import org.eclipse.smarthome.io.voice.KeywordSpottingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;

/**
 * This is a Keywordspot service implementation using pocketsphinx.
 *
 * @author Andre Natal - Initial contribution and API
 *
 */
public class KSServicePocketsphinx implements KSService {

    static {
        System.loadLibrary("pocketsphinx_jni");
    }

    private static final Logger logger = LoggerFactory.getLogger(KSServicePocketsphinx.class);

    /**
     * Set of supported locales
     */
    private final HashSet<Locale> locales = initLocales();

    /**
     * Set of supported audio formats
     */
    private final HashSet<AudioFormat> audioFormats = initAudioFormats();

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Locale> getSupportedLocales() {
        return this.locales;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return this.audioFormats;
    }

    /**
     * Initializes this.locales
     *
     * @return The locales of this instance
     */
    private final HashSet<Locale> initLocales() {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.add(new Locale("en", "US")); // For now we only support American English
        return locales;
    }

    /**
     * Initializes this.audioFormats
     *
     * @return The audio formats of this instance
     */
    private final HashSet<AudioFormat> initAudioFormats() {
        HashSet<AudioFormat> audioFormats = new HashSet<AudioFormat>();

        String containers[] = { "NONE", "ASF", "AVI", "DVR-MS", "MKV", "MPEG", "OGG", "QuickTime", "RealMedia",
                "WAVE" };
        String codecs[] = { "RAW", "A52", "ADPCM", "FLAC", "GSM", "A-LAW", "MU-LAW", "MP3", "QDM", "SPEEX", "VORBIS",
                "NIST", "VOC" };

        for (String container : containers) {
            for (String codec : codecs) {
                audioFormats.add(new AudioFormat(container, codec, null, null, null, null)); // TODO: Allow only valid
                                                                                             // combinations
            }
        }

        return audioFormats;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KSServiceHandle spot(KSListener ksListener, AudioSource audioSource, Locale locale, String keyword)
            throws KSException {

        // Start recognition
        KSServiceRunnable ksServiceRunnable = new KSServiceRunnable(ksListener, audioSource, keyword);
        Thread thread = new Thread(ksServiceRunnable);
        thread.start();

        return null;
    }
}

class KSServiceRunnable implements Runnable {

    String keyword;
    KSListener ksListener;
    AudioSource audioSource;

    public KSServiceRunnable(KSListener ksListener, AudioSource audioSource, String keyword) {
        this.keyword = keyword;
        this.audioSource = audioSource;
        this.ksListener = ksListener;
    }

    @Override
    public void run() {

        try {
            Config c = Decoder.defaultConfig();
            c.setString("-hmm", "/Users/anatal/projects/mozilla/vaani-iot/pocketsphinx/pocketsphinx/model/en-us/en-us");
            c.setString("-keyphrase", keyword);
            c.setString("-dict",
                    "/Users/anatal/projects/mozilla/vaani-iot/pocketsphinx/pocketsphinx/model/en-us/cmudict-en-us.dict");
            Decoder d = new Decoder(c);
            InputStream ais = audioSource.getInputStream();

            d.startUtt();
            byte[] b = new byte[512];
            int nbytes;
            while (((nbytes = ais.read(b)) >= 0)) {
                ByteBuffer bb = ByteBuffer.wrap(b, 0, nbytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                short[] s = new short[nbytes / 2];
                bb.asShortBuffer().get(s);
                d.processRaw(s, nbytes / 2, false, false);

                if (d.hyp() != null) {
                    System.out.println("found - na thread ks" + d.hyp().getHypstr());
                    KeywordSpottingEvent ksEvent = new KeywordSpottingEvent(audioSource);
                    ksListener.ksEventReceived(ksEvent);
                    break;
                }
            }
            d.endUtt();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("exception - na thread ks" + e.getMessage());
            KeywordSpottingErrorEvent ksEvent = new KeywordSpottingErrorEvent(e.getMessage());
            ksListener.ksEventReceived(ksEvent);
        }
    }

}
