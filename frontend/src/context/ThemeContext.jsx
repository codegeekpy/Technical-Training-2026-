import React, { createContext, useContext, useState, useEffect } from 'react';

const ThemeContext = createContext(null);

export const THEMES = {
  light: 'light',
  dark: 'dark',
  vibrant: 'vibrant',
};

const THEME_META = {
  light:   { label: '☀️ Light',   icon: '☀️' },
  dark:    { label: '🌙 Dark',    icon: '🌙' },
  vibrant: { label: '🎨 Vibrant', icon: '🎨' },
};

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') || 'light');

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const cycleTheme = () => {
    const order = ['light', 'dark', 'vibrant'];
    const next = order[(order.indexOf(theme) + 1) % order.length];
    setTheme(next);
  };

  return (
    <ThemeContext.Provider value={{ theme, setTheme, cycleTheme, THEME_META }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}
