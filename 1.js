// server-node/server.js
// Minimal Express + WebSocket server for Twilio Media Streams and TwiML endpoint.
require('dotenv').config();
const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const bodyParser = require('body-parser');

const app = express();
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

const PORT = process.env.MEDIA_PORT || 8081;
const BASE = process.env.BASE_URL || `https://localhost:${PORT}`;

app.post('/twiml', (req, res) => {
  // instruct Twilio to start media stream - Twilio will POST here on incoming call
  const wsUrl = (BASE.replace(/^http/, 'wss')) + '/media';
  const twiml = `<?xml version="1.0" encoding="UTF-8"?>
  <Response>
    <Say voice="alice">Connecting you to Queenchip. Please speak after the beep.</Say>
    <Start><Stream url="${wsUrl}" /></Start>
    <Pause length="600" />
  </Response>`;
  res.type('text/xml').send(twiml);
});

const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: '/media' });

// Twilio media frames handler
wss.on('connection', (ws, req) => {
  console.log('Media stream connected');
  ws.on('message', async (msg) => {
    try {
      const o = JSON.parse(msg.toString('utf8'));
      if (o.event === 'media') {
        // decode base64 payload
        const audioBuf = Buffer.from(o.media.payload, 'base64');
        // In production: stream audioBuf to STT (Deepgram/Assembly AI/Google) for realtime transcript
        // Then send transcript to LLM, then TTS, then instruct Twilio to play audio back into call
      } else if (o.event === 'start') {
        console.log('Stream start', o);
      } else if (o.event === 'stop') {
        console.log('Stream stop');
      }
    } catch (err) {
      console.error('WS parse error', err);
    }
  });
});

server.listen(PORT, () => console.log('Media server listening on', PORT));
