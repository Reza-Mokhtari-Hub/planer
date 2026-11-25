import { useEffect, useRef } from 'react';

interface ConfettiOverlayProps {
  triggerKey: number;
  grandCelebration: boolean;
}

interface Particle {
  angle: number;
  speed: number;
  color: string;
  size: number;
  rotationSpeed: number;
}

const COLORS = ['#F59E0B', '#10B981', '#F43F5E', '#6366F1', '#0EA5E9', '#EC4899'];

export function ConfettiOverlay({ triggerKey, grandCelebration }: ConfettiOverlayProps) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    if (triggerKey <= 0) return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const prefersReduced =
      typeof window !== 'undefined' &&
      window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
    if (prefersReduced) return;

    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    const count = grandCelebration ? 70 : 36;
    const particles: Particle[] = Array.from({ length: count }, (_, idx) => ({
      angle: Math.random() * Math.PI * 2,
      speed: 250 + Math.random() * 520,
      color: COLORS[idx % COLORS.length],
      size: 6 + Math.random() * 7,
      rotationSpeed: (Math.random() - 0.5) * 12,
    }));

    const duration = grandCelebration ? 1900 : 1150;
    const start = performance.now();
    let rafId = 0;

    const tick = (now: number) => {
      const elapsed = now - start;
      const p = Math.min(1, elapsed / duration);
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      const originX = canvas.width * 0.5;
      const originY = canvas.height * 0.35;
      const alpha = Math.max(0, 1 - p);

      for (const pt of particles) {
        const dx = Math.cos(pt.angle) * pt.speed * p;
        const dy = Math.sin(pt.angle) * pt.speed * p + 420 * p * p;
        const x = originX + dx;
        const y = originY + dy;

        ctx.save();
        ctx.translate(x, y);
        ctx.rotate(pt.rotationSpeed * p);
        ctx.globalAlpha = alpha;
        ctx.fillStyle = pt.color;
        ctx.fillRect(-pt.size / 2, -(pt.size * 0.65) / 2, pt.size, pt.size * 0.65);
        ctx.restore();
      }

      if (p < 0.99) {
        rafId = requestAnimationFrame(tick);
      } else {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
      }
    };

    rafId = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(rafId);
  }, [triggerKey, grandCelebration]);

  if (triggerKey <= 0) return null;

  return (
    <canvas
      ref={canvasRef}
      className="pointer-events-none fixed inset-0 z-50"
      aria-hidden="true"
    />
  );
}
