import {useEffect,useRef,type ReactNode} from 'react';
import {ApiError} from './api';
export function Dialog({title,children,onClose,wide=false}:{title:string;children:ReactNode;onClose:()=>void;wide?:boolean}){
 const ref=useRef<HTMLDialogElement>(null);
 useEffect(()=>{ref.current?.showModal();return()=>ref.current?.close()},[]);
 return <dialog ref={ref} className={wide?'wide':''} onCancel={e=>{e.preventDefault();onClose()}}><div className="dialog-heading"><h2>{title}</h2><button type="button" className="icon" aria-label="Закрыть" onClick={onClose}>×</button></div>{children}</dialog>;
}
export function ErrorBox({error}:{error:unknown}){
 if(!error)return null;
 return <div className="error" role="alert">{error instanceof Error?error.message:'Не удалось загрузить данные'}{error instanceof ApiError && error.status===409?<small>Закройте форму и откройте её снова, чтобы получить актуальные данные.</small>:null}</div>;
}
export function Field({label,name,error,children}:{label:string;name:string;error?:unknown;children:ReactNode}){
 const message=error instanceof ApiError?error.fields[name]:undefined;
 return <label className="field"><span>{label}</span>{children}{message&&<small className="field-error" role="alert">{message}</small>}</label>;
}
export function Empty({title='Здесь пока пусто',text='Создайте первый объект, чтобы начать работу.'}:{title?:string;text?:string}){
 return <div className="empty"><div className="empty-mark">＋</div><h3>{title}</h3><p>{text}</p></div>;
}
export function Loading(){return <div className="loading" role="status">Загружаем данные…</div>}
