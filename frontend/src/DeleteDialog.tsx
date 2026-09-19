import {useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {api,refresh} from './api';
import {Dialog,ErrorBox,Field} from './components';
import {type Kind,type Entity,type Car,type Coordinates,carLabel} from './types';
export default function DeleteDialog({kind,entity,onClose}:{kind:Kind;entity:Entity;onClose:()=>void}){
 const [replacement,setReplacement]=useState('');const [error,setError]=useState<unknown>();const [busy,setBusy]=useState(false);
 const options=useQuery({queryKey:[kind],queryFn:()=>api<Entity[]>('/'+kind),enabled:kind!=='humans'});
 async function remove(){setBusy(true);setError(undefined);try{await api(`/${kind}/${entity.id}?version=${entity.version}${replacement?'&replacementId='+replacement:''}`,'DELETE');await refresh();onClose()}catch(e){setError(e)}finally{setBusy(false)}}
 return <Dialog title={`Удалить объект #${entity.id}?`} onClose={onClose}><p className="muted">Это действие нельзя отменить.</p><ErrorBox error={error||options.error}/>
 {kind!=='humans'&&<><p>Если объект используется персонажами, выберите замену. Все ссылки будут перенесены.</p><Field label="Объект для замены" name="replacementId" error={error}><select value={replacement} onChange={e=>setReplacement(e.target.value)}><option value="">Не используется — удалить без замены</option>{options.data?.filter(v=>v.id!==entity.id).map(v=><option key={v.id} value={v.id}>{kind==='cars'?carLabel(v as Car):`#${v.id} · (${(v as Coordinates).x}; ${(v as Coordinates).y})`}</option>)}</select></Field></>}
 <div className="dialog-actions"><button className="secondary" disabled={busy} onClick={onClose}>Отмена</button><button className="danger" disabled={busy} onClick={remove}>{busy?'Удаляем…':'Удалить'}</button></div></Dialog>;
}
