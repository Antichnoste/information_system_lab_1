import React from 'react';
import {createRoot} from 'react-dom/client';
import {QueryClientProvider} from '@tanstack/react-query';
import {HashRouter} from 'react-router-dom';
import {queryClient} from './api';
import App from './App';
import './style.css';
createRoot(document.getElementById('root')!).render(<React.StrictMode><QueryClientProvider client={queryClient}><HashRouter><App/></HashRouter></QueryClientProvider></React.StrictMode>);
