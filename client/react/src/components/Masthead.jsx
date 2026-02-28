import { useEffect, useState } from 'react';
import AuthSlot from './AuthSlot';
import WotdStrip from './WotdStrip';

export default function Masthead({ onNavigate }) {
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
