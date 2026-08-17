import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { api, setApiLogger, type ApiLog } from './api'
import { beginGoogleLogin, completeGoogleLogin, tokenStore } from './auth'
import type { AppliedDetail, AppliedSummary, CheckStatus, ChecklistDetail, ChecklistSummary, Member, Page, PropertyItem, PropertyMemo, Stage, SystemItem } from './types'

const STAGES: Stage[] = ['ONLINE_PHONE', 'ON_SITE', 'PRE_CONTRACT']
const stageLabel: Record<Stage, string> = { ONLINE_PHONE: '온라인·전화', ON_SITE: '현장', PRE_CONTRACT: '계약 전' }

function App() {
  const [authenticated, setAuthenticated] = useState(Boolean(tokenStore.access()))
  const [member, setMember] = useState<Member | null>(null)
  const [tab, setTab] = useState<'properties' | 'checklists' | 'system'>('properties')
  const [logs, setLogs] = useState<ApiLog[]>([])
  const [error, setError] = useState('')
  const callback = location.pathname === '/oauth/google/callback'

  useEffect(() => setApiLogger(log => setLogs(current => [log, ...current].slice(0, 30))), [])
  useEffect(() => {
    if (!callback || tokenStore.access()) return
    completeGoogleLogin().then(() => { history.replaceState({}, '', '/'); setAuthenticated(true) }).catch(showError)
  }, [callback])
  useEffect(() => {
    if (authenticated) api<Member>('/api/members/me').then(setMember).catch(showError)
  }, [authenticated])

  function showError(reason: unknown) { setError(reason instanceof Error ? reason.message : String(reason)) }
  async function logout() {
    const refreshToken = tokenStore.refresh()
    if (refreshToken) await api<void>('/api/auth/logout', { method: 'POST', body: JSON.stringify({ refreshToken }) }).catch(showError)
    tokenStore.clear(); setAuthenticated(false); setMember(null)
  }

  if (callback && !authenticated) return <main className="center"><section className="card"><h1>Google 로그인 처리 중</h1><p>인증 코드를 서버에서 검증하고 있습니다.</p>{error && <ErrorBox message={error} />}</section></main>
  if (!authenticated) return <main className="center"><section className="card hero"><span className="eyebrow">LOCAL API QA</span><h1>자취선배</h1><p>실제 Google OAuth와 모든 MVP API 흐름을 로컬에서 검증합니다.</p><button className="primary" onClick={() => beginGoogleLogin().catch(showError)}>Google로 시작하기</button>{error && <ErrorBox message={error} />}</section></main>

  return <div className="app-shell">
    <header><div><b>자취선배</b><span className="muted"> API 검증 클라이언트</span></div><div>{member && <span>{member.name} · {member.email}</span>}<button className="ghost" onClick={logout}>로그아웃</button></div></header>
    <nav>{(['properties', 'checklists', 'system'] as const).map(value => <button key={value} className={tab === value ? 'active' : ''} onClick={() => setTab(value)}>{value === 'properties' ? '매물' : value === 'checklists' ? '내 체크리스트' : '시스템 항목'}</button>)}</nav>
    {error && <ErrorBox message={error} clear={() => setError('')} />}
    <main>{tab === 'properties' && <Properties onError={showError} />}{tab === 'checklists' && <Checklists onError={showError} />}{tab === 'system' && <SystemItems onError={showError} />}</main>
    <aside className="logs"><h3>API 로그 <small>본문·토큰 제외</small></h3>{logs.map((log, index) => <code key={`${log.at}-${index}`}><span>{log.at}</span> {log.method} {log.path} <b className={log.status >= 400 ? 'bad' : 'good'}>{log.status}</b></code>)}</aside>
  </div>
}

function ErrorBox({ message, clear }: { message: string; clear?: () => void }) { return <div className="error"><span>{message}</span>{clear && <button onClick={clear}>×</button>}</div> }

