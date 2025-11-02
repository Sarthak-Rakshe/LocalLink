// Simple client-side cooldown tracker for payment retries
// Stores last retry timestamp in localStorage under a namespaced key per transaction

const DEFAULT_COOLDOWN_MS =
  Number(import.meta?.env?.VITE_PAYMENT_RETRY_COOLDOWN_MS) || 120_000; // 2 minutes default

const key = (txId) => `paymentRetry:${txId}`;

export function markRetryAttempt(txId) {
  try {
    localStorage.setItem(key(txId), String(Date.now()));
  } catch {
    // localStorage may be unavailable (private mode); ignore
    void 0;
  }
}

export function getLastAttempt(txId) {
  try {
    const raw = localStorage.getItem(key(txId));
    const t = raw ? Number(raw) : 0;
    return Number.isFinite(t) ? t : 0;
  } catch {
    return 0;
  }
}

export function getCooldownMs() {
  return DEFAULT_COOLDOWN_MS;
}

export function getRemainingMs(txId) {
  const last = getLastAttempt(txId);
  const remain = last + getCooldownMs() - Date.now();
  return Math.max(0, remain);
}

export function isInCooldown(txId) {
  return getRemainingMs(txId) > 0;
}

export function formatDuration(ms) {
  const total = Math.ceil(ms / 1000);
  const m = Math.floor(total / 60);
  const s = total % 60;
  const mm = m.toString().padStart(2, "0");
  const ss = s.toString().padStart(2, "0");
  return `${mm}:${ss}`;
}
