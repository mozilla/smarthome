/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemNotUniqueException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.io.audio.AudioSource;
import org.eclipse.smarthome.io.voice.Voice;
import org.eclipse.smarthome.io.voice.TTSException;
import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.voice.TTSService;
import org.eclipse.smarthome.io.voice.internal.AudioPlayer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Console command extension to speak text by a text-to-speech service (TTS)
 *
 * @author Tilman Kamp - Initial contribution and API
 * @author Kelly Davis
 *
 */
public class SayConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private ItemRegistry itemRegistry;

    public SayConsoleCommandExtension() {
        super("say", "Speak text by a text-to-speech service (TTS).");
    }

    @Override
    public List<String> getUsages() {
        return Collections.singletonList(buildCommandUsage("<text>", "speaks a text"));
    }

    @Override
    public void execute(String[] args, Console console) {
        StringBuilder msg = new StringBuilder();
        for (String word : args) {
            if (word.startsWith("%") && word.endsWith("%") && word.length() > 2) {
                String itemName = word.substring(1, word.length() - 1);
                try {
                    Item item = this.itemRegistry.getItemByPattern(itemName);
                    msg.append(item.getState().toString());
                } catch (ItemNotFoundException e) {
                    console.println("Error: Item '" + itemName + "' does not exist.");
                } catch (ItemNotUniqueException e) {
                    console.print("Error: Multiple items match this pattern: ");
                    for (Item item : e.getMatchingItems()) {
                        console.print(item.getName() + " ");
                    }
                }
            } else {
                msg.append(word);
            }
            msg.append(" ");
        }

        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        TTSService ttsService = getTTSService(context);
        if (ttsService != null) {
            Set<Voice> voices = ttsService.getAvailableVoices();
            Set<AudioFormat> audioFormats = ttsService.getSupportedFormats();

            Voice voice = getPreferedVoice(voices);
            AudioFormat audioFormat = getPreferedAudioFormat(audioFormats);

            try {
                AudioSource audioSource = ttsService.synthesize(msg.toString(), voice, audioFormat);
                AudioPlayer audioPlayer = new AudioPlayer(audioSource);
                audioPlayer.start();
                audioPlayer.join();
                console.println("Said: " + msg);
            } catch(TTSException e) {
                console.println("TTS service failure - tried to say: '" + msg + "' error: " + e.getMessage());
            } catch(InterruptedException e) {
                console.println("TTS service failure - interrupted saying: '" + msg + "' error: " + e.getMessage());
            }
        } else {
            console.println("No TTS service available - tried to say: " + msg);
        }
    }

    protected Voice getPreferedVoice(Set<Voice> voices) {
        // Express preferences with a Language Priority List
        Locale locale = Locale.getDefault();
        String ranges = locale.toLanguageTag();
        List<Locale.LanguageRange> languageRanges = Locale.LanguageRange.parse(ranges);

        // Get collection of voice locales
        Collection<Locale> locales = new ArrayList<Locale>();
        for (Voice currentVoice : voices) {
            locales.add(currentVoice.getLocale());
        }

        // Determine prefered locale based on RFC 4647
        Locale preferedLocale = Locale.lookup(languageRanges,locales);

        // As a last resort choose some Locale
        if (null == preferedLocale) {
            preferedLocale = locales.iterator().next();
        }

        // Determine prefered voice
        Voice preferedVoice = null;
        for (Voice currentVoice : voices) {
            if (preferedLocale.equals(currentVoice.getLocale())) {
                preferedVoice = currentVoice;
            }
        }
        assert (preferedVoice != null);

        // Return prefered voice
       return preferedVoice;
    }

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

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    /**
     * Queries the OSGi service registry for a service that provides a TTS implementation
     * for a given platform.
     *
     * @param context the bundle context to access the OSGi service registry
     * @return a service instance or null, if none could be found
     */
    static private TTSService getTTSService(BundleContext context) {
        if (context != null) {
            try {
                Collection<ServiceReference<TTSService>> refs = context.getServiceReferences(TTSService.class, null);
                if (refs != null && refs.size() > 0) {
                    return context.getService(refs.iterator().next());
                } else {
                    return null;
                }
            } catch (InvalidSyntaxException e) {
                // this should never happen
            }
        }
        return null;
    }

}
