// src/hooks/useCounter.js
import { useState, useEffect } from 'react';

export default function useCounter(target, duration = 1000) {
  const [count, setCount] = useState(0);

  useEffect(() => {
    let start = 0;
    const end = parseFloat(target);
    if (isNaN(end)) return;

    const incrementTime = 15; // ms
    const totalIncrements = Math.ceil(duration / incrementTime);
    const increment = (end - start) / totalIncrements;

    let current = start;
    const timer = setInterval(() => {
      current += increment;
      if ((increment > 0 && current >= end) || (increment < 0 && current <= end)) {
        current = end;
        clearInterval(timer);
      }
      setCount(current);
    }, incrementTime);

    return () => clearInterval(timer);
  }, [target, duration]);

  return count;
}
