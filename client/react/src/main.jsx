import React from 'react';
import ReactDOM from 'react-dom/client';
import { AuthProvider } from './context/AuthContext';
import { LikeProvider } from './context/LikeContext';
import App from './App';
import './styles/style.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider>
      <LikeProvider>
        <App />
      </LikeProvider>
    </AuthProvider>
  </React.StrictMode>
);
