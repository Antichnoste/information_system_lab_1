import {useEffect,useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {api} from './api';
import {Empty,ErrorBox,Loading} from './components';
import {type Kind,type Entity,type Human,type Car,type Coordinates,type Page,display,moods,weapons,colors} from './types';
export type OpenEntity=(kind:Kind,entity:Entity,mode:'view'|'edit'|'delete')=>void;
const stringColumns:Record<string,string>={name:'Имя',soundtrackName:'Саундтрек',carName:'Автомобиль',carColor:'Цвет автомобиля',mood:'Настроение (код)',weaponType:'Оружие (код)'};
export function HumanTable({open}:{open:OpenEntity}){
 const [page,setPage]=useState(0);const [size,setSize]=useState(10);
 const [sort,setSort]=useState('id');const [direction,setDirection]=useState('asc');
 const [column,setColumn]=useState('name');const [search,setSearch]=useState('');const [filter,setFilter]=useState('');
 useEffect(()=>{const timer=setTimeout(()=>{setFilter(search);setPage(0)},250);return()=>clearTimeout(timer)},[search]);
 const params=new URLSearchParams({page:String(page),size:String(size),sort,direction,...(filter?{[column]:filter}:{})});
 const query=useQuery({queryKey:['humans',params.toString()],queryFn:()=>api<Page<Human>>('/humans?'+params)});
 useEffect(()=>{if(query.data && page>0 && page*size>=query.data.total)setPage(Math.max(0,Math.ceil(query.data.total/size)-1))},[query.data,page,size]);
 const order=(key:string)=>{setSort(key);setDirection(sort===key&&direction==='asc'?'desc':'asc');setPage(0)};
 const head=(label:string,key?:string)=><th key={label}>{key?<button className="sort" onClick={()=>order(key)}>{label}<span>{sort===key?(direction==='asc'?'↑':'↓'):'↕'}</span></button>:label}</th>;
 return <section className="panel">
  <div className="table-tools"><div className="search-control"><span aria-hidden="true">⌕</span><input aria-label="Поиск по подстроке" placeholder="Найти по подстроке…" value={search} onChange={e=>setSearch(e.target.value)}/></div><select aria-label="Поле фильтра" value={column} onChange={e=>{setColumn(e.target.value);setPage(0)}}>{Object.entries(stringColumns).map(([v,t])=><option key={v} value={v}>{t}</option>)}</select><span className="count">{query.data?.total??'—'} объектов</span>{search&&<button className="text-button" onClick={()=>setSearch('')}>Сбросить</button>}</div>
  <ErrorBox error={query.error}/>{query.isPending?<Loading/>:query.data?.items.length?<div className="table-scroll"><table><thead><tr>
   {head('ID','id')}{head('Имя','name')}{head('Координаты ID')}{head('X')}{head('Y')}{head('Создан')}{head('Герой')}{head('Зубочистка')}
   {head('Автомобиль ID')}{head('Автомобиль','carName')}{head('Крутой')}{head('Цвет','carColor')}{head('Настроение','mood')}{head('Скорость удара')}{head('Саундтрек','soundtrackName')}{head('Ожидание, мин')}{head('Оружие','weaponType')}{head('Действия')}
  </tr></thead><tbody>{query.data.items.map(h=><tr key={h.id}>
   <td className="mono muted">#{h.id}</td><td><button className="name-button" onClick={()=>open('humans',h,'view')}>{h.name}</button></td>
   <td>#{h.coordinates.id}</td><td className="mono">{h.coordinates.x}</td><td>{h.coordinates.y}</td><td>{new Date(h.creationDate).toLocaleString('ru-RU')}</td>
   <td><span className={h.realHero?'badge hero':'badge'}>{h.realHero?'Герой':'Нет'}</span></td><td>{h.hasToothpick?'Есть':'Нет'}</td>
   <td>{h.car?'#'+h.car.id:'—'}</td><td>{h.car?display(h.car.name):<span className="muted">Без автомобиля</span>}</td><td>{h.car?(h.car.cool?'Да':'Нет'):'—'}</td>
   <td>{h.car?display(colors[h.car.color??'']??h.car.color):'—'}</td><td>{h.mood?moods[h.mood]:'—'}</td><td className="mono">{h.impactSpeed}</td><td>{display(h.soundtrackName)}</td><td>{display(h.minutesOfWaiting)}</td><td>{weapons[h.weaponType]}</td>
   <td><Actions kind="humans" entity={h} open={open}/></td>
  </tr>)}</tbody></table></div>:!query.isError?<Empty title={filter?'Ничего не найдено':'Персонажей пока нет'} text={filter?'Попробуйте другую подстроку или поле поиска.':'Добавьте персонажа и выберите для него координаты.'}/>:null}
  <div className="pagination"><label>На странице <select aria-label="Объектов на странице" value={size} onChange={e=>{setSize(Number(e.target.value));setPage(0)}}>{[10,20,50,100].map(v=><option key={v}>{v}</option>)}</select></label><div><span>Страница {page+1} из {Math.max(1,Math.ceil((query.data?.total??0)/size))}</span><button className="secondary" aria-label="Предыдущая страница" disabled={!page} onClick={()=>setPage(page-1)}>←</button><button className="secondary" aria-label="Следующая страница" disabled={!query.data || (page+1)*size>=query.data.total} onClick={()=>setPage(page+1)}>→</button></div></div>
 </section>;
}
function Actions({kind,entity,open}:{kind:Kind;entity:Entity;open:OpenEntity}){return <div className="row-actions"><button className="text-button" onClick={()=>open(kind,entity,'edit')}>Изменить</button><button className="text-button red" onClick={()=>open(kind,entity,'delete')}>Удалить</button></div>}
export function AuxiliaryTable({kind,open}:{kind:'cars'|'coordinates';open:OpenEntity}){
 const query=useQuery({queryKey:[kind],queryFn:()=>api<(Car|Coordinates)[]>('/'+kind)});
 return <section className="panel"><div className="table-tools"><h3>{kind==='cars'?'Автомобили':'Координаты'}</h3><span className="count">{query.data?.length??'—'} объектов</span></div><ErrorBox error={query.error}/>
 {query.isPending?<Loading/>:query.data?.length?<div className="table-scroll"><table><thead><tr><th>ID</th>{kind==='cars'?<><th>Название</th><th>Цвет</th><th>Крутой</th></>:<><th>X</th><th>Y</th></>}<th>Действия</th></tr></thead><tbody>{query.data.map(v=><tr key={v.id}><td><button className="name-button" onClick={()=>open(kind,v,'view')}>#{v.id}</button></td>{kind==='cars'?<><td>{display((v as Car).name)}</td><td>{display(colors[(v as Car).color??'']??(v as Car).color)}</td><td>{(v as Car).cool?'Да':'Нет'}</td></>:<><td className="mono">{(v as Coordinates).x}</td><td>{(v as Coordinates).y}</td></>}<td><Actions kind={kind} entity={v} open={open}/></td></tr>)}</tbody></table></div>:!query.isError?<Empty/>:null}</section>;
}
