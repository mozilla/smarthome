/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.io.audio.AudioException;
import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;
import org.eclipse.smarthome.io.voice.KeywordSpottingEvent;
import org.eclipse.smarthome.io.voice.KeywordSpottingErrorEvent;
import org.eclipse.smarthome.io.voice.KSEvent;
import org.eclipse.smarthome.io.voice.KSException;
import org.eclipse.smarthome.io.voice.KSService;
import org.eclipse.smarthome.io.voice.KSListener;
import org.eclipse.smarthome.io.voice.RecognitionStopEvent;
import org.eclipse.smarthome.io.voice.SpeechRecognitionEvent;
import org.eclipse.smarthome.io.voice.SpeechRecognitionErrorEvent;
import org.eclipse.smarthome.io.voice.STTEvent;
import org.eclipse.smarthome.io.voice.STTException;
import org.eclipse.smarthome.io.voice.STTServiceHandle;
import org.eclipse.smarthome.io.voice.STTService;
import org.eclipse.smarthome.io.voice.TTSException;
import org.eclipse.smarthome.io.voice.TTSService;
import org.eclipse.smarthome.io.voice.STTListener;
import org.eclipse.smarthome.io.voice.Voice;
import org.eclipse.smarthome.io.voice.text.HumanLanguageInterpreter;
import org.eclipse.smarthome.io.voice.text.InterpretationException;


