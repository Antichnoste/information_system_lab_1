import {useState,type FormEvent} from 'react';
import {useQuery} from '@tanstack/react-query';
import {api,ApiError,refresh} from './api';
import {Dialog,ErrorBox,Field} from './components';
import {type Kind,type Entity,type Human,type Car,type Coordinates,moods,weapons,carLabel} from './types';

const titles:Record<Kind,string>={humans:'персонажа',cars:'автомобиль',coordinates:'координаты'};
function initial(kind:Kind,entity?:Entity):Record<string,string|boolean>{
 if(kind==='humans'){
  const h=entity as Human|undefined;
  return {name:h?.name??'',coordinatesId:String(h?.coordinates.id??''),realHero:h?.realHero??false,
   hasToothpick:h?.hasToothpick??false,carId:String(h?.car?.id??''),mood:h?.mood??'',impactSpeed:h?.impactSpeed??'0',
   soundtrackName:h?.soundtrackName??'',minutesOfWaiting:h?.minutesOfWaiting??'',weaponType:h?.weaponType??'HAMMER'};
 }
 if(kind==='cars'){const c=entity as Car|undefined;return {name:c?.name??'',cool:c?.cool??false,color:c?.color??''}}
 const c=entity as Coordinates|undefined;return {x:c?.x??'0',y:String(c?.y??'0')};
}
function longValue(value:string,name:string,optional=false){
 if(optional && value==='')return null;
 try {const v=BigInt(value);if(!/^-?\d+$/.test(value)||v< -9223372036854775808n||v>9223372036854775807n)throw new Error();return value}
 catch {throw new ApiError(400,'Проверьте целочисленные поля',{[name]:'Нужно целое число от −9223372036854775808 до 9223372036854775807'})}
}
export default function Editor({kind,entity,onClose,onSaved}:{kind:Kind;entity?:Entity;onClose:()=>void;onSaved?:(entity:Entity)=>void}){
 const [form,setForm]=useState(()=>initial(kind,entity));
 const [error,setError]=useState<unknown>();const [saving,setSaving]=useState(false);
 const [nested,setNested]=useState<'cars'|'coordinates'|null>(null);
 const cars=useQuery({queryKey:['cars'],queryFn:()=>api<Car[]>('/cars'),enabled:kind==='humans'});
 const coords=useQuery({queryKey:['coordinates'],queryFn:()=>api<Coordinates[]>('/coordinates'),enabled:kind==='humans'});
 const value=(key:string)=>String(form[key]??'');
 const set=(key:string,v:string|boolean)=>setForm(old=>({...old,[key]:v}));
 const input=(key:string,label:string,required=false,long=false)=><Field key={key} label={label} name={key} error={error}><input value={value(key)} onChange={e=>set(key,e.target.value)} required={required} pattern={long?'-?[0-9]+':undefined} inputMode={long?'numeric':undefined}/></Field>;
 const check=(key:string,label:string)=><label className="checkbox"><input type="checkbox" checked={Boolean(form[key])} onChange={e=>set(key,e.target.checked)}/>{label}</label>;
 async function submit(event:FormEvent){
  event.preventDefault();setError(undefined);setSaving(true);
  try{
   let body:Record<string,unknown>;
   if(kind==='humans')body={...form,coordinatesId:Number(form.coordinatesId),carId:form.carId?Number(form.carId):null,
    mood:form.mood||null,impactSpeed:longValue(value('impactSpeed'),'impactSpeed'),minutesOfWaiting:longValue(value('minutesOfWaiting'),'minutesOfWaiting',true)};
   else if(kind==='cars')body={...form,name:form.name||null,color:form.color||null};
   else {
    const y=Number(form.y);
    if(!Number.isFinite(y)||y<=-629)throw new ApiError(400,'Проверьте координату Y',{y:'Значение должно быть больше −629'});
    body={x:longValue(value('x'),'x'),y};
   }
   if(entity)body.version=entity.version;
   const saved=await api<Entity>('/'+kind+(entity?'/'+entity.id:''),entity?'PUT':'POST',body);
   await refresh();onSaved?.(saved);onClose();
  }catch(e){setError(e)}finally{setSaving(false)}
 }
 return <><Dialog title={`${entity?'Изменить':'Создать'} ${titles[kind]}`} onClose={onClose} wide={kind==='humans'}>
  <form onSubmit={submit}>
   <ErrorBox error={error}/><ErrorBox error={cars.error||coords.error}/>
   <div className="form-grid">
    {kind==='humans'?<>
     {input('name','Имя *',true)}
     <Field label="Оружие *" name="weaponType" error={error}><select value={value('weaponType')} onChange={e=>set('weaponType',e.target.value)}>{Object.entries(weapons).map(([v,t])=><option key={v} value={v}>{t}</option>)}</select></Field>
     <div><Field label="Координаты *" name="coordinatesId" error={error}><select required value={value('coordinatesId')} onChange={e=>set('coordinatesId',e.target.value)}><option value="">Выберите координаты</option>{coords.data?.map(c=><option key={c.id} value={c.id}>#{c.id} · ({c.x}; {c.y})</option>)}</select></Field><button type="button" className="text-button" onClick={()=>setNested('coordinates')}>＋ Создать координаты</button></div>
     <div><Field label="Автомобиль" name="carId" error={error}><select value={value('carId')} onChange={e=>set('carId',e.target.value)}><option value="">Без автомобиля</option>{cars.data?.map(c=><option key={c.id} value={c.id}>{carLabel(c)}</option>)}</select></Field><button type="button" className="text-button" onClick={()=>setNested('cars')}>＋ Создать автомобиль</button></div>
     <Field label="Настроение" name="mood" error={error}><select value={value('mood')} onChange={e=>set('mood',e.target.value)}><option value="">Не указано</option>{Object.entries(moods).map(([v,t])=><option key={v} value={v}>{t}</option>)}</select></Field>
     {input('impactSpeed','Скорость удара *',true,true)}
     {input('soundtrackName','Название саундтрека')}{input('minutesOfWaiting','Минуты ожидания',false,true)}
     {check('realHero','Настоящий герой')}{check('hasToothpick','Есть зубочистка')}
    </>:kind==='cars'?<>{input('name','Название автомобиля')}
      <Field label="Цвет" name="color" error={error}><input list="car-colors" value={value('color')} onChange={e=>set('color',e.target.value)}/><datalist id="car-colors"><option value="RED">Красный</option><option value="BLUE">Синий</option><option value="GREEN">Зелёный</option><option value="BLACK">Чёрный</option><option value="WHITE">Белый</option></datalist></Field>
      {check('cool','Крутой автомобиль')}</>:<>{input('x','Координата X *',true,true)}<Field label="Координата Y *" name="y" error={error}><input type="number" step="any" required value={value('y')} onChange={e=>set('y',e.target.value)}/><small>Строго больше −629</small></Field></>}
   </div>
   <div className="dialog-actions"><button type="button" className="secondary" onClick={onClose} disabled={saving}>Отмена</button><button disabled={saving || (kind==='humans' && (cars.isPending||coords.isPending||cars.isError||coords.isError))}>{saving?'Сохраняем…':'Сохранить'}</button></div>
  </form>
 </Dialog>{nested&&<Editor kind={nested} onClose={()=>setNested(null)} onSaved={saved=>set(nested==='cars'?'carId':'coordinatesId',String(saved.id))}/>}</>;
}
