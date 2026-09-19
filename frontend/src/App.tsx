import {useEffect,useState,type FormEvent} from 'react';
import {useQuery} from '@tanstack/react-query';
import {NavLink,Route,Routes,useLocation} from 'react-router-dom';
import {api,queryClient,refresh} from './api';
import {ErrorBox,Loading} from './components';
import {type Session,type Kind,type Entity} from './types';
import {HumanTable,AuxiliaryTable,type OpenEntity} from './Catalog';
import Editor from './Editor';
import Details from './Details';
import DeleteDialog from './DeleteDialog';
import Operations from './Operations';
function Login(){
 const [username,setUsername]=useState('');const [password,setPassword]=useState('');const [error,setError]=useState<unknown>();const [busy,setBusy]=useState(false);
 async function submit(e:FormEvent){e.preventDefault();setBusy(true);setError(undefined);try{const session=await api<Session>('/auth/login','POST',{username,password});queryClient.removeQueries({predicate:q=>q.queryKey[0]!=='session'});queryClient.setQueryData(['session'],session)}catch(e){setError(e)}finally{setBusy(false)}}
 return <main className="login"><div className="login-art"><div className="brand"><span className="logo">h.</span>humanbeing</div><div><span className="eyebrow">КАТАЛОГ ПЕРСОНАЖЕЙ</span><h1>У каждого героя<br/>своя история.</h1><p>Персонажи, автомобили и координаты.<br/>Одно пространство для совместной работы.</p></div><span className="login-bottom">Лабораторная работа / 01</span></div><div className="login-side"><form onSubmit={submit}><span className="eyebrow">РАБОЧЕЕ ПРОСТРАНСТВО</span><h2>С возвращением</h2><p className="muted">Войдите, чтобы открыть каталог.</p><ErrorBox error={error}/><label className="field"><span>Логин</span><input autoComplete="username" required value={username} onChange={e=>setUsername(e.target.value)}/></label><label className="field"><span>Пароль</span><input type="password" autoComplete="current-password" required value={password} onChange={e=>setPassword(e.target.value)}/></label><button disabled={busy}>{busy?'Входим…':'Войти в систему →'}</button></form></div></main>;
}
export default function App(){
 const session=useQuery({queryKey:['session'],queryFn:async()=>{try{return await api<Session>('/auth/session')}catch(e){if(e instanceof Error && 'status' in e && e.status===401)return null;throw e}},staleTime:Infinity});
 const [error,setError]=useState<unknown>();
 const [modal,setModal]=useState<{kind:Kind;entity?:Entity;mode:'view'|'edit'|'delete'}|null>(null);
 const location=useLocation();
 useEffect(()=>{
  if(!session.data){setModal(null);return}
    void refresh();
    const timer = window.setInterval(() => { void refresh(); }, 2000);
    return () => window.clearInterval(timer);
 },[session.data]);
 if(session.isPending)return <Loading/>;
 if(session.isError)return <main className="connection-error"><ErrorBox error={session.error}/><button onClick={()=>session.refetch()}>Повторить подключение</button></main>;
 if(!session.data)return <Login/>;
 const current=location.pathname==='/cars'?'cars':location.pathname==='/coordinates'?'coordinates':location.pathname==='/operations'?'operations':'humans';
 const headings={humans:['Персонажи','Все истории начинаются здесь.'],cars:['Автомобили','Машины, которые объединяют героев.'],coordinates:['Координаты','Место для каждого персонажа.'],operations:['Специальные операции','Найдите нужное и измените сразу несколько объектов.']};
 const open:OpenEntity=(kind,entity,mode)=>setModal({kind,entity,mode});
 async function logout(){try{await api('/auth/logout','POST');queryClient.removeQueries({predicate:q=>q.queryKey[0]!=='session'});queryClient.setQueryData(['session'],null)}catch(e){setError(e)}}
 return <div className="app-shell"><aside className="sidebar"><div className="brand"><span className="logo">h.</span>humanbeing</div><span className="nav-label">КАТАЛОГ</span><nav><NavLink to="/" end><span>◉</span>Персонажи</NavLink><NavLink to="/cars"><span>▱</span>Автомобили</NavLink><NavLink to="/coordinates"><span>⌖</span>Координаты</NavLink><div className="nav-divider"/><NavLink to="/operations"><span>↗</span>Операции</NavLink></nav><div className="sidebar-bottom"><div className="user"><span className="avatar">{session.data.username.slice(0,1).toUpperCase()}</span><div><strong>{session.data.username}</strong><small>Общее пространство</small></div></div><button className="text-button" onClick={logout}>Выйти из системы ↗</button></div></aside>
 <main className="workspace"><div className="topbar"><span>Рабочее пространство <span className="separator">/</span> {headings[current][0]}</span></div><div className="content"><header className="page-heading"><div><span className="eyebrow">HUMANBEING / {current==='operations'?'ИНСТРУМЕНТЫ':'КОЛЛЕКЦИЯ'}</span><h1>{headings[current][0]}</h1><p>{headings[current][1]}</p></div>{current!=='operations'&&<button onClick={()=>setModal({kind:current,mode:'edit'})}>＋ {current==='humans'?'Новый персонаж':current==='cars'?'Новый автомобиль':'Новые координаты'}</button>}</header><ErrorBox error={error}/>
 <Routes><Route path="/" element={<HumanTable open={open}/>}/><Route path="/cars" element={<AuxiliaryTable kind="cars" open={open}/>}/><Route path="/coordinates" element={<AuxiliaryTable kind="coordinates" open={open}/>}/><Route path="/operations" element={<Operations open={open}/>}/><Route path="*" element={<p>Страница не найдена. Выберите раздел в меню.</p>}/></Routes>
 <footer className="page-footer"><span>Общие данные · Изменения видны всем пользователям</span><span>Лабораторная работа № 1</span></footer></div></main>
 {modal?.mode==='edit'&&<Editor kind={modal.kind} entity={modal.entity} onClose={()=>setModal(null)}/>}
 {modal?.mode==='view'&&modal.entity&&<Details kind={modal.kind} entity={modal.entity} onClose={()=>setModal(null)} onEdit={entity=>setModal({...modal,entity,mode:'edit'})}/>}
 {modal?.mode==='delete'&&modal.entity&&<DeleteDialog kind={modal.kind} entity={modal.entity} onClose={()=>setModal(null)}/>}
 </div>;
}