function SystemItems({ onError, stage: initialStage }: { onError: (e: unknown) => void; stage?: Stage }) {
  const [stage, setStage] = useState<Stage>(initialStage ?? 'ON_SITE'); const [query, setQuery] = useState(''); const [items, setItems] = useState<SystemItem[]>([])
  const load = () => api<SystemItem[]>(`/api/check-items?stage=${stage}&query=${encodeURIComponent(query)}`).then(setItems).catch(onError)
  useEffect(() => { load() }, [stage])
  return <section className="card"><div className="section-title"><div><h2>시스템 체크 항목</h2><p>공개 API · 단계와 질문으로 검색</p></div><StageSelect value={stage} onChange={setStage} /></div><div className="row"><input value={query} onChange={e => setQuery(e.target.value)} placeholder="질문 검색"/><button onClick={load}>검색</button></div><div className="list">{items.map(item => <article key={item.checkItemId}><span className={`badge ${item.type.toLowerCase()}`}>{item.type}</span><div><b>{item.question}</b><p>{item.guide ?? '안내 없음'}</p></div></article>)}</div></section>
}

function Checklists({ onError }: { onError: (e: unknown) => void }) {
  const [stage, setStage] = useState<Stage>('ON_SITE'); const [list, setList] = useState<ChecklistSummary[]>([]); const [selected, setSelected] = useState<ChecklistDetail | null>(null); const [system, setSystem] = useState<SystemItem[]>([]); const [name, setName] = useState(''); const [ids, setIds] = useState<number[]>([])
  const load = () => api<ChecklistSummary[]>(`/api/checklists?stage=${stage}`).then(setList).catch(onError)
  useEffect(() => { load(); api<SystemItem[]>(`/api/check-items?stage=${stage}`).then(setSystem).catch(onError) }, [stage])
  async function open(id: number) { const detail = await api<ChecklistDetail>(`/api/checklists/${id}`); setSelected(detail); setName(detail.name); setIds(detail.items.map(i => i.checkItemId)) }
  function toggle(id: number) { setIds(values => values.includes(id) ? values.filter(value => value !== id) : [...values, id]) }
  async function save() { if (selected) await api(`/api/checklists/${selected.checklistId}`, { method: 'PUT', body: JSON.stringify({ name, checkItemIds: ids }) }); else await api('/api/checklists', { method: 'POST', body: JSON.stringify({ name, stage, checkItemIds: ids }) }); setSelected(null); setName(''); setIds([]); load() }
  async function remove() { if (!selected || !confirm('체크리스트를 삭제할까요?')) return; await api(`/api/checklists/${selected.checklistId}`, { method: 'DELETE' }); setSelected(null); setName(''); setIds([]); load() }
  return <div className="grid two"><section className="card"><div className="section-title"><div><h2>내 체크리스트</h2><p>생성·상세·소프트 삭제</p></div><StageSelect value={stage} onChange={value => { setStage(value); setSelected(null); setIds([]) }} /></div><button className="primary full" onClick={() => { setSelected(null); setName('새 체크리스트'); setIds([]) }}>새 체크리스트</button><div className="list clickable">{list.map(item => <article key={item.checklistId} onClick={() => open(item.checklistId).catch(onError)}><div><b>{item.name}</b><p>{item.itemCount}개 항목 · {item.appliedPropertyCount}개 매물 적용</p></div></article>)}</div></section><section className="card"><h2>{selected ? '체크리스트 일괄 편집' : '체크리스트 생성'}</h2><p>CORE도 생성 이후에는 선택 해제할 수 있습니다.</p><input value={name} onChange={e => setName(e.target.value)} placeholder="체크리스트 이름"/><div className="checks">{system.map(item => <label key={item.checkItemId}><input type="checkbox" checked={ids.includes(item.checkItemId)} onChange={() => toggle(item.checkItemId)}/><span><b>{item.question}</b><small>{item.type}</small></span></label>)}</div><div className="actions"><button className="primary" disabled={!name || ids.length === 0} onClick={() => save().catch(onError)}>전체 저장</button>{selected && <button className="danger" onClick={() => remove().catch(onError)}>체크리스트 삭제</button>}</div></section></div>
}

