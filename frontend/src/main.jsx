import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

const API = import.meta.env.VITE_API_URL || "http://localhost:8083/api";

function Service({ name, status }) {
  return <div className="card service">
    <div><small>SERVICE</small><h3>{name}</h3></div>
    <span className={status === "healthy" ? "ok" : "bad"}>● {status}</span>
  </div>;
}

function App() {
  const [data, setData] = useState(null);
  const [incidents, setIncidents] = useState([]);
  const [message, setMessage] = useState("System ready.");

  async function refresh() {
    try {
      const [a,b] = await Promise.all([
        fetch(`${API}/reliability/status`).then(r => r.json()),
        fetch(`${API}/incidents`).then(r => r.json())
      ]);
      setData(a); setIncidents(b);
    } catch {
      setMessage("Controller unavailable.");
    }
  }

  async function action(path) {
    try {
      const r = await fetch(`${API}${path}`, {method:"POST"});
      const body = await r.json();
      setMessage(JSON.stringify(body));
      await refresh();
    } catch {
      setMessage("Action failed. Check backend services.");
    }
  }

  useEffect(() => {
    refresh();
    const id = setInterval(refresh, 3000);
    return () => clearInterval(id);
  }, []);

  const latest = incidents.at(-1);

  return <main>
    <header>
      <div><b>SELF-HEALING</b><span>Distributed Reliability Control Center</span></div>
      <i>● LIVE</i>
    </header>

    <section className="hero">
      <small>RELIABILITY ENGINEERING</small>
      <h1>Detect. Recover. Verify.</h1>
      <p>Controlled failure injection, automated remediation and independent application-level recovery verification.</p>
    </section>

    <section className="grid">
      <Service name="Order Service" status={data?.orderService || "unknown"} />
      <Service name="Payment Service" status={data?.paymentService || "unknown"} />
      <Service name="Reliability Controller" status={data?.controller || "unknown"} />
    </section>

    <section className="card">
      <small>CONTROLLED EXPERIMENT</small>
      <h2>Failure & Recovery</h2>
      <div className="actions">
        <button className="danger" onClick={() => action("/failures/payment")}>Inject Payment Failure</button>
        <button onClick={() => action("/recovery/payment")}>Execute Remediation</button>
        <button onClick={() => action("/recovery/payment/verify")}>Verify Recovery</button>
        <button className="muted" onClick={refresh}>Refresh</button>
      </div>
      <code>{message}</code>
    </section>

    <section className="card">
      <small>LATEST INCIDENT</small>
      <h2>{latest ? `Incident #${latest.id}` : "No incidents yet"}</h2>
      {latest ? <div className="incident">
        <div>Service<strong>{latest.service}</strong></div>
        <div>Failure<strong>{latest.failureType}</strong></div>
        <div>Status<strong>{latest.status}</strong></div>
        <div>Verification<strong>{latest.verification}</strong></div>
      </div> : <p>Inject a controlled failure to start an experiment.</p>}
    </section>
  </main>;
}

createRoot(document.getElementById("root")).render(<React.StrictMode><App /></React.StrictMode>);
