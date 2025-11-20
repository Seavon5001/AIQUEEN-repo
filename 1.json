import React, { useState } from 'react';
import axios from 'axios';

export default function App(){
  const [prompt,setPrompt]=useState('');
  const [chat,setChat]=useState([]);
  const [amt,setAmt]=useState('');
  const [phone,setPhone]=useState('');

  async function sendPrompt(){
    if(!prompt) return;
    setChat(c=>[...c,{role:'user',text:prompt}]);
    setPrompt('');
    try {
      const r = await axios.post('/api/ai', { prompt });
      setChat(c=>[...c,{role:'ai',text: r.data.text || 'No reply'}]);
    } catch(e){
      setChat(c=>[...c,{role:'ai',text:'error: '+e.message}]);
    }
  }

  async function donate(){
    try {
      const r = await axios.post('/api/donate', { amount: parseFloat(amt) });
      if (r.data && r.data.url) window.open(r.data.url,'_blank');
    } catch(e){ alert('Donate error: '+e.message) }
  }

  async function call(){
    try{
      const r = await axios.post('/api/call', { to: phone });
      alert('Call initiated: ' + JSON.stringify(r.data));
    } catch(e){ alert('Call error: '+e.message) }
  }

  return (
    <div className="container py-4">
      <h2>AI Queen Superior</h2>
      <div className="row">
        <div className="col-md-8">
          <div style={{height:300,overflow:'auto',background:'#f7f7f7',padding:12}}>
            {chat.map((m,i)=> <div key={i} style={{textAlign:m.role==='user'?'right':'left'}}><div style={{display:'inline-block',padding:8,margin:4,background:m.role==='user'?'#cfe9ff':'#fff'}}>{m.text}</div></div>)}
          </div>
          <div className="d-flex mt-2">
            <input className="form-control me-2" value={prompt} onChange={e=>setPrompt(e.target.value)} />
            <button className="btn btn-primary" onClick={sendPrompt}>Send</button>
          </div>
        </div>
        <div className="col-md-4">
          <h5>Donations</h5>
          <input className="form-control mb-2" value={amt} onChange={e=>setAmt(e.target.value)} placeholder="Amount USD" />
          <button className="btn btn-warning w-100 mb-3" onClick={donate}>Donate</button>
          <h5>Call AI</h5>
          <input className="form-control mb-2" value={phone} onChange={e=>setPhone(e.target.value)} placeholder="+1..." />
          <button className="btn btn-secondary w-100" onClick={call}>Call</button>
        </div>
      </div>
    </div>
  );
}