function Properties({ onError }: { onError: (e: unknown) => void }) {
  const [page, setPage] = useState<Page<PropertyItem> | null>(null); const [query, setQuery] = useState(''); const [selected, setSelected] = useState<PropertyItem | null>(null); const [name, setName] = useState(''); const [address, setAddress] = useState('')
  const load = (number = 0) => api<Page<PropertyItem>>(`/api/properties?query=${encodeURIComponent(query)}&page=${number}&size=10`).then(setPage).catch(onError)
  useEffect(() => { load() }, [])
  async function save(event: FormEvent) { event.preventDefault(); const body = JSON.stringify({ name, address: address || null }); if (selected) await api(`/api/properties/${selected.propertyId}`, { method: 'PATCH', body }); else await api('/api/properties', { method: 'POST', body }); setSelected(null); setName(''); setAddress(''); load() }
  async function remove() { if (!selected || !confirm('매물과 종속 데이터를 삭제할까요?')) return; await api(`/api/properties/${selected.propertyId}`, { method: 'DELETE' }); setSelected(null); load() }
  return <><div className="grid two"><section className="card"><div className="section-title"><div><h2>후보 매물</h2><p>검색·페이징·전체 진행률</p></div><button className="primary" onClick={() => { setSelected(null); setName(''); setAddress('') }}>새 매물</button></div><div className="row"><input value={query} onChange={e => setQuery(e.target.value)} placeholder="이름 검색"/><button onClick={() => load()}>검색</button></div><div className="list clickable">{page?.content.map(item => <article key={item.propertyId} onClick={() => { setSelected(item); setName(item.name); setAddress(item.address ?? '') }}><div><b>{item.name}</b><p>{item.address ?? '주소 없음'}</p></div><Progress value={item.progress.progressPercent}/></article>)}</div><div className="pager"><button disabled={!page || page.page === 0} onClick={() => load((page?.page ?? 1) - 1)}>이전</button><span>{(page?.page ?? 0) + 1} / {Math.max(page?.totalPages ?? 1, 1)}</span><button disabled={!page?.hasNext} onClick={() => load((page?.page ?? 0) + 1)}>다음</button></div></section><section className="card"><h2>{selected ? '매물 상세·수정' : '매물 생성'}</h2><form onSubmit={e => save(e).catch(onError)}><label>이름<input value={name} onChange={e => setName(e.target.value)} required /></label><label>주소<input value={address} onChange={e => setAddress(e.target.value)} /></label><div className="actions"><button className="primary">저장</button>{selected && <button type="button" className="danger" onClick={() => remove().catch(onError)}>삭제</button>}</div></form>{selected && <PropertyWorkspace property={selected} onError={onError} reload={() => load(page?.page)} />}</section></div></>
}

