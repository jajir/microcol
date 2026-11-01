#!/usr/bin/env python3
"""Editable source of the MicroCol mission success cue (R-0228).

The cue is an original rising C major arpeggio, C5 E5 G5 C6, with a soft bell
timbre: each note is a sine with two quiet overtones and an exponential decay,
and the last note rings out over the earlier ones. Nothing is sampled or
copied; the sound is computed only from the numbers below with the Python
standard library.

Export, run from the repository root:

    python3 -I microcol-game/src/audio/mission-success-01.py

It writes microcol-game/src/main/resources/sounds/mission-success-01.wav as
uncompressed PCM WAV, 44,100 Hz, 16 bit, mono. Another output path may be
given as the only argument. The computation has no randomness, so the same
Python on the same platform writes the same file.

Author and rights: an original work written for the MicroCol project in
November 2026; it is held and distributed like the project's other original
sources. It contains no third-party material and needs no external credit.
"""

import math
import os
import struct
import sys
import wave

SAMPLE_RATE = 44100

# Seconds of the whole cue; the last note fades to exact silence at its end.
DURATION = 1.4

# Peak level relative to full scale: about -6 dBFS, well clear of clipping.
PEAK = 0.5

# (frequency in Hz, start in seconds, decay time constant in seconds, level)
NOTES = (
    (523.25, 0.00, 0.30, 0.80),  # C5
    (659.26, 0.11, 0.30, 0.80),  # E5
    (783.99, 0.22, 0.32, 0.85),  # G5
    (1046.50, 0.33, 0.45, 1.00),  # C6, held
)

# Relative levels of the fundamental and its 2nd and 3rd harmonics.
HARMONICS = ((1, 1.0), (2, 0.22), (3, 0.06))

# Raised-cosine attack of each note and fade-out of the whole cue, in seconds.
ATTACK = 0.008
FADE_OUT = 0.25

DEFAULT_OUTPUT = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), os.pardir, "main", "resources",
    "sounds", "mission-success-01.wav")


def raised_cosine(position):
    """Smooth 0..1 ramp for a position from 0 to 1."""
    return 0.5 - 0.5 * math.cos(math.pi * min(max(position, 0.0), 1.0))


def sample(t):
    value = 0.0
    for frequency, start, decay, level in NOTES:
        local = t - start
        if local < 0:
            continue
        envelope = raised_cosine(local / ATTACK) * math.exp(-local / decay)
        tone = sum(weight * math.sin(2 * math.pi * frequency * harmonic * local)
                   for harmonic, weight in HARMONICS)
        value += level * envelope * tone
    remaining = DURATION - t
    if remaining < FADE_OUT:
        value *= raised_cosine(remaining / FADE_OUT)
    return value


def render():
    frames = int(round(DURATION * SAMPLE_RATE))
    raw = [sample(i / SAMPLE_RATE) for i in range(frames)]
    # The first and last frames are exact silence, so the cue starts and ends
    # without a click.
    raw[0] = 0.0
    raw[-1] = 0.0
    scale = PEAK / max(abs(value) for value in raw)
    return [int(round(value * scale * 32767)) for value in raw]


def write(path, samples):
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
    with wave.open(path, "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(SAMPLE_RATE)
        out.writeframes(b"".join(struct.pack("<h", s) for s in samples))


def main(argv):
    path = argv[1] if len(argv) > 1 else DEFAULT_OUTPUT
    samples = render()
    write(path, samples)
    print("%s: %d frames, %.3f s, peak %d" % (
        os.path.normpath(path), len(samples), len(samples) / SAMPLE_RATE,
        max(abs(s) for s in samples)))


if __name__ == "__main__":
    main(sys.argv)
