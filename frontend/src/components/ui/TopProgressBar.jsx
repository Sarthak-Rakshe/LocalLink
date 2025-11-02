import { useEffect, useRef, useState } from "react";
import { useNavigation } from "react-router-dom";

// A lightweight top loading bar that reacts to route transitions
export default function TopProgressBar() {
  const navigation = useNavigation();
  const isLoading = navigation.state !== "idle";

  const [progress, setProgress] = useState(0);
  const [visible, setVisible] = useState(false);
  const timerRef = useRef(null);

  useEffect(() => {
    if (isLoading) {
      setVisible(true);
      setProgress(10);
      // Simulate incremental progress while loading
      timerRef.current = setInterval(() => {
        setProgress((p) => (p < 90 ? p + Math.random() * 10 : p));
      }, 200);
    } else {
      // Finish and fade out
      setProgress(100);
      const t = setTimeout(() => setVisible(false), 300);
      return () => clearTimeout(t);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
      timerRef.current = null;
    };
  }, [isLoading]);

  useEffect(
    () => () => timerRef.current && clearInterval(timerRef.current),
    []
  );

  if (!visible) return null;

  return (
    <div className="pointer-events-none fixed inset-x-0 top-0 z-50">
      <div
        className="h-0.5 bg-indigo-600 transition-[width] duration-200"
        style={{ width: `${progress}%` }}
        aria-hidden
      />
      {/* subtle glow */}
      <div
        className="h-0.5 bg-indigo-600/20 blur-[2px]"
        style={{ width: `${progress}%` }}
      />
    </div>
  );
}