function PropertyWorkspace({ property, onError, reload }: { property: PropertyItem; onError: (e: unknown) => void; reload: () => void }) {
  const [memo, setMemo] = useState<PropertyMemo>({ items: [], freeMemo: '', savedAt: null }); const [applied, setApplied] = useState<AppliedSummary[]>([]); const [checklists, setChecklists] = useState<ChecklistSummary[]>([]); const [detail, setDetail] = useState<AppliedDetail | null>(null)
  const load = () => { api<PropertyMemo>(`/api/properties/${property.propertyId}/memo`).then(setMemo).catch(onError); api<AppliedSummary[]>(`/api/properties/${property.propertyId}/checklists`).then(setApplied).catch(onError); api<ChecklistSummary[]>('/api/checklists').then(setChecklists).catch(onError) }
  useEffect(load, [property.propertyId])
  async function saveMemo() { await api(`/api/properties/${property.propertyId}/memo`, { method: 'PUT', body: JSON.stringify({ items: memo.items.map(({ label, content }) => ({ label, content })), freeMemo: memo.freeMemo }) }); load(); reload() }
  async function apply(stage: Stage, checklistId: number) { const value = await api<AppliedDetail>(`/api/properties/${property.propertyId}/checklists/${stage}`, { method: 'PUT', body: JSON.stringify({ userChecklistId: checklistId }) }); setDetail(value); load(); reload() }
  async function open(id: number | null) { if (id) setDetail(await api<AppliedDetail>(`/api/properties/${property.propertyId}/checklists/${id}`)) }
  async function status(itemId: number, value: CheckStatus) { if (!detail) return; await api(`/api/properties/${property.propertyId}/checklists/${detail.propertyChecklistId}/items/${itemId}/status`, { method: 'PATCH', body: JSON.stringify({ status: value }) }); await open(detail.propertyChecklistId); load(); reload() }
  async function saveItemMemo(itemId: number, value: string) { if (!detail) return; await api(`/api/properties/${property.propertyId}/checklists/${detail.propertyChecklistId}/items/${itemId}/memo`, { method: 'PATCH', body: JSON.stringify({ memo: value }) }); await open(detail.propertyChecklistId) }
  return <div className="workspace"><h3>메모 전체 교체</h3>{memo.items.map((item, index) => <div className="row" key={index}><input value={item.label} onChange={e => setMemo(v => ({ ...v, items: v.items.map((x, i) => i === index ? { ...x, label: e.target.value } : x) }))}/><input value={item.content} onChange={e => setMemo(v => ({ ...v, items: v.items.map((x, i) => i === index ? { ...x, content: e.target.value } : x) }))}/><button onClick={() => setMemo(v => ({ ...v, items: v.items.filter((_, i) => i !== index) }))}>삭제</button></div>)}<button onClick={() => setMemo(v => ({ ...v, items: [...v.items, { label: '', content: '' }] }))}>메모 항목 추가</button><textarea value={memo.freeMemo} onChange={e => setMemo(v => ({ ...v, freeMemo: e.target.value }))} placeholder="자유 메모"/><button className="primary" onClick={() => saveMemo().catch(onError)}>메모 전체 저장</button><h3>단계별 체크리스트</h3>{applied.map(row => <div className="applied" key={row.stage}><b>{stageLabel[row.stage]}</b>{row.applied ? <><button onClick={() => open(row.propertyChecklistId).catch(onError)}>{row.name} · {row.progress.progressPercent}%</button></> : <select defaultValue="" onChange={e => e.target.value && apply(row.stage, Number(e.target.value)).catch(onError)}><option value="">적용할 체크리스트</option>{checklists.filter(c => c.stage === row.stage).map(c => <option key={c.checklistId} value={c.checklistId}>{c.name}</option>)}</select>}</div>)}{detail && <div className="detail"><h3>{detail.name}</h3>{detail.items.map(item => <div className="check-result" key={item.itemId}><b>{item.question}</b><select value={item.status} onChange={e => status(item.itemId, e.target.value as CheckStatus).catch(onError)}><option>UNCONFIRMED</option><option>GOOD</option><option>CAUTION</option></select><input defaultValue={item.memo} onBlur={e => saveItemMemo(item.itemId, e.target.value).catch(onError)} placeholder="포커스를 벗어나면 메모 저장"/></div>)}</div>}<div className="photo-placeholder"><b>사진 API 미구현</b><p>백엔드 구현 후 업로드·목록·본문·삭제 검증을 연결합니다.</p></div></div>
}

function StageSelect({ value, onChange }: { value: Stage; onChange: (value: Stage) => void }) { return <select value={value} onChange={e => onChange(e.target.value as Stage)}>{STAGES.map(stage => <option key={stage} value={stage}>{stageLabel[stage]}</option>)}</select> }
function Progress({ value }: { value: number }) { return <div className="progress"><i style={{ width: `${value}%` }}/><span>{value}%</span></div> }

export default App
