let audioCtx: AudioContext | null = null;

function getAudioContext(): AudioContext | null {
  if (typeof window === 'undefined') return null;
  const Ctx = window.AudioContext || (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
  if (!Ctx) return null;
  if (!audioCtx) {
    audioCtx = new Ctx();
  }
  if (audioCtx.state === 'suspended') {
    audioCtx.resume().catch(() => {});
  }
  return audioCtx;
}

function playTone(freq: number, durationSec: number, delaySec = 0, type: OscillatorType = 'sine', gainLevel = 0.08) {
  try {
    const ctx = getAudioContext();
    if (!ctx) return;
    const now = ctx.currentTime + delaySec;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(freq, now);
    gain.gain.setValueAtTime(0.001, now);
    gain.gain.exponentialRampToValueAtTime(gainLevel, now + 0.015);
    gain.gain.exponentialRampToValueAtTime(0.001, now + durationSec);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start(now);
    osc.stop(now + durationSec + 0.02);
  } catch {
    // Ignore audio feedback errors if browser blocks audio
  }
}

export function playCompleteChime(enabled: boolean) {
  if (!enabled) return;
  playTone(587.33, 0.12, 0, 'triangle', 0.09);
  playTone(880.0, 0.2, 0.11, 'triangle', 0.1);
}

export function playPop(enabled: boolean) {
  if (!enabled) return;
  playTone(480, 0.08, 0, 'sine', 0.06);
}

export function playDrop(enabled: boolean) {
  if (!enabled) return;
  playTone(523.25, 0.09, 0, 'sine', 0.07);
  playTone(659.25, 0.11, 0.06, 'sine', 0.07);
}
