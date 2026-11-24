import { useApi } from '../../api/ApiProvider';
import { useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { Loading, useToast } from '../../components/ui';

/** The back office: the one wide, desktop first screen. Admin only. */
export function Admin() {
  const api = useApi();
  const overview = useLoad(() => api.adminOverview(), []);
  const [toast, showToast] = useToast();
  const o = overview.data;

  return (
    <div className="admin">
      <nav className="admin__nav" aria-label="Back office">
        <span className="admin__brand">Hlala</span>
        <a href="#overview" aria-current="true">
          Overview
        </a>
        <a href="#checks">Room checks</a>
        <a href="#flags">Fraud flags</a>
        <a href="#cases">Dispute desk</a>
        <a href="#audit">Money trail</a>
      </nav>

      <main className="admin__main" id="overview">
        {!o ? (
          <Loading error={overview.error} onRetry={overview.reload} />
        ) : (
          <>
            <div className="row row--between row--wrap">
              <div className="stack" style={{ gap: 2 }}>
                <h1 className="title title--xl">Back office</h1>
                <span className="subtitle">
                  {o.areaLabel} · {o.dateLabel}
                </span>
              </div>
              <div className="row">
                <span className="pill-btn">All areas</span>
                <span className="pill-btn pill-btn--dark">{o.staffName}</span>
              </div>
            </div>

            <div className="admin__stats">
              {o.stats.map((s) => (
                <div key={s.label} className="card card--pad" style={{ gap: 4 }}>
                  <span className="subtitle">{s.label}</span>
                  <span className="admin__stat-value">{s.value}</span>
                  <span className="small">{s.note}</span>
                </div>
              ))}
            </div>

            <div className="admin__panels">
              {o.panels.map((p) => (
                <section key={p.id} id={p.id} className="card card--pad" aria-labelledby={`${p.id}-h`}>
                  <div className="row row--between">
                    <h2 id={`${p.id}-h`} style={{ fontSize: 16, fontWeight: 700 }}>
                      {p.title}
                    </h2>
                    <span className="tag tag--dark">{p.rows.length}</span>
                  </div>
                  {p.rows.map((r) => (
                    <div key={r.id} className="admin__item">
                      <span style={{ fontSize: 14, fontWeight: 700 }}>{r.head}</span>
                      <span className="small">{r.body}</span>
                      <div className="row row--wrap" style={{ gap: 6 }}>
                        <button type="button" className="btn btn--primary btn--sm" onClick={() => showToast(`${r.act}: sample mode, nothing changed`)}>
                          {r.act}
                        </button>
                        <button type="button" className="btn btn--secondary btn--sm" onClick={() => showToast(`${r.alt}: sample mode, nothing changed`)}>
                          {r.alt}
                        </button>
                      </div>
                    </div>
                  ))}
                </section>
              ))}
            </div>

            <section id="audit" className="card card--pad" aria-labelledby="audit-h">
              <div className="row row--between row--wrap">
                <h2 id="audit-h" style={{ fontSize: 16, fontWeight: 700 }}>
                  Money trail: every move of money on hold
                </h2>
                <span className="small">Entries cannot be edited or deleted</span>
              </div>
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th scope="col">Time</th>
                      <th scope="col">Who</th>
                      <th scope="col">What happened</th>
                      <th scope="col">Amount</th>
                      <th scope="col">Hold reference</th>
                    </tr>
                  </thead>
                  <tbody>
                    {o.trail.map((t) => (
                      <tr key={t.id}>
                        <td>{t.time}</td>
                        <td>{t.who}</td>
                        <td>{t.what}</td>
                        <td className="num">{formatRand(t.amount)}</td>
                        <td>{t.ref}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <span className="small">
                A payout needs two things: the tenant confirmed keys, and no open case on the hold. Staff can freeze money, but no
                single staff member can release it alone.
              </span>
            </section>
          </>
        )}
      </main>
      {toast}
    </div>
  );
}
