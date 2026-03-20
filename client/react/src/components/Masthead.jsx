import { useEffect, useState } from 'react';
import AuthSlot from './AuthSlot';
import WotdStrip from './WotdStrip';

const THEMES = [
  { id: 'green', label: 'সবুজ', bg: '#FBF8F0', ring: '#C4881A' },
  { id: 'dark',  label: 'রাত',  bg: '#1A1510', ring: '#D4A83A' },
];

export default function Masthead({ onNavigate, theme, onThemeChange }) {
  const [dateStr, setDateStr] = useState('');

  useEffect(() => {
    setDateStr(new Date().toLocaleDateString('en-GB', {
      day: 'numeric', month: 'long', year: 'numeric',
    }));
  }, []);

  return (
    <header className="hdr-masthead">
      <div className="mast-outer">
        <div className="mast-rule mast-rule--thick"></div>
        <div className="mast-rule mast-rule--thin"></div>
        <div className="mast-main">
          <div className="mast-left">বাংলা অভিধান<br />21 February 2025</div>
          <div className="mast-center">
            <div className="mast-script">শব্দ</div>
            <div className="mast-title">SHOBDO</div>
            <div className="mast-subtitle">বাংলা শব্দকোষ · Bengali Dictionary</div>
          </div>
          <div className="mast-right">
            <div className="mast-theme-swatches">
              {THEMES.map(t => (
                <button
                  key={t.id}
                  className={`mast-swatch${theme === t.id ? ' active' : ''}`}
                  style={{ background: t.bg, '--swatch-ring': t.ring }}
                  title={t.label}
                  onClick={() => onThemeChange(t.id)}
                  aria-label={`থিম: ${t.label}`}
                />
              ))}
            </div>
            <AuthSlot />
            <span>{dateStr}</span><br />
            বাংলা · বাংলাদেশ
          </div>
        </div>
        <div className="mast-rule mast-rule--thin"></div>
        <div className="mast-rule mast-rule--thick"></div>
        <WotdStrip onNavigate={onNavigate} />
      </div>
    </header>
  );
}