/**
 * This is a class that orchestrates Vaani 
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class VaaniOrchestrator implements KSListener, STTListener {
    private static final Logger logger = LoggerFactory.getLogger(VaaniOrchestrator.class);

   /**
    * Keyword spotting service
    */
    private KSService ksService;

   /**
    * Speech to text service
    */
    private STTService sttService;

   /**
    * Text to speech service
    */
    private TTSService ttsService;

   /**
    * Human language interpreter
    */
    private HumanLanguageInterpreter humanLanguageInterpreter;

   /**
    * The STTServiceHandle for the current recognize call
    */
    private STTServiceHandle sttServiceHandle = null;

   /**
    * If the orchestrator should spot on RecognitionStopEvent
    */
    private boolean spotOnRecognitionStopEvent = true;

   /**
    * If the STT server is in the process of aborting
    */
    private boolean isSTTServerAborting = false;

   /**
    * Supported Locale
    */
    private final Locale locale = new Locale("en", "US");

   /**
    * Current keyword
    */
    private final String keyword = "Wakeup";

   /**
    * Called when the VaaniOrchestrator is activated
    */
    protected void activate() {
        this.spot();
    }

   /**
    * Called when the VaaniOrchestrator is deactivated
    */
    protected void deactivate() {
    }

   /**
    * {@inheritDoc}
    */
    public void ksEventReceived(KSEvent ksEvent) {
        this.isSTTServerAborting = false;
        if (ksEvent instanceof KeywordSpottingEvent) {
            KeywordSpottingEvent kse = (KeywordSpottingEvent) ksEvent;
            AudioSource audioSource = kse.getAudioSource();
            try {
                this.sttServiceHandle = this.sttService.recognize(this, audioSource, this.locale, new HashSet<String>());
            } catch(STTException e) {
                say("Encountered error recognizing, " + e.getMessage());
            }
        } else if (ksEvent instanceof KeywordSpottingErrorEvent) {
            KeywordSpottingErrorEvent kse = (KeywordSpottingErrorEvent) ksEvent;
            say("Encountered error spotting keywords, " + kse.getMessage());
        }
    }

   /**
    * {@inheritDoc}
    */
    public synchronized void sttEventReceived(STTEvent  sttEvent) {
        if (sttEvent instanceof SpeechRecognitionEvent) {
            if (false == this.isSTTServerAborting) {
                this.sttServiceHandle.abort();
                this.isSTTServerAborting = true;
                SpeechRecognitionEvent sre = (SpeechRecognitionEvent) sttEvent;
                String question = sre.getTranscript();
                try {
                    this.spotOnRecognitionStopEvent = false;
                    say(this.humanLanguageInterpreter.interpret(this.locale, question));
                } catch(InterpretationException e) {
                    say(e.getMessage());
                }
            }
        } else if(sttEvent instanceof RecognitionStopEvent) {
            if (this.spotOnRecognitionStopEvent) {
                spot();
            }
            this.spotOnRecognitionStopEvent = true;
        } else if (sttEvent instanceof SpeechRecognitionErrorEvent) {
            if (false == this.isSTTServerAborting ) {
                this.sttServiceHandle.abort();
                this.isSTTServerAborting = true;
                this.spotOnRecognitionStopEvent = false;
                SpeechRecognitionErrorEvent sre = (SpeechRecognitionErrorEvent) sttEvent;
                say("Encountered error: " + sre.getMessage());
            }
        }
    }

   /**
    * Says the passed command
    *
    * @param text The text to say
    */
    protected void say(String text) {
        try {
            Voice voice = null;
            for (Voice currentVoice : this.ttsService.getAvailableVoices()) {
                if (this.locale.getLanguage() == currentVoice.getLocale().getLanguage()) {
                    voice = currentVoice;
                    break;
                }
            }
            if (null == voice) {
                throw new TTSException("Unable to find apropos voice");
            }
            Set<AudioFormat> audioFormats = this.ttsService.getSupportedFormats();
            AudioFormat audioFormat = getPreferedAudioFormat(audioFormats);
            AudioSource audioSource = this.ttsService.synthesize(text, voice, audioFormat);

            play(audioSource);
        } catch(TTSException e) {
            logger.error("Error saying something"); 
        }
        spot();
    }

   /**
    * Gets the first concrete AudioFormat in the passed set or a prefered one
    * based on 16bit, 16KHz, big endian default
    *
    * @param audioFormats The AudioFormat from which to choose
    * @return The prefered AudioFormat. A passed concrete format is prefered adding
    *         default values to an abstract AudioFormat in the passed Set.
    */
    protected AudioFormat getPreferedAudioFormat(Set<AudioFormat> audioFormats) {
        // Return the first concrete AudioFormat found
        for (AudioFormat currentAudioFormat : audioFormats) {
            // Check if currentAudioFormat is abstract
            if (null == currentAudioFormat.getCodec()) { continue; }
            if (null == currentAudioFormat.getContainer()) { continue; }
            if (null == currentAudioFormat.isBigEndian()) { continue; }
            if (null == currentAudioFormat.getBitDepth()) { continue; }
            if (null == currentAudioFormat.getBitRate()) { continue; }
            if (null == currentAudioFormat.getFrequency()) { continue; }

            // Prefer WAVE container
            if (!currentAudioFormat.getContainer().equals("WAVE")) { continue; }

            // As currentAudioFormat is concreate, use it
            return currentAudioFormat;
        }

        // There's no concrete AudioFormat so we must create one
        for (AudioFormat currentAudioFormat : audioFormats) {
            // Define AudioFormat to return
            AudioFormat format = currentAudioFormat;

            // Not all Codecs and containers can be supported
            if (null == format.getCodec()) { continue; }
            if (null == format.getContainer()) { continue; }

            // Prefer WAVE container
            if (!format.getContainer().equals("WAVE")) { continue; }

            // If required set BigEndian, BitDepth, BitRate, and Frequency to default values
            if (null == format.isBigEndian()) {
                format = new AudioFormat(format.getContainer(), format.getCodec(), new Boolean(true), format.getBitDepth(), format.getBitRate(), format.getFrequency());
            }
            if (null == format.getBitDepth() || null == format.getBitRate() || null == format.getFrequency()) {
                // Define default values
                int defaultBitDepth = 16;
                int defaultBitRate = 262144;
                long defaultFrequency = 16384;

                // Obtain current values
                Integer bitRate = format.getBitRate();
                Long frequency = format.getFrequency();
                Integer bitDepth = format.getBitDepth();

                // These values must be interdependent (bitRate = bitDepth * frequency)
                if (null == bitRate) {
                    if (null == bitDepth) { bitDepth = new Integer(defaultBitDepth); }
                    if (null == frequency) { frequency = new Long(defaultFrequency); }
                    bitRate = new Integer(bitDepth.intValue() * frequency.intValue());
                } else if (null == bitDepth) {
                    if (null == frequency) { frequency = new Long(defaultFrequency); }
                    if (null == bitRate) { bitRate = new Integer(defaultBitRate); }
                    bitDepth = new Integer(bitRate.intValue() / frequency.intValue());
                } else if (null == frequency) {
                    if (null == bitRate) { bitRate = new Integer(defaultBitRate); }
                    if (null == bitDepth) { bitDepth = new Integer(defaultBitDepth); }
                    frequency = new Long(bitRate.longValue() / bitDepth.longValue());
                }

                format = new AudioFormat(format.getContainer(), format.getCodec(), format.isBigEndian(), bitDepth, bitRate, frequency);
            }

            // Retrun prefered AudioFormat
            return format;
        }

        // Indicates the passed audioFormats is empty or specified no codecs or containers
        assert (false);

        // Return null indicating failue
        return null;
    }

   /**
    * Plays the passed audio source
    *
    * @param audioSource The AudioSource to play 
    */
    protected void play(AudioSource audioSource) {
        AudioPlayer audioPlayer = new AudioPlayer(audioSource);
        audioPlayer.start();
        try {
            audioPlayer.join();
        } catch(InterruptedException e) {
            logger.error("Error playing audio"); 
        }
    }

   /**
    * Starts listening for the keyword
    */
    protected void spot() {
        try {
            Microphone microphone = new Microphone();
            AudioSource audioSource = microphone.getAudioSource();
            this.ksService.spot(this, audioSource, this.locale, this.keyword);
        } catch(AudioException e) {
            logger.error("Encountered error obtaining the audio source, " + e.getMessage());
        } catch(KSException e) {
            logger.error("Encountered error calling spot, " + e.getMessage());
        }
    }

   /**
    * Sets the keyword spotting service
    *
    * @param ksService The new keyword spotting service
    */
    protected void setKSService(KSService ksService) {
        this.ksService = ksService;
    }

   /**
    * Sets the keyword spotting service to null
    *
    * @param ksService This variable is ignored
    */
    protected void unsetKSService(KSService ksService) {
        this.ksService = null;
    }

   /**
    * Sets the speech to text service
    *
    * @param sttService The new speech to text service
    */
    protected void setSTTService(STTService sttService) {
        this.sttService = sttService;
    }

   /**
    * Sets the speech to text service to null
    *
    * @param sttService This variable is ignored
    */
    protected void unsetSTTService(STTService sttService) {
        this.sttService = null;
    }

   /**
    * Sets the text to speech service
    *
    * @param ttsService The new text to speech service
    */
    protected void setTTSService(TTSService ttsService) {
        this.ttsService = ttsService;
    }

   /**
    * Sets the text to speech service to null
    *
    * @param ttsService This variable is ignored
    */
    protected void unsetTTSService(TTSService ttsService) {
        this.ttsService = null;
    }

   /**
    * Sets the human language interpreter service
    *
    * @param humanLanguageInterpreter The new human language interpreter service
    */
    protected void setHumanLanguageInterpreter(HumanLanguageInterpreter humanLanguageInterpreter) {
        this.humanLanguageInterpreter = humanLanguageInterpreter;
    }

   /**
    * Sets the human language interpreter service to null
    *
    * @param humanLanguageInterpreter This variable is ignored
    */
    protected void unsetHumanLanguageInterpreter(HumanLanguageInterpreter humanLanguageInterpreter) {
        this.humanLanguageInterpreter = null;
    }
}
